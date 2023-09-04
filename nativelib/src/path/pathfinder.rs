use std::cell::Cell;
use std::collections::{BinaryHeap, HashMap, HashSet};
use std::hash::{BuildHasherDefault};
use std::time::Instant;
use num_traits::FromPrimitive;
use parking_lot::Mutex;
use crate::path::cellinfo::CellInfo;
use crate::path::state::State;
use crate::chunk::chunk::ChunkManager;
use crate::util::hashmap::FxHasher;
use crate::path::block_type::BlockType;

#[repr(C)]
pub struct Pathfinder<'a> {
    // Critical data
    pub path: Vec<State>,
    pub debug: Vec<State>,
    default_cost: f64,
    empty_cost: f64,
    start: State,
    goal: State,
    last: State,
    max_steps: i64,
    k_m: f64,
    open_hash: HashMap<State, f64, BuildHasherDefault<FxHasher>>,
    cell_hash: HashMap<State, CellInfo, BuildHasherDefault<FxHasher>>,
    open_list: BinaryHeap<State>,

    // Caches
    ground_cache: HashMap<(i64, i64), i64, BuildHasherDefault<FxHasher>>,
    sqrt2: f64,
    sqrt3: f64,

    // World info
    chunk_manager: &'a Mutex<Cell<Option<ChunkManager>>>,
}

impl<'a> Pathfinder<'a> {
    pub fn create(cm: &'a Mutex<Cell<Option<ChunkManager>>>) -> Self {
        Self {
            path: vec![],
            debug: vec![],
            default_cost: 1.0,
            empty_cost: 0.0,
            start: State::blank(),
            goal: State::blank(),
            last: State::blank(),
            max_steps: 80000,
            k_m: 0.0,
            open_hash: HashMap::default(),
            cell_hash: HashMap::default(),
            open_list: BinaryHeap::new(),
            ground_cache: HashMap::default(),
            sqrt2: 2.0f64.sqrt(),
            sqrt3: 3.0f64.sqrt(),
            chunk_manager: cm,
        }
    }

    pub fn init(&mut self, start: State, goal: State) {
        self.start = start;
        self.goal = goal;

        self.path.clear();
        self.cell_hash.clear();
        self.open_hash.clear();
        self.open_list.clear();

        self.k_m = 0.0;

        self.cell_hash.insert(self.goal, CellInfo::new(self.default_cost));

        self.make_new_cell(self.start);
        self.start = self.calculate_key(self.start);

        self.last = self.start;
    }

    fn get_rhs(&mut self, s: State) -> f64 {
        if s == self.goal { return 0.0 };

        match self.cell_hash.get(&s) {
            None => {
                let h = self.heuristic(s, self.goal);
                self.cell_hash.insert(s, CellInfo::create(h, h, self.empty_cost));
                h
            }
            Some(c) => { c.rhs }
        }
    }

    fn set_rhs(&mut self, s: State, rhs: f64) {
        self.make_new_cell(s);
        self.cell_hash.get_mut(&s).unwrap().rhs = rhs;
    }

    fn get_g(&mut self, s: State) -> f64 {
        match self.cell_hash.get(&s) {
            None => {
                let h = self.heuristic(s, self.goal);
                self.cell_hash.insert(s, CellInfo::create(h, h, self.empty_cost));
                h
            }
            Some(c) => { c.g }
        }
    }

    fn set_g(&mut self, s: State, g: f64) {
        self.make_new_cell(s);
        self.cell_hash.get_mut(&s).unwrap().g = g;
    }

    fn calculate_key(&mut self, mut s: State) -> State {
        let min = self.get_rhs(s).min(self.get_g(s));

        s.k0 = min + self.heuristic(s, self.start) + self.k_m;
        s.k1 = min;

        s
    }

    fn heuristic(&mut self, a: State, b: State) -> f64 {
        self.eight_condist(a, b, 1.0, 1.0) * self.default_cost
    }

    fn eight_condist(&self, state1: State, state2: State, up_cost: f64, down_cost: f64) -> f64 {

        let delta_x = f64::abs((state1.x - state2.x) as f64);
        let delta_y = f64::abs((state1.y - state2.y) as f64);

        let delta_z = match state1.z < state2.z {
            true => f64::abs((state1.z - state2.z) as f64) * down_cost,
            false => f64::abs((state1.z - state2.z) as f64) * up_cost,
        };

        let min = f64::min(delta_x, delta_y).min(delta_z);

        let mid = f64::min(delta_x, delta_y)
            .max(f64::min(delta_x, delta_y).min(delta_z));

        let max = f64::max(delta_x, delta_y).max(delta_z);

        (self.sqrt3 - 1.0) * min + (self.sqrt2 - self.sqrt3) * mid + max
    }

    fn get_block(&self, s: State) -> i8 {
        debug!("Getting block at: {}", s.to_string());
        let state = self.chunk_manager.lock().get_mut().as_mut().expect("Not initialized!").get(s.x as isize, s.y as isize, s.z as isize).expect(&*format!("Chunk doesn't exist! {}, {}, {}", s.x, s.y, s.z));
        trace!("({})", state);
        // BlockType::from_i8(state).expect("Invalid block!") // !path -47 71 -237
        state
    }

