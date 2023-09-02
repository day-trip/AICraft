package com.daytrip.aicraft.pathfinding;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.*;

public class DStarLite implements java.io.Serializable {
    private final List<State> path = new ArrayList<>();
    private final double C1;
    private final State s_goal = new State();
    private final int maxSteps;
    private final PriorityQueue<State> openList = new PriorityQueue<>();
    private final HashMap<State, Float> openHash = new HashMap<>();
    private final double M_SQRT3 = Math.sqrt(3.0);
    public HashMap<State, CellInfo> cellHash = new HashMap<>();
    private double k_m;
    private State s_start = new State();
    private State s_last = new State();
	private final ClientLevel level;
    private final LocalPlayer player;

    public DStarLite(ClientLevel level, LocalPlayer player) {
		this.level = level;
        this.player = player;

        maxSteps = 80000;
        C1 = 1;
    }

	public void init(BlockPos start, BlockPos goal) {
		this.init(new State(start), new State(goal));
	}

    public void init(State s, State g) {
        cellHash.clear();
        path.clear();
        openHash.clear();
        while (!openList.isEmpty()) openList.poll();

        k_m = 0;

        s_start.x = s.x;
        s_start.y = s.y;
        s_start.z = s.z;
        s_goal.x = g.x;
        s_goal.y = g.y;
        s_goal.z = g.z;

        CellInfo tmp = new CellInfo();
        tmp.g = 0;
        tmp.rhs = 0;
        tmp.cost = C1;

        cellHash.put(s_goal, tmp);

        makeNewCell(s_start);
        calculateKey(s_start);

        s_last = s_start;
    }

    private State calculateKey(State u) {
        double val = Math.min(getRHS(u), getG(u));

        u.k.setFirst(val + heuristic(u, s_start) + k_m);
        u.k.setSecond(val);

        return u;
    }

    private double getRHS(State u) {
        if (u == s_goal) return 0;

        CellInfo v = cellHash.get(u);
        if (v == null)
            return heuristic(u, s_goal);
        return v.rhs;
    }

    private double getG(State u) {
        CellInfo v = cellHash.get(u);
        if (v == null)
            return heuristic(u, s_goal);
        return v.g;
    }

    private double heuristic(State a, State b) {
        return eightCondist(a, b, M_SQRT3, M_SQRT2) * C1;
        // return trueDist(a, b) * C1;
    }

    private double eightCondist(State a, State b, double upCost, double downCost) {
        double temp;
        double deltaX = Math.abs(a.x - b.x);
        double deltaY = Math.abs(a.y - b.y);
        double deltaZ = Math.abs(a.z - b.z);

        // Calculate cost-adjusted deltaZ
        double adjustedDeltaZ = (a.z < b.z) ? deltaZ * downCost : deltaZ * upCost;

        // Find the two smallest differences among deltaX, deltaY, and adjustedDeltaZ
        double min = Math.min(Math.min(deltaX, deltaY), adjustedDeltaZ);
        double mid = Math.max(Math.min(deltaX, deltaY), Math.min(Math.max(deltaX, deltaY), adjustedDeltaZ));
        double max = Math.max(Math.max(deltaX, deltaY), adjustedDeltaZ);

        // Reorder the differences if necessary
        if (min > mid) {
            temp = min;
            min = mid;
            mid = temp;
        }
        if (mid > max) {
            temp = mid;
            mid = max;
            max = temp;
        }
        if (min > mid) {
            temp = min;
            min = mid;
            mid = temp;
        }

        return ((M_SQRT3 - 1.0) * min + (M_SQRT2 - M_SQRT3) * mid + max);
    }

