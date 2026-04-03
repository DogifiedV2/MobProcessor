package com.dog.mobprocessor.loot;

public final class LootSimulationGuard implements AutoCloseable {

    private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);

    private LootSimulationGuard() {
    }

    public static LootSimulationGuard enter() {
        DEPTH.set(DEPTH.get() + 1);
        return new LootSimulationGuard();
    }

    public static boolean isActive() {
        return DEPTH.get() > 0;
    }

    @Override
    public void close() {
        int depth = DEPTH.get() - 1;
        if (depth <= 0) {
            DEPTH.remove();
        } else {
            DEPTH.set(depth);
        }
    }
}
