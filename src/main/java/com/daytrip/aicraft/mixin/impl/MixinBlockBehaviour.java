package com.daytrip.aicraft.mixin.impl;

import com.daytrip.aicraft.mixin.IMixinBlockBehaviour;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BlockBehaviour.class)
public class MixinBlockBehaviour implements IMixinBlockBehaviour {
    @Shadow @Final protected boolean hasCollision;

    @Override
    @Unique
    public boolean hasCollision() {
        return this.hasCollision;
    }
}
