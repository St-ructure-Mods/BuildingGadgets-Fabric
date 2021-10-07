package com.direwolf20.buildinggadgets.common.tainted.concurrent;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

public final class ServerTickingScheduler {
    private static final List<BooleanSupplier> TASKS = new ArrayList<>();

    public static void runTicked(BooleanSupplier runUntilFalse) {
        TASKS.add(runUntilFalse);
    }

    static {
        ServerTickEvents.START_SERVER_TICK.register(server -> TASKS.removeIf(BooleanSupplier::getAsBoolean));
    }
}
