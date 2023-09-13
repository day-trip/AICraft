package com.daytrip.aicraft.navigation;

import net.minecraft.client.player.LocalPlayer;

public abstract class AIController {
    protected final LocalPlayer player;

    public AIController(LocalPlayer player) {
        this.player = player;
    }

    abstract void tick();

    abstract boolean isActive();
}
