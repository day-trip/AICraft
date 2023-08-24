package com.daytrip.aicraft.mixin;

import com.daytrip.aicraft.navigation.Navigator;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MixinMouseHandler {
    @Inject(method = "turnPlayer", at = @At("HEAD"), cancellable = true)
    void turnPlayer(CallbackInfo ci) {
        if (Navigator.getInstance() != null && Navigator.getInstance().lockControls()) {
            ci.cancel();
        }
    }
}
