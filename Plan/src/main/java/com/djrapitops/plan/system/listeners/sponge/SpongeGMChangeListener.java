package com.djrapitops.plan.system.listeners.sponge;

import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.system.cache.SessionCache;
import com.djrapitops.plan.system.settings.WorldAliasSettings;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.living.humanoid.ChangeGameModeEvent;

import javax.inject.Inject;
import java.util.Optional;
import java.util.UUID;

/**
 * Listener for GameMode change on Sponge.
 *
 * @author Rsl1122
 */
public class SpongeGMChangeListener {

    private ErrorHandler errorHandler;

    @Inject
    public SpongeGMChangeListener(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    @Listener(order = Order.POST)
    public void onGMChange(ChangeGameModeEvent.TargetPlayer event) {
        if (event.isCancelled()) {
            return;
        }

        try {
            actOnGMChangeEvent(event);
        } catch (Exception e) {
            errorHandler.log(L.ERROR, this.getClass(), e);
        }
    }

    private void actOnGMChangeEvent(ChangeGameModeEvent.TargetPlayer event) {
        Player player = event.getTargetEntity();
        UUID uuid = player.getUniqueId();
        long time = System.currentTimeMillis();

        String gameMode = event.getGameMode().getName().toUpperCase();
        String worldName = player.getWorld().getName();

        WorldAliasSettings.addWorld_Old(worldName);

        Optional<Session> cachedSession = SessionCache.getCachedSession(uuid);
        cachedSession.ifPresent(session -> session.changeState(worldName, gameMode, time));
    }

}