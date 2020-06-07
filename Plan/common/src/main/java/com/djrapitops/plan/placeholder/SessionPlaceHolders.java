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

import com.djrapitops.plan.delivery.domain.DateHolder;
import com.djrapitops.plan.delivery.domain.DateObj;
import com.djrapitops.plan.delivery.formatting.Formatter;
import com.djrapitops.plan.delivery.formatting.Formatters;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.analysis.PlayerCountQueries;
import com.djrapitops.plan.storage.database.queries.objects.KillQueries;
import com.djrapitops.plan.storage.database.queries.objects.PingQueries;
import com.djrapitops.plan.storage.database.queries.objects.SessionQueries;
import com.djrapitops.plan.storage.database.queries.objects.TPSQueries;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.Serializable;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static com.djrapitops.plan.utilities.MiscUtils.*;

/**
 * Placeholders about sessions.
 *
 * @author aidn5, Rsl1122
 */
@Singleton
public class SessionPlaceHolders implements Placeholders {

    private final PlanConfig config;
    private final DBSystem dbSystem;
    private final ServerInfo serverInfo;
    private final Formatters formatters;

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

    @Override
    public void register(
            PlanPlaceholders placeholders
    ) {
        int tzOffsetMs = config.getTimeZone().getOffset(System.currentTimeMillis());
        Formatter<Long> timeAmount = formatters.timeAmount();
        Formatter<DateHolder> year = formatters.year();
        Formatter<Double> decimals = formatters.decimals();
        Database database = dbSystem.getDatabase();
        UUID serverUUID = serverInfo.getServerUUID();

        placeholders.registerStatic("sessions_play_time_total",
                () -> timeAmount.apply(database.query(SessionQueries.playtime(0L, now(), serverUUID))));

        placeholders.registerStatic("sessions_play_time_day",
                () -> timeAmount.apply(database.query(SessionQueries.playtime(dayAgo(), now(), serverUUID))));

        placeholders.registerStatic("sessions_play_time_week",
                () -> timeAmount.apply(database.query(SessionQueries.playtime(weekAgo(), now(), serverUUID))));

        placeholders.registerStatic("sessions_play_time_month",
                () -> timeAmount.apply(database.query(SessionQueries.playtime(monthAgo(), now(), serverUUID))));

        placeholders.registerStatic("sessions_active_time_total",
                () -> timeAmount.apply(database.query(SessionQueries.activePlaytime(0L, now(), serverUUID))));

        placeholders.registerStatic("sessions_active_time_day",
                () -> timeAmount.apply(database.query(SessionQueries.activePlaytime(dayAgo(), now(), serverUUID))));

        placeholders.registerStatic("sessions_active_time_week",
                () -> timeAmount.apply(database.query(SessionQueries.activePlaytime(weekAgo(), now(), serverUUID))));

        placeholders.registerStatic("sessions_active_time_month",
                () -> timeAmount.apply(database.query(SessionQueries.activePlaytime(monthAgo(), now(), serverUUID))));

        placeholders.registerStatic("sessions_afk_time_total",
                () -> timeAmount.apply(database.query(SessionQueries.afkTime(0L, now(), serverUUID))));

        placeholders.registerStatic("sessions_afk_time_day",
                () -> timeAmount.apply(database.query(SessionQueries.afkTime(dayAgo(), now(), serverUUID))));

        placeholders.registerStatic("sessions_afk_time_week",
                () -> timeAmount.apply(database.query(SessionQueries.afkTime(weekAgo(), now(), serverUUID))));

        placeholders.registerStatic("sessions_afk_time_month",
                () -> timeAmount.apply(database.query(SessionQueries.afkTime(monthAgo(), now(), serverUUID))));

        Supplier<Serializable> uniquePlayers = () -> database.query(PlayerCountQueries.newPlayerCount(0L, now(), serverUUID));
        placeholders.registerStatic("sessions_unique_players_total", uniquePlayers);
        placeholders.registerStatic("sessions_new_players_total", uniquePlayers);

        placeholders.registerStatic("sessions_unique_players_day",
                () -> database.query(PlayerCountQueries.uniquePlayerCount(dayAgo(), now(), serverUUID)));

        placeholders.registerStatic("sessions_unique_players_today",
                () -> database.query(PlayerCountQueries.uniquePlayerCounts(dayAgo(), now(), config.getTimeZone().getOffset(now()), serverUUID))
                        .lastEntry().getValue());

        placeholders.registerStatic("sessions_unique_players_week",
                () -> database.query(PlayerCountQueries.uniquePlayerCount(weekAgo(), now(), serverUUID)));

        placeholders.registerStatic("sessions_unique_players_month",
                () -> database.query(PlayerCountQueries.uniquePlayerCount(monthAgo(), now(), serverUUID)));

        placeholders.registerStatic("sessions_players_death_total",
                () -> database.query(KillQueries.deathCount(0L, now(), serverUUID)));

        placeholders.registerStatic("sessions_players_death_day",
                () -> database.query(KillQueries.deathCount(dayAgo(), now(), serverUUID)));

        placeholders.registerStatic("sessions_players_death_week",
                () -> database.query(KillQueries.deathCount(weekAgo(), now(), serverUUID)));

        placeholders.registerStatic("sessions_players_death_month",
                () -> database.query(KillQueries.deathCount(monthAgo(), now(), serverUUID)));

        placeholders.registerStatic("sessions_players_kill_total",
                () -> database.query(KillQueries.playerKillCount(0L, now(), serverUUID)));

        placeholders.registerStatic("sessions_players_kill_day",
                () -> database.query(KillQueries.playerKillCount(dayAgo(), now(), serverUUID)));

        placeholders.registerStatic("sessions_players_kill_week",
                () -> database.query(KillQueries.playerKillCount(weekAgo(), now(), serverUUID)));

        placeholders.registerStatic("sessions_players_kill_month",
                () -> database.query(KillQueries.playerKillCount(monthAgo(), now(), serverUUID)));

        placeholders.registerStatic("sessions_mob_kill_total",
                () -> database.query(KillQueries.mobKillCount(0L, now(), serverUUID)));

        placeholders.registerStatic("sessions_mob_kill_day",
                () -> database.query(KillQueries.mobKillCount(dayAgo(), now(), serverUUID)));

        placeholders.registerStatic("sessions_mob_kill_week",
                () -> database.query(KillQueries.mobKillCount(weekAgo(), now(), serverUUID)));

        placeholders.registerStatic("sessions_mob_kill_month",
                () -> database.query(KillQueries.mobKillCount(monthAgo(), now(), serverUUID)));

        placeholders.registerStatic("sessions_average_session_length_total",
                () -> getPlaytime(database, 0L, now(), serverUUID, timeAmount));

        placeholders.registerStatic("sessions_average_session_length_day",
                () -> getPlaytime(database, dayAgo(), now(), serverUUID, timeAmount));

        placeholders.registerStatic("sessions_average_session_length_week",
                () -> getPlaytime(database, weekAgo(), now(), serverUUID, timeAmount));

        placeholders.registerStatic("sessions_average_session_length_month",
                () -> getPlaytime(database, monthAgo(), now(), serverUUID, timeAmount));

        placeholders.registerStatic("sessions_average_unique_players_total",
                () -> database.query(PlayerCountQueries.averageUniquePlayerCount(0L, now(), tzOffsetMs, serverUUID)));

        placeholders.registerStatic("sessions_average_unique_players_day",
                () -> database.query(PlayerCountQueries.averageUniquePlayerCount(dayAgo(), now(), tzOffsetMs, serverUUID)));

        placeholders.registerStatic("sessions_average_unique_players_week",
                () -> database.query(PlayerCountQueries.averageUniquePlayerCount(weekAgo(), now(), tzOffsetMs, serverUUID)));

        placeholders.registerStatic("sessions_average_unique_players_month",
                () -> database.query(PlayerCountQueries.averageUniquePlayerCount(monthAgo(), now(), tzOffsetMs, serverUUID)));

        placeholders.registerStatic("sessions_new_players_day",
                () -> database.query(PlayerCountQueries.newPlayerCount(dayAgo(), now(), serverUUID)));

        placeholders.registerStatic("sessions_new_players_week",
                () -> database.query(PlayerCountQueries.newPlayerCount(weekAgo(), now(), serverUUID)));

        placeholders.registerStatic("sessions_new_players_month",
                () -> database.query(PlayerCountQueries.newPlayerCount(monthAgo(), now(), serverUUID)));

        placeholders.registerStatic("ping_total",
                () -> decimals.apply(database.query(PingQueries.averagePing(0L, now(), serverUUID))) + " ms");

        placeholders.registerStatic("ping_day",
                () -> decimals.apply(database.query(PingQueries.averagePing(dayAgo(), now(), serverUUID))) + " ms");

        placeholders.registerStatic("ping_week",
                () -> decimals.apply(database.query(PingQueries.averagePing(weekAgo(), now(), serverUUID))) + " ms");

        placeholders.registerStatic("ping_month",
                () -> decimals.apply(database.query(PingQueries.averagePing(monthAgo(), now(), serverUUID))) + " ms");

        placeholders.registerStatic("sessions_peak_count",
                () -> database.query(TPSQueries.fetchAllTimePeakPlayerCount(serverUUID)).map(DateObj::getValue).orElse(0));

        placeholders.registerStatic("sessions_peak_date",
                () -> database.query(TPSQueries.fetchAllTimePeakPlayerCount(serverUUID)).map(year).orElse("-"));

        placeholders.registerStatic("sessions_recent_peak_count",
                () -> database.query(TPSQueries.fetchPeakPlayerCount(serverUUID, now() - TimeUnit.DAYS.toMillis(2L))).map(DateObj::getValue).orElse(0));

        placeholders.registerStatic("sessions_recent_peak_date",
                () -> database.query(TPSQueries.fetchPeakPlayerCount(serverUUID, now() - TimeUnit.DAYS.toMillis(2L))).map(year).orElse("-"));
    }

    private static String getPlaytime(Database database, long after, long before, UUID serverUUID, Formatter<Long> timeAmount) {
        Long playtime = database.query(SessionQueries.playtime(after, before, serverUUID));
        Long sessionCount = database.query(SessionQueries.sessionCount(after, before, serverUUID));
        return timeAmount.apply(sessionCount != 0 ? playtime / sessionCount : playtime);
    }
}
