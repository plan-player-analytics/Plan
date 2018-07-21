package com.djrapitops.plan.sponge.listeners.sponge;

import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.system.cache.SessionCache;
import com.djrapitops.plan.system.settings.WorldAliasSettings;
import com.djrapitops.plugin.api.utility.log.Log;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.filter.cause.First;

import java.util.Optional;
import java.util.UUID;

/**
 * Listener for World change on Sponge.
 *
 * @author Rsl1122
 */
public class SpongeWorldChangeListener {

    @Listener(order = Order.POST)
    public void onWorldChange(MoveEntityEvent.Teleport event, @First Player player) {
        if (event.isCancelled()) {
            return;
        }

        try {
            actOnEvent(event, player);
        } catch (Exception e) {
            Log.toLog(this.getClass(), e);
        }
    }

    private void actOnEvent(MoveEntityEvent.Teleport event, Player player) {
        long time = System.currentTimeMillis();

        UUID uuid = player.getUniqueId();

        String worldName = event.getToTransform().getExtent().getName();
        String gameMode = getGameMode(player);

        WorldAliasSettings.addWorld(worldName);

        Optional<Session> cachedSession = SessionCache.getCachedSession(uuid);
        cachedSession.ifPresent(session -> session.changeState(worldName, gameMode, time));
    }

    private String getGameMode(Player player) {
        Optional<GameMode> gameMode = player.getGameModeData().get(Keys.GAME_MODE);
        return gameMode.map(gm -> gm.getName().toUpperCase()).orElse("ADVENTURE");
    }

}