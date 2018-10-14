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
package com.djrapitops.plan.system.tasks.proxy.bungee;

import com.djrapitops.plan.data.store.objects.DateObj;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plan.system.processing.processors.Processors;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.task.RunnableFactory;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.event.ServerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Task that handles player ping calculation on Bungee based servers.
 *
 * @author BrainStone
 */
@Singleton
public class PingCountTimerBungee extends AbsRunnable implements Listener {

    //the server is pinging the client every 40 Ticks (2 sec) - so check it then
    //https://github.com/bergerkiller/CraftSource/blob/master/net.minecraft.server/PlayerConnection.java#L178
    public static final int PING_INTERVAL = 2 * 20;

    private final Map<UUID, List<DateObj<Integer>>> playerHistory;

    private final PlanConfig config;
    private final Processors processors;
    private final Processing processing;
    private final RunnableFactory runnableFactory;

    @Inject
    public PingCountTimerBungee(
            PlanConfig config,
            Processors processors,
            Processing processing,
            RunnableFactory runnableFactory
    ) {
        this.config = config;
        this.processors = processors;
        this.processing = processing;
        this.runnableFactory = runnableFactory;
        playerHistory = new HashMap<>();
    }

    @Override
    public void run() {
        long time = System.currentTimeMillis();
        Iterator<Map.Entry<UUID, List<DateObj<Integer>>>> iterator = playerHistory.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<UUID, List<DateObj<Integer>>> entry = iterator.next();
            UUID uuid = entry.getKey();
            List<DateObj<Integer>> history = entry.getValue();
            ProxiedPlayer player = ProxyServer.getInstance().getPlayer(uuid);
            if (player != null) {
                int ping = getPing(player);
                if (ping < -1 || ping > TimeUnit.SECONDS.toMillis(8L)) {
                    // Don't accept bad values
                    continue;
                }
                history.add(new DateObj<>(time, ping));
                if (history.size() >= 30) {
                    processing.submit(processors.player().pingInsertProcessor(uuid, new ArrayList<>(history)));
                    history.clear();
                }
            } else {
                iterator.remove();
            }
        }
    }

    public void addPlayer(ProxiedPlayer player) {
        playerHistory.put(player.getUniqueId(), new ArrayList<>());
    }

    public void removePlayer(ProxiedPlayer player) {
        playerHistory.remove(player.getUniqueId());
    }

    private int getPing(ProxiedPlayer player) {
        return player.getPing();
    }

    @EventHandler
    public void onPlayerJoin(ServerConnectedEvent joinEvent) {
        ProxiedPlayer player = joinEvent.getPlayer();
        runnableFactory.create("Add Player to Ping list", new AbsRunnable() {
            @Override
            public void run() {
                if (player.isConnected()) {
                    addPlayer(player);
                }
            }
        }).runTaskLater(TimeAmount.toTicks(config.getNumber(Settings.PING_PLAYER_LOGIN_DELAY), TimeUnit.SECONDS));
    }

    @EventHandler
    public void onPlayerQuit(ServerDisconnectEvent quitEvent) {
        removePlayer(quitEvent.getPlayer());
    }

    public void clear() {
        playerHistory.clear();
    }
}
