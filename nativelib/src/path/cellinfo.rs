use crate::BoolBitset;

#[derive(Copy, Clone, Serialize, Deserialize)]
#[repr(C)]
pub struct CellInfo {
    pub g: f64,
    pub rhs: f64,
    pub cost: f64,

    // TODO: use to store biases (eg mob bias, or fall damage)
    pub flags: BoolBitset,
}

unsafe impl Sync for CellInfo {}

unsafe impl Send for CellInfo {}

impl Default for CellInfo {
    fn default() -> Self {
        CellInfo::new(0.0)
    }
}

impl CellInfo {
    pub fn new(cost: f64) -> CellInfo {
        CellInfo {
            g: 0.0,
            rhs: 0.0,
            cost,
            flags: BoolBitset::default(),
        }
    }

    pub fn create(g: f64, rhs: f64, cost: f64) -> CellInfo {
        CellInfo {
            g,
            rhs,
            cost,
            flags: BoolBitset::default(),
        }
    }
}
