package com.daytrip.aicraft.navigation;

import net.minecraft.client.player.LocalPlayer;

public class Navigator {
    private static Navigator instance;
    private final LocalPlayer player;
    private final PlayerLookController lookController;

    public Navigator(LocalPlayer player) {
        instance = this;
        this.player = player;
        this.lookController = new PlayerLookController(player);
    }

    public static Navigator getInstance() {
        return instance;
    }

    public void tick() {
        lookController.tick();
    }

    public boolean lockControls() {
        return lookController.lookAtCooldown != 0;
    }

    public PlayerLookController getLookController() {
        return lookController;
    }
}
