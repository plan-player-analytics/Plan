package main.java.com.djrapitops.plan.data.listeners;

import main.java.com.djrapitops.plan.Plan;
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
    private final Plan plugin;

    public PlanWorldChangeListener(Plan plugin) {
        this.plugin = plugin;
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
        String gameMode = p.getGameMode().name();
        long time = MiscUtils.getTime();

        plugin.addToProcessQueue(new PlaytimeDependentInfo(uuid, InfoType.WORLD, time, gameMode, worldName));
    }
}
