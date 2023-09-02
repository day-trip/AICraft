mod path;
mod chunk;
mod util;

#[macro_use]
extern crate num_derive;

pub use crate::path::{pathfinder::Pathfinder, state::State};
pub use crate::util::{hashmap::FxHasher, bitset::BoolBitset};
pub use crate::chunk::{chunk::Chunk, chunk::ChunkManager};

use std::cell::Cell;
use std::{ptr, slice};
use parking_lot::Mutex;

static PATHFINDER_STATE: Mutex<Cell<Option<Pathfinder>>> = Mutex::new(Cell::new(None));
static CHUNK_STATE: Mutex<Cell<Option<ChunkManager>>> = Mutex::new(Cell::new(None));

#[no_mangle]
pub extern "C" fn init() {
    println!("Hello world 1");
    CHUNK_STATE.lock().set(Some(ChunkManager::create()));
    PATHFINDER_STATE.lock().set(Some(Pathfinder::create(&CHUNK_STATE)));
}

#[no_mangle]
pub extern "C" fn pf_init(start: State, goal: State) {
    println!("Hello world 2");
    PATHFINDER_STATE.lock().get_mut().as_mut().expect("Not initialized!").init(start, goal);
}

#[no_mangle]
pub extern "C" fn pf_update_cell(cell: State, cost: f64) {
    PATHFINDER_STATE.lock().get_mut().as_mut().expect("Not initialized!").update_cell(cell, cost, true);
}

#[no_mangle]
pub extern "C" fn pf_replan() -> i16 {
    PATHFINDER_STATE.lock().get_mut().as_mut().expect("Not initialized!").replan()
}

#[no_mangle]
pub extern "C" fn pf_update_start(start: State) {
    PATHFINDER_STATE.lock().get_mut().as_mut().expect("Not initialized!").update_start(start);
}

#[no_mangle]
pub extern "C" fn pf_update_goal(goal: State) {
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
pub extern "C" fn chunk_build(x: i64, y: i64, arr: *const i8, len: i32) {
    println!("New chunk: {}, {}", x, y);
    unsafe {
        CHUNK_STATE.lock().get_mut().as_mut().expect("Not initialized!").build((x, y), Box::from_raw(slice::from_raw_parts_mut(arr as *mut i8, len as usize)));
    }
}

#[no_mangle]
pub extern "C" fn chunk_remove(x: i64, y: i64) {
    println!("Removing chunk (but not really): {}, {}", x, y);
    CHUNK_STATE.lock().get_mut().as_mut().expect("Not initialized!").remove((x, y));
}

#[no_mangle]
pub extern "C" fn chunk_set(x: i64, y: i64, z: i64, value: i8) {
    println!("Setting block {}, {}, {} to {}", x, y, z, value);
    CHUNK_STATE.lock().get_mut().as_mut().expect("Not initialized!").set(x as isize, y as isize, z as isize, value);
}

#[cfg(test)]
mod tests {
    use crate::{ChunkManager};

    /*#[test]
    fn pathfinder_works() {
        let mut pf = Pathfinder::create();

        pf.init(State::create(0, 0, 0), State::create(0, 100, 0));

        assert!(pf.path.is_empty());

        println!("maybe");

        let mut time = Instant::now();
        pf.replan();
        println!("{}", (Instant::now() - time).as_nanos());
        time = Instant::now();
        pf.update_start(State::create(0, 1, 0));
        pf.replan();
        println!("{}", (Instant::now() - time).as_nanos());
        time = Instant::now();
        pf.update_start(State::create(0, 3, 0));
        pf.replan();
        println!("{}", (Instant::now() - time).as_nanos());

        assert!(!pf.path.is_empty());
    }*/

    #[test]
    fn chunking_works() {
        let mut cm = ChunkManager::create();
        cm.build((0, 0), vec![0i8; 16 * 16 * 384].into_boxed_slice());
        assert_eq!(cm.get(0, 0, 0).unwrap(), 0);
        cm.set(1, 1, 3210, 5);
        assert_eq!(cm.get(1, 1, 3210).unwrap(), 5);

        cm.build((1, 0), vec![0i8; 16 * 16 * 384].into_boxed_slice());
        assert_eq!(cm.get(17, 0, 0).unwrap(), 0);
        assert_ne!(cm.get(0, 17, 0).unwrap_or(0), 1);
    }

    #[test]
    fn math_works_owo() {
        println!("{}", wrap(-1));
        println!("{}", wrap(170));
    }

    #[test]
    fn jai_is_not_dumb() {
        let mut i = 10;
        while 10 == i {
            i -= 1;
            println!("Hello world!");
        }
    }

    fn wrap(mut num: isize) -> isize {
        num = num % 16;

        if num < 0 { num = 16 + num }

        num
    }
}
