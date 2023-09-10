use mlua::{Lua};
use crate::ai::ai::{LuaDataOutput, LuaDataOutputType};
use crate::{PATHFINDER_STATE, State, WORLD_STATE};

pub struct ScriptManager {

}

impl ScriptManager {
    pub fn create() -> ScriptManager {
        ScriptManager {

        }
    }

    pub fn execute(&self, script: &str) -> Vec<LuaDataOutput> {
        info!("Executing script: {}", script);

        let mut output = vec![];
        let mut lua = Lua::new();
        AiInterface::apply(&mut lua);

        let compiled = lua.load(script).set_name("__script__");
        let x = compiled.exec();
        if x.is_err() {
            output.push(LuaDataOutput {
                variant: LuaDataOutputType::ERROR,
                content: String::from("LUA Runtime Exception: ") + &*x.err().unwrap().to_string(),
            });
        }
        // debug!("{:?}", lua.globals().get::<&str, String>("y"));
        output
    }
}

struct AiInterface {

}

impl AiInterface {
    pub fn apply(lua: &mut Lua) {
        lua.globals().set("pathfind", lua.create_function(| _, pos: (i64, i64, i64) | {
            /*let (x, y, z) = pos;
            let mut plock = PATHFINDER_STATE.lock();
            let pf = plock.get_mut().as_mut().expect("Uninitialized!");
            let mut wlock = WORLD_STATE.lock();
            let world = wlock.get_mut().as_mut().expect("Uninitialized!");
            pf.init(State::create(world.player.x, world.player.z, world.player.y), State::create(x, z, y));
            if pf.replan() < 0 {
                info!("Pathfinding panicked lol.");
                panic!("Failed to find path from {:?} to {:?}!", pf.start, pf.goal);
            }
            info!("{:?}", pf.path);*/
            info!("Found path to pos: {:?}", pos);
            Ok(())
        }).unwrap()).expect("Could not add function!");

        lua.globals().set("find_block", lua.create_function(| _, block_type: String | {
            info!("Found block of type {}!", block_type);
            Ok((10, 5, 10))
        }).unwrap()).expect("Could not add function!");

        lua.globals().set("destroy_block", lua.create_function(| _, pos: (i64, i64, i64) | {
            info!("Destroyed vein at {:?}!", pos);
            Ok(())
        }).unwrap()).expect("Could not add function!");

        lua.globals().set("destroy_vein", lua.create_function(| _, pos: (i64, i64, i64) | {
            info!("Destroyed block at {:?}!", pos);
            Ok(())
        }).unwrap()).expect("Could not add function!");

        lua.globals().set("find_mob", lua.create_function(| _, args: (i64, String) | {
            info!("Found mob {} in range {}!", args.0, args.1);
            Ok((10, 5, 10))
        }).unwrap()).expect("Could not add function!");

        lua.globals().set("kill_nearby_mob", lua.create_function(| _, mob_type: String | {
            info!("Killed mob of type {}!", mob_type);
            Ok(())
        }).unwrap()).expect("Could not add function!");

        lua.globals().set("pickup_nearby_items", lua.create_function(| _, _args: () | {
            info!("Picked up nearby items!");
            Ok(())
        }).unwrap()).expect("Could not add function!");
    }

    /*pathfind(x: number, y: number, z: number) -> None -- Finds path to (x, y, z), automatically travels along path.
    find_block(block_id: string) -> (x, y, z) -- Finds nearest instance of block.
    destroy_block(x: number, y: number, z: number) -> None -- Destroys block at location
    destroy_vein(x: number, y: number, z: number) -> None -- Destroys vein of block at location
    find_mob(range: number, type: string) -> (x, y, z) -- Finds nearest mob of type in range
    kill_nearby_mob(type: string) -> None -- Fights nearby mob of type
    pickup_nearby_items() -> None -- Picks up all nearby items*/

    /*pub fn pathfind(x: i64, y: i64, z: i64) -> String {
        let mut plock = PATHFINDER_STATE.lock();
        let pf = plock.get_mut().as_mut().expect("Uninitialized!");
        let mut wlock = WORLD_STATE.lock();
        let world = wlock.get_mut().as_mut().expect("Uninitialized!");
        pf.init(State::create(world.player.x, world.player.z, world.player.y), State::create(x, z, y));
        if pf.replan() < 0 {
            panic!("Failed to find path from {:?} to {:?}!", pf.start, pf.goal);
        }
        info!("{:?}", pf.path);
        String::from("Successfully found path!")
    }*/
}
