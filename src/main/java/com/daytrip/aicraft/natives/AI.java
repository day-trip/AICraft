package com.daytrip.aicraft.natives;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;

import java.util.regex.Pattern;

import static com.daytrip.aicraft.natives.Natives.INSTANCE;

public class AI {
    public static void cycle(String goal, String feedback, Callback cb) {
        new Thread(() -> {
            String result = INSTANCE.ai_cycle(goal, feedback);
            System.out.println("Got result: " + result);
            String[] parts = result.split(Pattern.quote("(!###!)"));
            Minecraft.getInstance().execute(() -> cb.call(new Pair<>(String.valueOf(parts[0]), String.valueOf(parts[1]))));
        }).start();
    }
    
    public interface Callback {
        void call(Pair<String, String> result);
    }
}
