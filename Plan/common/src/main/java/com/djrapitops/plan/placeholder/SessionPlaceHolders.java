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

import com.djrapitops.plan.commands.use.Arguments;
import com.djrapitops.plan.delivery.domain.DateHolder;
import com.djrapitops.plan.delivery.domain.DateObj;
import com.djrapitops.plan.delivery.formatting.Formatter;
import com.djrapitops.plan.delivery.formatting.Formatters;
import com.djrapitops.plan.identification.Server;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.analysis.PlayerCountQueries;
import com.djrapitops.plan.storage.database.queries.objects.*;
import com.djrapitops.plan.utilities.dev.Untrusted;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.djrapitops.plan.utilities.MiscUtils.*;

/**
 * Placeholders about sessions.
 *
 * @author aidn5, AuroraLS3
 */
@Singleton
public class SessionPlaceHolders implements Placeholders {

    private final PlanConfig config;
    private final DBSystem dbSystem;
    private final ServerInfo serverInfo;
    private final Formatters formatters;

    private Formatter<Long> timeAmount;
    private Formatter<DateHolder> year;
    private Formatter<Double> decimals;
    private Database database;

    @Inject
    public SessionPlaceHolders(
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

    private static String getPlaytime(Database database, long after, long before, ServerUUID serverUUID, Formatter<Long> timeAmount) {
        Long playtime = database.query(SessionQueries.playtime(after, before, serverUUID));
        Long sessionCount = database.query(SessionQueries.sessionCount(after, before, serverUUID));
        return timeAmount.apply(sessionCount != 0 ? playtime / sessionCount : playtime);
    }


    private static String getPlaytime(Database database, long after, long before, Formatter<Long> timeAmount) {
        Long playtime = database.query(SessionQueries.playtime(after, before));
        Long sessionCount = database.query(SessionQueries.sessionCount(after, before));
        return timeAmount.apply(sessionCount != 0 ? playtime / sessionCount : playtime);
    }

    @Override
    public void register(
            PlanPlaceholders placeholders
    ) {
        int tzOffsetMs = config.getTimeZone().getOffset(System.currentTimeMillis());
        timeAmount = formatters.timeAmount();
        year = formatters.year();
        decimals = formatters.decimals();
        database = dbSystem.getDatabase();

        registerServerPlaytime(placeholders);
        registerNetworkPlaytime(placeholders);
        registerServerActivePlaytime(placeholders);
        registerNetworkActivePlaytime(placeholders);
        registerServerAfkTime(placeholders);
        registerNetworkAfkTime(placeholders);

        registerServerPve(placeholders);
        registerSessionLength(placeholders);

        registerServerUniquePlayers(placeholders);
        registerNetworkUniquePlayers(placeholders);
        registerAverageUniquePlayer(placeholders, tzOffsetMs);
        registerNewPlayer(placeholders);

        registerPing(placeholders);
        registerServerPeakCounts(placeholders);
    }

    private void registerServerPeakCounts(PlanPlaceholders placeholders) {
        placeholders.registerStatic("sessions_peak_count",
                parameters -> database.query(TPSQueries.fetchAllTimePeakPlayerCount(getServerUUID(parameters))).map(DateObj::getValue).orElse(0));
        placeholders.registerStatic("sessions_peak_date",
                parameters -> database.query(TPSQueries.fetchAllTimePeakPlayerCount(getServerUUID(parameters))).map(year).orElse("-"));
        placeholders.registerStatic("sessions_recent_peak_count",
                parameters -> database.query(TPSQueries.fetchPeakPlayerCount(getServerUUID(parameters), now() - TimeUnit.DAYS.toMillis(2L))).map(DateObj::getValue).orElse(0));
        placeholders.registerStatic("sessions_recent_peak_date",
                parameters -> database.query(TPSQueries.fetchPeakPlayerCount(getServerUUID(parameters), now() - TimeUnit.DAYS.toMillis(2L))).map(year).orElse("-"));
    }

    private void registerPing(PlanPlaceholders placeholders) {
        placeholders.registerStatic("ping_total",
                parameters -> decimals.apply(database.query(PingQueries.averagePing(0L, now(), getServerUUID(parameters)))) + " ms");
        placeholders.registerStatic("ping_day",
                parameters -> decimals.apply(database.query(PingQueries.averagePing(dayAgo(), now(), getServerUUID(parameters)))) + " ms");
        placeholders.registerStatic("ping_week",
                parameters -> decimals.apply(database.query(PingQueries.averagePing(weekAgo(), now(), getServerUUID(parameters)))) + " ms");
        placeholders.registerStatic("ping_month",
                parameters -> decimals.apply(database.query(PingQueries.averagePing(monthAgo(), now(), getServerUUID(parameters)))) + " ms");

        placeholders.registerStatic("network_ping_total",
                parameters -> decimals.apply(database.query(PingQueries.averagePing(0L, now()))) + " ms");
        placeholders.registerStatic("network_ping_day",
                parameters -> decimals.apply(database.query(PingQueries.averagePing(dayAgo(), now()))) + " ms");
        placeholders.registerStatic("network_ping_week",
                parameters -> decimals.apply(database.query(PingQueries.averagePing(weekAgo(), now()))) + " ms");
        placeholders.registerStatic("network_ping_month",
                parameters -> decimals.apply(database.query(PingQueries.averagePing(monthAgo(), now()))) + " ms");
    }

    private void registerNewPlayer(PlanPlaceholders placeholders) {
        placeholders.registerStatic("sessions_new_players_day",
                parameters -> database.query(PlayerCountQueries.newPlayerCount(dayAgo(), now(), getServerUUID(parameters))));
        placeholders.registerStatic("sessions_new_players_week",
                parameters -> database.query(PlayerCountQueries.newPlayerCount(weekAgo(), now(), getServerUUID(parameters))));
        placeholders.registerStatic("sessions_new_players_month",
                parameters -> database.query(PlayerCountQueries.newPlayerCount(monthAgo(), now(), getServerUUID(parameters))));

        placeholders.registerStatic("network_sessions_new_players_day",
                parameters -> database.query(PlayerCountQueries.newPlayerCount(dayAgo(), now())));
        placeholders.registerStatic("network_sessions_new_players_week",
                parameters -> database.query(PlayerCountQueries.newPlayerCount(weekAgo(), now())));
        placeholders.registerStatic("network_sessions_new_players_month",
                parameters -> database.query(PlayerCountQueries.newPlayerCount(monthAgo(), now())));
    }

    private void registerAverageUniquePlayer(PlanPlaceholders placeholders, int tzOffsetMs) {
        placeholders.registerStatic("sessions_average_unique_players_total",
                parameters -> database.query(PlayerCountQueries.averageUniquePlayerCount(0L, now(), tzOffsetMs, getServerUUID(parameters))));
        placeholders.registerStatic("sessions_average_unique_players_day",
                parameters -> database.query(PlayerCountQueries.averageUniquePlayerCount(dayAgo(), now(), tzOffsetMs, getServerUUID(parameters))));
        placeholders.registerStatic("sessions_average_unique_players_week",
                parameters -> database.query(PlayerCountQueries.averageUniquePlayerCount(weekAgo(), now(), tzOffsetMs, getServerUUID(parameters))));
        placeholders.registerStatic("sessions_average_unique_players_month",
                parameters -> database.query(PlayerCountQueries.averageUniquePlayerCount(monthAgo(), now(), tzOffsetMs, getServerUUID(parameters))));


        placeholders.registerStatic("network_sessions_average_unique_players_total",
                parameters -> database.query(PlayerCountQueries.averageUniquePlayerCount(0L, now(), tzOffsetMs)));
        placeholders.registerStatic("network_sessions_average_unique_players_day",
                parameters -> database.query(PlayerCountQueries.averageUniquePlayerCount(dayAgo(), now(), tzOffsetMs)));
        placeholders.registerStatic("network_sessions_average_unique_players_week",
                parameters -> database.query(PlayerCountQueries.averageUniquePlayerCount(weekAgo(), now(), tzOffsetMs)));
        placeholders.registerStatic("network_sessions_average_unique_players_month",
                parameters -> database.query(PlayerCountQueries.averageUniquePlayerCount(monthAgo(), now(), tzOffsetMs)));
    }

    private void registerSessionLength(PlanPlaceholders placeholders) {
        placeholders.registerStatic("sessions_average_session_length_total",
                parameters -> getPlaytime(database, 0L, now(), getServerUUID(parameters), timeAmount));
        placeholders.registerStatic("sessions_average_session_length_day",
                parameters -> getPlaytime(database, dayAgo(), now(), getServerUUID(parameters), timeAmount));
        placeholders.registerStatic("sessions_average_session_length_week",
                parameters -> getPlaytime(database, weekAgo(), now(), getServerUUID(parameters), timeAmount));
        placeholders.registerStatic("sessions_average_session_length_month",
                parameters -> getPlaytime(database, monthAgo(), now(), getServerUUID(parameters), timeAmount));

        placeholders.registerStatic("network_sessions_average_session_length_total",
                parameters -> getPlaytime(database, 0L, now(), timeAmount));
        placeholders.registerStatic("network_sessions_average_session_length_day",
                parameters -> getPlaytime(database, dayAgo(), now(), timeAmount));
        placeholders.registerStatic("network_sessions_average_session_length_week",
                parameters -> getPlaytime(database, weekAgo(), now(), timeAmount));
        placeholders.registerStatic("network_sessions_average_session_length_month",
                parameters -> getPlaytime(database, monthAgo(), now(), timeAmount));
    }

    private void registerNetworkUniquePlayers(PlanPlaceholders placeholders) {
        PlanPlaceholders.StaticPlaceholderLoader networkUniquePlayers = parameters -> database.query(PlayerCountQueries.newPlayerCount(0L, now()));
        placeholders.registerStatic("network_sessions_unique_players_total", networkUniquePlayers);
        placeholders.registerStatic("network_sessions_new_players_total", networkUniquePlayers);

        placeholders.registerStatic("network_sessions_unique_players_day",
                parameters -> database.query(PlayerCountQueries.uniquePlayerCount(dayAgo(), now())));
        placeholders.registerStatic("network_sessions_unique_players_today",
                parameters -> {
                    NavigableMap<Long, Integer> playerCounts = database.query(PlayerCountQueries.uniquePlayerCounts(dayAgo(), now(), config.getTimeZone().getOffset(now())));
                    return playerCounts.isEmpty() ? 0 : playerCounts.lastEntry().getValue();
                });
        placeholders.registerStatic("network_sessions_unique_players_week",
                parameters -> database.query(PlayerCountQueries.uniquePlayerCount(weekAgo(), now())));
        placeholders.registerStatic("network_sessions_unique_players_month",
                parameters -> database.query(PlayerCountQueries.uniquePlayerCount(monthAgo(), now())));
    }

    private void registerServerUniquePlayers(PlanPlaceholders placeholders) {
        PlanPlaceholders.StaticPlaceholderLoader uniquePlayers = parameters -> database.query(PlayerCountQueries.newPlayerCount(0L, now(), getServerUUID(parameters)));
        placeholders.registerStatic("sessions_unique_players_total", uniquePlayers);
        placeholders.registerStatic("sessions_new_players_total", uniquePlayers);

        placeholders.registerStatic("sessions_unique_players_day",
                parameters -> database.query(PlayerCountQueries.uniquePlayerCount(dayAgo(), now(), getServerUUID(parameters))));
        placeholders.registerStatic("sessions_unique_players_today",
                parameters -> {
                    NavigableMap<Long, Integer> playerCounts = database.query(PlayerCountQueries.uniquePlayerCounts(dayAgo(), now(), config.getTimeZone().getOffset(now()), getServerUUID(parameters)));
                    return playerCounts.isEmpty() ? 0 : playerCounts.lastEntry().getValue();
                });
        placeholders.registerStatic("sessions_unique_players_week",
                parameters -> database.query(PlayerCountQueries.uniquePlayerCount(weekAgo(), now(), getServerUUID(parameters))));
        placeholders.registerStatic("sessions_unique_players_month",
                parameters -> database.query(PlayerCountQueries.uniquePlayerCount(monthAgo(), now(), getServerUUID(parameters))));
    }

    private void registerServerPve(PlanPlaceholders placeholders) {
        placeholders.registerStatic("sessions_players_death_total",
                parameters -> database.query(KillQueries.deathCount(0L, now(), getServerUUID(parameters))));
        placeholders.registerStatic("sessions_players_death_day",
                parameters -> database.query(KillQueries.deathCount(dayAgo(), now(), getServerUUID(parameters))));
        placeholders.registerStatic("sessions_players_death_week",
                parameters -> database.query(KillQueries.deathCount(weekAgo(), now(), getServerUUID(parameters))));
        placeholders.registerStatic("sessions_players_death_month",
                parameters -> database.query(KillQueries.deathCount(monthAgo(), now(), getServerUUID(parameters))));

        placeholders.registerStatic("sessions_players_kill_total",
                parameters -> database.query(KillQueries.playerKillCount(0L, now(), getServerUUID(parameters))));
        placeholders.registerStatic("sessions_players_kill_day",
                parameters -> database.query(KillQueries.playerKillCount(dayAgo(), now(), getServerUUID(parameters))));
        placeholders.registerStatic("sessions_players_kill_week",
                parameters -> database.query(KillQueries.playerKillCount(weekAgo(), now(), getServerUUID(parameters))));
        placeholders.registerStatic("sessions_players_kill_month",
                parameters -> database.query(KillQueries.playerKillCount(monthAgo(), now(), getServerUUID(parameters))));

        placeholders.registerStatic("sessions_mob_kill_total",
                parameters -> database.query(KillQueries.mobKillCount(0L, now(), getServerUUID(parameters))));
        placeholders.registerStatic("sessions_mob_kill_day",
                parameters -> database.query(KillQueries.mobKillCount(dayAgo(), now(), getServerUUID(parameters))));
        placeholders.registerStatic("sessions_mob_kill_week",
                parameters -> database.query(KillQueries.mobKillCount(weekAgo(), now(), getServerUUID(parameters))));
        placeholders.registerStatic("sessions_mob_kill_month",
                parameters -> database.query(KillQueries.mobKillCount(monthAgo(), now(), getServerUUID(parameters))));
    }

    private void registerNetworkAfkTime(PlanPlaceholders placeholders) {
        placeholders.registerStatic("network_sessions_afk_time_total",
                parameters -> timeAmount.apply(database.query(SessionQueries.afkTime(0L, now()))));
        placeholders.registerStatic("network_sessions_afk_time_total_raw",
                parameters -> database.query(SessionQueries.afkTime(0L, now())));
        placeholders.registerStatic("network_sessions_afk_time_day",
                parameters -> timeAmount.apply(database.query(SessionQueries.afkTime(dayAgo(), now()))));
        placeholders.registerStatic("network_sessions_afk_time_day_raw",
                parameters -> database.query(SessionQueries.afkTime(dayAgo(), now())));
        placeholders.registerStatic("network_sessions_afk_time_week",
                parameters -> timeAmount.apply(database.query(SessionQueries.afkTime(weekAgo(), now()))));
        placeholders.registerStatic("network_sessions_afk_time_week_raw",
                parameters -> database.query(SessionQueries.afkTime(weekAgo(), now())));
        placeholders.registerStatic("network_sessions_afk_time_month",
                parameters -> timeAmount.apply(database.query(SessionQueries.afkTime(monthAgo(), now()))));
        placeholders.registerStatic("network_sessions_afk_time_month_raw",
                parameters -> database.query(SessionQueries.afkTime(monthAgo(), now())));
    }

    private void registerServerAfkTime(PlanPlaceholders placeholders) {
        placeholders.registerStatic("sessions_afk_time_total",
                parameters -> timeAmount.apply(database.query(SessionQueries.afkTime(0L, now(), getServerUUID(parameters)))));
        placeholders.registerStatic("sessions_afk_time_total_raw",
                parameters -> database.query(SessionQueries.afkTime(0L, now(), getServerUUID(parameters))));
        placeholders.registerStatic("sessions_afk_time_day",
                parameters -> timeAmount.apply(database.query(SessionQueries.afkTime(dayAgo(), now(), getServerUUID(parameters)))));
        placeholders.registerStatic("sessions_afk_time_day_raw",
                parameters -> database.query(SessionQueries.afkTime(dayAgo(), now(), getServerUUID(parameters))));
        placeholders.registerStatic("sessions_afk_time_week",
                parameters -> timeAmount.apply(database.query(SessionQueries.afkTime(weekAgo(), now(), getServerUUID(parameters)))));
        placeholders.registerStatic("sessions_afk_time_week_raw",
                parameters -> database.query(SessionQueries.afkTime(weekAgo(), now(), getServerUUID(parameters))));
        placeholders.registerStatic("sessions_afk_time_month",
                parameters -> timeAmount.apply(database.query(SessionQueries.afkTime(monthAgo(), now(), getServerUUID(parameters)))));
        placeholders.registerStatic("sessions_afk_time_month_raw",
                parameters -> database.query(SessionQueries.afkTime(monthAgo(), now(), getServerUUID(parameters))));
    }

    private void registerNetworkActivePlaytime(PlanPlaceholders placeholders) {
        placeholders.registerStatic("network_sessions_active_time_total",
                parameters -> timeAmount.apply(database.query(SessionQueries.activePlaytime(0L, now()))));
        placeholders.registerStatic("network_sessions_active_time_total_raw",
                parameters -> database.query(SessionQueries.activePlaytime(0L, now())));
        placeholders.registerStatic("network_sessions_active_time_day",
                parameters -> timeAmount.apply(database.query(SessionQueries.activePlaytime(dayAgo(), now()))));
        placeholders.registerStatic("network_sessions_active_time_day_raw",
                parameters -> database.query(SessionQueries.activePlaytime(dayAgo(), now())));
        placeholders.registerStatic("network_sessions_active_time_week",
                parameters -> timeAmount.apply(database.query(SessionQueries.activePlaytime(weekAgo(), now()))));
        placeholders.registerStatic("network_sessions_active_time_week_raw",
                parameters -> database.query(SessionQueries.activePlaytime(weekAgo(), now())));
        placeholders.registerStatic("network_sessions_active_time_month",
                parameters -> timeAmount.apply(database.query(SessionQueries.activePlaytime(monthAgo(), now()))));
        placeholders.registerStatic("network_sessions_active_time_month_raw",
                parameters -> database.query(SessionQueries.activePlaytime(monthAgo(), now())));
    }

    private void registerServerActivePlaytime(PlanPlaceholders placeholders) {
        placeholders.registerStatic("sessions_active_time_total",
                parameters -> timeAmount.apply(database.query(SessionQueries.activePlaytime(0L, now(), getServerUUID(parameters)))));
        placeholders.registerStatic("sessions_active_time_total_raw",
                parameters -> database.query(SessionQueries.activePlaytime(0L, now(), getServerUUID(parameters))));
        placeholders.registerStatic("sessions_active_time_day",
                parameters -> timeAmount.apply(database.query(SessionQueries.activePlaytime(dayAgo(), now(), getServerUUID(parameters)))));
        placeholders.registerStatic("sessions_active_time_day_raw",
                parameters -> database.query(SessionQueries.activePlaytime(dayAgo(), now(), getServerUUID(parameters))));
        placeholders.registerStatic("sessions_active_time_week",
                parameters -> timeAmount.apply(database.query(SessionQueries.activePlaytime(weekAgo(), now(), getServerUUID(parameters)))));
        placeholders.registerStatic("sessions_active_time_week_raw",
                parameters -> database.query(SessionQueries.activePlaytime(weekAgo(), now(), getServerUUID(parameters))));
        placeholders.registerStatic("sessions_active_time_month",
                parameters -> timeAmount.apply(database.query(SessionQueries.activePlaytime(monthAgo(), now(), getServerUUID(parameters)))));
        placeholders.registerStatic("sessions_active_time_month_raw",
                parameters -> database.query(SessionQueries.activePlaytime(monthAgo(), now(), getServerUUID(parameters))));
    }

    private void registerNetworkPlaytime(PlanPlaceholders placeholders) {
        placeholders.registerStatic("network_sessions_play_time_total",
                parameters -> timeAmount.apply(database.query(SessionQueries.playtime(0L, now()))));
        placeholders.registerStatic("network_sessions_play_time_total_raw",
                parameters -> database.query(SessionQueries.playtime(0L, now())));
        placeholders.registerStatic("network_sessions_play_time_day",
                parameters -> timeAmount.apply(database.query(SessionQueries.playtime(dayAgo(), now()))));
        placeholders.registerStatic("network_sessions_play_time_day_raw",
                parameters -> database.query(SessionQueries.playtime(dayAgo(), now())));
        placeholders.registerStatic("network_sessions_play_time_week",
                parameters -> timeAmount.apply(database.query(SessionQueries.playtime(weekAgo(), now()))));
        placeholders.registerStatic("network_sessions_play_time_week_raw",
                parameters -> database.query(SessionQueries.playtime(weekAgo(), now())));
        placeholders.registerStatic("network_sessions_play_time_month",
                parameters -> timeAmount.apply(database.query(SessionQueries.playtime(monthAgo(), now()))));
        placeholders.registerStatic("network_sessions_play_time_month_raw",
                parameters -> database.query(SessionQueries.playtime(monthAgo(), now())));
    }

    private void registerServerPlaytime(PlanPlaceholders placeholders) {
        placeholders.registerStatic("sessions_play_time_total",
                parameters -> timeAmount.apply(database.query(SessionQueries.playtime(0L, now(), getServerUUID(parameters)))));
        placeholders.registerStatic("sessions_play_time_total_raw",
                parameters -> database.query(SessionQueries.playtime(0L, now(), getServerUUID(parameters))));
        placeholders.registerStatic("sessions_play_time_day",
                parameters -> timeAmount.apply(database.query(SessionQueries.playtime(dayAgo(), now(), getServerUUID(parameters)))));
        placeholders.registerStatic("sessions_play_time_day_raw",
                parameters -> database.query(SessionQueries.playtime(dayAgo(), now(), getServerUUID(parameters))));
        placeholders.registerStatic("sessions_play_time_week",
                parameters -> timeAmount.apply(database.query(SessionQueries.playtime(weekAgo(), now(), getServerUUID(parameters)))));
        placeholders.registerStatic("sessions_play_time_week_raw",
                parameters -> database.query(SessionQueries.playtime(weekAgo(), now(), getServerUUID(parameters))));
        placeholders.registerStatic("sessions_play_time_month",
                parameters -> timeAmount.apply(database.query(SessionQueries.playtime(monthAgo(), now(), getServerUUID(parameters)))));
        placeholders.registerStatic("sessions_play_time_month_raw",
                parameters -> database.query(SessionQueries.playtime(monthAgo(), now(), getServerUUID(parameters))));
    }

    private ServerUUID getServerUUID(@Untrusted Arguments parameters) {
        return parameters.get(0)
                .flatMap(this::getServerUUIDForServerIdentifier)
                .orElseGet(serverInfo::getServerUUID);
    }

    private Optional<ServerUUID> getServerUUIDForServerIdentifier(@Untrusted String serverIdentifier) {
        return dbSystem.getDatabase().query(ServerQueries.fetchServerMatchingIdentifier(serverIdentifier))
                .map(Server::getUuid);
    }
}
