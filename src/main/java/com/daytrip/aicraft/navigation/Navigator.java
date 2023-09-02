package com.daytrip.aicraft.navigation;

import com.daytrip.aicraft.command.AICraftCommandHandler;
import com.daytrip.aicraft.natives.Pathfinder;
import com.daytrip.aicraft.natives.State;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;

public class Navigator {
    private static Navigator instance;
    private final LocalPlayer player;
    private final PlayerLookController lookController;
    private BlockPos initial;

    public Navigator(LocalPlayer player) {
        instance = this;

        this.player = player;

        this.lookController = new PlayerLookController(player);
    }

    public static Navigator getInstance() {
        return instance;
    }

    public void tick() {
        if (this.initial != null) {
            if (player.blockPosition().distSqr(this.initial) > 3) {
                this.initial = player.blockPosition();
                Pathfinder.updateStart(State.fromBlock(initial));
                this.replan();
            }
        }

        lookController.tick();
    }

    public boolean lockControls() {
        return lookController.lookAtCooldown != 0;
    }

    public PlayerLookController getLookController() {
        return lookController;
    }

    public void setGoal(BlockPos goal) {
        this.initial = player.blockPosition();
        // this.pathfinder.init(this.initial, goal);
        Pathfinder.init(State.fromBlock(initial), State.fromBlock(goal));
        this.replan();
    }

    private void replan() {
        AICraftCommandHandler.chatLog("Replanning took " + Pathfinder.replan() + "ms.");
        for (BlockPos b : Pathfinder.getDebugBlocks()) {
            this.player.clientLevel.setBlock(b.below(), Blocks.REDSTONE_BLOCK.defaultBlockState(), 2);
        }
        for (BlockPos b : Pathfinder.getBlocks()) {
            this.player.clientLevel.setBlock(b.below(), Blocks.DIAMOND_BLOCK.defaultBlockState(), 2);
        }
    }

    /*public List<BlockPos> getPath() {
        return this.pathfinder.getPath().stream().map(State::asBlock).toList();
    }*/

    public void clear() {
        this.initial = null;
    }
}
