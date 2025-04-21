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

import cn.nukkit.IPlayer;
import cn.nukkit.Player;
import cn.nukkit.level.Level;
import cn.nukkit.plugin.Plugin;
import com.djrapitops.plan.PlanNukkit;
import com.djrapitops.plan.gathering.domain.PluginMetadata;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Singleton
public class NukkitSensor implements ServerSensor<Level> {

    private final PlanNukkit plugin;

    @Inject
    public NukkitSensor(
            PlanNukkit plugin
    ) {
        this.plugin = plugin;
    }

    @Override
    public boolean supportsDirectTPS() {
        return true;
    }

    @Override
    public double getTPS() {
        return plugin.getServer().getTicksPerSecondAverage();
    }

    @Override
    public int getChunkCount(Level world) {
        return world.getChunks().size();
    }

    @Override
    public int getEntityCount(Level world) {
        return world.getEntities().length;
    }

    @Override
    public int getOnlinePlayerCount() {
        return plugin.getServer().getOnlinePlayers().size();
    }

    @Override
    public Iterable<Level> getWorlds() {
        return plugin.getServer().getLevels().values();
    }

    @Override
    public List<String> getOnlinePlayerNames() {
        return plugin.getServer().getOnlinePlayers()
                .values().stream()
                .map(Player::getName)
                .collect(Collectors.toList());
    }

    @Override
    public List<PluginMetadata> getInstalledPlugins() {
        return plugin.getServer().getPluginManager()
                .getPlugins().values().stream()
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
        IPlayer player = plugin.getServer().getOfflinePlayer(playerUUID);
        if (player == null) {return false;}
        return player.isBanned();
    }
}
