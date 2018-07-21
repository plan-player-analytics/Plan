package com.djrapitops.plan.bukkit.listeners.bukkit;

import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.system.cache.SessionCache;
import com.djrapitops.plan.system.settings.WorldAliasSettings;
import com.djrapitops.plugin.api.utility.log.Log;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

import java.util.Optional;
import java.util.UUID;

public class WorldChangeListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        try {
            actOnEvent(event);
        } catch (Exception e) {
            Log.toLog(this.getClass(), e);
        }
    }

    private void actOnEvent(PlayerChangedWorldEvent event) {
        long time = System.currentTimeMillis();

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        String worldName = player.getWorld().getName();
        String gameMode = player.getGameMode().name();

        WorldAliasSettings.addWorld(worldName);

        Optional<Session> cachedSession = SessionCache.getCachedSession(uuid);
        cachedSession.ifPresent(session -> session.changeState(worldName, gameMode, time));
    }
}