    fn ground_level(&mut self, mut s: State) -> State {
        return s;

        /*debug!("Transforming: {}", s.to_string());
        let os = s.clone();

        /*
        Went from (-30, -251, 63) to 63 (cached).
        Transforming: (-30, -251, 64)
        Went from (-30, -251, 64) to 63 (cached).
        Transforming: (-30, -250, 62)
        Went from (-30, -250, 62) to 62 (cached).
        Transforming: (-30, -250, 64)
        Went from (-30, -250, 64) to 63 (cached).
         */

        match self.ground_cache.get(&(s.x, s.y)) {
            None => {
                while self.get_block(s.below()) == 0 {
                    s.i_below();
                }

                println!("Went from {} to {}.", os.to_string(), s.to_string());
                self.ground_cache.insert((os.x, os.y), s.z);

                s
            }
            Some(z) => {
                println!("Went from {} to {} (cached).", s.to_string(), z.to_string());
                s.set(None, None, Some(*z))
            }
        }*/
    }

    fn occupied(&mut self, s: State, g: Option<State>, u: bool) -> bool {
        let v = self.cell_hash.get(&s);

        if v.is_some() && v.unwrap().cost != self.empty_cost {
            return v.unwrap().cost < 0.0;
        }

        let at = self.get_block(s);
        let above = self.get_block(s.above());

        let o = if at == -1 || above == -1 {
            trace!("too solid!");
            -1.0
        } else if s.z - g.unwrap_or(self.ground_level(s)).z > 5 {
            trace!("too high!");
            -1.0
        } else {
            self.default_cost
        };


        if u {
            self.update_cell(s, o, !self.close(o, self.default_cost));
        }

        o < 0.0
    }

    fn get_succ(&mut self, s: State) -> [State; 26] {
        let mut arr: [State; 26] = Default::default();

        let mut i = 0;
        for dx in -1..=1 {
            for dy in -1..=1 {
                for dz in -1..=1 {
                    if dx == 0 && dy == 0 && dz == 0 { continue; }
                    arr[i] = State::full(s.x + dx, s.y + dy, s.z + dz, -1.0, -1.0);
                    i += 1;
                }
            }
        }

        arr
    }

    fn cost(&self, a: State, b: State) -> f64 {
        // let scale = if (a.x - b.x).abs() + (a.y - b.y).abs() + (a.z - b.z).abs() > 1 && a.z - b.z >= 0 { self.sqrt2 } else { 1.0 };
        let scale = if (a.x - b.x).abs() + (a.y - b.y).abs() + (a.z - b.z).abs() > 1 { self.sqrt2 } else { 1.0 };

        match self.cell_hash.get(&a) {
            None => { scale * self.default_cost }
            Some(i) => { scale * i.cost }
        }
    }

    fn true_distance(&self, a: State, b: State) -> f64 {
        let x = a.x - b.x;
        let y = a.y - b.y;
        let z = a.z - b.z;

        ((x*x + y*y + z*z) as f64).sqrt()
    }

    fn close(&self, a: f64, b: f64) -> bool {
        if a.is_infinite() && b.is_infinite() { return true };

        (a - b).abs() < 0.00001
    }

    pub fn replan(&mut self) -> i16 {
        let ms = Instant::now();
        let mut k = 0;

        self.path.clear();
        self.debug.clear();
        self.cell_hash.retain(|_, v| { v.flags.all_false() });

        let mut visited: HashSet<State, BuildHasherDefault<FxHasher>> = HashSet::default();

        if self.compute_shortest_path() < 0 {
            warn!("No Path to Goal! (CSP)");
            return -1;
        }

        let mut best: Vec<State> = Vec::new();

        let mut cur = self.start;

        if self.get_g(cur).is_infinite() {
            warn!("No Path to Goal! (Bad G)");
            return -1;
        }

        while cur != self.goal {
            k += 1;
            if k > 20 {
                warn!("Path too long!");
                return -1;
            }

            self.path.push(cur);
            visited.insert(cur);

            debug!("Visiting {}", cur.to_string());

            let mut cmin = f64::INFINITY;
            let mut tmin = 0.0;
            let mut smin = State::blank();

            let s = self.get_succ(cur);

            let mut dedupe: HashSet<State, BuildHasherDefault<FxHasher>> = HashSet::default();

            for ii in s {
                if ii == cur { continue; }

                let i = self.ground_level(ii);

                self.debug.push(i);

                if i == cur || dedupe.contains(&i) || visited.contains(&i) || self.occupied(i, Some(ii), true) { continue; }
                dedupe.insert(i);

                let val = self.cost(cur, i) + self.get_g(i);
                let val2 = self.true_distance(i, self.goal) + self.true_distance(self.start, i);

                debug!("For {}: {}, {}", cur.to_string(), val, val2);

                if self.close(val, cmin) {
                    if tmin > val2 {
                        tmin = val2;
                        cmin = val;
                        smin = i;
                    }
                } else if val < cmin {
                    tmin = val2;
                    cmin = val;
                    smin = i;
                }
            }

            let elements: Vec<String> = dedupe.iter().map(|item| item.to_string()).collect();
            debug!("All valid: [{}]", elements.join(", "));

            if dedupe.is_empty() {
                best.retain(|&x| x != cur);
                smin = match best.pop() {
                    None => { self.start }
                    Some(b) => { b }
                };

                if let Some(index) = self.path.iter().position(|&x| x == smin) {
                    self.path.truncate(index + 1);
                };
                visited.clear();
                for s in &self.path {
                    visited.insert(*s);
                }

                debug!("Rerouting to {}.", smin.to_string());
            } else if dedupe.len() >= 2 {
                best.push(smin);
            }

            if cur == smin {
                warn!("No path found! (Duplicate State)");
                return -1;
            }

            cur = smin.clone();
        }

        self.path.push(self.goal);

        (Instant::now() - ms).as_millis() as i16
    }

