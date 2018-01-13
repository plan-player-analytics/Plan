package com.djrapitops.plan.system.listeners;

import com.djrapitops.plan.system.PlanSystem;
import com.djrapitops.plan.system.SubSystem;
import com.djrapitops.plan.utilities.NullCheck;
import com.djrapitops.plugin.api.Benchmark;

public abstract class ListenerSystem implements SubSystem {

    public static ListenerSystem getInstance() {
        ListenerSystem listenerSystem = PlanSystem.getInstance().getListenerSystem();
        NullCheck.check(listenerSystem, new IllegalStateException("Listener system was not initialized."));
        return listenerSystem;
    }
    
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


}
