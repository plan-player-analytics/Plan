package main.java.com.djrapitops.plan.systems.listeners;

import com.djrapitops.plugin.api.systems.NotificationCenter;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.container.Session;
import main.java.com.djrapitops.plan.systems.cache.DataCache;
import main.java.com.djrapitops.plan.systems.processing.info.NetworkPageUpdateProcessor;
import main.java.com.djrapitops.plan.systems.processing.player.*;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

/**
 * Event Listener for PlayerJoin, PlayerQuit and PlayerKickEvents.
 *
 * @author Rsl1122
 * @since 2.0.0
 */
public class PlanPlayerListener implements Listener {

    private static boolean countKicks = true;

    private final Plan plugin;
    private final DataCache cache;

    /**
     * Class Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public PlanPlayerListener(Plan plugin) {
        this.plugin = plugin;
        cache = plugin.getDataCache();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLogin(PlayerLoginEvent event) {
        PlayerLoginEvent.Result result = event.getResult();
        UUID uuid = event.getPlayer().getUniqueId();
        boolean op = event.getPlayer().isOp();
        if (result == PlayerLoginEvent.Result.KICK_BANNED) {
            plugin.addToProcessQueue(new BanAndOpProcessor(uuid, true, op));
        } else {
            plugin.addToProcessQueue(new BanAndOpProcessor(uuid, false, op));
        }
    }

    /**
     * PlayerKickEvent Listener.
     * <p>
     * Adds processing information to the ProcessingQueue.
     * After KickEvent, the QuitEvent is automatically called.
     *
     * @param event Fired event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerKick(PlayerKickEvent event) {
        if (!countKicks || event.isCancelled()) {
            return;
        }
        UUID uuid = event.getPlayer().getUniqueId();
        plugin.addToProcessQueue(new KickProcessor(uuid));
    }

    /**
     * PlayerJoinEvent Listener.
     * <p>
     * Adds processing information to the ProcessingQueue.
     *
     * @param event The Fired event.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        NotificationCenter.checkNotifications(player);

        UUID uuid = player.getUniqueId();
        long time = MiscUtils.getTime();

        String world = player.getWorld().getName();
        String gm = player.getGameMode().name();

        String ip = player.getAddress().getAddress().getHostAddress();

        String playerName = player.getName();
        String displayName = player.getDisplayName();

        int playersOnline = plugin.getTpsCountTimer().getLatestPlayersOnline();

//        BungeePluginChannelSenderProcessor bungeePluginChannelSenderProcessor = null;
//        if (!plugin.getInfoManager().isUsingAnotherWebServer() && Settings.BUNGEE_OVERRIDE_STANDALONE_MODE.isFalse()) {
//            bungeePluginChannelSenderProcessor = new BungeePluginChannelSenderProcessor(player);
//        }
        cache.cacheSession(uuid, Session.start(time, world, gm));
        plugin.addToProcessQueue(
                new RegisterProcessor(uuid, player.getFirstPlayed(), time, playerName, playersOnline,
                        new IPUpdateProcessor(uuid, ip, time),
                        new NameProcessor(uuid, playerName, displayName)
                ),
                new NetworkPageUpdateProcessor(plugin.getInfoManager())
        );
    }

    /**
     * PlayerQuitEvent Listener.
     * <p>
     * Adds processing information to the ProcessingQueue.
     *
     * @param event Fired event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {

        long time = MiscUtils.getTime();
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        plugin.addToProcessQueue(
                new BanAndOpProcessor(uuid, player.isBanned(), player.isOp()),
                new EndSessionProcessor(uuid, time),
                new NetworkPageUpdateProcessor(plugin.getInfoManager())
        );

        if (cache.isFirstSession(uuid)) {
            int messagesSent = plugin.getDataCache().getFirstSessionMsgCount(uuid);
            plugin.addToProcessQueue(new FirstLeaveProcessor(uuid, time, messagesSent));
        }
    }

    public static void setCountKicks(boolean value) {
        countKicks = value;
    }
}
