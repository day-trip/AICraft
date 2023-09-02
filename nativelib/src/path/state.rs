use std::cmp::{Ordering, PartialEq};
use std::cmp::PartialOrd;
use std::hash::{Hash, Hasher};

#[derive(Copy, Clone)]
#[repr(C)]
pub struct State {
    pub x: i64,
    pub y: i64,
    pub z: i64,
    pub k0: f64,
    pub k1: f64,
}

unsafe impl Sync for State {}

unsafe impl Send for State {}

impl Default for State {
    fn default() -> Self {
        State::blank()
    }
}

impl State {
    pub fn blank() -> State {
        State::create(0, 0, 0)
    }

    pub fn create(x: i64, y: i64, z: i64) -> State {
        State::full(x, y, z, 0.0, 0.0)
    }

    pub fn full(x: i64, y: i64, z: i64, k0: f64, k1: f64) -> State {
        State {
            x,
            y,
            z,
            k0,
            k1,
        }
    }

    pub fn set(&self, x: Option<i64>, y: Option<i64>, z: Option<i64>) -> State {
        State::full(x.unwrap_or(self.x), y.unwrap_or(self.y), z.unwrap_or(self.z), self.k0, self.k1)
    }
    
    pub fn add(&self, x: i64, y: i64, z: i64) -> State {
        State::full(self.x + x, self.y + y, self.z + z, self.k0, self.k1)
    }

    pub fn i_add(&mut self, x: i64, y: i64, z: i64) {
        self.x += x;
        self.y += y;
        self.z += z;
    }

    pub fn below(&self) -> State {
        self.add(0, 0, -1)
    }

    pub fn i_below(&mut self) {
        self.i_add(0, 0, -1);
    }

    pub fn above(&self) -> State {
        self.add(0, 0, 1)
    }

    pub fn i_above(&mut self) {
        self.i_add(0, 0, 1);
    }
}

impl PartialEq for State {
    fn eq(&self, other: &Self) -> bool {
        self.x == other.x && self.y == other.y && self.z == other.z
    }

    fn ne(&self, other: &Self) -> bool {
        self.x != other.x || self.y != other.y || self.z != other.z
    }
}

impl Eq for State {

}

impl PartialOrd for State {
    fn partial_cmp(&self, other: &Self) -> Option<Ordering> {
        Some(self.cmp(other))
    }
}

impl Ord for State {
    fn cmp(&self, other: &Self) -> Ordering {
        if (self.k0 - 0.00001).gt(&other.k0) {
            Ordering::Greater
        } else if (self.k0 + 0.000001).lt(&other.k0) {
            Ordering::Less
        } else if (self.k1 + 0.00001).lt(&other.k1) {
            Ordering::Less
        } else if (self.k1 - 0.00001).gt(&other.k1) {
            Ordering::Greater
        } else {
            Ordering::Equal
        }
    }
}

impl ToString for State {
    fn to_string(&self) -> String {
        format!("({}, {}, {})", self.x, self.y, self.z)
    }
}

impl Hash for State {
    fn hash<H: Hasher>(&self, state: &mut H) {
        self.x.hash(state);
        self.y.hash(state);
        self.z.hash(state);
    }
}
