package com.daytrip.aicraft.pathfinding;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

public class DStarLite implements java.io.Serializable {
    private final List<State> path = new ArrayList<State>();
    private final double C1;
    private final State s_goal = new State();
    private final int maxSteps;
    private final PriorityQueue<State> openList = new PriorityQueue<State>();
    private final HashMap<State, Float> openHash = new HashMap<State, Float>();
    private final double M_SQRT3 = Math.sqrt(3.0);
    public HashMap<State, CellInfo> cellHash = new HashMap<State, CellInfo>();
    private double k_m;
    private State s_start = new State();
    private State s_last = new State();
	private final ClientLevel level;

    public DStarLite(ClientLevel level) {
		this.level = level;

        maxSteps = 80000;
        C1 = 1;
    }

    public static void main(String[] args) {
        DStarLite pf = new DStarLite(null);
        pf.init(new State(0, 1, 0), new State(3, 1, 0));
        pf.updateCell(2, 1, 0, -1);
        pf.updateCell(2, 0, 0, -1);
        pf.updateCell(2, 2, 0, -1);
        pf.updateCell(3, 0, 0, -1);

        System.out.println("Start node: (0,1)");
        System.out.println("End node: (3,1)");

        //Time the replanning
        long begin = System.currentTimeMillis();
        pf.replan();
        pf.updateGoal(3, 2, 0);
        long end = System.currentTimeMillis();

        System.out.println("Time: " + (end - begin) + "ms");

        List<State> path = pf.getPath();
        for (State i : path) {
            System.out.println(i);
        }

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

        tmp = new CellInfo();
        tmp.g = tmp.rhs = heuristic(s_start, s_goal);
        tmp.cost = C1;
        cellHash.put(s_start, tmp);
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

        //if the cellHash doesn't contain the State u
        if (cellHash.get(u) == null)
            return heuristic(u, s_goal);
        return cellHash.get(u).rhs;
    }

    private double getG(State u) {
        //if the cellHash doesn't contain the State u
        if (cellHash.get(u) == null)
            return heuristic(u, s_goal);
        return cellHash.get(u).g;
    }

    private double heuristic(State a, State b) {
        return eightCondist(a, b) * C1;
    }

    private double eightCondist(State a, State b) {
        double temp;
        double minXY = Math.min(Math.abs(a.x - b.x), Math.abs(a.y - b.y));
        double maxXY = Math.max(Math.abs(a.x - b.x), Math.abs(a.y - b.y));
        double minXYZ = Math.min(minXY, Math.abs(a.z - b.z));
        double maxXYZ = Math.max(maxXY, Math.abs(a.z - b.z));

        if (minXYZ > maxXYZ) {
            temp = minXYZ;
            minXYZ = maxXYZ;
            maxXYZ = temp;
        }

        return ((M_SQRT3 - 1.0) * minXYZ + maxXYZ);
    }

    public boolean replan() {
        path.clear();

        int res = computeShortestPath();
        if (res < 0) {
            System.out.println("No Path to Goal");
            return false;
        }

        State cur = s_start;

        if (getG(s_start) == Double.POSITIVE_INFINITY) {
            System.out.println("No Path to Goal");
            return false;
        }

        int k = 0;

        while (cur.neq(s_goal)) {
            k++;
            System.out.println("On: " + cur + " (" + k + ")");
            path.add(cur);

            double cmin = Double.POSITIVE_INFINITY;
            double tmin = 0;
            State smin = new State();
            int j = 0;
            Set<State> n = getSucc(cur);
            n.add(openList.poll()); // add best
            System.out.println(n);

            // TODO: compare cell with best from priorityqueue, and add loop prevention, then add better occupied detection, find ground level, and test over large range

            for (State i : n) {
                if (i == null || occupied(i, cur, true)) { // important for the openList polling
                    continue;
                }
                i = groundLevel(i);
                if (path.contains(i)) {
                    continue;
                }
                j++;

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
            }

            if (j == 0) {
                path.remove(path.size() - 1);
                if (path.size() < 1) {
                    System.out.println("No path found!");
                    return false;
                }
                smin = path.get(path.size() - 1);
                System.out.println("Rerouting to " + smin + " (" + k + ")");
                updateCell(cur, -1);
            }

            if (cur == smin) {
                System.out.println("No path found!");
                return false;
            }

            cur = new State(smin);
            //cur = smin;
        }
        path.add(s_goal);
        return true;
    }

