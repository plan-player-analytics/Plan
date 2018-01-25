package com.djrapitops.plan.system.listeners.bukkit;

import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.system.cache.SessionCache;
import com.djrapitops.plan.system.settings.WorldAliasSettings;
import com.djrapitops.plan.utilities.MiscUtils;
import com.djrapitops.plugin.api.utility.log.Log;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;

import java.util.Optional;
import java.util.UUID;

/**
 * Event Listener for PlayerGameModeChangeEvents.
 *
 * @author Rsl1122
 */
public class GamemodeChangeListener implements Listener {

    /**
     * GM Change Event Listener.
     *
     * @param event Fired Event.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onGamemodeChange(PlayerGameModeChangeEvent event) {
        if (event.isCancelled()) {
            return;
        }
        try {
            Player p = event.getPlayer();
            UUID uuid = p.getUniqueId();
            long time = MiscUtils.getTime();
            String gameMode = event.getNewGameMode().name();
            String worldName = p.getWorld().getName();

            new WorldAliasSettings().addWorld(worldName);

            Optional<Session> cachedSession = SessionCache.getCachedSession(uuid);
            cachedSession.ifPresent(session -> session.changeState(worldName, gameMode, time));
        } catch (Exception e) {
            Log.toLog(this.getClass(), e);
        }
    }
}
