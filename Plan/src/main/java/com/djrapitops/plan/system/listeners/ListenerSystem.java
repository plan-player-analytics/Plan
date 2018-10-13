package com.djrapitops.plan.system.listeners;

import com.djrapitops.plan.system.SubSystem;

public abstract class ListenerSystem implements SubSystem {

    @Override
    public void enable() {
        registerListeners();
    }

    @Override
    public void disable() {
        unregisterListeners();
    }

    protected abstract void registerListeners();

    protected abstract void unregisterListeners();

}
