package com.djrapitops.plan.system.listeners;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.system.listeners.bukkit.*;
import org.bukkit.event.HandlerList;

public class BukkitListenerSystem extends ListenerSystem {

    private final Plan plugin;

    public BukkitListenerSystem(Plan plugin) {
        this.plugin = plugin;
    }

    @Override
    protected void registerListeners() {
        plugin.registerListener(
                new PlayerOnlineListener(),
                new ChatListener(),
                new GamemodeChangeListener(),
                new WorldChangeListener(),
                new CommandPreprocessListener(plugin),
                new DeathEventListener(),
                new AFKListener()
        );
        PlayerOnlineListener.setCountKicks(true);
    }

    @Override
    protected void unregisterListeners() {
        HandlerList.unregisterAll(plugin);
    }
}
