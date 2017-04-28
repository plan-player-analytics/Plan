package main.java.com.djrapitops.plan.data.listeners;

import java.util.Date;
import java.util.UUID;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.cache.DataCacheHandler;
import main.java.com.djrapitops.plan.data.handling.info.KickInfo;
import main.java.com.djrapitops.plan.data.handling.info.LoginInfo;
import main.java.com.djrapitops.plan.data.handling.info.LogoutInfo;
import main.java.com.djrapitops.plan.utilities.NewPlayerCreator;
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
        handler.startSession(uuid);
        BukkitTask asyncNewPlayerCheckTask = (new BukkitRunnable() {
            @Override
            public void run() {
                LoginInfo loginInfo = new LoginInfo(uuid, new Date().getTime(), player.getAddress().getAddress(), player.isBanned(), player.getDisplayName(), player.getGameMode(), 1);
                boolean isNewPlayer = !plugin.getDB().wasSeenBefore(uuid);
                if (isNewPlayer) {
                    UserData newUserData = NewPlayerCreator.createNewPlayer(player);
                    loginInfo.process(newUserData);
                    handler.newPlayer(newUserData);
                } else {
                    handler.addToPool(loginInfo);
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
        handler.endSession(uuid);
        handler.addToPool(new LogoutInfo(uuid, new Date().getTime(), player.isBanned(), player.getGameMode(), handler.getSession(uuid)));        
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
        handler.endSession(uuid);
        handler.addToPool(new LogoutInfo(uuid, new Date().getTime(), player.isBanned(), player.getGameMode(), handler.getSession(uuid)));
        handler.addToPool(new KickInfo(uuid));
        handler.saveCachedData(uuid);
    }
}
