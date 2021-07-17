package net.playeranalytics.plan.gathering.listeners.fabric;

import com.djrapitops.plan.delivery.domain.Nickname;
import com.djrapitops.plan.delivery.domain.PlayerName;
import com.djrapitops.plan.delivery.domain.ServerName;
import com.djrapitops.plan.delivery.export.Exporter;
import com.djrapitops.plan.extension.CallEvents;
import com.djrapitops.plan.extension.ExtensionSvc;
import com.djrapitops.plan.gathering.cache.NicknameCache;
import com.djrapitops.plan.gathering.cache.SessionCache;
import com.djrapitops.plan.gathering.domain.ActiveSession;
import com.djrapitops.plan.gathering.geolocation.GeolocationCache;
import com.djrapitops.plan.gathering.listeners.Status;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.processing.Processing;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.DataGatheringSettings;
import com.djrapitops.plan.settings.config.paths.ExportSettings;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.transactions.events.*;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import com.google.common.net.InetAddresses;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.playeranalytics.plan.gathering.listeners.FabricListener;

import javax.inject.Inject;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public class PlayerOnlineListener implements FabricListener {

    private final PlanConfig config;
    private final Processing processing;
    private final ServerInfo serverInfo;
    private final DBSystem dbSystem;
    private final ExtensionSvc extensionService;
    private final Exporter exporter;
    private final GeolocationCache geolocationCache;
    private final NicknameCache nicknameCache;
    private final SessionCache sessionCache;
    private final ErrorLogger errorLogger;
    private final MinecraftDedicatedServer server;

    private final Map<UUID, String> joinAddresses;

    private boolean isEnabled = false;

    @Inject
    public PlayerOnlineListener(
            PlanConfig config,
            Processing processing,
            ServerInfo serverInfo,
            DBSystem dbSystem,
            ExtensionSvc extensionService,
            Exporter exporter,
            GeolocationCache geolocationCache,
            NicknameCache nicknameCache,
            SessionCache sessionCache,
            ErrorLogger errorLogger,
            MinecraftDedicatedServer server
    ) {
        this.config = config;
        this.processing = processing;
        this.serverInfo = serverInfo;
        this.dbSystem = dbSystem;
        this.extensionService = extensionService;
        this.exporter = exporter;
        this.geolocationCache = geolocationCache;
        this.nicknameCache = nicknameCache;
        this.sessionCache = sessionCache;
        this.errorLogger = errorLogger;
        this.server = server;

        joinAddresses = new HashMap<>();
    }

    @Override
    public void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (!this.isEnabled) {
                return;
            }
            actOnJoinEvent(handler.player);
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            if (!this.isEnabled) {
                return;
            }
            actOnQuitEvent(handler.player);
        });
        this.enable();
    }

    //TODO: Call
    public void onPlayerLogin(ServerPlayerEntity player, boolean banned) {
        try {
            UUID playerUUID = player.getUuid();
            ServerUUID serverUUID = serverInfo.getServerUUID();
            String joinAddress = player.networkHandler.connection.getAddress().toString();
            if (!joinAddress.isEmpty()) {
                joinAddresses.put(playerUUID, joinAddress.substring(0, joinAddress.lastIndexOf(':')));
            }
            dbSystem.getDatabase().executeTransaction(new BanStatusTransaction(playerUUID, serverUUID, () -> banned));
        } catch (Exception e) {
            errorLogger.error(e, ErrorContext.builder().related(getClass(), player).build());
        }
    }

    //TODO: Call
    public void onPlayerKick(ServerPlayerEntity player) {
        try {
            UUID uuid = player.getUuid();
            if (FabricAFKListener.afkTracker.isAfk(uuid)) {
                return;
            }

            dbSystem.getDatabase().executeTransaction(new KickStoreTransaction(uuid));
        } catch (Exception e) {
            errorLogger.error(e, ErrorContext.builder().related(getClass(), player).build());
        }
    }

    private void actOnJoinEvent(ServerPlayerEntity player) {

        UUID playerUUID = player.getUuid();
        ServerUUID serverUUID = serverInfo.getServerUUID();
        long time = System.currentTimeMillis();

        FabricAFKListener.afkTracker.performedAction(playerUUID, time);

        String world = player.getServerWorld().getRegistryKey().getValue().toString();
        String gm = player.interactionManager.getGameMode().name();

        Database database = dbSystem.getDatabase();
        database.executeTransaction(new WorldNameStoreTransaction(serverUUID, world));

        InetSocketAddress socketAddress = (InetSocketAddress) player.networkHandler.connection.getAddress();
        InetAddress address = InetAddresses.forString(socketAddress.getAddress().toString().replace("/", ""));
        Supplier<String> getHostName = () -> getHostname(player);

        String playerName = player.getEntityName();
        String displayName = player.getDisplayName().asString();

        boolean gatheringGeolocations = config.isTrue(DataGatheringSettings.GEOLOCATIONS);
        if (gatheringGeolocations) {
            database.executeTransaction(
                    new GeoInfoStoreTransaction(playerUUID, address, time, geolocationCache::getCountry)
            );
        }

        database.executeTransaction(new PlayerServerRegisterTransaction(playerUUID,
                System::currentTimeMillis, playerName, serverUUID, getHostName));
        database.executeTransaction(new OperatorStatusTransaction(playerUUID, serverUUID, server.getPlayerManager().getOpList().isOp(player.getGameProfile())));

        ActiveSession session = new ActiveSession(playerUUID, serverUUID, time, world, gm);
        session.getExtraData().put(PlayerName.class, new PlayerName(playerName));
        session.getExtraData().put(ServerName.class, new ServerName(serverInfo.getServer().getIdentifiableName()));
        sessionCache.cacheSession(playerUUID, session)
                .ifPresent(previousSession -> database.executeTransaction(new SessionEndTransaction(previousSession)));

        database.executeTransaction(new NicknameStoreTransaction(
                playerUUID, new Nickname(displayName, time, serverUUID),
                (uuid, name) -> nicknameCache.getDisplayName(playerUUID).map(name::equals).orElse(false)
        ));

        processing.submitNonCritical(() -> extensionService.updatePlayerValues(playerUUID, playerName, CallEvents.PLAYER_JOIN));
        if (config.isTrue(ExportSettings.EXPORT_ON_ONLINE_STATUS_CHANGE)) {
            processing.submitNonCritical(() -> exporter.exportPlayerPage(playerUUID, playerName));
        }
    }

    private String getHostname(ServerPlayerEntity player) {
        return joinAddresses.get(player.getUuid());
    }

    //TODO: Call
    public void beforePlayerQuit(ServerPlayerEntity player) {
        UUID playerUUID = player.getUuid();
        String playerName = player.getEntityName();
        processing.submitNonCritical(() -> extensionService.updatePlayerValues(playerUUID, playerName, CallEvents.PLAYER_LEAVE));
    }

    //TODO: Call
    public void onPlayerQuit(ServerPlayerEntity player) {
        try {
            actOnQuitEvent(player);
        } catch (Exception e) {
            errorLogger.error(e, ErrorContext.builder().related(getClass(), player).build());
        }
    }

    private void actOnQuitEvent(ServerPlayerEntity player) {
        long time = System.currentTimeMillis();
        String playerName = player.getEntityName();
        UUID playerUUID = player.getUuid();
        ServerUUID serverUUID = serverInfo.getServerUUID();

        FabricAFKListener.afkTracker.loggedOut(playerUUID, time);

        joinAddresses.remove(playerUUID);
        nicknameCache.removeDisplayName(playerUUID);

        dbSystem.getDatabase().executeTransaction(new BanStatusTransaction(playerUUID, serverUUID, () -> server.getPlayerManager().getUserBanList().contains(player.getGameProfile())));

        sessionCache.endSession(playerUUID, time)
                .ifPresent(endedSession -> dbSystem.getDatabase().executeTransaction(new SessionEndTransaction(endedSession)));

        if (config.isTrue(ExportSettings.EXPORT_ON_ONLINE_STATUS_CHANGE)) {
            processing.submitNonCritical(() -> exporter.exportPlayerPage(playerUUID, playerName));
        }
    }

    @Override
    public boolean isEnabled() {
        return this.isEnabled;
    }

    @Override
    public void enable() {
        this.isEnabled = true;
    }

    @Override
    public void disable() {
        this.isEnabled = false;
    }


}
