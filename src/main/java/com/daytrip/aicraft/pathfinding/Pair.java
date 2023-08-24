package com.daytrip.aicraft.pathfinding;

public class Pair<T, U> implements Cloneable, java.io.Serializable {
    private T object1;
    private U object2;
    private boolean object1Null;
    private boolean object2Null;
    private boolean dualNull;

    public Pair(T object1, U object2) {

        this.object1 = object1;
        this.object2 = object2;
        object1Null = object1 == null;
        object2Null = object2 == null;
        dualNull = object1Null && object2Null;

    }

    public T first() {

        return object1;

    }

    public U second() {

        return object2;

    }

    public void setFirst(T object1) {
        this.object1 = object1;
        object1Null = object1 == null;
        dualNull = object1Null && object2Null;
    }

    public void setSecond(U object2) {
        this.object2 = object2;
        object2Null = object2 == null;
        dualNull = object1Null && object2Null;
    }

    @Override
    public Pair<T, U> clone() {

        return new Pair<>(object1, object2);

    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;

        if (this == obj)
            return true;

        if (!(obj instanceof Pair<?, ?> otherPair))
            return false;

        if (dualNull)
            return otherPair.dualNull;

        //After this we're sure at least one part in this is not null

        if (otherPair.dualNull)
            return false;

        //After this we're sure at least one part in obj is not null

        if (object1Null) {
            if (otherPair.object1Null) //Yes: this and other both have non-null part2
                return object2.equals(otherPair.object2);
            else if (otherPair.object2Null) //Yes: this has non-null part2, other has non-null part1
                return object2.equals(otherPair.object1);
            else //Remaining case: other has no non-null parts
                return false;
        } else if (object2Null) {
            if (otherPair.object2Null) //Yes: this and other both have non-null part1
                return object1.equals(otherPair.object1);
            else if (otherPair.object1Null) //Yes: this has non-null part1, other has non-null part2
                return object1.equals(otherPair.object2);
            else //Remaining case: other has no non-null parts
                return false;
        } else {
            //Transitive and symmetric requirements of equals will make sure
            //checking the following cases are sufficient
            if (object1.equals(otherPair.object1))
                return object2.equals(otherPair.object2);
            else if (object1.equals(otherPair.object2))
                return object2.equals(otherPair.object1);
            else
                return false;
        }
    }

    @Override
    public int hashCode() {
        int hashCode = object1Null ? 0 : object1.hashCode();
        hashCode += (object2Null ? 0 : object2.hashCode());
        return hashCode;
    }

    @Override
    public String toString() {
        return "Pair{" +
                "object1=" + object1 +
                ", object2=" + object2 +
                '}';
    }
}
