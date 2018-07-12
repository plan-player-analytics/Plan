package com.djrapitops.plan.system.listeners.bukkit;

import com.djrapitops.plan.system.afk.AFKTracker;
import com.djrapitops.plugin.api.utility.log.Log;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.UUID;

/**
 * Listener that keeps track of actions that are not considered being AFK.
 * <p>
 * Additional Listener calls in PlayerOnlineListener to avoid having HIGHEST priority listeners.
 *
 * @author Rsl1122
 * @see PlayerOnlineListener
 */
public class AFKListener implements Listener {

    // Static so that /reload does not cause afk tracking to fail.
    public static final AFKTracker AFK_TRACKER = new AFKTracker();

    private void event(PlayerEvent event) {
        try {
            UUID uuid = event.getPlayer().getUniqueId();
            long time = System.currentTimeMillis();

            AFK_TRACKER.performedAction(uuid, time);
        } catch (Exception e) {
            Log.toLog(this.getClass(), e);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMove(PlayerMoveEvent event) {
        event(event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        event(event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        event(event);
        boolean isAfkCommand = event.getMessage().substring(1).toLowerCase().startsWith("afk");
        if (isAfkCommand) {
            UUID uuid = event.getPlayer().getUniqueId();
            AFK_TRACKER.usedAfkCommand(uuid, System.currentTimeMillis());
        }
    }

}