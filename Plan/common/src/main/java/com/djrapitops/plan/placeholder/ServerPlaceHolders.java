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
import com.djrapitops.plan.delivery.domain.DateObj;
import com.djrapitops.plan.delivery.domain.mutators.DateObjMutator;
import com.djrapitops.plan.delivery.formatting.Formatter;
import com.djrapitops.plan.delivery.formatting.Formatters;
import com.djrapitops.plan.gathering.ServerUptimeCalculator;
import com.djrapitops.plan.identification.Server;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.TimeSettings;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.Query;
import com.djrapitops.plan.storage.database.queries.analysis.ActivityIndexQueries;
import com.djrapitops.plan.storage.database.queries.analysis.NetworkActivityIndexQueries;
import com.djrapitops.plan.storage.database.queries.analysis.PlayerCountQueries;
import com.djrapitops.plan.storage.database.queries.analysis.TopListQueries;
import com.djrapitops.plan.storage.database.queries.objects.ServerQueries;
import com.djrapitops.plan.storage.database.queries.objects.TPSQueries;
import com.djrapitops.plan.utilities.dev.Untrusted;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.djrapitops.plan.utilities.MiscUtils.*;

/**
 * Placeholders about a servers.
 *
 * @author aidn5, AuroraLS3
 */
@Singleton
public class ServerPlaceHolders implements Placeholders {

    private final PlanConfig config;
    private final DBSystem dbSystem;
    private final ServerInfo serverInfo;
    private final Formatters formatters;
    private final ServerUptimeCalculator serverUptimeCalculator;

    @Inject
    public ServerPlaceHolders(
            PlanConfig config,
            DBSystem dbSystem,
            ServerInfo serverInfo,
            Formatters formatters, ServerUptimeCalculator serverUptimeCalculator
    ) {
        this.config = config;
        this.dbSystem = dbSystem;
        this.serverInfo = serverInfo;
        this.formatters = formatters;
        this.serverUptimeCalculator = serverUptimeCalculator;
    }

