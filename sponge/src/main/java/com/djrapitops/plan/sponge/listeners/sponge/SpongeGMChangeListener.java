package com.djrapitops.plan.sponge.listeners.sponge;

import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.system.cache.SessionCache;
import com.djrapitops.plan.system.settings.WorldAliasSettings;
import com.djrapitops.plugin.api.utility.log.Log;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.living.humanoid.ChangeGameModeEvent;

import java.util.Optional;
import java.util.UUID;

/**
 * Listener for GameMode change on Sponge.
 *
 * @author Rsl1122
 */
public class SpongeGMChangeListener {

    @Listener(order = Order.POST)
    public void onGMChange(ChangeGameModeEvent.TargetPlayer event) {
        if (event.isCancelled()) {
            return;
        }

        try {
            actOnGMChangeEvent(event);
        } catch (Exception e) {
            Log.toLog(this.getClass(), e);
        }
    }

    private void actOnGMChangeEvent(ChangeGameModeEvent.TargetPlayer event) {
        Player player = event.getTargetEntity();
        UUID uuid = player.getUniqueId();
        long time = System.currentTimeMillis();

        String gameMode = event.getGameMode().getName().toUpperCase();
        String worldName = player.getWorld().getName();

        WorldAliasSettings.addWorld(worldName);

        Optional<Session> cachedSession = SessionCache.getCachedSession(uuid);
        cachedSession.ifPresent(session -> session.changeState(worldName, gameMode, time));
    }

}