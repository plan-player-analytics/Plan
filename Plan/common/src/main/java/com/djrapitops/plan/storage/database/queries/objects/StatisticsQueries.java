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
package com.djrapitops.plan.storage.database.queries.objects;

import com.djrapitops.plan.delivery.domain.ServerMinecraftStatistics;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.queries.Query;
import com.djrapitops.plan.storage.database.queries.QueryAllStatement;
import com.djrapitops.plan.storage.database.queries.QueryStatement;
import com.djrapitops.plan.storage.database.sql.building.Select;
import com.djrapitops.plan.storage.database.sql.tables.ServerTable;
import com.djrapitops.plan.storage.database.sql.tables.StatisticTable;
import com.djrapitops.plan.storage.database.sql.tables.StatisticValueTable;
import com.djrapitops.plan.storage.database.sql.tables.UsersTable;
import com.djrapitops.plan.utilities.java.Maps;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Queries into {@link com.djrapitops.plan.storage.database.sql.tables.StatisticTable} and {@link com.djrapitops.plan.storage.database.sql.tables.StatisticValueTable}
 *
 * @author AuroraLS3
 */
public class StatisticsQueries {

    private StatisticsQueries() {
        /* Static method class */
    }

    public static Query<Map<String, Integer>> fetchStatisticNameToId() {
        return db -> db.queryMap(StatisticTable.SELECT_STATISTICS, (row, map) ->
                map.put(row.getString(StatisticTable.STATISTIC_NAME), row.getInt(StatisticTable.ID)));
    }

    public static Query<List<StatisticValueTable.Row>> fetchStatistics(int afterId, int limit) {
        String sql = Select.all(StatisticValueTable.TABLE_NAME)
                .where(StatisticValueTable.ID + ">" + afterId)
                .orderBy(StatisticValueTable.ID)
                .limit(limit)
                .toString();
        return db -> db.queryList(sql, StatisticValueTable.Row::extract);
    }

