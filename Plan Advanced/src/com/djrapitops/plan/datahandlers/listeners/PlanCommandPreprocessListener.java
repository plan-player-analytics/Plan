package com.djrapitops.plan.datahandlers.listeners;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.datahandlers.DataHandler;
import com.djrapitops.plan.datahandlers.ServerDataHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class PlanCommandPreprocessListener implements Listener {

    private final Plan plugin;
    private final DataHandler handler;
    private final ServerDataHandler serverH;

    public PlanCommandPreprocessListener(Plan plugin) {
        this.plugin = plugin;
        handler = plugin.getHandler();
        serverH = handler.getServerDataHandler();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        if (event.isCancelled()) {
            return;
        }
        serverH.handleCommand(event.getMessage().split(" ")[0]);
    }
}
