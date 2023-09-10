mod path;
mod chunk;
mod util;
mod ai;
mod debug;
mod state;
mod save;

#[macro_use]
extern crate num_derive;

#[macro_use]
extern crate dotenv_codegen;

#[macro_use]
extern crate log;

#[macro_use]
extern crate serde;

pub use crate::path::{pathfinder::Pathfinder, state::State};
pub use crate::util::{hashmap::FxHasher, bitset::BoolBitset};
pub use crate::chunk::{chunk::Chunk, chunk::ChunkManager};
pub use crate::ai::script::{ScriptManager};
pub use crate::ai::ai::{AI};
use log::LevelFilter;
use log4rs::append::file::FileAppender;
use log4rs::append::console::ConsoleAppender;
use log4rs::config::{Appender, Root};
use log4rs::encode::pattern::PatternEncoder;

use std::cell::Cell;
use std::{ptr, slice};
use std::ffi::{c_char, CStr, CString};
use log4rs::Config;
use parking_lot::Mutex;
use crate::chunk::chunk::{HEIGHT, WIDTH};
pub use crate::state::world::World;

pub static PATHFINDER_STATE: Mutex<Cell<Option<Pathfinder>>> = Mutex::new(Cell::new(None));
pub static CHUNK_STATE: Mutex<Cell<Option<ChunkManager>>> = Mutex::new(Cell::new(None));
pub static AI_STATE: Mutex<Cell<Option<AI>>> = Mutex::new(Cell::new(None));
pub static WORLD_STATE: Mutex<Cell<Option<World>>> = Mutex::new(Cell::new(None));

pub fn configure_logging(file: Option<&str>) {
    let pattern = "{l} [{d(%H:%M:%S)}]: {m}\n";

    let stdout = ConsoleAppender::builder()
        .encoder(Box::new(PatternEncoder::new(pattern)))
        .build();

    let mut builder = Config::builder();
    let mut root = Root::builder();
    if file.is_some() {
        let logfile = FileAppender::builder()
            .encoder(Box::new(PatternEncoder::new(pattern)))
            .build(file.unwrap())
            .expect("Logging filesystem setup failed!");
        builder = builder.appender(Appender::builder().build("logfile", Box::new(logfile)));
        root = root.appender("logfile");
    }

    let config = builder
        .appender(Appender::builder().build("stdout", Box::new(stdout)))
        .build(root
            .appender("stdout")
            .build(LevelFilter::Trace))
        .expect("Logging configuration failed!");

    log4rs::init_config(config).expect("Logging initialization failed!");
}

/**
Initialized the library, including
- Logging
- Global state
*/
#[no_mangle]
pub extern "C" fn init() {
    configure_logging(Some("nativelog/output.log"));
    info!("Native library initialized!");

    CHUNK_STATE.lock().set(Some(ChunkManager::create()));
    PATHFINDER_STATE.lock().set(Some(Pathfinder::create(&CHUNK_STATE)));
    AI_STATE.lock().set(Some(AI::create(None)));
}

/**
Initializes the pathfinding algorithm to the start [`State`] and the goal [`State`]
*/
#[no_mangle]
pub extern "C" fn pf_init(start: State, goal: State) {
    info!("Pathfinding initialized (from {} to {})!", start.to_string(), goal.to_string());
    PATHFINDER_STATE.lock().get_mut().as_mut().expect("Not initialized!").init(start, goal);
}

/**
Updates the cost of a cell
*/
#[no_mangle]
pub extern "C" fn pf_update_cell(cell: State, cost: f64) {
    info!("Externally updated cell {} to {}", cell.to_string(), cost);
    PATHFINDER_STATE.lock().get_mut().as_mut().expect("Not initialized!").update_cell(cell, cost, true);
}

#[no_mangle]
pub extern "C" fn pf_replan() -> i16 {
    info!("Replanning path!");
    PATHFINDER_STATE.lock().get_mut().as_mut().expect("Not initialized!").replan()
}

#[no_mangle]
pub extern "C" fn pf_update_start(start: State) {
    info!("Updating start to {}", start.to_string());
    PATHFINDER_STATE.lock().get_mut().as_mut().expect("Not initialized!").update_start(start);
}

#[no_mangle]
pub extern "C" fn pf_update_goal(goal: State) {
    info!("Updating goal to {}", goal.to_string());
    PATHFINDER_STATE.lock().get_mut().as_mut().expect("Not initialized!").update_goal(goal);
}

#[no_mangle]
pub extern "C" fn pf_get_path_len() -> usize {
    PATHFINDER_STATE.lock().get_mut().as_mut().expect("Not initialized!").path.len()
}

