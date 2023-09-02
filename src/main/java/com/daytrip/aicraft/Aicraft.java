package com.daytrip.aicraft;

import com.daytrip.aicraft.natives.Natives;
import com.daytrip.aicraft.natives.Pathfinder;
import com.daytrip.aicraft.natives.State;
import net.fabricmc.api.ModInitializer;

import java.util.Arrays;

public class Aicraft implements ModInitializer {
    @Override
    public void onInitialize() {
        System.out.println("AICraft initializing...");
        System.out.println(System.getProperty("java.library.path"));
        Natives.init();
        /*Pathfinder.init(State.create(0, 0, 0), State.create(0, 10, 0));
        Pathfinder.replan();
        System.out.println(Arrays.toString(Pathfinder.getPath()));*/
    }
}
