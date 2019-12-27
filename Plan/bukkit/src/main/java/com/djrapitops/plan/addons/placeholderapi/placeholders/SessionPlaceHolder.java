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
package com.djrapitops.plan.addons.placeholderapi.placeholders;

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
import org.bukkit.entity.Player;

import java.io.Serializable;
import java.util.UUID;

/**
 * Placeholders about a sessions.
 *
 * @author aidn5, Rsl1122
 */
public class SessionPlaceHolder extends AbstractPlanPlaceHolder {

    private final DBSystem dbSystem;
    private Formatter<Long> timeAmount;
    private int tzOffsetMs;
    private Formatter<DateHolder> year;

    public SessionPlaceHolder(
            PlanConfig config,
            DBSystem dbSystem,
            ServerInfo serverInfo,
            Formatters formatters
    ) {
        super(serverInfo);
        this.dbSystem = dbSystem;

        tzOffsetMs = config.getTimeZone().getOffset(System.currentTimeMillis());
        timeAmount = formatters.timeAmount();
        year = formatters.year();
    }

    @Override
    public String onPlaceholderRequest(Player p, String params) throws Exception {
        Serializable got = get(params);
        return got != null ? got.toString() : null;
    }

    // Checkstyle.OFF: CyclomaticComplexity

    public Serializable get(String params) {
        Database database = dbSystem.getDatabase();
        UUID serverUUID = serverUUID();

        switch (params.toLowerCase()) {

            case "sessions_play_time_total":
                return timeAmount.apply(database.query(SessionQueries.playtime(0L, now(), serverUUID)));
            case "sessions_play_time_day":
                return timeAmount.apply(database.query(SessionQueries.playtime(dayAgo(), now(), serverUUID)));
            case "sessions_play_time_week":
                return timeAmount.apply(database.query(SessionQueries.playtime(weekAgo(), now(), serverUUID)));
            case "sessions_play_time_month":
                return timeAmount.apply(database.query(SessionQueries.playtime(monthAgo(), now(), serverUUID)));

            case "sessions_active_time_total":
                return timeAmount.apply(database.query(SessionQueries.activePlaytime(0L, now(), serverUUID)));
            case "sessions_active_time_day":
                return timeAmount.apply(database.query(SessionQueries.activePlaytime(dayAgo(), now(), serverUUID)));
            case "sessions_active_time_week":
                return timeAmount.apply(database.query(SessionQueries.activePlaytime(weekAgo(), now(), serverUUID)));
            case "sessions_active_time_month":
                return timeAmount.apply(database.query(SessionQueries.activePlaytime(monthAgo(), now(), serverUUID)));

            case "sessions_afk_time_total":
                return timeAmount.apply(database.query(SessionQueries.afkTime(0L, now(), serverUUID)));
            case "sessions_afk_time_day":
                return timeAmount.apply(database.query(SessionQueries.afkTime(dayAgo(), now(), serverUUID)));
            case "sessions_afk_time_week":
                return timeAmount.apply(database.query(SessionQueries.afkTime(weekAgo(), now(), serverUUID)));
            case "sessions_afk_time_month":
                return timeAmount.apply(database.query(SessionQueries.afkTime(monthAgo(), now(), serverUUID)));

            case "sessions_unique_players_total":
            case "sessions_new_players_total":
                return database.query(PlayerCountQueries.newPlayerCount(0L, now(), serverUUID));
            case "sessions_unique_players_day":
                return database.query(PlayerCountQueries.uniquePlayerCount(dayAgo(), now(), serverUUID));
            case "sessions_unique_players_week":
                return database.query(PlayerCountQueries.uniquePlayerCount(weekAgo(), now(), serverUUID));
            case "sessions_unique_players_month":
                return database.query(PlayerCountQueries.uniquePlayerCount(monthAgo(), now(), serverUUID));

            case "sessions_players_death_total":
                return database.query(KillQueries.deathCount(0L, now(), serverUUID));
            case "sessions_players_death_day":
                return database.query(KillQueries.deathCount(dayAgo(), now(), serverUUID));
            case "sessions_players_death_week":
                return database.query(KillQueries.deathCount(weekAgo(), now(), serverUUID));
            case "sessions_players_death_month":
                return database.query(KillQueries.deathCount(monthAgo(), now(), serverUUID));

            case "sessions_players_kill_total":
                return database.query(KillQueries.playerKillCount(0L, now(), serverUUID));
            case "sessions_players_kill_day":
                return database.query(KillQueries.playerKillCount(dayAgo(), now(), serverUUID));
            case "sessions_players_kill_week":
                return database.query(KillQueries.playerKillCount(weekAgo(), now(), serverUUID));
            case "sessions_players_kill_month":
                return database.query(KillQueries.playerKillCount(monthAgo(), now(), serverUUID));

            case "sessions_mob_kill_total":
                return database.query(KillQueries.mobKillCount(0L, now(), serverUUID));
            case "sessions_mob_kill_day":
                return database.query(KillQueries.mobKillCount(dayAgo(), now(), serverUUID));
            case "sessions_mob_kill_week":
                return database.query(KillQueries.mobKillCount(weekAgo(), now(), serverUUID));
            case "sessions_mob_kill_month":
                return database.query(KillQueries.mobKillCount(monthAgo(), now(), serverUUID));

            case "sessions_average_session_length_total":
                return getPlaytime(database, 0L, now(), serverUUID);
            case "sessions_average_session_length_day":
                return getPlaytime(database, dayAgo(), now(), serverUUID);
            case "sessions_average_session_length_week":
                return getPlaytime(database, weekAgo(), now(), serverUUID);
            case "sessions_average_session_length_month":
                return getPlaytime(database, monthAgo(), now(), serverUUID);

            case "sessions_average_unique_players_total":
                return database.query(PlayerCountQueries.averageUniquePlayerCount(0L, now(), tzOffsetMs, serverUUID));
            case "sessions_average_unique_players_day":
                return database.query(PlayerCountQueries.averageUniquePlayerCount(dayAgo(), now(), tzOffsetMs, serverUUID));
            case "sessions_average_unique_players_week":
                return database.query(PlayerCountQueries.averageUniquePlayerCount(weekAgo(), now(), tzOffsetMs, serverUUID));
            case "sessions_average_unique_players_month":
                return database.query(PlayerCountQueries.averageUniquePlayerCount(monthAgo(), now(), tzOffsetMs, serverUUID));
            case "sessions_new_players_day":
                return database.query(PlayerCountQueries.newPlayerCount(dayAgo(), now(), serverUUID));
            case "sessions_new_players_week":
                return database.query(PlayerCountQueries.newPlayerCount(weekAgo(), now(), serverUUID));
            case "sessions_new_players_month":
                return database.query(PlayerCountQueries.newPlayerCount(monthAgo(), now(), serverUUID));

            case "ping_total":
                return database.query(PingQueries.averagePing(0L, now(), serverUUID));
            case "ping_day":
                return database.query(PingQueries.averagePing(dayAgo(), now(), serverUUID));
            case "ping_week":
                return database.query(PingQueries.averagePing(weekAgo(), now(), serverUUID));
            case "ping_month":
                return database.query(PingQueries.averagePing(monthAgo(), now(), serverUUID));

            case "sessions_peak_count":
                return database.query(TPSQueries.fetchAllTimePeakPlayerCount(serverUUID)).map(DateObj::getValue).orElse(0);
            case "sessions_peak_date":
                return database.query(TPSQueries.fetchAllTimePeakPlayerCount(serverUUID)).map(year).orElse("-");

            case "sessions_recent_peak_count":
                return database.query(TPSQueries.fetchPeakPlayerCount(serverUUID, dayAgo() * 2L)).map(DateObj::getValue).orElse(0);
            case "sessions_recent_peak_date":
                return database.query(TPSQueries.fetchPeakPlayerCount(serverUUID, dayAgo() * 2L)).map(year).orElse("-");

            default:
                return null;
        }
    }
    // Checkstyle.ON: CyclomaticComplexity

    private String getPlaytime(Database database, long after, long before, UUID serverUUID) {
        Long playtime = database.query(SessionQueries.playtime(after, before, serverUUID));
        Long sessionCount = database.query(SessionQueries.sessionCount(after, before, serverUUID));
        return timeAmount.apply(sessionCount != 0 ? playtime / sessionCount : playtime);
    }
}
