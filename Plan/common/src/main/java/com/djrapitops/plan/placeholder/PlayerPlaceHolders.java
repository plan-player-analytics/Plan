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

import com.djrapitops.plan.delivery.domain.container.PlayerContainer;
import com.djrapitops.plan.delivery.domain.keys.PlayerKeys;
import com.djrapitops.plan.delivery.domain.mutators.*;
import com.djrapitops.plan.delivery.formatting.Formatter;
import com.djrapitops.plan.delivery.formatting.Formatters;
import com.djrapitops.plan.gathering.afk.AFKTracker;
import com.djrapitops.plan.gathering.cache.SessionCache;
import com.djrapitops.plan.gathering.domain.ActiveSession;
import com.djrapitops.plan.gathering.domain.FinishedSession;
import com.djrapitops.plan.gathering.domain.GeoInfo;
import com.djrapitops.plan.gathering.domain.PlayerKill;
import com.djrapitops.plan.gathering.domain.event.JoinAddress;
import com.djrapitops.plan.identification.Server;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.TimeSettings;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.GenericLang;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.queries.objects.ServerQueries;
import com.djrapitops.plan.utilities.Predicates;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;

import static com.djrapitops.plan.utilities.MiscUtils.*;

/**
 * Placeholders about a player.
 *
 * @author aidn5, AuroraLS3
 */
@Singleton
public class PlayerPlaceHolders implements Placeholders {

    private final Locale locale;
    private final PlanConfig config;
    private final DBSystem dbSystem;
    private final ServerInfo serverInfo;
    private final Formatters formatters;

    @Inject
    public PlayerPlaceHolders(
            Locale locale,
            PlanConfig config,
            DBSystem dbSystem,
            ServerInfo serverInfo,
            Formatters formatters
    ) {
        this.locale = locale;
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

        placeholders.register("player_is_afk", this::isAfk);
        placeholders.register("player_is_afk_badge", player -> isAfk(player) ? "AFK" : "");

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
        placeholders.register("player_lastseen_server",
                player -> SessionsMutator.forContainer(player).latestSession()
                        .map(FinishedSession::getServerUUID)
                        .flatMap(serverUUID -> dbSystem.getDatabase().query(ServerQueries.fetchServerMatchingIdentifier(serverUUID)))
                        .map(Server::getIdentifiableName)
                        .orElse("-")
        );
        placeholders.register("player_lastseen_raw",
                player -> player.getValue(PlayerKeys.LAST_SEEN)
                        .orElse((long) 0)
        );

        placeholders.register("player_registered",
                player -> year.apply(player.getValue(PlayerKeys.REGISTERED)
                        .orElse((long) 0))
        );
        placeholders.register("player_registered_raw",
                player -> player.getValue(PlayerKeys.REGISTERED)
                        .orElse((long) 0)
        );

        placeholders.register("player_geolocation",
                player -> GeoInfoMutator.forContainer(player)
                        .mostRecent()
                        .map(GeoInfo::getGeolocation)
                        .orElse(locale.getString(GenericLang.UNKNOWN))
        );

        placeholders.register("player_join_address",
                player -> SessionsMutator.forContainer(player)
                        .latestSession()
                        .flatMap(session -> session.getExtraData(JoinAddress.class))
                        .map(JoinAddress::getAddress)
                        .orElse(locale.getString(GenericLang.UNKNOWN))
        );

        registerPlaytimePlaceholders(placeholders, time);
        registerSessionLengthPlaceholders(placeholders, time);

        placeholders.register("player_favorite_server",
                player -> PerServerMutator.forContainer(player).favoriteServer()
                        .flatMap(serverUUID -> dbSystem.getDatabase().query(ServerQueries.fetchServerMatchingIdentifier(serverUUID)))
                        .map(Server::getName)
                        .orElse("-")
        );

        placeholders.register("player_activity_index",
                player -> decimals.apply(player.getActivityIndex(
                        now(),
                        config.get(TimeSettings.ACTIVE_PLAY_THRESHOLD)
                ).getValue())
        );
        placeholders.register("player_activity_group",
                player -> player.getActivityIndex(
                        now(),
                        config.get(TimeSettings.ACTIVE_PLAY_THRESHOLD)
                ).getGroup()
        );

        registerKillPlaceholders(placeholders);
    }

    private void registerSessionLengthPlaceholders(PlanPlaceholders placeholders, Formatter<Long> time) {
        placeholders.register("player_current_session_length",
                player -> time.apply(getActiveSessionLength(player).orElse(-1L)));
        placeholders.register("player_current_session_length_raw",
                player -> getActiveSessionLength(player).orElse(0L));

        placeholders.register("player_latest_session_length",
                player -> time.apply(getActiveSessionLength(player)
                        .orElseGet(() -> SessionsMutator.forContainer(player).latestSession()
                                .map(FinishedSession::getLength)
                                .orElse(-1L))));
        placeholders.register("player_latest_session_length_raw",
                player -> getActiveSessionLength(player)
                        .orElseGet(() -> SessionsMutator.forContainer(player).latestSession()
                                .map(FinishedSession::getLength)
                                .orElse(0L)));

        placeholders.register("player_previous_session_length",
                player -> time.apply(SessionsMutator.forContainer(player).previousSession()
                        .map(FinishedSession::getLength)
                        .orElse(-1L)));
        placeholders.register("player_previous_session_length_raw",
                player -> SessionsMutator.forContainer(player).previousSession()
                        .map(FinishedSession::getLength)
                        .orElse(0L));
    }

