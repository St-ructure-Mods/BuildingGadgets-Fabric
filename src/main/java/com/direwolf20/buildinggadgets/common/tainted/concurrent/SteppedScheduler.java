package com.direwolf20.buildinggadgets.common.tainted.concurrent;

import java.util.function.BooleanSupplier;

public abstract class SteppedScheduler implements BooleanSupplier {

    private final int steps;

    public SteppedScheduler(int steps) {
        this.steps = steps;
    }

    @Override
    public boolean getAsBoolean() {
        int step = 0;

        while (step++ < steps) {
            if (!advance()) {
                onFinish();
                return true;
            }
        }

        return false;
    }

    /**
     * @return Should continue advancing
     */
    protected abstract boolean advance();

    protected abstract void onFinish();
}
