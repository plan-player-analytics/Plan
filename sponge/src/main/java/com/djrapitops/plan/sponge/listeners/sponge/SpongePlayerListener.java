package com.djrapitops.plan.sponge.listeners.sponge;

import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.system.cache.SessionCache;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plan.system.processing.processors.info.NetworkPageUpdateProcessor;
import com.djrapitops.plan.system.processing.processors.info.PlayerPageUpdateProcessor;
import com.djrapitops.plan.system.processing.processors.player.*;
import com.djrapitops.plugin.api.systems.NotificationCenter;
import com.djrapitops.plugin.api.utility.log.Log;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.living.humanoid.player.KickPlayerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.service.ProviderRegistration;
import org.spongepowered.api.service.ban.BanService;

import java.net.InetAddress;
import java.util.Optional;
import java.util.UUID;

/**
 * Listener for Player Join/Leave on Sponge.
 *
 * @author Rsl1122
 */
public class SpongePlayerListener {

    @Listener(order = Order.POST)
    public void onLogin(ClientConnectionEvent.Login event) {
        try {
            actOnLoginEvent(event);
        } catch (Exception e) {
            Log.toLog(this.getClass(), e);
        }
    }

    private void actOnLoginEvent(ClientConnectionEvent.Login event) {
        GameProfile profile = event.getProfile();
        UUID uuid = profile.getUniqueId();
        boolean banned = isBanned(profile);
        Processing.submit(new BanAndOpProcessor(uuid, () -> banned, false));
    }

    @Listener(order = Order.POST)
    public void onKick(KickPlayerEvent event) {
        try {
            UUID uuid = event.getTargetEntity().getUniqueId();
            Processing.submit(new KickProcessor(uuid));
        } catch (Exception e) {
            Log.toLog(this.getClass(), e);
        }
    }

    private boolean isBanned(GameProfile profile) {
        Optional<ProviderRegistration<BanService>> banService = Sponge.getServiceManager().getRegistration(BanService.class);
        boolean banned = false;
        if (banService.isPresent()) {
            banned = banService.get().getProvider().isBanned(profile);
        }
        return banned;
    }

    @Listener(order = Order.POST)
    public void onJoin(ClientConnectionEvent.Join event) {
        try {
            actOnJoinEvent(event);
        } catch (Exception e) {
            Log.toLog(this.getClass(), e);
        }
    }

    private void actOnJoinEvent(ClientConnectionEvent.Join event) {
        Player player = event.getTargetEntity();

        NotificationCenter.checkNotifications(player);

        UUID uuid = player.getUniqueId();
        long time = System.currentTimeMillis();

        SpongeAFKListener.AFK_TRACKER.performedAction(uuid, time);

        String world = player.getWorld().getName();
        Optional<GameMode> gameMode = player.getGameModeData().get(Keys.GAME_MODE);
        String gm = "ADVENTURE";
        if (gameMode.isPresent()) {
            gm = gameMode.get().getName().toUpperCase();
        }

        InetAddress address = player.getConnection().getAddress().getAddress();

        String playerName = player.getName();
        String displayName = player.getDisplayNameData().displayName().get().toPlain();

        SessionCache.getInstance().cacheSession(uuid, new Session(uuid, time, world, gm));

        Processing.submit(
                new RegisterProcessor(uuid, time, playerName,
                        new IPUpdateProcessor(uuid, address, time),
                        new NameProcessor(uuid, playerName, displayName),
                        new PlayerPageUpdateProcessor(uuid)
                )
        );
        Processing.submit(new NetworkPageUpdateProcessor());
    }

    @Listener(order = Order.POST)
    public void onQuit(ClientConnectionEvent.Disconnect event) {
        try {
            actOnQuitEvent(event);
        } catch (Exception e) {
            Log.toLog(this.getClass(), e);
        }
    }

    private void actOnQuitEvent(ClientConnectionEvent.Disconnect event) {
        long time = System.currentTimeMillis();
        Player player = event.getTargetEntity();
        UUID uuid = player.getUniqueId();

        SpongeAFKListener.AFK_TRACKER.loggedOut(uuid, time);

        boolean banned = isBanned(player.getProfile());
        Processing.submit(new BanAndOpProcessor(uuid, () -> banned, false));
        Processing.submit(new EndSessionProcessor(uuid, time));
        Processing.submit(new NetworkPageUpdateProcessor());
        Processing.submit(new PlayerPageUpdateProcessor(uuid));
    }
}