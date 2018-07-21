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
package com.djrapitops.plan.bukkit.tasks.server;

import com.djrapitops.plan.bukkit.utils.java.Reflection;
import com.djrapitops.plan.data.store.objects.DateObj;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plan.system.processing.processors.player.PingInsertProcessor;
import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.task.RunnableFactory;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Task that handles player ping calculation on Bukkit based servers.
 * <p>
 * Modified PingManager from LagMonitor plugin.
 * https://github.com/games647/LagMonitor/blob/master/src/main/java/com/github/games647/lagmonitor/task/PingManager.java
 *
 * @author games647
 */
public class PingCountTimer extends AbsRunnable implements Listener {

    //the server is pinging the client every 40 Ticks (2 sec) - so check it then
    //https://github.com/bergerkiller/CraftSource/blob/master/net.minecraft.server/PlayerConnection.java#L178
    public static final int PING_INTERVAL = 2 * 20;

    private static final boolean pingMethodAvailable;

    private static final MethodHandle pingField;
    private static final MethodHandle getHandleMethod;

    static {
        pingMethodAvailable = isPingMethodAvailable();

        MethodHandle localHandle = null;
        MethodHandle localPing = null;
        if (!pingMethodAvailable) {
            Class<?> craftPlayerClass = Reflection.getCraftBukkitClass("entity.CraftPlayer");
            Class<?> entityPlayer = Reflection.getMinecraftClass("EntityPlayer");

            Lookup lookup = MethodHandles.publicLookup();
            try {
                Method getHandleMethod = craftPlayerClass.getDeclaredMethod("getHandle");
                localHandle = lookup.unreflect(getHandleMethod);

                localPing = lookup.findGetter(entityPlayer, "ping", Integer.TYPE);
            } catch (NoSuchMethodException | IllegalAccessException | NoSuchFieldException reflectiveEx) {
                Log.toLog(PingCountTimer.class, reflectiveEx);
            }
        }

        getHandleMethod = localHandle;
        pingField = localPing;
    }

    private final Map<UUID, List<DateObj<Integer>>> playerHistory = new HashMap<>();

    private static boolean isPingMethodAvailable() {
        try {
            //Only available in Paper
            Player.Spigot.class.getDeclaredMethod("getPing");
            return true;
        } catch (NoSuchMethodException noSuchMethodEx) {
            return false;
        }
    }

    @Override
    public void run() {
        List<UUID> loggedOut = new ArrayList<>();
        long time = System.currentTimeMillis();
        playerHistory.forEach((uuid, history) -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                int ping = getPing(player);
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
        if (pingMethodAvailable) {
            return player.spigot().getPing();
        }

        return getReflectionPing(player);
    }

    private int getReflectionPing(Player player) {
        try {
            Object entityPlayer = getHandleMethod.invoke(player);
            return (int) pingField.invoke(entityPlayer);
        } catch (Exception ex) {
            return -1;
        } catch (Throwable throwable) {
            throw (Error) throwable;
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent joinEvent) {
        Player player = joinEvent.getPlayer();
        RunnableFactory.createNew("Add Player to Ping list", new AbsRunnable() {
            @Override
            public void run() {
                if (player.isOnline()) {
                    addPlayer(player);
                }
            }
        }).runTaskLater(TimeAmount.SECOND.ticks() * 10L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent quitEvent) {
        removePlayer(quitEvent.getPlayer());
    }

    public void clear() {
        playerHistory.clear();
    }
}
