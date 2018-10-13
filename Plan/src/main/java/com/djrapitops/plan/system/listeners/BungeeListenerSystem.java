package com.djrapitops.plan.system.listeners;

import com.djrapitops.plan.PlanBungee;
import com.djrapitops.plan.system.listeners.bungee.PlayerOnlineListener;

import javax.inject.Inject;

public class BungeeListenerSystem extends ListenerSystem {

    private final PlanBungee plugin;
    private PlayerOnlineListener playerOnlineListener;

    @Inject
    public BungeeListenerSystem(PlanBungee plugin, PlayerOnlineListener playerOnlineListener) {
        this.plugin = plugin;
        this.playerOnlineListener = playerOnlineListener;
    }

    @Override
    protected void registerListeners() {
        plugin.registerListener(playerOnlineListener);
    }

    @Override
    protected void unregisterListeners() {
        plugin.getProxy().getPluginManager().unregisterListeners(plugin);
    }
}
