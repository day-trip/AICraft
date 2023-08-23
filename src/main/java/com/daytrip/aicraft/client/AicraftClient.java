package com.daytrip.aicraft.client;

import com.daytrip.aicraft.command.AICraftCommandHandler;
import com.daytrip.aicraft.event.SendChatCallback;
import net.fabricmc.api.ClientModInitializer;


public class AicraftClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        System.out.println("AICraft initializing...");

        SendChatCallback.EVENT.register(message -> {
            if (message.startsWith("!")) {
                AICraftCommandHandler.handle(message);
                return true;
            }
            return false;
        });
    }
}
