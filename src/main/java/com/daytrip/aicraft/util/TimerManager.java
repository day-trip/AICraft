package com.daytrip.aicraft.util;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class TimerManager {
    private static final List<TickTimer> tickTimers = new CopyOnWriteArrayList<>();

    public static void registerTimer(TickTimer tickTimer) {
        tickTimers.add(tickTimer);
    }

    public static void destroy() {
        for(TickTimer tickTimer : tickTimers) {
            tickTimer.stop();
            finished(tickTimer);
        }
    }

    public static void finished(TickTimer tickTimer) {
        tickTimers.remove(tickTimer);
    }

    public static void tick() {
        for(TickTimer tickTimer : tickTimers) {
            tickTimer.update();
        }
    }
}
