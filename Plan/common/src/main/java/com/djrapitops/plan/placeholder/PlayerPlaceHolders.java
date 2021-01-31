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
package com.djrapitops.plan.placeholder;

import com.djrapitops.plan.delivery.domain.keys.PlayerKeys;
import com.djrapitops.plan.delivery.domain.mutators.PerServerMutator;
import com.djrapitops.plan.delivery.domain.mutators.PingMutator;
import com.djrapitops.plan.delivery.domain.mutators.PlayerVersusMutator;
import com.djrapitops.plan.delivery.domain.mutators.SessionsMutator;
import com.djrapitops.plan.delivery.formatting.Formatter;
import com.djrapitops.plan.delivery.formatting.Formatters;
import com.djrapitops.plan.identification.Server;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.TimeSettings;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.queries.objects.ServerQueries;
import com.djrapitops.plan.utilities.Predicates;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.djrapitops.plan.utilities.MiscUtils.*;

/**
 * Placeholders about a player.
 *
 * @author aidn5, Rsl1122
 */
@Singleton
public class PlayerPlaceHolders implements Placeholders {

    private final PlanConfig config;
    private final DBSystem dbSystem;
    private final ServerInfo serverInfo;
    private final Formatters formatters;

    @Inject
    public PlayerPlaceHolders(
            PlanConfig config,
            DBSystem dbSystem,
            ServerInfo serverInfo,
            Formatters formatters
    ) {
        this.config = config;
        this.dbSystem = dbSystem;
        this.serverInfo = serverInfo;
        this.formatters = formatters;
    }

    @Override
    public void register(
            PlanPlaceholders placeholders
    ) {
        Formatter<Double> decimals = formatters.decimals();
        Formatter<Long> year = formatters.yearLong();
        Formatter<Long> time = formatters.timeAmount();

        placeholders.register("player_banned",
                player -> player.getValue(PlayerKeys.BANNED)
                        .orElse(Boolean.FALSE)
        );

        placeholders.register("player_operator",
                player -> player.getValue(PlayerKeys.OPERATOR)
                        .orElse(Boolean.FALSE)
        );

        placeholders.register("player_sessions_count",
                player -> SessionsMutator.forContainer(player)
                        .count()
        );

        placeholders.register("player_kick_count",
                player -> player.getValue(PlayerKeys.KICK_COUNT)
                        .orElse(0)
        );

        placeholders.register("player_death_count",
                player -> player.getValue(PlayerKeys.DEATH_COUNT)
                        .orElse(0)
        );

        placeholders.register("player_mob_kill_count",
                player -> player.getValue(PlayerKeys.MOB_KILL_COUNT)
                        .orElse(0)
        );

        placeholders.register("player_player_kill_count",
                player -> player.getValue(PlayerKeys.PLAYER_KILL_COUNT)
                        .orElse(0)
        );

        placeholders.register("player_kill_death_ratio",
                player -> PlayerVersusMutator.forContainer(player).toKillDeathRatio());

        placeholders.register("player_ping_average_day",
                player -> decimals.apply(PingMutator.forContainer(player)
                        .filterBy(Predicates.within(dayAgo(), now()))
                        .average()) + " ms"
        );

        placeholders.register("player_ping_average_week",
                player -> decimals.apply(PingMutator.forContainer(player)
                        .filterBy(Predicates.within(weekAgo(), now()))
                        .average()) + " ms"
        );

        placeholders.register("player_ping_average_month",
                player -> decimals.apply(PingMutator.forContainer(player)
                        .filterBy(Predicates.within(monthAgo(), now()))
                        .average()) + " ms"
        );

        placeholders.register("player_lastseen",
                player -> year.apply(player.getValue(PlayerKeys.LAST_SEEN)
                        .orElse((long) 0))
        );

        placeholders.register("player_registered",
                player -> year.apply(player.getValue(PlayerKeys.REGISTERED)
                        .orElse((long) 0))
        );

        registerPlaytimePlaceholders(placeholders, time);

        placeholders.register("player_favorite_server",
                player -> PerServerMutator.forContainer(player).favoriteServer()
                        .flatMap(serverUUID -> dbSystem.getDatabase().query(ServerQueries.fetchServerMatchingIdentifier(serverUUID)))
                        .map(Server::getName)
                        .orElse("-")
        );

        placeholders.register("player_activity_index",
                player -> player.getActivityIndex(
                        now(),
                        config.get(TimeSettings.ACTIVE_PLAY_THRESHOLD)
                ).getValue()
        );
        placeholders.register("player_activity_group",
                player -> player.getActivityIndex(
                        now(),
                        config.get(TimeSettings.ACTIVE_PLAY_THRESHOLD)
                ).getGroup()
        );
    }

    private void registerPlaytimePlaceholders(PlanPlaceholders placeholders, Formatter<Long> time) {
        placeholders.register("player_time_active",
                player -> time.apply(SessionsMutator.forContainer(player)
                        .toActivePlaytime())
        );

        placeholders.register("player_time_afk",
                player -> time.apply(SessionsMutator.forContainer(player)
                        .toAfkTime())
        );

        placeholders.register("player_time_total",
                player -> time.apply(SessionsMutator.forContainer(player)
                        .toPlaytime())
        );

        placeholders.register("player_time_day",
                player -> time.apply(SessionsMutator.forContainer(player)
                        .filterSessionsBetween(dayAgo(), now())
                        .toPlaytime())
        );

        placeholders.register("player_time_week",
                player -> time.apply(SessionsMutator.forContainer(player)
                        .filterSessionsBetween(weekAgo(), now())
                        .toPlaytime())
        );

        placeholders.register("player_time_month",
                player -> time.apply(SessionsMutator.forContainer(player)
                        .filterSessionsBetween(monthAgo(), now())
                        .toPlaytime())
        );

        placeholders.register("player_server_time_active",
                player -> time.apply(SessionsMutator.forContainer(player)
                        .filterPlayedOnServer(serverInfo.getServerUUID())
                        .toActivePlaytime())
        );

        placeholders.register("player_server_time_afk",
                player -> time.apply(SessionsMutator.forContainer(player)
                        .filterPlayedOnServer(serverInfo.getServerUUID())
                        .toAfkTime())
        );

        placeholders.register("player_server_time_total",
                player -> time.apply(SessionsMutator.forContainer(player)
                        .filterPlayedOnServer(serverInfo.getServerUUID())
                        .toPlaytime())
        );

        placeholders.register("player_server_time_day",
                player -> time.apply(SessionsMutator.forContainer(player)
                        .filterSessionsBetween(dayAgo(), now())
                        .filterPlayedOnServer(serverInfo.getServerUUID())
                        .toPlaytime())
        );

        placeholders.register("player_server_time_week",
                player -> time.apply(SessionsMutator.forContainer(player)
                        .filterSessionsBetween(weekAgo(), now())
                        .filterPlayedOnServer(serverInfo.getServerUUID())
                        .toPlaytime())
        );

        placeholders.register("player_server_time_month",
                player -> time.apply(SessionsMutator.forContainer(player)
                        .filterSessionsBetween(monthAgo(), now())
                        .filterPlayedOnServer(serverInfo.getServerUUID())
                        .toPlaytime())
        );
    }
}
