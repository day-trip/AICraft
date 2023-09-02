use std::collections::HashMap;
use std::hash::BuildHasherDefault;
use crate::FxHasher;

const WIDTH: usize = 16;
const HEIGHT: usize = 384;
const SHIFT: usize = 64;

pub struct Chunk {
    data: Box<[i8]>,
}

impl Chunk {
    pub fn create(data: Box<[i8]>) -> Chunk {
        Chunk {
            data,
        }
    }

    pub fn populate(&mut self, data: Box<[i8]>) {
        self.data = data;
    }

    pub fn set(&mut self, x: usize, y: usize, z: isize, value: i8) {
        if z > (HEIGHT - SHIFT) as isize || z < -(SHIFT as isize) {
            panic!("Cannot set: Out of bounds!");
        }

        self.data[(x * WIDTH * HEIGHT) + ((z + SHIFT as isize) as usize * WIDTH) + y] = value;
    }

    pub fn get(&self, x: usize, y: usize, z: isize) -> i8 {
        if z > (HEIGHT - SHIFT) as isize || z < -(SHIFT as isize) {
            panic!("Cannot get: Out of bounds!");
        }
        self.data[(x * WIDTH * HEIGHT) + ((z + SHIFT as isize) as usize * WIDTH) + y]
    }
}

/*pub struct Chunk {
    top: i16,
    matrix: CsMat<i4>,
}

impl Chunk {
    fn create(array: &[[[i4; HEIGHT]; WIDTH]; WIDTH]) -> Chunk {
        let mut rows = Vec::with_capacity(WIDTH * HEIGHT * WIDTH);
        let mut cols = Vec::with_capacity(WIDTH * HEIGHT * WIDTH);
        let mut data = Vec::with_capacity(WIDTH * HEIGHT * WIDTH);

        for (z, layer) in array.iter().enumerate() {
            for (x, row) in layer.iter().enumerate() {
                for (y, &value) in row.iter().enumerate() {
                    let index = x * WIDTH + y;
                    rows.push(index);
                    cols.push(z);
                    data.push(value);
                }
            }
        }

        let csc_matrix = CsMat::new((WIDTH * WIDTH, HEIGHT), rows, cols, data);

        Chunk {
            top: 384,
            matrix: csc_matrix
        }
    }

    fn get(&self, x: usize, y: usize, z: isize) -> Option<i4> {
        let index = x + WIDTH * y;
        self.matrix.get(index, (z + 64) as usize).map(|x| *x)
    }

    fn set(&mut self, x: usize, y: usize, z: isize, value: i4) {
        let index = x + WIDTH * y;
        self.matrix.set(index, (z + 64) as usize, value);
    }
}*/

pub struct ChunkManager(HashMap<(i64, i64), Chunk, BuildHasherDefault<FxHasher>>);

impl ChunkManager {
    pub fn create() -> ChunkManager {
        ChunkManager {
            0: HashMap::default()
        }
    }

    pub fn get(&self, x: isize, y: isize, z: isize) -> Option<i8> {
        self._debug(x, y, z);
        let coords = &(self.proc_c(x), self.proc_c(y));
        match self.0.get(coords) {
            None => {
                if self.request_chunk(coords) { self.get(x, y, z) } else { panic!("Can't get at {}, {}, {}; Chunk doesn't exist!", x, y, z); }
            }
            Some(chunk) => { Some(chunk.get(self.proc_p(x), self.proc_p(y), z)) }
        }
    }

    pub fn set(&mut self, x: isize, y: isize, z: isize, value: i8) {
        let xx = self.proc_p(x);
        let yy = self.proc_p(y);
        self._debug(x, y, z);
        match self.0.get_mut(&(self.proc_c(x), self.proc_c(y))) {
            None => { panic!("Can't set at {}, {}, {}; Chunk doesn't exist!", x, y, z) }
            Some(chunk) => {
                chunk.set(xx, yy, z, value)
            }
        };
    }

    fn _debug(&self, x: isize, y: isize, z: isize) {
        // let elements: Vec<String> = self.0.keys().map(|k| k.clone().0.to_string() + " " + &*k.1.to_string()).collect();
        // println!("WOWWW: {}", elements.join(","));
        println!("Cool: {}, {} from {}, {}", self.proc_c(x), self.proc_c(y), x as f64 / WIDTH as f64, y as f64 / WIDTH as f64);
        println!("Oh yeah, and: {}, {}, {}", self.proc_p(x), self.proc_p(y), z);
    }

    fn proc_c(&self, num: isize) -> i64 {
        let float = num as f64 / WIDTH as f64;
        (if float < 0.0 { float.floor() } else { float.ceil() }) as i64
    }

    fn proc_p(&self, mut num: isize) -> usize {
        num = num % WIDTH as isize;

        if num < 0 { num = WIDTH as isize + num }

        num as usize
    }

    fn request_chunk(&self, coords: &(i64, i64)) -> bool {
        // TODO: implement some blocking logic
        false
    }

    pub fn build(&mut self, coords: (i64, i64), data: Box<[i8]>) {
        if self.0.contains_key(&coords) { self.swap(coords, data); } else { self.0.insert(coords, Chunk::create(data)); }
    }

    pub fn remove(&mut self, coords: (i64, i64)) {
        self.0.remove(&coords);
    }

    pub fn swap(&mut self, coords: (i64, i64), data: Box<[i8]>) {
        match self.0.get_mut(&coords) {
            None => { panic!("Can't swap chunk because it doesn't exist!"); }
            Some(chunk) => {
                chunk.populate(data);
            }
        }
    }
}
