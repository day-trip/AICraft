package com.daytrip.aicraft.navigation;

import com.daytrip.aicraft.command.AiCraftCommands;
import com.daytrip.aicraft.natives.Pathfinder;
import com.daytrip.aicraft.natives.State;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

public class Navigator {
    private static Navigator instance;
    private final LocalPlayer player;
    private final PlayerLookController lookController;
    private final PlayerMoveController moveController;
    private BlockPos initial;
    private List<BlockPos> path;
    private BlockPos current;

    public Navigator(LocalPlayer player) {
        instance = this;

        this.player = player;

        this.lookController = new PlayerLookController(player);
        this.moveController = new PlayerMoveController(player);
    }

    public static Navigator getInstance() {
        return instance;
    }

    public void tick() {
        if (this.initial != null) {
            if (player.blockPosition().getCenter().distanceTo(this.initial.getCenter()) > 3) {
                this.initial = player.blockPosition();
                Pathfinder.updateStart(State.fromBlock(initial));
                this.replan();
            }

            if (current == null) {
                current = path.get(0);
            } else if (player.distanceToSqr(current.getCenter()) < 2.500000277905201E-7) {
                int i = path.indexOf(current);
                if (i < path.size()) {
                    current = path.get(i + 1);
                } else {
                    System.out.println("Done!");
                    initial = null;
                }
            }

            moveController.init(current.getCenter());
        }

        moveController.tick();
        lookController.tick();
    }

    public boolean lockControls() {
        return lookController.isActive() || moveController.isActive();
    }

    public PlayerLookController getLookController() {
        return lookController;
    }

    public void setGoal(BlockPos goal) {
        this.initial = player.blockPosition();
        Pathfinder.init(State.fromBlock(initial), State.fromBlock(goal));
        this.replan();
    }

    private void replan() {
        int ms = Pathfinder.replan();
        AiCraftCommands.chatLog("Replanning took " + ms + "ms.");
        if (ms < 0) {
            this.initial = null;
            return;
        }
        this.path = List.of(Pathfinder.getBlocks());
        for (BlockPos b : Pathfinder.getDebugBlocks()) {
            this.player.clientLevel.setBlock(b, Blocks.REDSTONE_BLOCK.defaultBlockState(), 2);
        }
        for (BlockPos b : this.path) {
            this.player.clientLevel.setBlock(b.below(), Blocks.DIAMOND_BLOCK.defaultBlockState(), 2);
        }
    }

    public void clear() {
        this.initial = null;
    }
}
