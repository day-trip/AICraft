package com.daytrip.aicraft.natives;

import jnr.ffi.Runtime;
import jnr.ffi.Struct;
import net.minecraft.core.BlockPos;

import static com.daytrip.aicraft.natives.Natives.INSTANCE;

public class State extends Struct {
    public final Struct.Signed64 x = new Struct.Signed64();
    public final Struct.Signed64 y = new Struct.Signed64();
    public final Struct.Signed64 z = new Struct.Signed64();
    protected final Struct.Double k0 = new Struct.Double();
    protected final Struct.Double k1 = new Struct.Double();

    public State(Runtime runtime) {
        super(runtime);
    }

    public static State blank() {
        return create(0, 0, 0);
    }

    public static State create(long x, long y, long z) {
        var s =  new State(Runtime.getRuntime(INSTANCE));
        s.x.set(x);
        s.y.set(y);
        s.z.set(z);
        return s;
    }

    public static State fromBlock(BlockPos initial) {
        return create(initial.getX(), initial.getZ(), initial.getY());
    }

    @Override
    public java.lang.String toString() {
        return "(" + x.get() + ", " + y.get() + ", " + z.get() + ")";
    }
}
