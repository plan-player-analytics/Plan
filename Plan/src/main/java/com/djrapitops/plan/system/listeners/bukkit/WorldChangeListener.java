package com.djrapitops.plan.system.listeners.bukkit;

import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.settings.WorldAliasSettings;
import com.djrapitops.plan.system.cache.SessionCache;
import com.djrapitops.plan.utilities.MiscUtils;
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
            Player p = event.getPlayer();
            String worldName = p.getWorld().getName();

            UUID uuid = p.getUniqueId();
            String gameMode = p.getGameMode().name();
            long time = MiscUtils.getTime();

            new WorldAliasSettings().addWorld(worldName);

            Optional<Session> cachedSession = SessionCache.getCachedSession(uuid);
            cachedSession.ifPresent(session -> session.changeState(worldName, gameMode, time));
        } catch (Exception e) {
            Log.toLog(this.getClass(), e);
        }
    }
}
