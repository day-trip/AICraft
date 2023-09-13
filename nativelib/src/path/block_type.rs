#[derive(Eq, PartialEq, FromPrimitive, ToPrimitive, Debug, Copy, Clone)]
pub enum BlockType {
    SOLID = -1,
    AIR = 0,
    WATER = 1,
    OTHER = 2,
}
