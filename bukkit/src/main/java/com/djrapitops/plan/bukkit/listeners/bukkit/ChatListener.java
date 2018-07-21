package com.djrapitops.plan.bukkit.listeners.bukkit;

import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plan.system.processing.processors.player.NameProcessor;
import com.djrapitops.plugin.api.utility.log.Log;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.UUID;

/**
 * Event Listener for AsyncPlayerChatEvents.
 *
 * @author Rsl1122
 */
public class ChatListener implements Listener {

    /**
     * ChatEvent listener.
     *
     * @param event Fired Event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) {
            return;
        }

        try {
            actOnChatEvent(event);
        } catch (Exception e) {
            Log.toLog(this.getClass(), e);
        }
    }

    private void actOnChatEvent(AsyncPlayerChatEvent event) {
        Player p = event.getPlayer();
        UUID uuid = p.getUniqueId();
        String name = p.getName();
        String displayName = p.getDisplayName();
        Processing.submit(new NameProcessor(uuid, name, displayName));
    }
}
