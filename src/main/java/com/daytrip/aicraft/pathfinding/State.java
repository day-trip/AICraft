package com.daytrip.aicraft.pathfinding;

import net.minecraft.core.BlockPos;

public class State implements Comparable, java.io.Serializable {
    public int x = 0;
    public int y = 0;
    public int z = 0;
    public Pair<Double, Double> k = new Pair(0.0, 0.0);


    public State() {

    }

    public State(int x, int y, int z, Pair<Double, Double> k) {
        this(x, y, z);
        this.k = k;
    }

    public State(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public State(State other) {
        this(other.x, other.y, other.z, other.k);
    }

    public State(BlockPos pos) {
        this(pos.getX(), pos.getZ(), pos.getY());
    }

    public boolean eq(final State s2) {
        return ((this.x == s2.x) && (this.y == s2.y) && (this.z == s2.z));
    }

    public boolean neq(final State s2) {
        return ((this.x != s2.x) || (this.y != s2.y) || (this.z != s2.z));
    }

    public boolean gt(final State s2) {
        if (k.first() - 0.00001 > s2.k.first()) return true;
        else if (k.first() < s2.k.first() - 0.00001) return false;
        return k.second() > s2.k.second();
    }

    public boolean lte(final State s2) {
        if (k.first() < s2.k.first()) return true;
        else if (k.first() > s2.k.first()) return false;
        return k.second() < s2.k.second() + 0.00001;
    }

    public boolean lt(final State s2) {
        if (k.first() + 0.000001 < s2.k.first()) return true;
        else if (k.first() - 0.000001 > s2.k.first()) return false;
        return k.second() < s2.k.second();
    }

    public int compareTo(Object that) {
        State other = (State) that;
        if (k.first() - 0.00001 > other.k.first()) return 1;
        else if (k.first() < other.k.first() - 0.00001) return -1;
        if (k.second() > other.k.second()) return 1;
        else if (k.second() < other.k.second()) return -1;
        return 0;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
		result = prime * result + z;
		return result;
	}

    @Override
    public boolean equals(Object aThat) {
        if (this == aThat) return true;
        if (!(aThat instanceof State that)) return false;
		return this.x == that.x && this.y == that.y && this.z == that.z;

	}

    @Override
    public String toString() {
        return "State{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }

    public BlockPos asBlock() {
        return new BlockPos(this.x, this.z, this.y);
    }
}
