package com.daytrip.aicraft.mixin.impl;

import com.daytrip.aicraft.event.SendChatCallback;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class MixinClientPacketListener {
    @Inject(method = "sendChat", at = @At("HEAD"), cancellable = true)
    void sendChat(String string, CallbackInfo ci) {
        if (SendChatCallback.EVENT.invoker().sendChat(string)) {
            ci.cancel();
        }
    }
}
