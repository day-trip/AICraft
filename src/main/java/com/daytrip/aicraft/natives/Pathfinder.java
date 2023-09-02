package com.daytrip.aicraft.natives;

import jnr.ffi.Runtime;
import jnr.ffi.Struct;
import net.minecraft.core.BlockPos;

import static com.daytrip.aicraft.natives.Natives.INSTANCE;

public class Pathfinder {
    public static void init(State start, State goal) {
        INSTANCE.pf_init(start, goal);
    }

    public static void updateStart(State start) {
        INSTANCE.pf_update_start(start);
    }

    public static void updateGoal(State goal) {
        INSTANCE.pf_update_goal(goal);
    }

    public static short replan() {
        return INSTANCE.pf_replan();
    }

    public static void updateCell(State state, float cost) {
        INSTANCE.pf_update_cell(state, cost);
    }

    public static State[] getPath() {
        State[] array = Struct.arrayOf(Runtime.getRuntime(Natives.INSTANCE), State.class, INSTANCE.pf_get_path_len());
        INSTANCE.pf_get_path(array);
        return array;
    }

    public static State[] getDebug() {
        State[] array = Struct.arrayOf(Runtime.getRuntime(Natives.INSTANCE), State.class, INSTANCE.pf_get_debug_len());
        INSTANCE.pf_get_debug(array);
        return array;
    }

    public static BlockPos[] getBlocks() {
        var path = getPath();
        BlockPos[] blocks = new BlockPos[path.length];
        for (int i = 0; i < path.length; i++) {
            blocks[i] = new BlockPos(path[i].x.intValue(), path[i].z.intValue(), path[i].y.intValue());
        }
        return blocks;
    }

    public static BlockPos[] getDebugBlocks() {
        var path = getDebug();
        BlockPos[] blocks = new BlockPos[path.length];
        for (int i = 0; i < path.length; i++) {
            blocks[i] = new BlockPos(path[i].x.intValue(), path[i].z.intValue(), path[i].y.intValue());
        }
        return blocks;
    }
}
