use mlua::Lua;

pub struct ScriptManager {
    lua: Lua,
}

impl ScriptManager {
    pub fn create() -> ScriptManager {
        ScriptManager {
            lua: Lua::new(),
        }
    }

    pub fn execute(&self, script: &str) {
        let compiled = self.lua.load(script).set_name("__script__");
        compiled.exec().expect("LUA ISSUE");
    }
}
