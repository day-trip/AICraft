package com.daytrip.aicraft.command;

import com.daytrip.aicraft.graph.RecipeGraph;
import com.daytrip.aicraft.natives.AI;
import com.daytrip.aicraft.natives.Natives;
import com.daytrip.aicraft.navigation.Navigator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public class AiCraftCommands {
    public static void chatLog(String message) {
        Minecraft.getInstance().gui.getChat().addMessage(Component.literal(message));
        System.out.println("[CHATLOG] " + message);
    }

    private static LocalPlayer getPlayer() {
        return Minecraft.getInstance().player;
    }

    @AiCommand(name = "reload")
    public static void reload() {
        chatLog("Reloading...");
        Natives.init();
    }

    @AiCommand(name = "build")
    public static void build() {
        RecipeGraph.buildCache(getPlayer().level(), getPlayer());
    }

    @AiCommand(name = "recipe")
    public static void recipe(String id) {
        var graph = new RecipeGraph(BuiltInRegistries.ITEM.get(new ResourceLocation("minecraft", id)));
        chatLog(graph.toString());
        chatLog(graph.getRemainingRequirements(getPlayer().getInventory()).toString());
    }

    @AiCommand(name = "path")
    public static void pathfind(int x, int y, int z) { // !path 0, -60, 12        !path -91, 78, -254          !path -45, 106, -253
        Navigator.getInstance().setGoal(new BlockPos(x, y, z));
    }

    @AiCommand(name = "clear")
    public static void clear() {
        Navigator.getInstance().clear();
    }

    @AiCommand(name = "look")
    public static void look(int x, int y, int z) { // !look -91, 78, -254
        Navigator.getInstance().getLookController().init(new Vec3(x, y, z));
    }

    @AiCommand(name = "cycle")
    public static void cycle(String goal, String feedback) { // !cycle "Beat the ender dragon", "None"
        AI.cycle(goal, feedback, result -> {
            System.out.println("CYCLE RESULT OOF");
            System.out.println(result);
        });
    }
}