    private boolean isAfk(PlayerContainer player) {
        return SessionCache.getCachedSession(player.getUnsafe(PlayerKeys.UUID))
                .map(ActiveSession::getLastMovementForAfkCalculation)
                .filter(lastMovement -> lastMovement != AFKTracker.IGNORES_AFK
                        && now() - lastMovement > config.get(TimeSettings.AFK_THRESHOLD))
                .isPresent();
    }

    private void registerKillPlaceholders(PlanPlaceholders placeholders) {
        Formatter<Double> decimals = formatters.decimals();
        placeholders.register("player_player_caused_deaths",
                player -> PlayerVersusMutator.forContainer(player).toPlayerDeathCount()
        );
        placeholders.register("player_deaths",
                player -> PlayerVersusMutator.forContainer(player).toDeathCount()
        );
        placeholders.register("player_mob_caused_deaths",
                player -> PlayerVersusMutator.forContainer(player).toMobDeathCount()
        );
        placeholders.register("player_kdr",
                player -> decimals.apply(PlayerVersusMutator.forContainer(player).toKillDeathRatio())
        );
        placeholders.register("player_mob_kdr",
                player -> decimals.apply(PlayerVersusMutator.forContainer(player).toMobKillDeathRatio())
        );
        for (int i = 1; i <= 10; i++) {
            final int index = i;
            placeholders.register("player_recent_kill_" + index,
                    player -> player.getValue(PlayerKeys.PLAYER_KILLS)
                            .filter(list -> list.size() >= index)
                            .map(list -> list.get(index - 1))
                            .map(PlayerKill::getVictim)
                            .map(PlayerKill.Victim::getName)
                            .orElse("-")
            );
            placeholders.register("player_recent_death_" + index,
                    player -> player.getValue(PlayerKeys.PLAYER_DEATHS_KILLS)
                            .filter(list -> list.size() >= index)
                            .map(list -> list.get(index - 1))
                            .map(PlayerKill::getKiller)
                            .map(PlayerKill.Killer::getName)
                            .orElse("-")
            );
        }
    }

    private void registerPlaytimePlaceholders(PlanPlaceholders placeholders, Formatter<Long> time) {
        registerActivePlaytimePlaceholders(placeholders, time);
        registerAfkTimePlaceholders(placeholders, time);
        registerPlayerPlaytimePlaceholders(placeholders, time);
        registerServerSpecificPlaytimePlaceholders(placeholders, time);
    }

    private void registerServerSpecificPlaytimePlaceholders(PlanPlaceholders placeholders, Formatter<Long> time) {
        placeholders.register("player_server_time_active",
                player -> time.apply(SessionsMutator.forContainer(player)
                        .filterPlayedOnServer(serverInfo.getServerUUID())
                        .toActivePlaytime())
        );
        placeholders.register("player_server_time_active_raw",
                player -> SessionsMutator.forContainer(player)
                        .filterPlayedOnServer(serverInfo.getServerUUID())
                        .toActivePlaytime()
        );

        placeholders.register("player_server_time_afk",
                player -> time.apply(SessionsMutator.forContainer(player)
                        .filterPlayedOnServer(serverInfo.getServerUUID())
                        .toAfkTime())
        );
        placeholders.register("player_server_time_afk_raw",
                player -> SessionsMutator.forContainer(player)
                        .filterPlayedOnServer(serverInfo.getServerUUID())
                        .toAfkTime()
        );

        placeholders.register("player_server_time_total",
                player -> time.apply(SessionsMutator.forContainer(player)
                        .filterPlayedOnServer(serverInfo.getServerUUID())
                        .toPlaytime())
        );
        placeholders.register("player_server_time_total_raw",
                player -> SessionsMutator.forContainer(player)
                        .filterPlayedOnServer(serverInfo.getServerUUID())
                        .toPlaytime()
        );

        placeholders.register("player_server_time_day",
                player -> time.apply(SessionsMutator.forContainer(player)
                        .filterSessionsBetween(dayAgo(), now())
                        .filterPlayedOnServer(serverInfo.getServerUUID())
                        .toPlaytime())
        );
        placeholders.register("player_server_time_day_raw",
                player -> SessionsMutator.forContainer(player)
                        .filterSessionsBetween(dayAgo(), now())
                        .filterPlayedOnServer(serverInfo.getServerUUID())
                        .toPlaytime()
        );

        placeholders.register("player_server_time_week",
                player -> time.apply(SessionsMutator.forContainer(player)
                        .filterSessionsBetween(weekAgo(), now())
                        .filterPlayedOnServer(serverInfo.getServerUUID())
                        .toPlaytime())
        );
        placeholders.register("player_server_time_week_raw",
                player -> SessionsMutator.forContainer(player)
                        .filterSessionsBetween(weekAgo(), now())
                        .filterPlayedOnServer(serverInfo.getServerUUID())
                        .toPlaytime()
        );

        placeholders.register("player_server_time_month",
                player -> time.apply(SessionsMutator.forContainer(player)
                        .filterSessionsBetween(monthAgo(), now())
                        .filterPlayedOnServer(serverInfo.getServerUUID())
                        .toPlaytime())
        );
        placeholders.register("player_server_time_month_raw",
                player -> SessionsMutator.forContainer(player)
                        .filterSessionsBetween(monthAgo(), now())
                        .filterPlayedOnServer(serverInfo.getServerUUID())
                        .toPlaytime()
        );
    }

