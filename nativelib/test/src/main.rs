use std::time::{SystemTime};

fn main() {
    let start = SystemTime::now();
    for i in 0..20 {
        println!("Hello, world!");
    }
    println!("{}", SystemTime::now().duration_since(start).expect("oof :(").as_nanos());
}
