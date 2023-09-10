use openai_api_rs::v1::api::Client;
use openai_api_rs::v1::chat_completion::{ChatCompletionMessage, ChatCompletionRequest, GPT3_5_TURBO_0613, MessageRole};
use openai_api_rs::v1::error::APIError;
use serde::{Serialize, Deserialize};
use serde_json;
use crate::ScriptManager;

const PROMPT_TEMPLATE: &str = r#"
Your goal: {goal}.
Previous subgoal: {subgoal}.
Human feedback from last iteration: {feedback}
Results from previous script: {results}

You are must return valid JSON in the format: {
    script: string, # The LUA script you want to run
    subgoal: string, # The current subgoal
    reasoning: string # Explanation for this script and subgoal
}.
{
"#;

const PROMPT_TEMPLATE_INITIAL: &str = r#"
You can control Minecraft by writing LUA scripts.

The global scope is extended with the following LUA functions:
pathfind(x: number, y: number, z: number) # Finds path to (x, y, z), automatically travels along path.
find_block(block_id: string) -> (x, y, z) # Finds nearest instance of block.
destroy_block(x: number, y: number, z: number) # Destroys block at location
destroy_vein(x: number, y: number, z: number) # Destroys vein of block at location
find_mob(range: number, type: string) -> (x, y, z) # Finds nearest entity in range, type is optional, finds any mob if not present
kill_nearby_mob(type: string) # Fights nearby mob of type
pickup_nearby_items() # Picks up all nearby items
You unlock more functions over time (eg. as you get certain items or acheivements).

Scripts can do anything you would normally do in LUA and can be multiline.
You start with ABSOLUTELY NOTHING! No tools, resources, or armor. You need to progress and acquire resouces step by step. For example, you cannot acquire diamonds before first having iron tools, and you cannot get iron without stone tools.
DO NOT WRITE TOO MUCH IN THIS SCRIPT! This script should aim to achieve one super tiny tiny subgoal of the primary goal. You can write the script for the next logical step in the next iteration. For example, this script could be focused on just getting wood and making a crafting table.

Example scripts:
Gather wood:
```
local x, y, z = find_block('minecraft:oak_log')
pathfind(x, y, z)
destroy_vein('minecraft:oak_log')
pickup_nearby_items()
```
Gather porkchop:
```
local x, y, z = find_mob(50, "minecraft:pig")
pathfind(x, y, z)
kill_nearby_mob("minecraft:pig")
pickup_nearby_items()
```

You will also explain your thinking process and reasoning which will be automatically committed to long-term memory. The explanation does NOT need to cover the code, only the logic and reasoning.
You are must return valid JSON in the format: {
    script: string, # The LUA script you want to run
    subgoal: string, # The current subgoal
    reasoning: string # Explanation for this script and subgoal
}.
Your goal: {goal}.
{
"#;

#[derive(Serialize, Deserialize)]
struct CompleteArgs {
    script: String,
    subgoal: String,
    reasoning: String,
}

#[derive(Serialize, Deserialize)]
struct FunctionCall {
    name: String,
}

#[derive(Copy, Clone, Debug)]
pub enum LuaDataOutputType {
    SUCCESS = 0,
    ERROR = 1,
}

#[derive(Debug)]
pub struct LuaDataOutput {
    pub variant: LuaDataOutputType,
    pub content: String,
}

pub struct AI {
    client: Client,
    pub goal: Option<String>,
    script_manager: ScriptManager,
    messages: Vec<ChatCompletionMessage>,
    pub feedback: Option<String>,
    results: Vec<LuaDataOutput>,
    prev_subgoal: Option<String>,
}

impl AI {
    pub fn create(goal: Option<String>) -> AI {
        AI {
            client: Client::new(String::from(dotenv!("OPENAI_KEY"))),
            goal,
            script_manager: ScriptManager::create(),
            messages: vec![/*ChatCompletionMessage {
                role: MessageRole::system,
                content: String::from("You are an AI agent that completes goals in minecraft using AI. Always use the `complete` function, as well as writing out a brief explanation of your logic and reasoning."),
                name: None,
                function_call: None,
            }*/],
            feedback: None,
            results: vec![],
            prev_subgoal: None,
        }
    }

    pub fn test_loop(&mut self) {
        /*info!("Entered test loop.");
        loop {
            print!("Do you want to continue (Y/n): ");
            let mut line = String::new();
            std::io::stdin().read_line(&mut line).unwrap();
            if line == "n" {
                info!("Exiting!");
                break;
            }
            info!("Continuing...");
            self.cycle();
            print!("Feedback: ");
            line.clear();
            std::io::stdin().read_line(&mut line).unwrap();
            self.feedback = Some(line);
        }*/
        self.cycle();
    }

    pub fn cycle(&mut self) -> (String, String) {
        let mut results_string = String::new();
        for r in &self.results {
            results_string.push_str(&r.content);
            results_string.push('\n');
            if r.variant as u8 == 1 {
                results_string.push_str("FAILED!");
                if self.feedback.is_none() {
                    self.feedback = Some(String::from("Your code requires fixing. Please try again."));
                }
                break;
            }
        }
        self.results.clear();
        let prompt = if self.messages.len() == 0 {
            PROMPT_TEMPLATE_INITIAL
                .replace("{goal}", &*self.goal.clone().expect("No goal!"))
        } else {
            PROMPT_TEMPLATE
                .replace("{goal}", &*self.goal.clone().expect("No goal!"))
                .replace("{subgoal}", &*self.prev_subgoal.clone().unwrap_or(String::from("None")))
                .replace("{feedback}", &*self.feedback.clone().unwrap_or(String::from("None")))
                .replace("{results}", &*results_string)
        };
        match self.send(prompt) {
            Ok((message, content, script, subgoal)) => {
                self.messages.push(message);
                self.results = self.script_manager.execute(&*script);
                self.feedback = None;
                info!("Content: {content}");
                info!("Subgoal: {subgoal}");
                info!("Script: {script}");
                (subgoal, content)
            }
            Err(e) => {
                panic!("Could not make AI request because of: {}", e.to_string());
            }
        }

    }

    fn send(&mut self, message: String) -> Result<(ChatCompletionMessage, String, String, String), APIError> {
        debug!("Executing prompt: {message}");
        let mut messages = self.messages.clone();
        messages.push(ChatCompletionMessage {
            role: MessageRole::user,
            content: message,
            name: None,
            function_call: None,
        });
        let response = self.client.chat_completion(ChatCompletionRequest {
            model: GPT3_5_TURBO_0613.to_string(),
            messages,
            functions: None,
            function_call: None,
            temperature: Some(0.8),
            top_p: None,
            n: None,
            stream: None,
            stop: None,
            max_tokens: Some(3000),
            presence_penalty: None,
            frequency_penalty: Some(0.5),
            logit_bias: None,
            user: None,
        })?;

        let mut c = None;
        for choice in response.choices {
            c = Some(choice.message);
        }
        let completion = c.expect("No completion!");
        trace!("Content: {}", completion.content.clone().unwrap());

        let a: CompleteArgs = serde_json::from_str(&*(AI::proc_json(completion.content.clone().unwrap(), '{', '}'))).expect("Could not parse arguments!");
        Ok((ChatCompletionMessage {
            role: MessageRole::assistant,
            content: completion.content.clone().unwrap_or(String::new()),
            name: None,
            function_call: None,
        }, a.reasoning, a.script, a.subgoal))
    }

    fn proc_json(mut string: String, c1: char, c2: char) -> String {
        if !string.starts_with(c1) {
            string = String::from(c1) + &*string;
        }
        if !string.ends_with(c2) {
            string = string + &*String::from(c2);
        }
        string
    }
}
