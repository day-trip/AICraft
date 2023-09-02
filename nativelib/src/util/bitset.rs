#[derive(Copy, Clone)]
pub struct BoolBitset(u8);

impl BoolBitset {
    pub fn set(&mut self, index: u8) {
        self.0 |= 1 << index;
    }

    pub fn unset(&mut self, index: u8) {
        self.0 &= !(1 << index);
    }

    pub fn toggle(&mut self, index: u8) {
        self.0 ^= 1 << index;
    }

    pub fn get(&self, index: u8) -> bool {
        self.0 & (1 << index) != 0
    }

    pub fn all_false(&self) -> bool {
        self.0 == 0
    }

    pub fn any_true(&self) -> bool {
        self.0 != 0
    }
}

impl Default for BoolBitset {
    fn default() -> Self {
        BoolBitset {
            0: 0,
        }
    }
}