    @Override
    public void register(
            PlanPlaceholders placeholders
    ) {
        Formatter<Double> decimals = formatters.decimals();
        Formatter<Double> percentage = formatters.percentage();

        Database database = dbSystem.getDatabase();

        placeholders.registerStatic("network_players_online",
                parameters -> {
                    DateObj<Long> count = new DateObj<>(System.currentTimeMillis(), 0L);
                    Collection<ServerUUID> servers = database.query(ServerQueries.fetchProxyServerUUIDs());
                    if (servers.isEmpty()) servers = database.query(ServerQueries.fetchServerNamesToUUIDs()).values();
                    for (ServerUUID serverUUID : servers) {
                        List<DateObj<Integer>> data = database.query(TPSQueries.fetchPlayersOnlineOfServer(fiveMinAgo(), now(), serverUUID));
                        new DateObjMutator<>(data)
                                .mostRecent()
                                .ifPresent(playersOnline -> {
                                    if (Math.abs(count.getDate() - playersOnline.getDate()) < TimeUnit.MINUTES.toMillis(2L)) {
                                        count.setValue(count.getValue() + playersOnline.getValue());
                                    }
                                });
                    }
                    return count.getValue();
                });

        placeholders.registerStatic("server_players_registered_total",
                parameters -> database.query(PlayerCountQueries.newPlayerCount(0, now(), getServerUUID(parameters))));

        placeholders.registerStatic("server_players_registered_day",
                parameters -> database.query(PlayerCountQueries.newPlayerCount(dayAgo(), now(), getServerUUID(parameters))));

        placeholders.registerStatic("server_players_registered_week",
                parameters -> database.query(PlayerCountQueries.newPlayerCount(weekAgo(), now(), getServerUUID(parameters))));

        placeholders.registerStatic("server_players_registered_month",
                parameters -> database.query(PlayerCountQueries.newPlayerCount(monthAgo(), now(), getServerUUID(parameters))));

        placeholders.registerStatic("network_players_registered_total",
                parameters -> database.query(PlayerCountQueries.newPlayerCount(0, now())));

        placeholders.registerStatic("network_players_registered_day",
                parameters -> database.query(PlayerCountQueries.newPlayerCount(dayAgo(), now())));

        placeholders.registerStatic("network_players_registered_week",
                parameters -> database.query(PlayerCountQueries.newPlayerCount(weekAgo(), now())));

        placeholders.registerStatic("network_players_registered_month",
                parameters -> database.query(PlayerCountQueries.newPlayerCount(monthAgo(), now())));

        placeholders.registerStatic("server_players_unique_total",
                parameters -> database.query(PlayerCountQueries.newPlayerCount(0, now(), getServerUUID(parameters))));

        placeholders.registerStatic("server_players_unique_day",
                parameters -> database.query(PlayerCountQueries.uniquePlayerCount(dayAgo(), now(), getServerUUID(parameters))));

        placeholders.registerStatic("server_players_unique_week",
                parameters -> database.query(PlayerCountQueries.uniquePlayerCount(weekAgo(), now(), getServerUUID(parameters))));

        placeholders.registerStatic("server_players_unique_month",
                parameters -> database.query(PlayerCountQueries.uniquePlayerCount(monthAgo(), now(), getServerUUID(parameters))));

        placeholders.registerStatic("network_players_unique_total",
                parameters -> database.query(PlayerCountQueries.uniquePlayerCount(0, now())));

        placeholders.registerStatic("network_players_unique_day",
                parameters -> database.query(PlayerCountQueries.uniquePlayerCount(dayAgo(), now())));

        placeholders.registerStatic("network_players_unique_week",
                parameters -> database.query(PlayerCountQueries.uniquePlayerCount(weekAgo(), now())));

        placeholders.registerStatic("network_players_unique_month",
                parameters -> database.query(PlayerCountQueries.uniquePlayerCount(monthAgo(), now())));

        placeholders.registerStatic("server_players_online",
                parameters -> new DateObjMutator<>(
                        database.query(TPSQueries.fetchPlayersOnlineOfServer(fiveMinAgo(), now(), getServerUUID(parameters))))
                        .mostRecent()
                        .map(DateObj::getValue)
                        .map(String::valueOf)
                        .orElse("-"));

        placeholders.registerStatic("server_tps_day",
                parameters -> decimals.apply(database.query(TPSQueries.averageTPS(dayAgo(), now(), getServerUUID(parameters)))));

        placeholders.registerStatic("server_tps_week",
                parameters -> decimals.apply(database.query(TPSQueries.averageTPS(weekAgo(), now(), getServerUUID(parameters)))));

        placeholders.registerStatic("server_tps_month",
                parameters -> decimals.apply(database.query(TPSQueries.averageTPS(monthAgo(), now(), getServerUUID(parameters)))));

        placeholders.registerStatic("server_cpu_day",
                parameters -> percentage.apply(database.query(TPSQueries.averageCPU(dayAgo(), now(), getServerUUID(parameters)))));

        placeholders.registerStatic("server_cpu_week",
                parameters -> percentage.apply(database.query(TPSQueries.averageCPU(weekAgo(), now(), getServerUUID(parameters)))));

        placeholders.registerStatic("server_cpu_month",
                parameters -> percentage.apply(database.query(TPSQueries.averageCPU(monthAgo(), now(), getServerUUID(parameters)))));

        placeholders.registerStatic("server_ram_day",
                parameters -> formatters.byteSizeLong().apply(database.query(TPSQueries.averageRAM(dayAgo(), now(), getServerUUID(parameters)))));

        placeholders.registerStatic("server_ram_week",
                parameters -> formatters.byteSizeLong().apply(database.query(TPSQueries.averageRAM(weekAgo(), now(), getServerUUID(parameters)))));

        placeholders.registerStatic("server_ram_month",
                parameters -> formatters.byteSizeLong().apply(database.query(TPSQueries.averageRAM(monthAgo(), now(), getServerUUID(parameters)))));

        placeholders.registerStatic("server_chunks_day",
                parameters -> database.query(TPSQueries.averageChunks(dayAgo(), now(), getServerUUID(parameters))));

        placeholders.registerStatic("server_chunks_week",
                parameters -> database.query(TPSQueries.averageChunks(weekAgo(), now(), getServerUUID(parameters))));

        placeholders.registerStatic("server_chunks_month",
                parameters -> database.query(TPSQueries.averageChunks(monthAgo(), now(), getServerUUID(parameters))));

        placeholders.registerStatic("server_entities_day",
                parameters -> database.query(TPSQueries.averageEntities(dayAgo(), now(), getServerUUID(parameters))));

        placeholders.registerStatic("server_entities_week",
                parameters -> database.query(TPSQueries.averageEntities(weekAgo(), now(), getServerUUID(parameters))));

        placeholders.registerStatic("server_entities_month",
                parameters -> database.query(TPSQueries.averageEntities(monthAgo(), now(), getServerUUID(parameters))));

        placeholders.registerStatic("server_max_free_disk_day",
                parameters -> database.query(TPSQueries.maxFreeDisk(dayAgo(), now(), getServerUUID(parameters))));

        placeholders.registerStatic("server_max_free_disk_week",
                parameters -> database.query(TPSQueries.maxFreeDisk(weekAgo(), now(), getServerUUID(parameters))));

        placeholders.registerStatic("server_max_free_disk_month",
                parameters -> database.query(TPSQueries.maxFreeDisk(monthAgo(), now(), getServerUUID(parameters))));

        placeholders.registerStatic("server_min_free_disk_day",
                parameters -> database.query(TPSQueries.minFreeDisk(dayAgo(), now(), getServerUUID(parameters))));

        placeholders.registerStatic("server_min_free_disk_week",
                parameters -> database.query(TPSQueries.minFreeDisk(weekAgo(), now(), getServerUUID(parameters))));

        placeholders.registerStatic("server_min_free_disk_month",
                parameters -> database.query(TPSQueries.minFreeDisk(monthAgo(), now(), getServerUUID(parameters))));

        placeholders.registerStatic("server_average_free_disk_day",
                parameters -> formatters.byteSizeLong().apply(database.query(TPSQueries.averageFreeDisk(dayAgo(), now(), getServerUUID(parameters)))));

        placeholders.registerStatic("server_average_free_disk_week",
                parameters -> formatters.byteSizeLong().apply(database.query(TPSQueries.averageFreeDisk(weekAgo(), now(), getServerUUID(parameters)))));

        placeholders.registerStatic("server_average_free_disk_month",
                parameters -> formatters.byteSizeLong().apply(database.query(TPSQueries.averageFreeDisk(monthAgo(), now(), getServerUUID(parameters)))));

        placeholders.registerStatic("server_name",
                () -> serverInfo.getServer().getName());

        placeholders.registerStatic("server_uptime",
                parameters -> serverUptimeCalculator.getServerUptimeMillis(getServerUUID(parameters))
                        .map(String::valueOf)
                        .orElse("-"));

        placeholders.registerStatic("server_uuid",
                serverInfo::getServerUUID);

        placeholders.registerStatic("regular_players",
                parameters -> database.query(ActivityIndexQueries.fetchRegularPlayerCount(System.currentTimeMillis(), getServerUUID(parameters), config.get(TimeSettings.ACTIVE_PLAY_THRESHOLD))));
        placeholders.registerStatic("network_regular_players",
                () -> database.query(NetworkActivityIndexQueries.fetchRegularPlayerCount(System.currentTimeMillis(), config.get(TimeSettings.ACTIVE_PLAY_THRESHOLD))));

        registerDynamicCategoryPlaceholders(placeholders, database);
    }

