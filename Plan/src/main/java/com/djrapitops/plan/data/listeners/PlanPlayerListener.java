package main.java.com.djrapitops.plan.data.listeners;

import java.util.UUID;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.cache.DataCacheHandler;
import main.java.com.djrapitops.plan.data.handlers.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

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
    private final LocationHandler locationH;
    private final DemographicsHandler demographicH;
    private final RuleBreakingHandler rulebreakH;
    private final PlanLiteHandler planLiteH;
    private final CommandUseHandler serverHandler;

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
        locationH = handler.getLocationHandler();
        rulebreakH = handler.getRuleBreakingHandler();
        planLiteH = handler.getPlanLiteHandler();
        serverHandler = handler.getServerDataHandler();
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
        UserData data = handler.getCurrentData(uuid);
        activityH.handleLogin(event, data);
        basicInfoH.handleLogin(event, data);
        gmTimesH.handleLogin(event, data);
        demographicH.handleLogin(event, data);
        planLiteH.handleLogin(event, data);
        (new BukkitRunnable() {
            @Override
            public void run() {
                handler.saveCachedData(uuid);
            }
        }).runTaskLater(plugin, 15 * 20);
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
        UUID uuid = event.getPlayer().getUniqueId();
        UserData data = handler.getCurrentData(uuid);
        activityH.handleLogOut(event, data);
        locationH.handleLogOut(event, data);
        gmTimesH.handleLogOut(event, data);
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
        UUID uuid = event.getPlayer().getUniqueId();
        UserData data = handler.getCurrentData(uuid);
        rulebreakH.handleKick(event, data);
        handler.saveCachedData(uuid);
        handler.clearFromCache(uuid);
    }
}
