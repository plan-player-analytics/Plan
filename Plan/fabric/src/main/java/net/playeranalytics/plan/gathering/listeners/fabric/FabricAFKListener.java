package net.playeranalytics.plan.gathering.listeners.fabric;

import com.djrapitops.plan.gathering.afk.AFKTracker;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.playeranalytics.plan.gathering.listeners.FabricListener;
import net.playeranalytics.plan.gathering.listeners.events.PlanFabricEvents;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FabricAFKListener implements FabricListener {

    // Static so that /reload does not cause afk tracking to fail.
    static AFKTracker afkTracker;
    private final Map<UUID, Boolean> ignorePermissionInfo;
    private final ErrorLogger errorLogger;
    private boolean isEnabled = false;

    @Inject
    public FabricAFKListener(PlanConfig config, ErrorLogger errorLogger) {
        this.errorLogger = errorLogger;
        this.ignorePermissionInfo = new HashMap<>();

        FabricAFKListener.assignAFKTracker(config);
    }

    private static void assignAFKTracker(PlanConfig config) {
        if (afkTracker == null) {
            afkTracker = new AFKTracker(config);
        }
    }

    private void event(ServerPlayerEntity player) {
        try {
            UUID uuid = player.getUuid();
            long time = System.currentTimeMillis();

            //TODO: Implement https://github.com/lucko/fabric-permissions-api
            boolean ignored = ignorePermissionInfo.computeIfAbsent(uuid, keyUUID -> /*player.hasPermission(Permissions.IGNORE_AFK.getPermission())*/ false);
            if (ignored) {
                afkTracker.hasIgnorePermission(uuid);
                ignorePermissionInfo.put(uuid, true);
                return;
            } else {
                ignorePermissionInfo.put(uuid, false);
            }

            afkTracker.performedAction(uuid, time);
        } catch (Exception e) {
            errorLogger.error(e, ErrorContext.builder().related(getClass(), player).build());
        }
    }

    @Override
    public void register() {
        this.enable();
        PlanFabricEvents.ON_CHAT.register((handler, message) -> {
            if (!isEnabled) {
                return;
            }
            event(handler.player);
            boolean isAfkCommand = message.substring(1).toLowerCase().startsWith("afk");
            if (isAfkCommand) {
                UUID uuid = handler.player.getUuid();
                afkTracker.usedAfkCommand(uuid, System.currentTimeMillis());
            }
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            if (!this.isEnabled) {
                return;
            }
            ignorePermissionInfo.remove(handler.player.getUuid());
        });
        PlanFabricEvents.ON_MOVE.register((handler, packet) -> {
            event(handler.player);
        });
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
