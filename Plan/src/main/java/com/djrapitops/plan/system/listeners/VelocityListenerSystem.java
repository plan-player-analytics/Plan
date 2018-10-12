package com.djrapitops.plan.system.listeners;

import com.djrapitops.plan.PlanVelocity;
import com.djrapitops.plan.system.listeners.velocity.PlayerOnlineListener;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class VelocityListenerSystem extends ListenerSystem {

    private final PlanVelocity plugin;

    private final PlayerOnlineListener playerOnlineListener;

    @Inject
    public VelocityListenerSystem(
            PlanVelocity plugin,
            PlayerOnlineListener playerOnlineListener
    ) {
        this.plugin = plugin;
        this.playerOnlineListener = playerOnlineListener;
    }

    @Override
    protected void registerListeners() {
        plugin.registerListener(playerOnlineListener);
    }

    @Override
    protected void unregisterListeners() {
        plugin.getProxy().getEventManager().unregisterListeners(plugin);
    }
}
