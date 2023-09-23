/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016-2018
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.playeranalytics.plan.gathering.timed;

import com.djrapitops.plan.TaskSystem;
import com.djrapitops.plan.delivery.domain.DateObj;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.DataGatheringSettings;
import com.djrapitops.plan.settings.config.paths.TimeSettings;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.transactions.events.PingStoreTransaction;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.playeranalytics.plan.gathering.listeners.FabricListener;
import net.playeranalytics.plugin.scheduling.RunnableFactory;
import net.playeranalytics.plugin.scheduling.TimeAmount;
import net.playeranalytics.plugin.server.Listeners;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Task that handles player ping calculation on Fabric based servers.
 *
 * @author BrainStone
 * @author DrexHD
 */
public class FabricPingCounter extends TaskSystem.Task implements FabricListener {

    private final Map<UUID, Long> startRecording;
    private final Map<UUID, List<DateObj<Integer>>> playerHistory;

    private final Listeners listeners;
    private final PlanConfig config;
    private final DBSystem dbSystem;
    private final ServerInfo serverInfo;
    private final MinecraftDedicatedServer server;

    private boolean isEnabled = false;

    @Inject
    public FabricPingCounter(
            Listeners listeners,
            PlanConfig config,
            DBSystem dbSystem,
            ServerInfo serverInfo,
            MinecraftDedicatedServer server
    ) {
        this.listeners = listeners;
        this.config = config;
        this.dbSystem = dbSystem;
        this.serverInfo = serverInfo;
        this.server = server;
        startRecording = new ConcurrentHashMap<>();
        playerHistory = new HashMap<>();
        ServerPlayConnectionEvents.JOIN.register((handler, sender, minecraftServer) -> onPlayerJoin(handler.player));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, minecraftServer) -> onPlayerQuit(handler.player));
    }

    @Override
    public void run() {
        if (!this.isEnabled) {
            return;
        }
        long time = System.currentTimeMillis();

        Iterator<Map.Entry<UUID, Long>> starts = startRecording.entrySet().iterator();
        while (starts.hasNext()) {
            Map.Entry<UUID, Long> start = starts.next();
            if (time >= start.getValue()) {
                addPlayer(start.getKey());
                starts.remove();
            }
        }

        Iterator<Map.Entry<UUID, List<DateObj<Integer>>>> iterator = playerHistory.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<UUID, List<DateObj<Integer>>> entry = iterator.next();
            UUID uuid = entry.getKey();
            List<DateObj<Integer>> history = entry.getValue();
            ServerPlayerEntity player = server.getPlayerManager().getPlayer(uuid);
            if (player != null) {
                int ping = getPing(player);
                if (ping <= -1 || ping > TimeUnit.SECONDS.toMillis(8L)) {
                    // Don't accept bad values
                    continue;
                }
                history.add(new DateObj<>(time, ping));
                if (history.size() >= 30) {
                    dbSystem.getDatabase().executeTransaction(
                            new PingStoreTransaction(uuid, serverInfo.getServerUUID(), new ArrayList<>(history))
                    );
                    history.clear();
                }
            } else {
                iterator.remove();
            }
        }
    }

    @Override
    public void register(RunnableFactory runnableFactory) {
        Long startDelay = config.get(TimeSettings.PING_SERVER_ENABLE_DELAY);
        if (startDelay < TimeUnit.HOURS.toMillis(1L) && config.isTrue(DataGatheringSettings.PING)) {
            listeners.registerListener(this);
            long delay = TimeAmount.toTicks(startDelay, TimeUnit.MILLISECONDS);
            long period = 40L;
            runnableFactory.create(this).runTaskTimer(delay, period);
        }
        this.enable();
    }

    public void addPlayer(UUID uuid) {
        playerHistory.put(uuid, new ArrayList<>());
    }

    public void removePlayer(ServerPlayerEntity player) {
        playerHistory.remove(player.getUuid());
        startRecording.remove(player.getUuid());
    }

    private int getPing(ServerPlayerEntity player) {
        return player.networkHandler.getLatency();
    }

    public void onPlayerJoin(ServerPlayerEntity player) {
        if (!this.isEnabled) {
            return;
        }

        Long pingDelayMs = config.get(TimeSettings.PING_PLAYER_LOGIN_DELAY);
        if (pingDelayMs >= TimeUnit.HOURS.toMillis(2L)) {
            return;
        }
        startRecording.put(player.getUuid(), System.currentTimeMillis() + pingDelayMs);
    }

    public void onPlayerQuit(ServerPlayerEntity player) {
        if (!this.isEnabled) {
            return;
        }

        removePlayer(player);
    }

    public void clear() {
        playerHistory.clear();
    }

    @Override
    public void register() {
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
