package com.daytrip.aicraft.command;

import com.daytrip.aicraft.graph.RecipeGraph;
import com.daytrip.aicraft.pathfinding.DStarLite;
import com.daytrip.aicraft.pathfinding.State;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;

public class AICraftCommandHandler {
    public static void handle(String command) {
        String[] parts = command.substring(1).split(" ");

        switch (parts[0]) {
            case "build" -> build();
            case "recipe" -> recipe(parts[1]);
            case "path" -> pathfind(new BlockPos(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3])));
            default -> chatLog("Unknown command.");
        }
    }

    public static void chatLog(String message) {
        System.out.println("[AICraft] " + message);
        getPlayer().connection.sendChat("[AICraft] " + message);
    }

    private static LocalPlayer getPlayer() {
        return Minecraft.getInstance().player;
    }

    private static void build() {
        RecipeGraph.buildCache(getPlayer().level().getRecipeManager(), getPlayer().level().registryAccess());
    }

    private static void recipe(String id) {
        RecipeGraph graph = RecipeGraph.create(BuiltInRegistries.ITEM.get(new ResourceLocation("minecraft", id)));
        chatLog(graph.toString());
        chatLog(graph.getRemainingRequirements(getPlayer().getInventory()).toString());
    }

    private static void pathfind(BlockPos destination) { // !path 0 -60 12        !path -91 78 -254
        DStarLite dStarLite = new DStarLite(getPlayer().clientLevel);
        dStarLite.init(getPlayer().blockPosition(), destination);
        dStarLite.replan();
        for (State s : dStarLite.getPath()) {
            BlockPos pos = new BlockPos(s.x, s.z, s.y);
            chatLog(pos.toShortString());
            getPlayer().clientLevel.setBlock(pos.below(), Blocks.DIAMOND_BLOCK.defaultBlockState(), 2);
        }
    }
}
