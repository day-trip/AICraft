package com.daytrip.aicraft.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public interface BlockUpdateCallback {
    Event<BlockUpdateCallback> EVENT = EventFactory.createArrayBacked(BlockUpdateCallback.class, listeners -> (pos, state) -> {
        for (BlockUpdateCallback listener : listeners) {
            if (listener.updateBlock(pos, state)) {
                return true;
            }
        }

        return false;
    });

    boolean updateBlock(BlockPos pos, BlockState state);
}
