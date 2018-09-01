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
package com.djrapitops.plan.system.tasks.server;

import com.djrapitops.plan.data.store.objects.DateObj;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plan.system.processing.processors.player.PingInsertProcessor;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.task.RunnableFactory;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;

import java.util.*;

/**
 * Task that handles player ping calculation on Sponge based servers.
 *
 * @author BrainStone
 */
public class PingCountTimerSponge extends AbsRunnable {

    //the server is pinging the client every 40 Ticks (2 sec) - so check it then
    //https://github.com/bergerkiller/CraftSource/blob/master/net.minecraft.server/PlayerConnection.java#L178
    public static final int PING_INTERVAL = 2 * 20;

    private final Map<UUID, List<DateObj<Integer>>> playerHistory = new HashMap<>();

    @Override
    public void run() {
        List<UUID> loggedOut = new ArrayList<>();
        long time = System.currentTimeMillis();
        playerHistory.forEach((uuid, history) -> {
            Optional<Player> player = Sponge.getServer().getPlayer(uuid);
            if (player.isPresent()) {
                int ping = getPing(player.get());
                if (ping < -1 || ping > TimeAmount.SECOND.ms() * 8L) {
                    // Don't accept bad values
                    return;
                }
                history.add(new DateObj<>(time, ping));
                if (history.size() >= 30) {
                    Processing.submit(new PingInsertProcessor(uuid, new ArrayList<>(history)));
                    history.clear();
                }
            } else {
                loggedOut.add(uuid);
            }
        });
        loggedOut.forEach(playerHistory::remove);
    }

    public void addPlayer(Player player) {
        playerHistory.put(player.getUniqueId(), new ArrayList<>());
    }

    public void removePlayer(Player player) {
        playerHistory.remove(player.getUniqueId());
    }

    private int getPing(Player player) {
        return player.getConnection().getLatency();
    }

    @Listener
    public void onPlayerJoin(ClientConnectionEvent.Join joinEvent) {
        Player player = joinEvent.getTargetEntity();
        RunnableFactory.createNew("Add Player to Ping list", new AbsRunnable() {
            @Override
            public void run() {
                if (player.isOnline()) {
                    addPlayer(player);
                }
            }
        }).runTaskLater(TimeAmount.SECOND.ticks() * (long) Settings.PING_PLAYER_LOGIN_DELAY.getNumber());
    }

    @Listener
    public void onPlayerQuit(ClientConnectionEvent.Disconnect quitEvent) {
        removePlayer(quitEvent.getTargetEntity());
    }

    public void clear() {
        playerHistory.clear();
    }
}
