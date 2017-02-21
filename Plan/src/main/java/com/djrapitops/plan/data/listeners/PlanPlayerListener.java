package main.java.com.djrapitops.plan.data.listeners;

import java.net.InetAddress;
import java.util.UUID;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.cache.DBCallableProcessor;
import main.java.com.djrapitops.plan.data.cache.DataCacheHandler;
import main.java.com.djrapitops.plan.data.handlers.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 *
 * @author Rsl1122
 */
public class PlanPlayerListener implements Listener {

    private final Plan plugin;
    private final DataCacheHandler handler;
    private final ActivityHandler activityH;
    private final BasicInfoHandler basicInfoH;
    private final GamemodeTimesHandler gmTimesH;
    private final DemographicsHandler demographicH;
    private final RuleBreakingHandler rulebreakH;
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
        activityH = handler.getActivityHandler();
        basicInfoH = handler.getBasicInfoHandler();
        gmTimesH = handler.getGamemodeTimesHandler();
        demographicH = handler.getDemographicsHandler();
        rulebreakH = handler.getRuleBreakingHandler();
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
        boolean isNewPlayer = activityH.isFirstTimeJoin(uuid);
        if (isNewPlayer) {
            handler.newPlayer(player);
        }
        DBCallableProcessor loginProcessor = new DBCallableProcessor() {
            @Override
            public void process(UserData data) {
                activityH.handleLogin(player.isBanned(), data);
                InetAddress ip = player.getAddress().getAddress();
                basicInfoH.handleLogin(player.getDisplayName(), ip, data);
                gmTimesH.handleLogin(player.getGameMode(), data);
                demographicH.handleLogin(ip, data);
                handler.saveCachedData(uuid);
            }
        };
        handler.getUserDataForProcessing(loginProcessor, uuid);
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
        DBCallableProcessor logoutProcessor = new DBCallableProcessor() {
            @Override
            public void process(UserData data) {
                activityH.handleLogOut(data);
                gmTimesH.handleLogOut(player.getGameMode(), data);
                data.addLocations(locationH.getLocationsForSaving(uuid));
                handler.saveCachedData(uuid);
                locationH.clearLocations(uuid);
            }
        };
        handler.getUserDataForProcessing(logoutProcessor, uuid);
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
        UUID uuid = event.getPlayer().getUniqueId();
        DBCallableProcessor kickProcessor = new DBCallableProcessor() {
            @Override
            public void process(UserData data) {
                rulebreakH.handleKick(data);
                data.addLocations(locationH.getLocationsForSaving(uuid));
                handler.saveCachedData(uuid);
                locationH.clearLocations(uuid);
                handler.clearFromCache(uuid);
            }
        };
        handler.getUserDataForProcessing(kickProcessor, uuid);
    }
}
