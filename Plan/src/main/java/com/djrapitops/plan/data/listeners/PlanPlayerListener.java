package main.java.com.djrapitops.plan.data.listeners;

import java.util.Date;
import java.util.UUID;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.cache.DataCacheHandler;
import main.java.com.djrapitops.plan.data.handlers.*;
import main.java.com.djrapitops.plan.data.handling.InfoPoolProcessor;
import main.java.com.djrapitops.plan.data.handling.info.KickInfo;
import main.java.com.djrapitops.plan.data.handling.info.LoginInfo;
import main.java.com.djrapitops.plan.data.handling.info.LogoutInfo;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

/**
 *
 * @author Rsl1122
 */
public class PlanPlayerListener implements Listener {

    private final Plan plugin;
    private final DataCacheHandler handler;
    private final InfoPoolProcessor processor;
    private final LocationHandler locationH;

    /**
     * Class Constructor.
     *
     * Copies the references to multiple handlers from Current instance of
     * handler.
     *
     * @param plugin Current instance of Plan
     */
    public PlanPlayerListener(Plan plugin) {
        this.plugin = plugin;
        handler = plugin.getHandler();
        processor = plugin.getInfoPoolProcessor();
        locationH = handler.getLocationHandler();
    }

    /**
     * PlayerJoinEvent Listener.
     *
     * If player is a new player, creates a new data in the database for the
     * player. Retrieves the UserData, updates and then saves it to the Cache.
     *
     * @param event The Fired event.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLogin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        processor.addToPool(new LoginInfo(uuid, new Date().getTime(), player.getAddress().getAddress(), player.isBanned(), player.getDisplayName(), player.getGameMode(), 1));
        handler.getSessionHandler().startSession(uuid);
        BukkitTask asyncNewPlayerCheckTask = (new BukkitRunnable() {
            @Override
            public void run() {
                boolean isNewPlayer = !plugin.getDB().wasSeenBefore(uuid);
                if (isNewPlayer) {
                    handler.newPlayer(player);
                }
                this.cancel();
            }
        }).runTaskAsynchronously(plugin);
    }

    /**
     * PlayerQuitEvent Listener.
     *
     * Retrieves the current UserData for the Player, updates it, saves the data
     * to Database and clears it from cache.
     *
     * @param event Fired event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        processor.addToPool(new LogoutInfo(uuid, new Date().getTime(), player.isBanned(), player.getGameMode()));
        handler.saveCachedData(uuid);        
    }

    /**
     * PlayerKickEvent Listener.
     *
     * Updates current playerdata and saves it to the Database.
     *
     * @param event Fired event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerKick(PlayerKickEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        processor.addToPool(new LogoutInfo(uuid, new Date().getTime(), player.isBanned(), player.getGameMode()));
        processor.addToPool(new KickInfo(uuid));
        handler.saveCachedData(uuid); 
    }
}
