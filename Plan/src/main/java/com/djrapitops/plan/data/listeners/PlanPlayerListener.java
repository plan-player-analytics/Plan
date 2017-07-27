package main.java.com.djrapitops.plan.data.listeners;

import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.utilities.player.Fetch;
import com.djrapitops.plugin.utilities.player.Gamemode;
import com.djrapitops.plugin.utilities.player.IPlayer;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.cache.DataCacheHandler;
import main.java.com.djrapitops.plan.data.handling.info.KickInfo;
import main.java.com.djrapitops.plan.data.handling.info.LoginInfo;
import main.java.com.djrapitops.plan.data.handling.info.LogoutInfo;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import main.java.com.djrapitops.plan.utilities.NewPlayerCreator;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

/**
 * Event Listener for PlayerJoin, PlayerQuit and PlayerKickEvents.
 *
 * @author Rsl1122
 * @since 2.0.0
 */
public class PlanPlayerListener implements Listener {

    private final Plan plugin;
    private final DataCacheHandler handler;

    /**
     * Class Constructor.
     * <p>
     * Copies the references to multiple handlers from Current instance of handler.
     *
     * @param plugin Current instance of Plan
     */
    public PlanPlayerListener(Plan plugin) {
        this.plugin = plugin;
        handler = plugin.getHandler();
    }

    /**
     * PlayerJoinEvent Listener.
     * <p>
     * If player is a new player, creates new data for the player.
     * <p>
     * Adds a LoginInfo to the processingQueue if the user is not new.
     *
     * @param event The Fired event.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLogin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        IPlayer iPlayer = Fetch.wrapBukkit(player);
        plugin.getNotificationCenter().checkNotifications(iPlayer);

        UUID uuid = player.getUniqueId();
        handler.startSession(uuid);
        Log.debug(uuid + ": PlayerJoinEvent");

        plugin.getRunnableFactory().createNew(new AbsRunnable("NewPlayerCheckTask") {
            @Override
            public void run() {
                LoginInfo loginInfo = new LoginInfo(uuid, MiscUtils.getTime(), player.getAddress().getAddress(), player.isBanned(), player.getDisplayName(), Gamemode.wrap(player.getGameMode()), 1);
                boolean isNewPlayer = !plugin.getDB().wasSeenBefore(uuid);

                if (isNewPlayer) {
                    UserData newUserData = NewPlayerCreator.createNewPlayer(iPlayer);
                    loginInfo.process(newUserData);
                    handler.newPlayer(newUserData);
                } else {
                    handler.addToPool(loginInfo);
                }

                Log.debug(uuid + ": PlayerJoinEvent_AsyncTask_END, New:" + isNewPlayer);
                this.cancel();
            }
        }).runTaskAsynchronously();

        Log.debug(uuid + ": PlayerJoinEvent_END");
    }

    /**
     * PlayerQuitEvent Listener.
     * <p>
     * Adds a LogoutInfo to the processing Queue.
     *
     * @param event Fired event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        handler.endSession(uuid);
        Log.debug(uuid + ": PlayerQuitEvent");
        handler.addToPool(new LogoutInfo(uuid, MiscUtils.getTime(), player.isBanned(), Gamemode.wrap(player.getGameMode()), handler.getSession(uuid)));
        handler.saveCachedData(uuid);
        Log.debug(uuid + ": PlayerQuitEvent_END");
    }

    /**
     * PlayerKickEvent Listener.
     * <p>
     * Adds a KickInfo & LogoutInfo to the processing Queue.
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
        Log.debug(uuid + ": PlayerKickEvent");
        handler.addToPool(new LogoutInfo(uuid, MiscUtils.getTime(), player.isBanned(), Gamemode.wrap(player.getGameMode()), handler.getSession(uuid)));
        handler.addToPool(new KickInfo(uuid));
        handler.saveCachedData(uuid);
        Log.debug(uuid + ": PlayerKickEvent_END");
    }
}
