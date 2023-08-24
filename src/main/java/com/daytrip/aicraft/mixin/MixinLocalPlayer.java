package com.daytrip.aicraft.mixin;

import com.daytrip.aicraft.navigation.Navigator;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(LocalPlayer.class)
public class MixinLocalPlayer {
    @Unique
    private LocalPlayer player;

    @Unique
    private Navigator navigator;

    @Inject(method = "<init>", at = @At("TAIL"))
    void constructor(CallbackInfo ci) {
        System.out.println("Local player mixin activated!");
        this.player = (LocalPlayer) (Object) this;
        this.navigator = new Navigator(this.player);
    }

    @Inject(method = "serverAiStep", at = @At("TAIL"))
    void serverAiStep(CallbackInfo ci) {
        this.navigator.tick();
    }
}
