use std::collections::HashMap;
use std::hash::BuildHasherDefault;
use crate::FxHasher;

pub const WIDTH: usize = 16;
pub const HEIGHT: usize = 384;
pub const SHIFT: usize = 64;
pub const FULL: usize = WIDTH * WIDTH * HEIGHT;

pub struct Chunk {
    pub data: Box<[(u8, u16)]>,
}

impl Chunk {
    pub fn create(data: &[u8]) -> Chunk {
        assert_eq!(data.len(), FULL, "Data length doesn't match!");
        Chunk {
            data: Chunk::create_rle(Chunk::create_bitvec(data)),
        }
    }

    pub fn populate(&mut self, data: &[u8]) {
        assert_eq!(data.len(), FULL, "Data length doesn't match!");
        self.data = Chunk::create_rle(Chunk::create_bitvec(data));
        info!("Successfully populated chunk!");
    }

    fn create_bitvec(data: &[u8]) -> [u8; 49152] {
        let mut bit_vector = [0u8; 49152];
        for (index, &value) in data.iter().enumerate() {
            let byte_index = (index * 4) / 8;
            let bit_index = (index * 4) % 8;
            let value_mask = 0b00001111 << bit_index;
            let shifted_value = (value << bit_index) & value_mask;
            bit_vector[byte_index] &= !value_mask;
            bit_vector[byte_index] |= shifted_value;
        }
        bit_vector
    }

    fn encode_run(value: u8, run_length: u16) -> (u8, u16) {
        (value, run_length - 1)
    }

    fn create_rle(bit_vector: [u8; 49152]) -> Box<[(u8, u16)]> {
        let mut encoded: Vec<(u8, u16)> = Vec::new();
        let mut current_value = bit_vector[0];
        let mut run_length = 1;

        for &bit in bit_vector.iter().skip(1) {
            if bit == current_value && run_length < u16::MAX {
                run_length += 1;
            } else {
                encoded.push(Chunk::encode_run(current_value, run_length));
                current_value = bit;
                run_length = 1;
            }
        }

        encoded.push(Chunk::encode_run(current_value, run_length));

        encoded.into_boxed_slice()
    }

    fn decode_rle(&self, compressed_index: u16) -> Option<(u8, usize, u16)> {
        let mut index = 0;

        for i in 0..self.data.len() {
            let (value, length) = self.data[i];

            if i == 0 && compressed_index < length {
                return Some((value, i, length));
            }

            if i < self.data.len() - 1 {
                let next_end_index = index + self.data[i+1].1;
                if compressed_index < next_end_index {
                    return Some((value, i, length));
                }
            }

            if length > 0 {
                index += length;
            }
        }

        None
    }

    fn update_rle(&mut self, compressed_index: u16, new_value: u8) {
        todo!("Implement");
    }

    pub fn set(&mut self, x: usize, y: usize, z: isize, value: u8) {
        assert!(z < (HEIGHT - SHIFT) as isize && z > -(SHIFT as isize), "Cannot set: Out of bounds!");
        let index = Chunk::calc_index(x, y, z);
        assert!(index < FULL, "Cannot set: Out of bounds array access!");
        trace!("(Set) Accessing data at {}", Chunk::calc_index(x, y, z));
        let byte_index = (index * 4) / 8;
        let bit_index = (index * 4) % 8;
        let value_mask = 0b00001111 << bit_index;
        let shifted_value = (value << bit_index) & value_mask;
        let mut old = self.decode_rle(byte_index as u16).expect("RLE issue!").0;
        old &= !value_mask;
        old |= shifted_value;
        self.update_rle(byte_index as u16, old);
        trace!("(Set) successful! {}", value);
    }