    public static Query<List<ServerMinecraftStatistics>> fetchStatistics(UUID playerUUID) {
        String sql = SELECT + ServerTable.SERVER_UUID + ',' + StatisticTable.STATISTIC_NAME + ',' + StatisticValueTable.VALUE +
                FROM + StatisticValueTable.TABLE_NAME + " v" +
                INNER_JOIN + StatisticTable.TABLE_NAME + " s ON s." + StatisticTable.ID + "=v." + StatisticValueTable.STATISTIC_ID +
                INNER_JOIN + ServerTable.TABLE_NAME + " ser ON ser." + ServerTable.ID + "=v." + StatisticValueTable.SERVER_ID +
                WHERE + StatisticValueTable.USER_ID + "=" + UsersTable.SELECT_USER_ID;
        return new QueryStatement<>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, playerUUID.toString());
            }

            @Override
            public List<ServerMinecraftStatistics> processResults(ResultSet set) throws SQLException {
                Map<String, Map<String, Integer>> statsByServerUuid = new HashMap<>();
                while (set.next()) {
                    Map<String, Integer> stats = statsByServerUuid.computeIfAbsent(set.getString(ServerTable.SERVER_UUID), Maps::create);
                    stats.put(set.getString(StatisticTable.STATISTIC_NAME), set.getInt(StatisticValueTable.VALUE));
                }
                return statsByServerUuid.entrySet().stream()
                        .map(entry -> new ServerMinecraftStatistics(entry.getKey(), entry.getValue()))
                        .collect(Collectors.toList());
            }
        };
    }

    public static Query<List<ServerMinecraftStatistics>> fetchStatistics(ServerUUID serverUUID) {
        String sql = SELECT + ServerTable.SERVER_UUID + ',' + StatisticTable.STATISTIC_NAME +
                ",SUM(" + StatisticValueTable.VALUE + ") AS " + StatisticValueTable.VALUE +
                FROM + StatisticValueTable.TABLE_NAME + " v" +
                INNER_JOIN + StatisticTable.TABLE_NAME + " s ON s." + StatisticTable.ID + "=v." + StatisticValueTable.STATISTIC_ID +
                INNER_JOIN + ServerTable.TABLE_NAME + " ser ON ser." + ServerTable.ID + "=v." + StatisticValueTable.SERVER_ID +
                WHERE + ServerTable.SERVER_UUID + "=?" +
                GROUP_BY + ServerTable.SERVER_UUID + ',' + StatisticTable.STATISTIC_NAME;
        return new QueryStatement<>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
            }

            @Override
            public List<ServerMinecraftStatistics> processResults(ResultSet set) throws SQLException {
                Map<String, Map<String, Integer>> statsByServerUuid = new HashMap<>();
                while (set.next()) {
                    Map<String, Integer> stats = statsByServerUuid.computeIfAbsent(set.getString(ServerTable.SERVER_UUID), Maps::create);
                    stats.put(set.getString(StatisticTable.STATISTIC_NAME), set.getInt(StatisticValueTable.VALUE));
                }
                return statsByServerUuid.entrySet().stream()
                        .map(entry -> new ServerMinecraftStatistics(entry.getKey(), entry.getValue()))
                        .collect(Collectors.toList());
            }
        };
    }

    public static Query<List<ServerMinecraftStatistics>> fetchStatistics() {
        String sql = SELECT + ServerTable.SERVER_UUID + ',' + StatisticTable.STATISTIC_NAME +
                ",SUM(" + StatisticValueTable.VALUE + ") AS " + StatisticValueTable.VALUE +
                FROM + StatisticValueTable.TABLE_NAME + " v" +
                INNER_JOIN + StatisticTable.TABLE_NAME + " s ON s." + StatisticTable.ID + "=v." + StatisticValueTable.STATISTIC_ID +
                INNER_JOIN + ServerTable.TABLE_NAME + " ser ON ser." + ServerTable.ID + "=v." + StatisticValueTable.SERVER_ID +
                GROUP_BY + ServerTable.SERVER_UUID + ',' + StatisticTable.STATISTIC_NAME;
        return new QueryAllStatement<>(sql) {
            @Override
            public List<ServerMinecraftStatistics> processResults(ResultSet set) throws SQLException {
                Map<String, Map<String, Integer>> statsByServerUuid = new HashMap<>();
                while (set.next()) {
                    Map<String, Integer> stats = statsByServerUuid.computeIfAbsent(set.getString(ServerTable.SERVER_UUID), Maps::create);
                    stats.put(set.getString(StatisticTable.STATISTIC_NAME), set.getInt(StatisticValueTable.VALUE));
                }
                return statsByServerUuid.entrySet().stream()
                        .map(entry -> new ServerMinecraftStatistics(entry.getKey(), entry.getValue()))
                        .collect(Collectors.toList());
            }
        };
    }

    public static Query<List<ServerMinecraftStatistics>> fetchStatisticsAggregate() {
        String sql = SELECT + StatisticTable.STATISTIC_NAME +
                ",SUM(" + StatisticValueTable.VALUE + ") AS " + StatisticValueTable.VALUE +
                FROM + StatisticValueTable.TABLE_NAME + " v" +
                INNER_JOIN + StatisticTable.TABLE_NAME + " s ON s." + StatisticTable.ID + "=v." + StatisticValueTable.STATISTIC_ID +
                INNER_JOIN + ServerTable.TABLE_NAME + " ser ON ser." + ServerTable.ID + "=v." + StatisticValueTable.SERVER_ID +
                GROUP_BY + StatisticTable.STATISTIC_NAME;
        return new QueryAllStatement<>(sql) {
            @Override
            public List<ServerMinecraftStatistics> processResults(ResultSet set) throws SQLException {
                Map<String, Integer> stats = new HashMap<>();
                while (set.next()) {
                    int newValue = set.getInt(StatisticValueTable.VALUE);
                    stats.compute(set.getString(StatisticTable.STATISTIC_NAME), (key, value) -> value != null ? value + newValue : newValue);
                }
                return List.of(new ServerMinecraftStatistics(null, stats));
            }
        };
    }
}
