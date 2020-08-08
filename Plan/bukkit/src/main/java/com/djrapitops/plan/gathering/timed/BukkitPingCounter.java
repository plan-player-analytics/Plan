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
package com.djrapitops.plan.gathering.timed;

import com.djrapitops.plan.delivery.domain.DateObj;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.TimeSettings;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.transactions.events.PingStoreTransaction;
import com.djrapitops.plan.utilities.java.Reflection;
import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.task.RunnableFactory;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Task that handles player ping calculation on Bukkit based servers.
 * <p>
 * Modified PingManager from LagMonitor plugin.
 * https://github.com/games647/LagMonitor/blob/master/src/main/java/com/github/games647/lagmonitor/task/PingManager.java
 *
 * @author games647
 */
@Singleton
public class BukkitPingCounter extends AbsRunnable implements Listener {

    //the server is pinging the client every 40 Ticks (2 sec) - so check it then
    //https://github.com/bergerkiller/CraftSource/blob/master/net.minecraft.server/PlayerConnection.java#L178

    private static boolean PING_METHOD_AVAILABLE;

    private static MethodHandle PING_FIELD;
    private static MethodHandle GET_HANDLE_METHOD;

    @Inject
    public BukkitPingCounter(
            PlanConfig config,
            DBSystem dbSystem,
            ServerInfo serverInfo,
            RunnableFactory runnableFactory
    ) {
        BukkitPingCounter.loadPingMethodDetails();
        this.config = config;
        this.dbSystem = dbSystem;
        this.serverInfo = serverInfo;
        this.runnableFactory = runnableFactory;
        playerHistory = new HashMap<>();
    }

    private final Map<UUID, List<DateObj<Integer>>> playerHistory;

    private final PlanConfig config;
    private final DBSystem dbSystem;
    private final ServerInfo serverInfo;
    private final RunnableFactory runnableFactory;

    private static void loadPingMethodDetails() {
        PING_METHOD_AVAILABLE = isPingMethodAvailable();

        MethodHandle localHandle = null;
        MethodHandle localPing = null;
        if (!PING_METHOD_AVAILABLE) {
            try {
                Class<?> craftPlayerClass = Reflection.getCraftBukkitClass("entity.CraftPlayer");
                Class<?> entityPlayer = Reflection.getMinecraftClass("EntityPlayer");

                Lookup lookup = MethodHandles.publicLookup();

                Method getHandleMethod = craftPlayerClass.getDeclaredMethod("getHandle");
                localHandle = lookup.unreflect(getHandleMethod);

                localPing = lookup.findGetter(entityPlayer, "ping", Integer.TYPE);
            } catch (NoSuchMethodException | IllegalAccessException | NoSuchFieldException reflectiveEx) {
                Logger.getGlobal().log(
                        Level.WARNING,
                        "Plan: Could not register Ping counter due to " + reflectiveEx
                );
            } catch (IllegalArgumentException e) {
                Logger.getGlobal().log(
                        Level.WARNING,
                        "Plan: No Ping method handle found - Ping will not be recorded."
                );
            }
        }

        GET_HANDLE_METHOD = localHandle;
        PING_FIELD = localPing;
    }

    private static boolean isPingMethodAvailable() {
        try {
            //Only available in Paper
            Class.forName("org.bukkit.entity.Player$Spigot").getDeclaredMethod("getPing");
            return true;
        } catch (ClassNotFoundException | NoSuchMethodException noSuchMethodEx) {
            return false;
        }
    }

    @Override
    public void run() {
        long time = System.currentTimeMillis();
        Iterator<Map.Entry<UUID, List<DateObj<Integer>>>> iterator = playerHistory.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<UUID, List<DateObj<Integer>>> entry = iterator.next();
            UUID uuid = entry.getKey();
            List<DateObj<Integer>> history = entry.getValue();
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                int ping = getPing(player);
                if (ping < -1 || ping > TimeUnit.SECONDS.toMillis(8L)) {
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

    public void addPlayer(Player player) {
        playerHistory.put(player.getUniqueId(), new ArrayList<>());
    }

    public void removePlayer(Player player) {
        playerHistory.remove(player.getUniqueId());
    }

    private int getPing(Player player) {
        if (PING_METHOD_AVAILABLE) {
            // This method is from Paper
            return player.spigot().getPing();
        }

        return getReflectionPing(player);
    }

    private int getReflectionPing(Player player) {
        try {
            Object entityPlayer = GET_HANDLE_METHOD.invoke(player);
            return (int) PING_FIELD.invoke(entityPlayer);
        } catch (Exception ex) {
            return -1;
        } catch (Throwable throwable) {
            throw (Error) throwable;
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent joinEvent) {
        Player player = joinEvent.getPlayer();
        Long pingDelay = config.get(TimeSettings.PING_PLAYER_LOGIN_DELAY);
        if (pingDelay >= TimeUnit.HOURS.toMillis(2L)) {
            return;
        }
        runnableFactory.create("Add Player to Ping list", new AbsRunnable() {
            @Override
            public void run() {
                if (player.isOnline()) {
                    addPlayer(player);
                }
            }
        }).runTaskLater(TimeAmount.toTicks(pingDelay, TimeUnit.MILLISECONDS));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent quitEvent) {
        removePlayer(quitEvent.getPlayer());
    }

    public void clear() {
        playerHistory.clear();
    }
}
