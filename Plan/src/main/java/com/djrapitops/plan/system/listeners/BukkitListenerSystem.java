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
                new PlayerOnlineListener(plugin),
                new ChatListener(plugin),
                new GamemodeChangeListener(plugin),
                new WorldChangeListener(plugin),
                new CommandPreprocessListener(plugin),
                new DeathEventListener(plugin)
        );
        PlayerOnlineListener.setCountKicks(true);
    }

    @Override
    protected void unregisterListeners() {
        HandlerList.unregisterAll(plugin);
    }
}
