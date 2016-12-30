package com.djrapitops.plan.datahandlers.listeners;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.datahandlers.DataHandler;
import com.djrapitops.plan.datahandlers.DemographicsHandler;
import com.djrapitops.plan.database.UserData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class PlanChatListener implements Listener {

    private final Plan plugin;
    private final DataHandler handler;
    private final DemographicsHandler demographicsHandler;

    public PlanChatListener(Plan plugin) {
        this.plugin = plugin;
        handler = plugin.getHandler();
        demographicsHandler = handler.getDemographicsHandler();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Player p = event.getPlayer();
        UserData data = handler.getCurrentData(p.getUniqueId());        
        data.addNickname(p.getDisplayName());
        demographicsHandler.handleChatEvent(event, data);
    }
}