    private int computeShortestPath() {
        LinkedList<State> s = new LinkedList<>();

        if (openList.isEmpty()) return 1;

        int k = 0;
        while ((!openList.isEmpty()) &&
                (openList.peek().lt(s_start = calculateKey(s_start))) ||
                (getRHS(s_start) != getG(s_start))) {

            if (k++ > maxSteps) {
                System.out.println("At maxsteps");
                return -1;
            }

            State u;

            boolean test = (getRHS(s_start) != getG(s_start));

            //lazy remove
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
                s = getPred(u);
                for (State i : s) {
                    updateVertex(i);
                }
            } else {                         // g <= rhs, state has got worse
                setG(u, Double.POSITIVE_INFINITY);
                s = getPred(u);

                for (State i : s) {
                    updateVertex(i);
                }
                updateVertex(u);
            }
        } //while
        return 0;
    }

    private Set<State> getSucc(State u) {
        Set<State> s = new HashSet<>();

        // Generate the successors in a 3D grid
        for (int dz = -1; dz <= 1; dz++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dx = -1; dx <= 1; dx++) {
                    // Skip the center cell (dx=0, dy=0, dz=0)
                    if (dx == 0 && dy == 0 && dz == 0) continue;

                    State tempState = new State(u.x + dx, u.y + dy, u.z + dz, new Pair(-1.0, -1.0));
                    s.add(tempState);
                }
            }
        }

        return s;
    }

    private LinkedList<State> getPred(State u) {
        LinkedList<State> s = new LinkedList<State>();
        State tempState;

        for (int dz = -1; dz <= 1; dz++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dx = -1; dx <= 1; dx++) {
                    // Skip the center cell (dx=0, dy=0, dz=0)
                    if (dx == 0 && dy == 0 && dz == 0) continue;

                    tempState = new State(u.x + dx, u.y + dy, u.z + dz, new Pair(-1.0, -1.0));
                    if (!occupied(tempState)) s.addFirst(tempState);
                }
            }
        }

        return s;
    }

    public void updateStart(int x, int y, int z) {
        s_start.x = x;
        s_start.y = y;
        s_start.z = z;

        k_m += heuristic(s_last, s_start);

        s_start = calculateKey(s_start);
        s_last = s_start;

    }

    public void updateGoal(int x, int y, int z) {
        List<Pair<ipoint3, Double>> toAdd = new ArrayList<Pair<ipoint3, Double>>();
        Pair<ipoint3, Double> tempPoint;

        for (Map.Entry<State, CellInfo> entry : cellHash.entrySet()) {
            if (!close(entry.getValue().cost, C1)) {
                tempPoint = new Pair(
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

        s_goal.x = x;
        s_goal.y = y;
        s_goal.z = z;

        CellInfo tmp = new CellInfo();
        tmp.g = tmp.rhs = 0;
        tmp.cost = C1;

        cellHash.put(s_goal, tmp);

        tmp = new CellInfo();
        tmp.g = tmp.rhs = heuristic(s_start, s_goal);
        tmp.cost = C1;
        cellHash.put(s_start, tmp);
        s_start = calculateKey(s_start);

        s_last = s_start;

        Iterator<Pair<ipoint3, Double>> iterator = toAdd.iterator();
        while (iterator.hasNext()) {
            tempPoint = iterator.next();
            updateCell(tempPoint.first().x, tempPoint.first().y, tempPoint.first().z, tempPoint.second());
        }


    }

    private void updateVertex(State u) {

        if (u.neq(s_goal)) {
            var s = getSucc(u);
            double tmp = Double.POSITIVE_INFINITY;
            double tmp2;

            for (State i : s) {
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

	private double calculateCost(State s) {
		BlockPos pos = s.asBlock();

        BlockState state = level.getBlockState(pos);
		if (!state.isSolid() && !level.getBlockState(pos.above()).isSolid() && (state.getBlock() == Blocks.WATER || s.z - groundLevel(s).z < 7)) {
			return C1;
		}

		return -1;
	}

    private State groundLevel(State s) {
        BlockPos pos = s.asBlock();

        while (!level.getBlockState(pos.below()).isSolid() && level.getBlockState(pos).getBlock() != Blocks.WATER) {
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
        return this.occupied(u, null, false);
    }

    private boolean occupied(State u, State f, boolean r) {
        if (path.contains(u)) {
            return true;
        }

        double c = calculateCost(u);

        if (cellHash.get(u) == null && r) {
            updateCell(u, c);
        }

        return c < 0;
    }

    private double trueDist(State a, State b) {
        float x = a.x - b.x;
        float y = a.y - b.y;
        float z = a.z - b.z;
        return Math.sqrt(x * x + y * y + z * z);
    }

    private double cost(State a, State b) {
        int xd = Math.abs(a.x - b.x);
        int yd = Math.abs(a.y - b.y);
        int zd = Math.abs(a.z - b.z);
        double scale = 1;

        if (xd + yd + zd > 1) scale = M_SQRT3;

        if (!cellHash.containsKey(a)) return scale * C1;
        return scale * cellHash.get(a).cost;
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
