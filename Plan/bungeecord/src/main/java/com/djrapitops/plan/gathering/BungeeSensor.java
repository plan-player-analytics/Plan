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

import com.djrapitops.plan.PlanBungee;
import com.djrapitops.plan.gathering.domain.PluginMetadata;
import com.djrapitops.plan.identification.properties.RedisCheck;
import com.djrapitops.plan.identification.properties.RedisPlayersOnlineSupplier;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.List;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Singleton
public class BungeeSensor implements ServerSensor<Object> {

    private final IntSupplier onlinePlayerCountSupplier;
    private final IntSupplier onlinePlayerCountBungee;
    private final Supplier<Collection<ProxiedPlayer>> getPlayers;
    private final Supplier<Collection<Plugin>> getPlugins;

    @Inject
    public BungeeSensor(PlanBungee plugin) {
        getPlayers = plugin.getProxy()::getPlayers;
        onlinePlayerCountBungee = plugin.getProxy()::getOnlineCount;
        getPlugins = plugin.getProxy().getPluginManager()::getPlugins;
        onlinePlayerCountSupplier = RedisCheck.isClassAvailable() ? new RedisPlayersOnlineSupplier() : onlinePlayerCountBungee;
    }

    @Override
    public boolean supportsDirectTPS() {
        return false;
    }

    @Override
    public int getOnlinePlayerCount() {
        int count = onlinePlayerCountSupplier.getAsInt();
        return count != -1 ? count : onlinePlayerCountBungee.getAsInt();
    }

    @Override
    public List<String> getOnlinePlayerNames() {
        return getPlayers.get().stream().map(ProxiedPlayer::getName).collect(Collectors.toList());
    }

    @Override
    public boolean usingRedisBungee() {
        return RedisCheck.isClassAvailable();
    }

    @Override
    public List<PluginMetadata> getInstalledPlugins() {
        return getPlugins.get().stream()
                .map(Plugin::getDescription)
                .map(description -> new PluginMetadata(description.getName(), description.getVersion()))
                .collect(Collectors.toList());
    }
}
