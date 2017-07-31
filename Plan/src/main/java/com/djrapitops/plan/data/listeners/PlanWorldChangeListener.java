package main.java.com.djrapitops.plan.data.listeners;

import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.cache.DataCacheHandler;
import main.java.com.djrapitops.plan.data.handling.info.InfoType;
import main.java.com.djrapitops.plan.data.handling.info.PlaytimeDependentInfo;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

import java.util.UUID;

public class PlanWorldChangeListener implements Listener {
    private final DataCacheHandler handler;

    public PlanWorldChangeListener(Plan plugin) {
        this.handler = plugin.getHandler();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player p = event.getPlayer();
        String previousWorld = event.getFrom().getName();
        String worldName = p.getWorld().getName();
        if (previousWorld.equals(worldName)) {
            return;
        }
        UUID uuid = p.getUniqueId();
        long time = MiscUtils.getTime();
        handler.addToPool(new PlaytimeDependentInfo(uuid, InfoType.GM, time, p.getGameMode().name(), p.getWorld().getName()));
    }
}