    private ServerUUID getServerUUID(@Untrusted Arguments parameters) {
        return parameters.get(0).flatMap(this::getServerUUIDForServerIdentifier).orElseGet(serverInfo::getServerUUID);
    }

    private Optional<ServerUUID> getServerUUIDForServerIdentifier(@Untrusted String serverIdentifier) {
        return dbSystem.getDatabase().query(ServerQueries.fetchServerMatchingIdentifier(serverIdentifier))
                .map(Server::getUuid);
    }

    private void registerDynamicCategoryPlaceholders(PlanPlaceholders placeholders, Database database) {
        List<TopCategoryQuery<Long>> queries = new ArrayList<>();
        queries.addAll(createCategoryQueriesForAllTimespans("playtime", (index, timespan, parameters) -> TopListQueries.fetchNthTop10PlaytimePlayerOn(getServerUUID(parameters), index, System.currentTimeMillis() - timespan, System.currentTimeMillis())));
        queries.addAll(createCategoryQueriesForAllTimespans("network_playtime", (index, timespan, parameters) -> TopListQueries.fetchNthTop10PlaytimePlayerOn(null, index, System.currentTimeMillis() - timespan, System.currentTimeMillis())));
        queries.addAll(createCategoryQueriesForAllTimespans("active_playtime", (index, timespan, parameters) -> TopListQueries.fetchNthTop10ActivePlaytimePlayerOn(getServerUUID(parameters), index, System.currentTimeMillis() - timespan, System.currentTimeMillis())));
        queries.addAll(createCategoryQueriesForAllTimespans("network_active_playtime", (index, timespan, parameters) -> TopListQueries.fetchNthTop10ActivePlaytimePlayerOn(null, index, System.currentTimeMillis() - timespan, System.currentTimeMillis())));
        queries.addAll(createCategoryQueriesForAllTimespans("player_kills", (index, timespan, parameters) -> TopListQueries.fetchNthTop10PlayerKillCountOn(getServerUUID(parameters), index, System.currentTimeMillis() - timespan, System.currentTimeMillis())));

        for (int i = 0; i < 10; i++) {
            for (TopCategoryQuery<Long> query : queries) {
                final int nth = i;
                placeholders.registerStatic(String.format("top_%s_%s_%s", query.getCategory(), query.getTimeSpan(), nth + 1),
                        parameters -> database.query(query.getQuery(nth, parameters))
                                .map(TopListQueries.TopListEntry::getPlayerName)
                                .orElse("-"));
                placeholders.registerStatic(String.format("top_%s_%s_%s_value", query.getCategory(), query.getTimeSpan(), nth + 1),
                        parameters -> database.query(query.getQuery(nth, parameters))
                                .map(TopListQueries.TopListEntry::getValue)
                                .map(query.getCategory().equals("player_kills") ? Function.identity() : formatters.timeAmount())
                                .orElse("-"));
                placeholders.registerStatic(String.format("top_%s_%s_%s_value_raw", query.getCategory(), query.getTimeSpan(), nth + 1),
                        parameters -> database.query(query.getQuery(nth, parameters))
                                .map(TopListQueries.TopListEntry::getValue)
                                .map(String::valueOf)
                                .orElse("-"));
            }
        }
    }

