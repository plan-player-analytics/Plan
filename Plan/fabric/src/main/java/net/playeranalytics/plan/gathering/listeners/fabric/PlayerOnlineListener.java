package net.playeranalytics.plan.gathering.listeners.fabric;

import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.transactions.events.BanStatusTransaction;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.playeranalytics.plan.gathering.listeners.FabricListener;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerOnlineListener implements FabricListener {

    private final ServerInfo serverInfo;
    private final DBSystem dbSystem;
    private final MinecraftDedicatedServer server;
    private final ErrorLogger errorLogger;

    private final Map<UUID, String> joinAddresses;

    private boolean isEnabled = false;

    @Inject
    public PlayerOnlineListener(
            ServerInfo serverInfo,
            DBSystem dbSystem,
            MinecraftDedicatedServer server,
            ErrorLogger errorLogger
    ) {
        this.serverInfo = serverInfo;
        this.dbSystem = dbSystem;
        this.server = server;
        this.errorLogger = errorLogger;

        joinAddresses = new HashMap<>();
    }

    @Override
    public void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (!this.isEnabled) {
                return;
            }
            try {
                boolean disconnected = false;
                ServerPlayConnectionEvents.DISCONNECT.register((handler1, server1) -> {

                    handler1.getConnection().getDisconnectReason().getString();
                });
                UUID uuid = handler.getPlayer().getUuid();
                ServerUUID serverUUID = serverInfo.getServerUUID();
                boolean banned = server.getPlayerManager().getUserBanList().contains(handler.getPlayer().getGameProfile());
                String joinAddress = handler.getConnection().getAddress().toString();
                if (!joinAddress.isEmpty()) {
                    joinAddresses.put(uuid, joinAddress.substring(0, joinAddress.lastIndexOf(':')));
                }
                dbSystem.getDatabase().executeTransaction(new BanStatusTransaction(uuid, serverUUID, () -> banned));
            } catch (Exception e) {
                errorLogger.error(e, ErrorContext.builder()
                        .related("this is fabric so no context sry")
                        .whatToDo("idk, report this")
                        .build());
            }
        });
        this.enable();
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
