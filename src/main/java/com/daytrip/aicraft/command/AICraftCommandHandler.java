package com.daytrip.aicraft.command;

import com.daytrip.aicraft.graph.RecipeGraph;
import com.daytrip.aicraft.navigation.Navigator;
import com.daytrip.aicraft.navigation.PlayerLookController;
import com.daytrip.aicraft.pathfinding.DStarLite;
import com.daytrip.aicraft.pathfinding.State;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class AICraftCommandHandler {
    public static void handle(String command) {
        String[] parts = command.substring(1).split(" ");

        switch (parts[0]) {
            case "build" -> build();
            case "recipe" -> recipe(parts[1]);
            case "path" -> pathfind(new BlockPos(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3])));
            case "look" -> look(new Vec3(Float.parseFloat(parts[1]), Float.parseFloat(parts[2]), Float.parseFloat(parts[3])));
            case "clear" -> clear();
            default -> chatLog("Unknown command.");
        }
    }

    public static void chatLog(String message) {
        Minecraft.getInstance().gui.getChat().addMessage(Component.literal(message));
        System.out.println("[CHATLOG] " + message);
    }

    private static LocalPlayer getPlayer() {
        return Minecraft.getInstance().player;
    }

    private static void build() {
        RecipeGraph.buildCache(getPlayer().level(), getPlayer());
    }

    private static void recipe(String id) {
        var graph = new RecipeGraph(BuiltInRegistries.ITEM.get(new ResourceLocation("minecraft", id)));
        chatLog(graph.toString());
        chatLog(graph.getRemainingRequirements(getPlayer().getInventory()).toString());
    }

    private static void pathfind(BlockPos destination) { // !path 0 -60 12        !path -91 78 -254
        /*var dStarLite = new DStarLite(getPlayer().clientLevel, getPlayer());
        dStarLite.init(getPlayer().blockPosition(), destination);
        dStarLite.replan();
        for (State s : dStarLite.getPath()) {
            var pos = new BlockPos(s.x, s.z, s.y);
            chatLog(pos.toShortString());
            getPlayer().clientLevel.setBlock(pos.below(), Blocks.DIAMOND_BLOCK.defaultBlockState(), 2);
        }*/
        Navigator.getInstance().setGoal(destination);
    }

    private static void clear() {
        Navigator.getInstance().clear();
    }

    private static void look(Vec3 pos) {
        Navigator.getInstance().getLookController().setLookAt(pos);
    }
}