#[no_mangle]
pub extern "C" fn pf_get_path(arr: *mut State) {
    let path = PATHFINDER_STATE.lock().get_mut().as_mut().expect("Not initialized!").path.clone();

    unsafe {
        ptr::copy_nonoverlapping(path.as_ptr(), arr, path.len());
    }
}

#[no_mangle]
pub extern "C" fn pf_get_debug_len() -> usize {
    PATHFINDER_STATE.lock().get_mut().as_mut().expect("Not initialized!").debug.len()
}

#[no_mangle]
pub extern "C" fn pf_get_debug(arr: *mut State) {
    let path = PATHFINDER_STATE.lock().get_mut().as_mut().expect("Not initialized!").debug.clone();

        unsafe {
            ptr::copy_nonoverlapping(path.as_ptr(), arr, path.len());
    }
}

#[no_mangle]
pub extern "C" fn chunk_build(x: i64, y: i64, arr: *const u8) {
    info!("Building chunk: {}, {}", x, y);
    unsafe {
        CHUNK_STATE.lock().get_mut().as_mut().expect("Not initialized!").build((x, y), slice::from_raw_parts_mut(arr as *mut u8, WIDTH * WIDTH * HEIGHT));
    }
}

#[no_mangle]
pub extern "C" fn chunk_remove(x: i64, y: i64) {
    info!("Removing chunk: {}, {}", x, y);
    CHUNK_STATE.lock().get_mut().as_mut().expect("Not initialized!").remove((x, y));
}

#[no_mangle]
pub extern "C" fn chunk_set(x: i64, y: i64, z: i64, value: i8) {
    info!("Setting block {}, {}, {} to {}", x, y, z, value);
    CHUNK_STATE.lock().get_mut().as_mut().expect("Not initialized!").set(x as isize, y as isize, z as isize, value);
}

fn get_string(raw: *const c_char) -> String {
    unsafe {
        String::from(std::str::from_utf8(CStr::from_ptr(raw).to_bytes()).expect("Could not create string!"))
    }
}

#[no_mangle]
pub extern "C" fn ai_cycle(goal: *const c_char, feedback: *const c_char) -> *const c_char {
    let mut lock = AI_STATE.lock();
    let ai = lock.get_mut().as_mut().expect("Not initialized!");
    ai.goal = Some(get_string(goal));
    ai.feedback = Some(get_string(feedback));
    let (s, c) = ai.cycle();
    let final_str = s + "(!###!)" + &*c;
    let final_c_str = CString::new(final_str).expect("Could not convert string to C-style!");
    final_c_str.into_raw()
}

#[cfg(test)]
mod tests {
    use crate::chunk::chunk::{FULL, SHIFT};
    use super::*;

    #[cfg(test)]
    #[ctor::ctor]
    fn init() {
        configure_logging(None);
    }

    #[test]
    fn chunking_works() {
        let mut cm = ChunkManager::create();
        let mut data = vec![0; FULL];
        data[(15 * WIDTH * HEIGHT) + ((13 + SHIFT as isize) as usize * WIDTH) + 14] = 12;
        data[(15 * WIDTH * HEIGHT) + ((304 + SHIFT as isize) as usize * WIDTH) + 14] = 13;
        data[(15 * WIDTH * HEIGHT) + ((96 + SHIFT as isize) as usize * WIDTH) + 14] = 14;
        cm.build((0, 0), data.as_slice());

        assert_eq!(cm.get(0, 0, 0).unwrap(), -1);
        assert_eq!(cm.get(15, 14, 13).unwrap(), 11);
        assert_eq!(cm.get(15, 14, 304).unwrap(), 12);
        assert_eq!(cm.get(15, 14, 96).unwrap(), 13);

        cm.set(1, 1, 128, 5);
        assert_eq!(cm.get(1, 1, 128).unwrap(), 5);

        assert_eq!(cm.get(0, 0, 0).unwrap(), -1);
        assert_eq!(cm.get(15, 14, 13).unwrap(), 11);
        assert_eq!(cm.get(15, 14, 304).unwrap(), 12);
        assert_eq!(cm.get(15, 14, 96).unwrap(), 13);

        cm.build((1, 0), vec![2; FULL].as_slice());

        assert_eq!(cm.get(17, 0, 0).unwrap(), 1);
    }

    #[test]
    fn ai_works() {
        let mut ai = AI::create(Some(String::from("Beat the ender dragon")));
        ai.test_loop();
        info!("Done!");
    }

    #[test]
    fn scripting_works() {
        let manager = ScriptManager::create();
        let result = manager.execute(r#"
        local x, y, z = find_block('minecraft:oak_log')
        pathfind(x, y, z)
        destroy_vein(x, y, z)
        pickup_nearby_items()
        "#);

        info!("{:?}", result);
    }
}