    private <T> List<TopCategoryQuery<T>> createCategoryQueriesForAllTimespans(String category, QueryCreator<T> queryCreator) {
        return Arrays.asList(
                new TopCategoryQuery<>(category, queryCreator, "month", TimeUnit.DAYS.toMillis(30)),
                new TopCategoryQuery<>(category, queryCreator, "week", TimeUnit.DAYS.toMillis(7)),
                new TopCategoryQuery<>(category, queryCreator, "day", TimeUnit.DAYS.toMillis(1)),
                new TopCategoryQuery<>(category, queryCreator, "total", System.currentTimeMillis())
        );
    }

    public interface QueryCreator<T> {
        Query<Optional<TopListQueries.TopListEntry<T>>> apply(Integer number, Long timespan, @Untrusted Arguments parameters);
    }

    public static class TopCategoryQuery<T> {
        private final String category;
        private final QueryCreator<T> queryCreator;
        private final String timeSpan;
        private final long timeSpanMillis;

        public TopCategoryQuery(String category, QueryCreator<T> queryCreator, String timeSpan, long timespan) {
            this.category = category;
            this.queryCreator = queryCreator;
            this.timeSpan = timeSpan;
            this.timeSpanMillis = timespan;
        }

        public String getCategory() {
            return category;
        }

        public String getTimeSpan() {
            return timeSpan;
        }

        public Query<Optional<TopListQueries.TopListEntry<T>>> getQuery(int i, @Untrusted Arguments parameters) {
            return queryCreator.apply(i, timeSpanMillis, parameters);
        }
    }
}
