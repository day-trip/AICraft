package com.daytrip.aicraft.mixin.impl;

import com.daytrip.aicraft.navigation.Navigator;
import net.minecraft.client.player.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public class MixinKeyboardInput {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    void tick(boolean bl, float f, CallbackInfo ci) {
        if (Navigator.getInstance().lockControls()) {
            ci.cancel();
        }
    }
}