    pub fn get(&self, x: usize, y: usize, z: isize) -> u8 {
        // println!("{:?}\n---------------------------------------\n{}", self.data, self.data.len());
        assert!(z < (HEIGHT - SHIFT) as isize && z > -(SHIFT as isize), "Cannot get: Out of bounds!");
        let index = Chunk::calc_index(x, y, z);
        assert!(index < FULL, "Cannot get: Out of bounds array access!");
        trace!("(Get) Accessing data at {}", Chunk::calc_index(x, y, z));
        let byte_index = (index * 4) / 8;
        let bit_index = (index * 4) % 8;
        let value_byte = self.decode_rle(byte_index as u16).expect("RLE issue!").0;
        let value_mask = 0b00001111 << bit_index;
        let value = (value_byte & value_mask) >> bit_index;
        trace!("(Get) successful! {}", value);
        value
    }

    fn calc_index(x: usize, y: usize, z: isize) -> usize {
        (x * WIDTH * HEIGHT) + ((z + SHIFT as isize) as usize * WIDTH) + y
    }
}

pub struct ChunkManager(HashMap<(i64, i64), Chunk, BuildHasherDefault<FxHasher>>);

impl ChunkManager {
    pub fn create() -> ChunkManager {
        ChunkManager {
            0: HashMap::default()
        }
    }

    pub fn get(&self, x: isize, y: isize, z: isize) -> Option<i8> {
        self._debug(x, y, z);
        let coords = &(ChunkManager::proc_c(x), ChunkManager::proc_c(y));
        match self.0.get(coords) {
            None => {
                if self.request_chunk(coords) { self.get(x, y, z) } else { panic!("Can't get at {}, {}, {}; Chunk doesn't exist!", x, y, z); }
            }
            Some(chunk) => { Some(chunk.get(ChunkManager::proc_p(x), ChunkManager::proc_p(y), z) as i8 - 1) }
        }
    }

    pub fn set(&mut self, x: isize, y: isize, z: isize, value: i8) {
        self._debug(x, y, z);
        match self.0.get_mut(&(ChunkManager::proc_c(x), ChunkManager::proc_c(y))) {
            None => { panic!("Can't set at {}, {}, {}; Chunk doesn't exist!", x, y, z); }
            Some(chunk) => {
                chunk.set(ChunkManager::proc_p(x), ChunkManager::proc_p(y), z, (value + 1) as u8);
            }
        };
    }

    fn _debug(&self, _x: isize, _y: isize, _z: isize) {
        let elements: Vec<String> = self.0.keys().map(|k| k.clone().0.to_string() + " " + &*k.1.to_string()).collect();
        trace!("WOW: {}", elements.join(","));
        trace!("Cool: {}, {} from {}, {}", ChunkManager::proc_c(_x), ChunkManager::proc_c(_y), _x as f64 / WIDTH as f64, _y as f64 / WIDTH as f64);
        trace!("Oh yeah, and: {}, {}, {}", ChunkManager::proc_p(_x), ChunkManager::proc_p(_y), _z);
    }

    fn proc_c(num: isize) -> i64 {
        let float = num as f64 / WIDTH as f64;
        (if float < 0.0 { float.ceil() } else { float.floor() }) as i64
    }

    fn proc_p(mut num: isize) -> usize {
        num %= WIDTH as isize;

        if num < 0 { num += WIDTH as isize }

        num as usize
    }

    fn request_chunk(&self, coords: &(i64, i64)) -> bool {
        //todo: todo!("implement some blocking logic");
        false
    }

    pub fn build(&mut self, coords: (i64, i64), data: &[u8]) {
        if self.0.contains_key(&coords) { self.swap(coords, data); } else { self.0.insert(coords, Chunk::create(data)); }
    }

    pub fn remove(&mut self, coords: (i64, i64)) {
        self.0.remove(&coords);
    }

    fn swap(&mut self, coords: (i64, i64), data: &[u8]) {
        match self.0.get_mut(&coords) {
            None => { panic!("Can't swap chunk because it doesn't exist!"); }
            Some(chunk) => {
                chunk.populate(data);
            }
        }
    }
}
