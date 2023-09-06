use std::collections::HashMap;
use openai_api_rs::v1::api::Client;
use openai_api_rs::v1::chat_completion::{ChatCompletionMessage, ChatCompletionRequest, Function, FunctionParameters, GPT3_5_TURBO_0613, JSONSchemaDefine, JSONSchemaType, MessageRole};
use openai_api_rs::v1::chat_completion::FinishReason::function_call;

const PROMPT_TEMPLATE: &str = r#"
Your goal: {goal}.
"#;

const PROMPT_TEMPLATE_INITIAL: &str = r#"
You are an autonomous AI agent who can control Minecraft by writing LUA scripts. The standard library is extended with agent and game specific functions.
You will also explain your thinking process which will be automatically committed to long-term memory.
Here are the functions in your global scope:
pathfind(x: number, y: number, z: number) -> None -- Finds path to (x, y, z), automatically travels along path.
find_block(block_id: string) -> (x, y, z) -- Finds nearest instance of block.
(more functions later, you unlock them over time)
Your goal: {goal}.
"#;

/// * script - The executable LUA script you want to run.
/// * reasoning - Why you want to perform this action.
async fn complete(script: String, reasoning: String) {
    println!("Hello world!");
}

pub struct AI {
    new_client: Client,
    messages: Vec<ChatCompletionMessage>,
}

impl AI {
    pub fn create() -> AI {
        AI {
            new_client: Client::new(dotenv!("OPENAI_KEY").parse().unwrap()),
            messages: vec![],
        }
    }

    pub fn cycle(&mut self) {
        let prompt = PROMPT_TEMPLATE_INITIAL
            .replace("{goal}", "Beat the ender dragon");
        let (message, script, reasoning) = self.send(prompt);
        self.messages.push(message);
    }

    fn send(&mut self, message: String) -> (ChatCompletionMessage, String, String) {
        let mut msgs = self.messages.clone();
        msgs.push(ChatCompletionMessage {
            role: MessageRole::user,
            content: message,
            name: None,
            function_call: None,
        });
        let response = self.new_client.chat_completion(ChatCompletionRequest {
            model: GPT3_5_TURBO_0613.to_string(),
            messages: msgs,
            functions: Some(vec![
                Function {
                    name: String::from("complete"),
                    description: Some(String::from("Perform an action")),
                    parameters: FunctionParameters {
                        schema_type: JSONSchemaType::Object,
                        properties: Some(HashMap::from([
                            (String::from("script"), Box::new(JSONSchemaDefine {
                                schema_type: Some(JSONSchemaType::String),
                                description: Some(String::from("The LUA executable script")),
                                enum_values: None,
                                properties: None,
                                required: None,
                                items: None,
                            })),
                            (String::from("reasoning"), Box::new(JSONSchemaDefine {
                                schema_type: Some(JSONSchemaType::String),
                                description: Some(String::from("Why you want to perform this action")),
                                enum_values: None,
                                properties: None,
                                required: None,
                                items: None,
                            }))
                        ])),
                        required: Some(vec![String::from("script"), String::from("reasoning")]),
                    },
                }
            ]),
            function_call: Some(String::from("complete")),
            temperature: None,
            top_p: None,
            n: None,
            stream: None,
            stop: None,
            max_tokens: Some(2048),
            presence_penalty: None,
            frequency_penalty: None,
            logit_bias: None,
            user: None,
        }).expect("Could not submit request!");

        let completion = response.choices.as_slice().get(0).as_ref().unwrap().message;
        let function = completion.message.function_call;
        assert_eq!(function.name, Some(String::from("complete")));
        let args = function.arguments.expect("No arguments!");
        (ChatCompletionMessage {
            role: MessageRole::assistant,
            content: String::from(""),
            name: None,
            function_call: None,
        }, args, String::from("lazy"))
    }
}
