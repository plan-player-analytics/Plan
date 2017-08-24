package main.java.com.djrapitops.plan.systems.listeners;

import com.djrapitops.plugin.utilities.player.Fetch;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.Session;
import main.java.com.djrapitops.plan.systems.cache.DataCache;
import main.java.com.djrapitops.plan.systems.processing.DBCommitProcessor;
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

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Event Listener for PlayerJoin, PlayerQuit and PlayerKickEvents.
 *
 * @author Rsl1122
 * @since 2.0.0
 */
public class PlanPlayerListener implements Listener {

    private final Plan plugin;
    private final DataCache cache;

    private final Set<UUID> playersWithFirstSession;

    /**
     * Class Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public PlanPlayerListener(Plan plugin) {
        this.plugin = plugin;
        cache = plugin.getDataCache();
        playersWithFirstSession = new HashSet<>();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLogin(PlayerLoginEvent event) {
        PlayerLoginEvent.Result result = event.getResult();
        UUID uuid = event.getPlayer().getUniqueId();
        if (result == PlayerLoginEvent.Result.KICK_BANNED) {
            plugin.addToProcessQueue(new BanProcessor(uuid, true));
        } else {
            plugin.addToProcessQueue(new BanProcessor(uuid, false));
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
        if (event.isCancelled()) {
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
        plugin.getNotificationCenter().checkNotifications(Fetch.wrapBukkit(player));

        UUID uuid = player.getUniqueId();
        long time = MiscUtils.getTime();

        String world = player.getWorld().getName();
        String gm = player.getGameMode().name();

        String ip = player.getAddress().getAddress().toString();

        String playerName = player.getName();
        String displayName = player.getDisplayName();

        int playersOnline = plugin.getTpsCountTimer().getLatestPlayersOnline();

        cache.cacheSession(uuid, Session.start(time, world, gm));

        plugin.addToProcessQueue(
                new RegisterProcessor(this, uuid, player.getFirstPlayed(), playerName, playersOnline),
                new IPUpdateProcessor(uuid, ip),
                new NameProcessor(uuid, playerName, displayName), // TODO NameCache to DataCache
                new DBCommitProcessor(plugin.getDB())
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
                new BanProcessor(uuid, player.isBanned()),
                new EndSessionProcessor(uuid, time)
        );

        int messagesSent = 0; // TODO messages Sent on first session

        if (playersWithFirstSession.contains(uuid)) {
            plugin.addToProcessQueue(new FirstLeaveProcessor(uuid, time, messagesSent));
        }
    }

    // TODO MOVE TO DATACACHE
    public void addFirstLeaveCheck(UUID uuid) {
        playersWithFirstSession.add(uuid);
    }
}
