package com.djrapitops.plan.system.listeners;

import com.djrapitops.plan.PlanBungee;
import com.djrapitops.plan.system.listeners.bungee.PlayerOnlineListener;

public class BungeeListenerSystem extends ListenerSystem {

    private final PlanBungee plugin;

    public BungeeListenerSystem(PlanBungee plugin) {
        this.plugin = plugin;
    }

    @Override
    protected void registerListeners() {
        plugin.registerListener(new PlayerOnlineListener(plugin));
    }

    @Override
    protected void unregisterListeners() {
        plugin.getProxy().getPluginManager().unregisterListeners(plugin);
    }
}
