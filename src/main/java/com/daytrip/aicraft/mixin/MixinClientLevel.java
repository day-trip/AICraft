package com.daytrip.aicraft.mixin;

import com.daytrip.aicraft.event.BlockUpdateCallback;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientLevel.class)
public class MixinClientLevel {
    @Inject(method = "setBlock", at = @At("HEAD"), cancellable = true)
    void setBlock(BlockPos blockPos, BlockState blockState, int i, int j, CallbackInfoReturnable<Boolean> cir) {
        if (BlockUpdateCallback.EVENT.invoker().updateBlock(blockPos, blockState)) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
}
