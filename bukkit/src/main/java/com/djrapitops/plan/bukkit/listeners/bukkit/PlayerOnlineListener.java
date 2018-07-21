package com.djrapitops.plan.bukkit.listeners.bukkit;

import com.djrapitops.plan.bukkit.listeners.BukkitListenerSystem;
import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.system.cache.SessionCache;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plan.system.processing.processors.info.NetworkPageUpdateProcessor;
import com.djrapitops.plan.system.processing.processors.info.PlayerPageUpdateProcessor;
import com.djrapitops.plan.system.processing.processors.player.*;
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

import java.net.InetAddress;
import java.util.UUID;

/**
 * Event Listener for PlayerJoin, PlayerQuit and PlayerKickEvents.
 *
 * @author Rsl1122
 * @since 2.0.0
 */
public class PlayerOnlineListener implements Listener {
    private BukkitListenerSystem listenerSystem;

    public PlayerOnlineListener(BukkitListenerSystem bukkitListenerSystem) {
        this.listenerSystem = bukkitListenerSystem;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLogin(PlayerLoginEvent event) {
        try {
            PlayerLoginEvent.Result result = event.getResult();
            UUID uuid = event.getPlayer().getUniqueId();
            boolean op = event.getPlayer().isOp();
            boolean banned = result == PlayerLoginEvent.Result.KICK_BANNED;
            Processing.submit(new BanAndOpProcessor(uuid, () -> banned, op));
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
            if (!listenerSystem.isCountKicks() || event.isCancelled()) {
                return;
            }
            UUID uuid = event.getPlayer().getUniqueId();
            Processing.submit(new KickProcessor(uuid));
        } catch (Exception e) {
            Log.toLog(this.getClass(), e);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        try {
            actOnJoinEvent(event);
        } catch (Exception e) {
            Log.toLog(this.getClass(), e);
        }
    }

    private void actOnJoinEvent(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        NotificationCenter.checkNotifications(player);

        UUID uuid = player.getUniqueId();
        long time = System.currentTimeMillis();

        AFKListener.AFK_TRACKER.performedAction(uuid, time);

        String world = player.getWorld().getName();
        String gm = player.getGameMode().name();

        InetAddress address = player.getAddress().getAddress();

        String playerName = player.getName();
        String displayName = player.getDisplayName();

        SessionCache.getInstance().cacheSession(uuid, new Session(uuid, time, world, gm));

        Processing.submit(
                new RegisterProcessor(uuid, player.getFirstPlayed(), playerName,
                        new IPUpdateProcessor(uuid, address, time),
                        new NameProcessor(uuid, playerName, displayName),
                        new PlayerPageUpdateProcessor(uuid)
                )
        );
        Processing.submit(new NetworkPageUpdateProcessor());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        try {
            actOnQuitEvent(event);
        } catch (Exception e) {
            Log.toLog(this.getClass(), e);
        }
    }

    private void actOnQuitEvent(PlayerQuitEvent event) {
        long time = System.currentTimeMillis();
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        AFKListener.AFK_TRACKER.loggedOut(uuid, time);

        Processing.submit(new BanAndOpProcessor(uuid, player::isBanned, player.isOp()));
        Processing.submit(new EndSessionProcessor(uuid, time));
        Processing.submit(new NetworkPageUpdateProcessor());
        Processing.submit(new PlayerPageUpdateProcessor(uuid));
    }
}
