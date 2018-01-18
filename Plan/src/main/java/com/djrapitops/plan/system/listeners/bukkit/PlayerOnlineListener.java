package com.djrapitops.plan.system.listeners.bukkit;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.system.cache.DataCache;
import com.djrapitops.plan.system.processing.processors.Processor;
import com.djrapitops.plan.system.processing.processors.info.NetworkPageUpdateProcessor;
import com.djrapitops.plan.system.processing.processors.player.*;
import com.djrapitops.plan.system.tasks.TaskSystem;
import com.djrapitops.plan.utilities.MiscUtils;
import com.djrapitops.plugin.api.systems.NotificationCenter;
import com.djrapitops.plugin.api.utility.log.Log;
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
public class PlayerOnlineListener implements Listener {

    private static boolean countKicks = true;

    private final Plan plugin;
    private final DataCache cache;

    /**
     * Class Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public PlayerOnlineListener(Plan plugin) {
        this.plugin = plugin;
        cache = plugin.getDataCache();
    }

    public static void setCountKicks(boolean value) {
        countKicks = value;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLogin(PlayerLoginEvent event) {
        try {
            PlayerLoginEvent.Result result = event.getResult();
            UUID uuid = event.getPlayer().getUniqueId();
            boolean op = event.getPlayer().isOp();
            boolean banned = result == PlayerLoginEvent.Result.KICK_BANNED;
            new BanAndOpProcessor(uuid, banned, op).queue();
        } catch (Exception e) {
            Log.toLog(this.getClass(), e);
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
        try {
            if (!countKicks || event.isCancelled()) {
                return;
            }
            UUID uuid = event.getPlayer().getUniqueId();
            new KickProcessor(uuid).queue();
        } catch (Exception e) {
            Log.toLog(this.getClass(), e);
        }
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
        try {
            Player player = event.getPlayer();
            NotificationCenter.checkNotifications(player);

            UUID uuid = player.getUniqueId();
            long time = MiscUtils.getTime();

            String world = player.getWorld().getName();
            String gm = player.getGameMode().name();

            String ip = player.getAddress().getAddress().getHostAddress();

            String playerName = player.getName();
            String displayName = player.getDisplayName();

            int playersOnline = TaskSystem.getInstance().getTpsCountTimer().getLatestPlayersOnline();

            cache.cacheSession(uuid, Session.start(time, world, gm));

            Processor.queueMany(
                    new RegisterProcessor(uuid, player.getFirstPlayed(), time, playerName, playersOnline,
                            new IPUpdateProcessor(uuid, ip, time),
                            new NameProcessor(uuid, playerName, displayName)
                    ),
                    new NetworkPageUpdateProcessor());
        } catch (Exception e) {
            Log.toLog(this.getClass(), e);
        }
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
        try {
            long time = MiscUtils.getTime();
            Player player = event.getPlayer();
            UUID uuid = player.getUniqueId();

            Processor.queueMany(
                    new BanAndOpProcessor(uuid, player.isBanned(), player.isOp()),
                    new EndSessionProcessor(uuid, time),
                    new NetworkPageUpdateProcessor()
            );

            if (cache.isFirstSession(uuid)) {
                int messagesSent = plugin.getDataCache().getFirstSessionMsgCount(uuid);
                new FirstLeaveProcessor(uuid, time, messagesSent).queue();
            }
        } catch (Exception e) {
            Log.toLog(this.getClass(), e);
        }
    }
}
