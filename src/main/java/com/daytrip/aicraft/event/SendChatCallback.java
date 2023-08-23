package com.daytrip.aicraft.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface SendChatCallback {
    Event<SendChatCallback> EVENT = EventFactory.createArrayBacked(SendChatCallback.class, listeners -> message -> {
        for (SendChatCallback listener : listeners) {
            if (listener.sendChat(message)) {
                return true;
            }
        }

        return false;
    });

    boolean sendChat(String message);
}