    private void registerPlayerPlaytimePlaceholders(PlanPlaceholders placeholders, Formatter<Long> time) {
        placeholders.register("player_time_total",
                player -> time.apply(SessionsMutator.forContainer(player)
                        .toPlaytime())
        );
        placeholders.register("player_time_total_raw",
                player -> SessionsMutator.forContainer(player)
                        .toPlaytime()
        );

        placeholders.register("player_time_day",
                player -> time.apply(SessionsMutator.forContainer(player)
                        .filterSessionsBetween(dayAgo(), now())
                        .toPlaytime())
        );
        placeholders.register("player_time_day_raw",
                player -> SessionsMutator.forContainer(player)
                        .filterSessionsBetween(dayAgo(), now())
                        .toPlaytime()
        );

        placeholders.register("player_time_week",
                player -> time.apply(SessionsMutator.forContainer(player)
                        .filterSessionsBetween(weekAgo(), now())
                        .toPlaytime())
        );
        placeholders.register("player_time_week_raw",
                player -> SessionsMutator.forContainer(player)
                        .filterSessionsBetween(weekAgo(), now())
                        .toPlaytime()
        );

        placeholders.register("player_time_month",
                player -> time.apply(SessionsMutator.forContainer(player)
                        .filterSessionsBetween(monthAgo(), now())
                        .toPlaytime())
        );
        placeholders.register("player_time_month_raw",
                player -> SessionsMutator.forContainer(player)
                        .filterSessionsBetween(monthAgo(), now())
                        .toPlaytime()
        );
    }

    private void registerAfkTimePlaceholders(PlanPlaceholders placeholders, Formatter<Long> time) {
        placeholders.register("player_time_afk",
                player -> time.apply(SessionsMutator.forContainer(player)
                        .toAfkTime())
        );
        placeholders.register("player_time_afk_raw",
                player -> SessionsMutator.forContainer(player)
                        .toAfkTime()
        );
    }

    private void registerActivePlaytimePlaceholders(PlanPlaceholders placeholders, Formatter<Long> time) {
        placeholders.register("player_time_active",
                player -> time.apply(SessionsMutator.forContainer(player)
                        .toActivePlaytime())
        );
        placeholders.register("player_time_active_raw",
                player -> SessionsMutator.forContainer(player)
                        .toActivePlaytime()
        );

        placeholders.register("player_time_active_day",
                player -> time.apply(SessionsMutator.forContainer(player)
                        .filterSessionsBetween(dayAgo(), now())
                        .toActivePlaytime())
        );
        placeholders.register("player_time_active_day_raw",
                player -> SessionsMutator.forContainer(player)
                        .filterSessionsBetween(dayAgo(), now())
                        .toActivePlaytime()
        );

        placeholders.register("player_time_active_week",
                player -> time.apply(SessionsMutator.forContainer(player)
                        .filterSessionsBetween(weekAgo(), now())
                        .toActivePlaytime())
        );
        placeholders.register("player_time_active_week_raw",
                player -> SessionsMutator.forContainer(player)
                        .filterSessionsBetween(weekAgo(), now())
                        .toActivePlaytime()
        );

        placeholders.register("player_time_active_month",
                player -> time.apply(SessionsMutator.forContainer(player)
                        .filterSessionsBetween(monthAgo(), now())
                        .toActivePlaytime())
        );
        placeholders.register("player_time_active_month_raw",
                player -> SessionsMutator.forContainer(player)
                        .filterSessionsBetween(monthAgo(), now())
                        .toActivePlaytime()
        );
    }

    @NotNull
    private Optional<Long> getActiveSessionLength(PlayerContainer player) {
        SessionCache.refreshActiveSessionsState();
        return SessionCache.getCachedSession(player.getUnsafe(PlayerKeys.UUID))
                .map(ActiveSession::toFinishedSessionFromStillActive)
                .map(FinishedSession::getLength);
    }
}