    public boolean replan() {
        long time = System.currentTimeMillis();

        Stack<State> best = new Stack<>();
        path.clear();

        int res = computeShortestPath();
        if (res < 0) {
            System.out.println("No Path to Goal! (CSP)");
            return false;
        }

        State cur = s_start;

        if (getG(s_start) == Double.POSITIVE_INFINITY) {
            System.out.println("No Path to Goal! (Bad G)");
            return false;
        }

        int k = 0;

        double m_cmin = Double.POSITIVE_INFINITY;
        double m_tmin = 0;
        State m_smin = new State();

        while (cur.neq(s_goal)) {
            k++;
            if (k > maxSteps) {
                System.out.println("Took to long!");
                return false;
            }
            System.out.println("On: " + cur + " (" + k + ")");
            path.add(cur);

            double cmin = Double.POSITIVE_INFINITY;
            double tmin = 0;
            State smin = new State();
            var n = getSucc(cur);
            var dedupe = new ArrayList<State>(27);
            n[26] = m_smin;
            System.out.println(Arrays.toString(n));

            for (State ii : n) {
                // Skip if null
                if (ii.eq(cur)) {
                    continue;
                }

                State i = groundLevel(ii);
                if (i.eq(cur) || dedupe.contains(i) || path.contains(i) || occupied(i, cur, ii, true)/* || (i.neq(m_smin) && path.contains(i))*/) { // TODO: add back later & debug for better quality paths; my brain is too fried right now
                    continue;
                }
                dedupe.add(i);
                double val = cost(cur, i);
                double val2 = trueDist(i, s_goal) + trueDist(s_start, i);
                val += getG(i);

                if (close(val, cmin)) {
                    if (tmin > val2) {
                        tmin = val2;
                        cmin = val;
                        smin = i;
                    }
                } else if (val < cmin) {
                    tmin = val2;
                    cmin = val;
                    smin = i;
                }

                if (close(val, m_cmin)) {
                    if (m_tmin > val2) {
                        m_tmin = val2;
                        m_cmin = val;
                        m_smin = i;
                    }
                } else if (val < m_cmin) {
                    m_tmin = val2;
                    m_cmin = val;
                    m_smin = i;
                }
            }

            if (dedupe.isEmpty()) {
                best.remove(cur);
                smin = best.isEmpty() ? s_start : best.pop();
                int idx = path.indexOf(smin);
                if (idx > 0) {
                    path.subList(idx, path.size()).clear();
                }
                System.out.println("Rerouting to " + smin + " (" + k + ")");
                updateCell(cur, -2);
            } else if (dedupe.size() >= 2) {
                best.push(smin);
            }

            if (cur.eq(smin)) {
                System.out.println("No path found! (hmm...)");
                return false;
            }

            /*if (smin.eq(m_smin)) {
                System.out.println("Going to minimum value!");
                updateCell(cur, -2);
                int idx = path.indexOf(smin);
                if (idx >= 0) {
                    path.subList(idx, path.size()).clear();
                }
            }*/

            cur = new State(smin);
        }
        path.add(s_goal);
        System.out.println("Only took so long *ugh* (" + (System.currentTimeMillis() - time) + "ms) (" + k + " steps)");
        return true;
    }

    private int computeShortestPath() {
        if (openList.isEmpty()) return 1;

        int k = 0;
        while (!openList.isEmpty() && openList.peek().lt(s_start = calculateKey(s_start)) || getRHS(s_start) != getG(s_start)) {

            if (k++ > maxSteps) {
                System.out.println("At maxsteps");
                return -1;
            }

            State u;

            boolean test = (getRHS(s_start) != getG(s_start));

            while (true) {
                if (openList.isEmpty()) return 1;
                u = openList.poll();

                if (!isValid(u)) continue;
                if (!(u.lt(s_start)) && (!test)) return 2;
                break;
            }

            openHash.remove(u);

            State k_old = new State(u);

            if (k_old.lt(calculateKey(u))) { //u is out of date
                insert(u);
            } else if (getG(u) > getRHS(u)) { //needs update (got better)
                setG(u, getRHS(u));
                List<State> s = getPred(u);
                for (State i : s) {
                    updateVertex(i);
                }
            } else {                         // g <= rhs, state has got worse
                setG(u, Double.POSITIVE_INFINITY);
                List<State> s = getPred(u);

                for (State i : s) {
                    updateVertex(i);
                }
                updateVertex(u);
            }
        }
        return 0;
    }

