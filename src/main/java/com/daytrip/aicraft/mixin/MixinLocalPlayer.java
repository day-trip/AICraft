package com.daytrip.aicraft.mixin;

import net.fabricmc.fabric.mixin.resource.conditions.LootManagerMixin;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithLootingCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(LocalPlayer.class)
public class MixinLocalPlayer {
    @Unique
    private LocalPlayer player;

    @Inject(method = "<init>", at = @At("TAIL"))
    void constructor(CallbackInfo ci) {
        System.out.println("Local player mixin activated!");
        this.player = (LocalPlayer) (Object) this;
    }
}