    fn key_hashcode(&self, s: State) -> f64 {
        s.k0 + 1193.0 * s.k1
    }

    fn insert(&mut self, mut s: State) {
        s = self.calculate_key(s);
        self.open_hash.insert(s, self.key_hashcode(s));
        self.open_list.push(s);
    }

    fn update_vertex(&mut self, s: State) {
        let mut rhs = self.get_rhs(s);

        if s != self.goal && !self.occupied(s, None, false) {
            let n = self.get_succ(s);
            let mut tmp = f64::INFINITY;

            for i in n {
                let tmp2 = self.get_g(i) + self.cost(s, i);
                if tmp2 < tmp { tmp = tmp2 };
            }

            if !self.close(rhs, tmp) {
                self.set_rhs(s, tmp);
                rhs = tmp;
            }
        }

        let g= self.get_g(s);
        if !self.close(g, rhs) { self.insert(s); }
    }

    fn make_new_cell(&mut self, s: State) {
        if self.cell_hash.contains_key(&s) { return; }
        let h = self.heuristic(s, self.goal);
        self.cell_hash.insert(s, CellInfo::create(h, h, self.default_cost));
    }

    pub fn update_cell(&mut self, s: State, cost: f64, a: bool) {
        if s == self.start || s == self.goal { return; }
        self.make_new_cell(s);
        self.cell_hash.get_mut(&s).unwrap().cost = cost;
        if a {
            self.update_vertex(s);
        }
    }

    pub fn update_start(&mut self, s: State) {
        self.start = s;
        self.k_m += self.heuristic(self.last, self.start);
        self.start = self.calculate_key(self.start);
        self.last = self.start;
    }

    pub fn update_goal(&mut self, s: State) {
        let mut to_add: Vec<(State, f64)> = Vec::new();

        for key in self.cell_hash.keys() {
            let value = self.cell_hash.get(key).unwrap();
            if !self.close(value.cost, self.default_cost) {
                to_add.push((*key, value.cost));
            }
        }

        self.cell_hash.clear();
        self.open_hash.clear();
        self.open_list.clear();

        self.k_m = 0.0;

        self.goal = s;

        self.cell_hash.insert(self.goal, CellInfo::create(0.0, 0.0, self.default_cost));

        self.make_new_cell(self.start);
        self.start = self.calculate_key(self.start);

        self.last = self.start;

        for p in to_add {
            self.update_cell(State::create(p.0.x, p.0.y, p.0.z), p.1, true);
        }
    }

    fn valid(&self, s: State) -> bool {
        match self.open_hash.get(&s) {
            None => { false }
            Some(x) => { self.close(self.key_hashcode(s), *x) }
        }
    }

    fn compute_shortest_path(&mut self) -> i8 {
        if self.open_list.is_empty() { return 1; }

        let mut k = 0;

        let elements: Vec<String> = self.open_list.iter().map(|item| item.to_string()).collect();
        debug!("All open: [{}]", elements.join(", "));

        let key = self.calculate_key(self.start);
        while !self.open_list.is_empty() && (*self.open_list.peek().unwrap() < key) || self.get_rhs(self.start) != self.get_g(self.start) {
            k += 1;

            if k > self.max_steps {
                warn!("At maximum steps!");
                return -1;
            }

            let mut u: State;

            let test = self.get_rhs(self.start) != self.get_g(self.start);

            loop {
                if self.open_list.is_empty() { return 1; }
                u = self.open_list.pop().unwrap();

                if !self.valid(u) { continue; }
                if u >= self.start && !test { return 2; }
                break;
            }

            self.open_hash.remove(&u);

            let old = u.clone();

            if old < self.calculate_key(u) {
                self.insert(u);
            } else if self.get_g(u) > self.get_rhs(u) {
                let rhs = self.get_rhs(u);
                self.set_g(u, rhs);
                let s = self.get_succ(u);

                for i in s {
                    if !self.occupied(i, None, false) {
                        self.update_vertex(i);
                    }
                }
            } else {
                self.set_g(u, f64::INFINITY);

                let s = self.get_succ(u);

                for i in s {
                    if !self.occupied(i, None, false) {
                        self.update_vertex(i);
                    }
                }
                self.update_vertex(u);
            }
        }

        0
    }
}
