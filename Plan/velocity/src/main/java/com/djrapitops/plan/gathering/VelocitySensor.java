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

import com.djrapitops.plan.PlanVelocity;
import com.djrapitops.plan.gathering.domain.PluginMetadata;
import com.djrapitops.plan.identification.properties.VelocityRedisCheck;
import com.djrapitops.plan.identification.properties.VelocityRedisPlayersOnlineSupplier;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.Player;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.List;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Singleton
public class VelocitySensor implements ServerSensor<Object> {

    private final IntSupplier onlinePlayerCountSupplier;
    private final Supplier<Collection<Player>> getPlayers;
    private final Supplier<Collection<PluginContainer>> getPlugins;

    @Inject
    public VelocitySensor(PlanVelocity plugin) {
        getPlayers = plugin.getProxy()::getAllPlayers;
        onlinePlayerCountSupplier = VelocityRedisCheck.isClassAvailable()
                ? new VelocityRedisPlayersOnlineSupplier()
                : plugin.getProxy()::getPlayerCount;
        getPlugins = () -> plugin.getProxy().getPluginManager().getPlugins();
    }

    @Override
    public boolean supportsDirectTPS() {
        return false;
    }

    @Override
    public int getOnlinePlayerCount() {
        return onlinePlayerCountSupplier.getAsInt();
    }

    @Override
    public List<String> getOnlinePlayerNames() {
        return getPlayers.get().stream().map(Player::getUsername).collect(Collectors.toList());
    }

    @Override
    public boolean usingRedisBungee() {
        return VelocityRedisCheck.isClassAvailable();
    }

    @Override
    public List<PluginMetadata> getInstalledPlugins() {
        return getPlugins.get().stream()
                .map(PluginContainer::getDescription)
                .map(description -> new PluginMetadata(
                        description.getName().orElse(description.getId()),
                        description.getVersion().orElse("html.label.installed")))
                .collect(Collectors.toList());
    }
}
