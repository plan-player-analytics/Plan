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
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.queries.objects.ServerQueries;
import com.djrapitops.plan.utilities.Predicates;

/**
 * Placeholders about a player.
 *
 * @author aidn5, Rsl1122
 */
public class PlayerPlaceHolder extends AbstractPlanPlaceHolder {

    private final Formatter<Double> decimals;
    private Formatter<Long> year;
    private Formatter<Long> time;

    public PlayerPlaceHolder(
            DBSystem dbSystem,
            ServerInfo serverInfo,
            Formatters formatters
    ) {
        super(serverInfo, dbSystem);
        time = formatters.timeAmount();
        year = formatters.yearLong();
        decimals = formatters.decimals();
    }

    public void register() {
        PlanPlaceholders.register("player_banned", p -> p.getValue(PlayerKeys.BANNED)
                .orElse(Boolean.FALSE));

        PlanPlaceholders.register("player_operator", p -> p.getValue(PlayerKeys.OPERATOR)
                .orElse(Boolean.FALSE));

        PlanPlaceholders.register("player_sessions_count", p -> SessionsMutator.forContainer(p).count());

        PlanPlaceholders.register("player_kick_count", p -> p.getValue(PlayerKeys.KICK_COUNT).orElse(0));

        PlanPlaceholders.register("player_death_count", p -> p.getValue(PlayerKeys.DEATH_COUNT).orElse(0));

        PlanPlaceholders.register("player_mob_kill_count", p -> p.getValue(PlayerKeys.MOB_KILL_COUNT).orElse(0));

        PlanPlaceholders.register("player_player_kill_count", p -> p.getValue(PlayerKeys.PLAYER_KILL_COUNT).orElse(0));

        PlanPlaceholders.register("player_kill_death_ratio", p -> PlayerVersusMutator.forContainer(p).toKillDeathRatio());

        PlanPlaceholders.register("player_ping_average_day", p -> decimals.apply(PingMutator.forContainer(p)
                .filterBy(Predicates.within(dayAgo(), now()))
                .average()) + " ms");

        PlanPlaceholders.register("player_ping_average_week", p -> decimals.apply(PingMutator.forContainer(p)
                .filterBy(Predicates.within(weekAgo(), now()))
                .average()) + " ms");

        PlanPlaceholders.register("player_ping_average_month", p -> decimals.apply(PingMutator.forContainer(p)
                .filterBy(Predicates.within(monthAgo(), now()))
                .average()) + " ms");

        PlanPlaceholders.register("player_lastseen", p -> year.apply(p.getValue(PlayerKeys.LAST_SEEN).orElse((long) 0)));

        PlanPlaceholders.register("player_registered", p -> year.apply(p.getValue(PlayerKeys.REGISTERED).orElse((long) 0)));

        PlanPlaceholders.register("player_time_active", p -> time.apply(SessionsMutator.forContainer(p)
                .toActivePlaytime()));

        PlanPlaceholders.register("player_time_afk", p -> time.apply(SessionsMutator.forContainer(p)
                .toAfkTime()));

        PlanPlaceholders.register("player_time_total", p -> time.apply(SessionsMutator.forContainer(p)
                .toPlaytime()));

        PlanPlaceholders.register("player_time_day", p -> time.apply(SessionsMutator.forContainer(p)
                .filterSessionsBetween(dayAgo(), now())
                .toPlaytime()));

        PlanPlaceholders.register("player_time_week", p -> time.apply(SessionsMutator.forContainer(p)
                .filterSessionsBetween(weekAgo(), now())
                .toPlaytime()));

        PlanPlaceholders.register("player_time_month", p -> time.apply(SessionsMutator.forContainer(p)
                .filterSessionsBetween(monthAgo(), now())
                .toPlaytime()));

        PlanPlaceholders.register("player_server_time_active", p -> time.apply(SessionsMutator.forContainer(p)
                .filterPlayedOnServer(serverUUID())
                .toActivePlaytime()));

        PlanPlaceholders.register("player_server_time_afk", p -> time.apply(SessionsMutator.forContainer(p)
                .filterPlayedOnServer(serverUUID())
                .toAfkTime()));

        PlanPlaceholders.register("player_server_time_total", p -> time.apply(SessionsMutator.forContainer(p)
                .filterPlayedOnServer(serverUUID())
                .toPlaytime()));

        PlanPlaceholders.register("player_server_time_day", p -> time.apply(SessionsMutator.forContainer(p)
                .filterSessionsBetween(dayAgo(), now())
                .filterPlayedOnServer(serverUUID())
                .toPlaytime()));

        PlanPlaceholders.register("player_server_time_week", p -> time.apply(SessionsMutator.forContainer(p)
                .filterSessionsBetween(weekAgo(), now())
                .filterPlayedOnServer(serverUUID())
                .toPlaytime()));

        PlanPlaceholders.register("player_server_time_month", p -> time.apply(SessionsMutator.forContainer(p)
                .filterSessionsBetween(monthAgo(), now())
                .filterPlayedOnServer(serverUUID())
                .toPlaytime()));

        PlanPlaceholders.register("player_favorite_server", p -> PerServerMutator.forContainer(p).favoriteServer()
                .flatMap(serverUUID -> dbSystem.getDatabase().query(ServerQueries.fetchServerMatchingIdentifier(serverUUID)))
                .map(Server::getName)
                .orElse("-"));
    }
}