    private State[] getSucc(State u) {
        State[] s = new State[27];
        int i = 0;

        for (int dz = -1; dz <= 1; dz++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dx = -1; dx <= 1; dx++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;

                    s[i] = (new State(u.x + dx, u.y + dy, u.z + dz, new Pair<>(-1.0, -1.0)));
                    i++;
                }
            }
        }

        return s;
    }

    private List<State> getPred(State u) {
        List<State> s = new ArrayList<>(26);
        State tempState;

        for (int dz = -1; dz <= 1; dz++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dx = -1; dx <= 1; dx++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;

                    tempState = new State(u.x + dx, u.y + dy, u.z + dz, new Pair<>(-1.0, -1.0));
                    if (!occupied(tempState)) s.add(tempState);
                }
            }
        }

        return s;
    }

    public void updateStart(BlockPos b) {
        this.updateStart(new State(b));
    }

    public void updateStart(State s) {
        clean();
        s_start.x = s.x;
        s_start.y = s.y;
        s_start.z = s.z;

        k_m += heuristic(s_last, s_start);

        calculateKey(s_start);
        s_last = s_start;

    }

    public void updateGoal(BlockPos b) {
        this.updateGoal(new State(b));
    }

    public void updateGoal(State s) {
        List<Pair<ipoint3, Double>> toAdd = new ArrayList<>();
        Pair<ipoint3, Double> tempPoint;

        for (Map.Entry<State, CellInfo> entry : cellHash.entrySet()) {
            if (!close(entry.getValue().cost, C1)) {
                tempPoint = new Pair<>(
                        new ipoint3(entry.getKey().x, entry.getKey().y, entry.getKey().z),
                        entry.getValue().cost);
                toAdd.add(tempPoint);
            }
        }

        cellHash.clear();
        openHash.clear();

        while (!openList.isEmpty())
            openList.poll();

        k_m = 0;

        s_goal.x = s.x;
        s_goal.y = s.y;
        s_goal.z = s.z;

        CellInfo tmp = new CellInfo();
        tmp.g = tmp.rhs = 0;
        tmp.cost = C1;

        cellHash.put(s_goal, tmp);

        makeNewCell(s_start);
        calculateKey(s_start);

        s_last = s_start;

        for (Pair<ipoint3, Double> ipoint3DoublePair : toAdd) {
            tempPoint = ipoint3DoublePair;
            updateCell(tempPoint.first().x, tempPoint.first().y, tempPoint.first().z, tempPoint.second());
        }
    }

    private void updateVertex(State u) {

        if (u.neq(s_goal)) {
            var s = getSucc(u);
            double tmp = Double.POSITIVE_INFINITY;
            double tmp2;

            for (State i : s) {
                if (i == null) {
                    continue;
                }
                tmp2 = getG(i) + cost(u, i);
                if (tmp2 < tmp) tmp = tmp2;
            }
            if (!close(getRHS(u), tmp)) setRHS(u, tmp);
        }

        if (!close(getG(u), getRHS(u))) insert(u);
    }

    private boolean isValid(State u) {
        if (openHash.get(u) == null) return false;
		return close(keyHashCode(u), openHash.get(u));
	}

    private void setG(State u, double g) {
        makeNewCell(u);
        cellHash.get(u).g = g;
    }

    private void setRHS(State u, double rhs) {
        makeNewCell(u);
        cellHash.get(u).rhs = rhs;
    }

    private void makeNewCell(State u) {
        if (cellHash.get(u) != null) return;
        CellInfo tmp = new CellInfo();
        tmp.g = tmp.rhs = heuristic(u, s_goal);
        tmp.cost = C1;
        cellHash.put(u, tmp);
    }

	private double calculateCost(State s, State g, CellInfo info) {
		BlockPos pos = s.asBlock();

        if (!level.isInWorldBounds(pos)) {
            return -1;
        }

        BlockState state = level.getBlockState(pos);
        BlockState above = level.getBlockState(pos.above());

        if (state.isSolid() || above.isSolid()) {
            // TODO: check tools for mining
            /*if (state.getBlock().defaultDestroyTime() < 4 && !state.requiresCorrectToolForDrops()) {
                return C1 * 6;
            }*/

            return -1;
        }

        BlockState ground = level.getBlockState(g.asBlock());
        // BlockState bG = level.getBlockState(g.asBlock().below());

        if (ground.getBlock() == Blocks.LAVA && !player.hasEffect(MobEffects.FIRE_RESISTANCE)) {
            return -1;
        }

        /*float m = ground.getBlock() == Blocks.WATER ? 0 : bG.getBlock() == Blocks.SLIME_BLOCK ? 0 : 1; // TODO: update to any such block
        if (player.getHealth() - calculateFallDamage(s.z - g.z, m) < 2) {
            if (player.getInventory().contains(Items.WATER_BUCKET.getDefaultInstance())) {
                return C1 * 1.5d;
            }

            return C1 * 6;
        }*/
        int d = s.z - g.z;
        if (d > 7) {
            return -1;
        }
        double cost = C1;
        // if (d > 0) cost *= (double) d /2;
        if (!level.getEntitiesOfClass(Monster.class, new AABB(pos, pos.offset(1, 1, 1)).inflate(5), EntitySelector.ENTITY_STILL_ALIVE).isEmpty()) {
            cost *= 3;
            info.mobBias = true;
        }

        if (state.getBlock() == Blocks.WATER) {
            cost *= 1.5;
        }

        return cost;
	}

    private int calculateFallDamage(float f, float g) {
        if (this.player.getType().is(EntityTypeTags.FALL_DAMAGE_IMMUNE)) {
            return 0;
        } else {
            MobEffectInstance mobEffectInstance = this.player.getEffect(MobEffects.JUMP);
            float h = mobEffectInstance == null ? 0.0F : (float)(mobEffectInstance.getAmplifier() + 1);
            return Mth.ceil((f - 3.0F - h) * g);
        }
    }

    private State groundLevel(State s) {
        BlockPos pos = s.asBlock();

        while (!level.getBlockState(pos.below()).isSolid() && level.getBlockState(pos).getBlock() != Blocks.WATER) {
            if (s.z - pos.getY() > level.getHeight()) {
                System.out.println("Ground level is negative infinity at " + s);
                return new State(s.x, s.y, Integer.MIN_VALUE);
            }
            pos = pos.below();
        }

        return new State(pos);
    }

    public void updateCell(State s, double val) {
        this.updateCell(s.x, s.y, s.z, val);
    }

    public void updateCell(int x, int y, int z, double val) {
        State u = new State(x, y, z);

        if ((u.eq(s_start)) || (u.eq(s_goal))) return;

        makeNewCell(u);
        cellHash.get(u).cost = val;
        updateVertex(u);
    }

    private void insert(State u) {
        //iterator cur
        float csum;

		calculateKey(u);
		//cur = openHash.find(u);
        csum = keyHashCode(u);

        // return if cell is already in list. TODO: this should be
        // uncommented except it introduces a bug, I suspect that there is a
        // bug somewhere else and having duplicates in the openList queue
        // hides the problem...
        //if ((cur != openHash.end()) && (close(csum,cur->second))) return;

        openHash.put(u, csum);
        openList.add(u);
    }

    private float keyHashCode(State u) {
        return (float) (u.k.first() + 1193 * u.k.second());
    }

    private boolean occupied(State u) {
        return this.occupied(u, null, groundLevel(u), false);
    }

    private void clean() {
        cellHash.entrySet().removeIf(x -> x.getValue().cost == -2 || x.getValue().mobBias);
    }

    private boolean occupied(State position, State from, State groundPosition, boolean writeCost) {
        if (cellHash.get(groundPosition) == null) {
            CellInfo info = new CellInfo();
            double c = calculateCost(position, groundPosition, info);

            if (writeCost) {
                updateCell(groundPosition, c);
                cellHash.get(groundPosition).mobBias = info.mobBias;
            }
        }

        return cellHash.getOrDefault(groundPosition, new CellInfo()).cost < 0;
    }

    private double trueDist(State a, State b) {
        float x = a.x - b.x;
        float y = a.y - b.y;
        float z = a.z - b.z;
        return Math.sqrt(x * x + y * y + z * z);
    }

    private static final double M_SQRT2 = Math.sqrt(2.0d);

    private double cost(State a, State b) {
        int xd = Math.abs(a.x - b.x);
        int yd = Math.abs(a.y - b.y);
        int zd = Math.abs(a.z - b.z);
        // double scale = xd + yd + zd > 1 && a.z - b.z >= 0 ? M_SQRT2 : 1;
        double scale = xd + yd + zd > 1 ? M_SQRT2 : 1;

        CellInfo cellInfo = cellHash.get(a);
        if (cellInfo == null) {
            return scale * C1;
        }
        return scale * cellInfo.cost;
    }

    private boolean close(double x, double y) {
        if (x == Double.POSITIVE_INFINITY && y == Double.POSITIVE_INFINITY) return true;
        return (Math.abs(x - y) < 0.00001);
    }

    public List<State> getPath() {
        return path;
    }
}

class CellInfo implements java.io.Serializable {
    public double g = 0;
    public double rhs = 0;
    public double cost = 0;
    public boolean mobBias = false;
}

class ipoint3 {
    public int x;
    public int y;
    public int z;

    public ipoint3(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
