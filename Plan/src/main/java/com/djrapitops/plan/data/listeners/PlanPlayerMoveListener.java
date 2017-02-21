package main.java.com.djrapitops.plan.data.listeners;

import java.util.UUID;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.cache.DBCallableProcessor;
import main.java.com.djrapitops.plan.data.cache.DataCacheHandler;
import main.java.com.djrapitops.plan.data.handlers.LocationHandler;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 *
 * @author Rsl1122
 */
public class PlanPlayerMoveListener implements Listener {

    private final Plan plugin;
    private final DataCacheHandler handler;
    private final LocationHandler locationH;

    /**
     * Class Consturctor.
     *
     * @param plugin Current instance of Plan
     */
    public PlanPlayerMoveListener(Plan plugin) {
        this.plugin = plugin;
        handler = plugin.getHandler();
        locationH = handler.getLocationHandler();
    }

    /**
     * MoveEventListener.
     *
     * Adds location to UserData if the player has moved a block.
     *
     * @param event Event that is fired
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Location from = event.getFrom();
        Location to = event.getTo();
        if (from.getBlockX() == to.getBlockX() && from.getBlockZ() == to.getBlockZ()) {
            return;
        }
        UUID uuid = event.getPlayer().getUniqueId();
        Location savedLocation = to.getBlock().getLocation();
        locationH.addLocation(uuid, savedLocation);
    }
}
