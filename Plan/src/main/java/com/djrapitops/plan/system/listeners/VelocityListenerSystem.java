package com.djrapitops.plan.system.listeners;

import com.djrapitops.plan.PlanVelocity;
import com.djrapitops.plan.system.listeners.velocity.PlayerOnlineListener;

public class VelocityListenerSystem extends ListenerSystem {

    private final PlanVelocity plugin;

    public VelocityListenerSystem(PlanVelocity plugin) {
        this.plugin = plugin;
    }

    @Override
    protected void registerListeners() {
        plugin.registerListener(new PlayerOnlineListener());
    }

    @Override
    protected void unregisterListeners() {
        plugin.getProxy().getEventManager().unregisterListeners(plugin);
    }
}
