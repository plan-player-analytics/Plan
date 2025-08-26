/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.gathering;

import com.djrapitops.plan.gathering.domain.PluginMetadata;
import com.djrapitops.plan.gathering.timed.mspt.SpigotMspt;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Singleton
public class BukkitSensor implements ServerSensor<World> {

    private final Server server;

    private final boolean hasTPSMethod;
    private final boolean hasEntityCountMethod;
    private final boolean hasChunkCountMethod;

    @Inject
    public BukkitSensor(
            Server server
    ) {
        this.server = server;
        boolean hasPaper = isPaperAvailable();
        hasTPSMethod = hasPaper && hasPaperMethod(Server.class, "getTPS");
        hasEntityCountMethod = hasPaper && hasPaperMethod(World.class, "getEntityCount");
        hasChunkCountMethod = hasPaper && hasPaperMethod(World.class, "getChunkCount");
    }

    public static boolean isPaperAvailable() {
        try {
            Class.forName("co.aikar.timings.Timing");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean supportsDirectTPS() {
        return hasTPSMethod;
    }

    @Override
    public double getTPS() {
        return server.getTPS()[0];
    }

    @Override
    public int getChunkCount(World world) {
        if (hasChunkCountMethod) {
            try {
                return getChunkCountPaperWay(world);
            } catch (BootstrapMethodError | NoSuchMethodError e) {
                // Use spigot method
            }
        }
        return getChunkCountSpigotWay(world);
    }

    private int getChunkCountSpigotWay(World world) {
        return world.getLoadedChunks().length;
    }

    private int getChunkCountPaperWay(World world) {
        return world.getChunkCount();
    }

    @Override
    public int getEntityCount(World world) {
        if (hasEntityCountMethod) {
            try {
                return getEntitiesPaperWay(world);
            } catch (BootstrapMethodError | NoSuchMethodError e) {
                // Use spigot method
            }
        }
        return getEntitiesSpigotWay(world);
    }

    private int getEntitiesSpigotWay(World world) {
        return world.getEntities().size();
    }

    private int getEntitiesPaperWay(World world) {
        return world.getEntityCount();
    }

    @Override
    public int getOnlinePlayerCount() {
        return server.getOnlinePlayers().size();
    }

    @Override
    public Iterable<World> getWorlds() {
        return server.getWorlds();
    }

    private boolean hasPaperMethod(Class<?> clazz, String methodName) {
        try {
            return clazz.getMethod(methodName) != null;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    @Override
    public List<String> getOnlinePlayerNames() {
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .collect(Collectors.toList());
    }

    @Override
    public List<PluginMetadata> getInstalledPlugins() {
        return Arrays.stream(Bukkit.getPluginManager().getPlugins())
                .map(Plugin::getDescription)
                .map(description -> new PluginMetadata(description.getName(), description.getVersion()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean supportsBans() {
        return true;
    }

    @Override
    public boolean isBanned(UUID playerUUID) {
        OfflinePlayer player = server.getOfflinePlayer(playerUUID);
        if (player == null) {return false;}
        return player.isBanned();
    }

    @Override
    public Optional<long[]> getMspt() {
        return SpigotMspt.getMspt();
    }
}
