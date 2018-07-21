package com.djrapitops.plan.bukkit.listeners;

import com.djrapitops.plan.bukkit.PlanBukkit;
import com.djrapitops.plan.bukkit.listeners.bukkit.*;
import com.djrapitops.plan.system.listeners.ListenerSystem;
import org.bukkit.event.HandlerList;

public class BukkitListenerSystem extends ListenerSystem {

    private final PlanBukkit plugin;

    public BukkitListenerSystem(PlanBukkit plugin) {
        this.plugin = plugin;
    }

    @Override
    protected void registerListeners() {
        plugin.registerListener(
                new PlayerOnlineListener(this),
                new ChatListener(),
                new GamemodeChangeListener(),
                new WorldChangeListener(),
                new CommandPreprocessListener(plugin),
                new DeathEventListener(),
                new AFKListener()
        );
        plugin.getSystem().getListenerSystem().setCountKicks(true);
    }

    @Override
    protected void unregisterListeners() {
        HandlerList.unregisterAll(plugin);
    }
}
