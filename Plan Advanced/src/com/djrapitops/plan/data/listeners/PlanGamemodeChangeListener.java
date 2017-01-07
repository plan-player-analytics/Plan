package com.djrapitops.plan.data.listeners;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.data.cache.DataCacheHandler;
import com.djrapitops.plan.data.handlers.GamemodeTimesHandler;
import com.djrapitops.plan.data.UserData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;

public class PlanGamemodeChangeListener implements Listener {

    private final Plan plugin;
    private final DataCacheHandler handler;
    private final GamemodeTimesHandler gmTimesH;

    public PlanGamemodeChangeListener(Plan plugin) {
        this.plugin = plugin;
        handler = plugin.getHandler();
        gmTimesH = handler.getGamemodeTimesHandler();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onGamemodeChange(PlayerGameModeChangeEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Player p = event.getPlayer();
        UserData data = handler.getCurrentData(p.getUniqueId());        
        gmTimesH.handleChangeEvent(event, data);
    }
}

