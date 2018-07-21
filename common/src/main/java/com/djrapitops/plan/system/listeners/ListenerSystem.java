package com.djrapitops.plan.system.listeners;

import com.djrapitops.plan.system.SubSystem;
import com.djrapitops.plugin.api.Benchmark;

public abstract class ListenerSystem implements SubSystem {
    private boolean countKicks = true;

    @Override
    public void enable() {
        Benchmark.start("Register Listeners");
        registerListeners();
        Benchmark.stop("Enable", "Register Listeners");
    }

    @Override
    public void disable() {
        unregisterListeners();
    }

    protected abstract void registerListeners();

    protected abstract void unregisterListeners();


    public boolean isCountKicks() {
        return countKicks;
    }

    public void setCountKicks(boolean countKicks) {
        this.countKicks = countKicks;
    }
}
