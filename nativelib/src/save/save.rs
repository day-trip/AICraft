use serde::{Serialize};

trait Saveable {
    fn get_save_data<T>(&self) -> T;
}