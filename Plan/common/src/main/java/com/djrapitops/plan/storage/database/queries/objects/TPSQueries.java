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

import com.djrapitops.plan.delivery.domain.DateObj;
import com.djrapitops.plan.gathering.domain.TPS;
import com.djrapitops.plan.gathering.domain.builders.TPSBuilder;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.queries.Query;
import com.djrapitops.plan.storage.database.queries.QueryStatement;
import com.djrapitops.plan.storage.database.sql.building.Select;
import com.djrapitops.plan.storage.database.sql.tables.ServerTable;
import com.djrapitops.plan.storage.database.sql.tables.TPSTable;
import com.djrapitops.plan.utilities.dev.Benchmark;
import com.djrapitops.plan.utilities.java.Lists;
import org.intellij.lang.annotations.Language;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;
import static com.djrapitops.plan.storage.database.sql.tables.TPSTable.*;

/**
 * Queries for {@link com.djrapitops.plan.gathering.domain.TPS} objects.
 *
 * @author AuroraLS3
 */
public class TPSQueries {

    private TPSQueries() {
        /* Static method class */
    }

    public static Query<List<TPS>> fetchTPSDataOfServerInResolution(long after, long before, long resolution, ServerUUID serverUUID) {
        return db -> {
            String sql = SELECT +
                    min("t." + DATE) + " as " + DATE + ',' +
                    min("t." + TPS) + " as " + TPS + ',' +
                    max("t." + PLAYERS_ONLINE) + " as " + PLAYERS_ONLINE + ',' +
                    max("t." + RAM_USAGE) + " as " + RAM_USAGE + ',' +
                    max("t." + CPU_USAGE) + " as " + CPU_USAGE + ',' +
                    max("t." + ENTITIES) + " as " + ENTITIES + ',' +
                    max("t." + CHUNKS) + " as " + CHUNKS + ',' +
                    max("t." + FREE_DISK) + " as " + FREE_DISK + ',' +
                    min("t." + MSPT_AVERAGE) + " as " + MSPT_AVERAGE + ',' +
                    max("t." + MSPT_95TH_PERCENTILE) + " as " + MSPT_95TH_PERCENTILE +
                    FROM + TABLE_NAME + " t" +
                    INNER_JOIN + ServerTable.TABLE_NAME + " s ON s." + ServerTable.ID + "=t." + SERVER_ID +
                    WHERE + "s." + ServerTable.SERVER_UUID + "=?" +
                    AND + DATE + ">=?" +
                    AND + DATE + "<?" +
                    GROUP_BY + floor(DATE + "/?") +
                    ORDER_BY + DATE;

            return db.query(new QueryStatement<List<TPS>>(sql, 50000) {
                @Override
                public void prepare(PreparedStatement statement) throws SQLException {
                    statement.setString(1, serverUUID.toString());
                    statement.setLong(2, after);
                    statement.setLong(3, before);
                    statement.setLong(4, resolution);
                }

                @Override
                public List<TPS> processResults(ResultSet set) throws SQLException {
                    List<TPS> data = new ArrayList<>();
                    while (set.next()) {
                        data.add(extractTPS(set));
                    }
                    return data;
                }
            });
        };
    }

    public static TPS extractTPS(ResultSet set) throws SQLException {
        return TPSBuilder.get()
                .date(set.getLong(DATE))
                .tps(set.getDouble(TPS))
                .playersOnline(set.getInt(PLAYERS_ONLINE))
                .usedCPU(set.getDouble(CPU_USAGE))
                .usedMemory(set.getLong(RAM_USAGE))
                .entities(set.getInt(ENTITIES))
                .chunksLoaded(set.getInt(CHUNKS))
                .freeDiskSpace(set.getLong(FREE_DISK))
                .msptAverage(set.getDouble(MSPT_AVERAGE), set.wasNull())
                .mspt95thPercentile(set.getDouble(MSPT_95TH_PERCENTILE), set.wasNull())
                .toTPS();
    }

    public static Query<List<TPS>> fetchTPSDataOfServer(long after, long before, ServerUUID serverUUID) {
        String sql = SELECT + "t.*" + FROM + TABLE_NAME + " t" +
                INNER_JOIN + ServerTable.TABLE_NAME + " s ON s." + ServerTable.ID + "=t." + SERVER_ID +
                WHERE + "s." + ServerTable.SERVER_UUID + "=?" +
                AND + DATE + ">=?" +
                AND + DATE + "<=?" +
                ORDER_BY + DATE;

        return new QueryStatement<>(sql, 50000) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
                statement.setLong(2, after);
                statement.setLong(3, before);
            }

            @Override
            public List<TPS> processResults(ResultSet set) throws SQLException {
                List<TPS> data = new ArrayList<>();
                while (set.next()) {
                    TPS tps = extractTPS(set);
                    data.add(tps);
                }
                return data;
            }
        };
    }

    public static Query<Map<Integer, Long>> occupiedTime(long after, long before, List<ServerUUID> serverUUIDs) {
        String sql = SELECT + SERVER_ID + ",SUM(" +
                "CASE " +
                "WHEN " + PLAYERS_ONLINE + " > 0 " +
                "AND (date - prev_date) <= 180000 " +
                "THEN (date - prev_date) " +
                "ELSE 0 " +
                "END" +
                ") AS active_time" +
                FROM + "(" +
                SELECT +
                SERVER_ID + "," +
                DATE + "," +
                PLAYERS_ONLINE + "," +
                "LAG(" + DATE + ") OVER (PARTITION BY " + SERVER_ID + " ORDER BY " + DATE + ") AS prev_date" +
                FROM + TABLE_NAME +
                WHERE + (!serverUUIDs.isEmpty() ? SERVER_ID + " IN " + ServerTable.selectServerIds(serverUUIDs) +
                                                  AND : "") + DATE + ">=?" +
                AND + DATE + "<=?" +
                ") q1" +
                GROUP_BY + SERVER_ID;
        return db -> db.queryMap(sql, (row, map) ->
                        map.put(row.getInt(SERVER_ID), row.getLong("active_time")),
                after, before);
    }

    public static Query<Map<Integer, Long>> uptime(long after, long before, List<ServerUUID> serverUUIDs) {
        String sql = SELECT + SERVER_ID + ",SUM(" +
                "CASE " +
                "WHEN diff <= 120000 " +
                "THEN diff " +
                "ELSE 0 " +
                "END" +
                ") AS uptime" +
                FROM + "(" +
                SELECT +
                SERVER_ID + "," +
                DATE + "-LAG(" + DATE + ") OVER (PARTITION BY " + SERVER_ID + " ORDER BY " + DATE + ") AS diff" +
                FROM + TABLE_NAME +
                WHERE + (!serverUUIDs.isEmpty() ? SERVER_ID + " IN " + ServerTable.selectServerIds(serverUUIDs) +
                                                  AND : "") + DATE + ">=?" +
                AND + DATE + "<=?" +
                ") q1" +
                GROUP_BY + SERVER_ID;
        return db -> db.queryMap(sql, (row, map) ->
                        map.put(row.getInt(SERVER_ID), row.getLong("uptime")),
                after, before);
    }

    public static Query<List<DateObj<Integer>>> fetchViewPreviewGraphData(ServerUUID serverUUID) {
        String sql = SELECT + min(DATE) + " as " + DATE + ',' +
                max(PLAYERS_ONLINE) + " as " + PLAYERS_ONLINE +
                FROM + TABLE_NAME + " t" +
                INNER_JOIN + ServerTable.TABLE_NAME + " s ON s." + ServerTable.ID + "=t." + SERVER_ID +
                WHERE + "s." + ServerTable.SERVER_UUID + "=?" +
                GROUP_BY + floor(DATE + "/?");

        return new QueryStatement<>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
                statement.setLong(2, TimeUnit.MINUTES.toMillis(15));
            }

            @Override
            public List<DateObj<Integer>> processResults(ResultSet set) throws SQLException {
                List<DateObj<Integer>> ofServer = new ArrayList<>();
                while (set.next()) ofServer.add(new DateObj<>(set.getLong(DATE), set.getInt(PLAYERS_ONLINE)));
                return ofServer;
            }
        };
    }

    public static Query<List<DateObj<Integer>>> fetchPlayersOnlineOfServer(long after, long before, ServerUUID serverUUID) {
        String sql = SELECT + ServerTable.SERVER_UUID + ',' + DATE + ',' + PLAYERS_ONLINE +
                FROM + TABLE_NAME +
                INNER_JOIN + ServerTable.TABLE_NAME + " on " + ServerTable.TABLE_NAME + '.' + ServerTable.ID + '=' + SERVER_ID +
                WHERE + ServerTable.SERVER_UUID + "=?" +
                AND + DATE + "<?" +
                AND + DATE + ">?" +
                ORDER_BY + DATE + " ASC";
        return new QueryStatement<>(sql, 1000) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
                statement.setLong(2, before);
                statement.setLong(3, after);
            }

            @Override
            public List<DateObj<Integer>> processResults(ResultSet set) throws SQLException {
                List<DateObj<Integer>> ofServer = new ArrayList<>();

                while (set.next()) {
                    ofServer.add(new DateObj<>(set.getLong(DATE), set.getInt(PLAYERS_ONLINE)));
                }

                return ofServer;
            }
        };
    }

    public static Query<Map<Integer, List<TPS>>> fetchTPSDataOfAllServersBut(long after, long before, ServerUUID leaveOut) {
        String sql = SELECT + DATE + ',' + TPS + ',' + PLAYERS_ONLINE + ',' + SERVER_ID +
                FROM + TABLE_NAME +
                INNER_JOIN + ServerTable.TABLE_NAME + " on " + ServerTable.TABLE_NAME + '.' + ServerTable.ID + '=' + SERVER_ID +
                WHERE + ServerTable.SERVER_UUID + "!=?" +
                AND + ServerTable.INSTALLED + "=?" +
                AND + DATE + "<?" +
                AND + DATE + ">?";
        return new QueryStatement<>(sql, 5000) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                if (leaveOut != null) {
                    statement.setString(1, leaveOut.toString());
                } else {
                    statement.setNull(1, Types.VARCHAR);
                }
                statement.setBoolean(2, true);
                statement.setLong(3, before);
                statement.setLong(4, after);
            }

            @Override
            public Map<Integer, List<TPS>> processResults(ResultSet set) throws SQLException {
                Map<Integer, List<TPS>> byServer = new HashMap<>();
                while (set.next()) {
                    Integer serverUID = set.getInt(SERVER_ID);
                    List<TPS> ofServer = byServer.computeIfAbsent(serverUID, Lists::create);
                    ofServer.add(TPSBuilder.get()
                            .date(set.getLong(DATE))
                            .tps(set.getDouble(TPS))
                            .playersOnline(set.getInt(PLAYERS_ONLINE))
                            .toTPS());
                }
                return byServer;
            }
        };
    }

    public static Query<Optional<DateObj<Integer>>> fetchPeakPlayerCount(ServerUUID serverUUID, long afterDate, long beforeDate) {
        return fetchPeakPlayerCount(Collections.singletonList(serverUUID), afterDate, beforeDate);
    }

    public static Query<Optional<DateObj<Integer>>> fetchPeakPlayerCount(List<ServerUUID> serverUUIDs, long afterDate, long beforeDate) {
        if (serverUUIDs.isEmpty()) {
            return db -> Optional.empty();
        }

        String subQuery = '(' + SELECT + "MAX(" + PLAYERS_ONLINE + ") as " + PLAYERS_ONLINE + FROM + TABLE_NAME + " t2" +
                INNER_JOIN + ServerTable.TABLE_NAME + " s2 ON s2." + ServerTable.ID + "=t2." + SERVER_ID +
                WHERE + "s2." + ServerTable.SERVER_UUID + " IN (" + ServerTable.uuids(serverUUIDs) + ")" +
                AND + DATE + ">= ?" +
                AND + DATE + "<= ?" +
                GROUP_BY + "t2." + SERVER_ID + ")";
        String sql = SELECT +
                "t." + DATE + ',' + "t." + PLAYERS_ONLINE +
                FROM + TABLE_NAME + " t" +
                INNER_JOIN + ServerTable.TABLE_NAME + " s ON s." + ServerTable.ID + "=t." + SERVER_ID +
                INNER_JOIN + subQuery + " max on t." + PLAYERS_ONLINE + "=max." + PLAYERS_ONLINE +
                WHERE + "s." + ServerTable.SERVER_UUID + " IN (" + ServerTable.uuids(serverUUIDs) + ")" +
                AND + "t." + DATE + ">= ?" +
                AND + "t." + DATE + "<= ?" +
                ORDER_BY + "t." + DATE + " DESC LIMIT 1";

        return new QueryStatement<>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setLong(1, afterDate);
                statement.setLong(2, beforeDate);
                statement.setLong(3, afterDate);
                statement.setLong(4, beforeDate);
            }

            @Override
            public Optional<DateObj<Integer>> processResults(ResultSet set) throws SQLException {
                if (set.next()) {
                    return Optional.of(new DateObj<>(
                            set.getLong(DATE),
                            set.getInt(PLAYERS_ONLINE)
                    ));
                }
                return Optional.empty();
            }
        };
    }

    public static Query<Optional<DateObj<Integer>>> fetchNetworkPeakPlayerCount(long afterDate, long beforeDate, boolean onlyOneProxy) {
        String subQuery;
        if (onlyOneProxy) {
            subQuery = '(' + SELECT + "MAX(" + PLAYERS_ONLINE + ") as " + PLAYERS_ONLINE + FROM + TABLE_NAME + " t2" +
                    INNER_JOIN + ServerTable.TABLE_NAME + " s2 ON s2." + ServerTable.ID + "=t2." + SERVER_ID +
                    WHERE + "s2." + ServerTable.PROXY + "=?" +
                    AND + DATE + ">= ?" +
                    AND + DATE + "<= ?" +
                    GROUP_BY + "t2." + SERVER_ID + ")";
        } else {
            subQuery = '(' + SELECT + "MAX(network_players) as " + PLAYERS_ONLINE + FROM +
                    '(' + SELECT + "SUM(" + PLAYERS_ONLINE + ") as network_players" + FROM + TABLE_NAME + " t3" +
                    INNER_JOIN + ServerTable.TABLE_NAME + " s3 ON s3." + ServerTable.ID + "=t3." + SERVER_ID +
                    WHERE + "s3." + ServerTable.PROXY + "=?" +
                    AND + DATE + ">= ?" +
                    AND + DATE + "<= ?" +
                    GROUP_BY + DATE + ") as network_counts" + ')';
        }

        String sql;
        if (onlyOneProxy) {
            sql = SELECT +
                    "t." + DATE + ',' + "t." + PLAYERS_ONLINE +
                    FROM + TABLE_NAME + " t" +
                    INNER_JOIN + ServerTable.TABLE_NAME + " s ON s." + ServerTable.ID + "=t." + SERVER_ID +
                    INNER_JOIN + subQuery + " max on t." + PLAYERS_ONLINE + "=max." + PLAYERS_ONLINE +
                    WHERE + "s." + ServerTable.PROXY + "=?" +
                    AND + "t." + DATE + ">= ?" +
                    AND + "t." + DATE + "<= ?" +
                    ORDER_BY + "t." + DATE + " DESC LIMIT 1";
        } else {
            sql = SELECT + DATE + ", network_players as " + PLAYERS_ONLINE + FROM +
                    '(' + SELECT + DATE + ", SUM(" + PLAYERS_ONLINE + ") as network_players" + FROM + TABLE_NAME + " t" +
                    INNER_JOIN + ServerTable.TABLE_NAME + " s ON s." + ServerTable.ID + "=t." + SERVER_ID +
                    WHERE + "s." + ServerTable.PROXY + "=?" +
                    AND + "t." + DATE + ">= ?" +
                    AND + "t." + DATE + "<= ?" +
                    GROUP_BY + DATE + ") as network_counts" +
                    INNER_JOIN + subQuery + " max on network_players=max." + PLAYERS_ONLINE +
                    ORDER_BY + DATE + " DESC LIMIT 1";
        }

        return new QueryStatement<>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setBoolean(1, true);
                statement.setLong(2, afterDate);
                statement.setLong(3, beforeDate);
                statement.setBoolean(4, true);
                statement.setLong(5, afterDate);
                statement.setLong(6, beforeDate);
            }

            @Override
            public Optional<DateObj<Integer>> processResults(ResultSet set) throws SQLException {
                if (set.next()) {
                    return Optional.of(new DateObj<>(
                            set.getLong(DATE),
                            set.getInt(PLAYERS_ONLINE)
                    ));
                }
                return Optional.empty();
            }
        };
    }

    public static Query<Optional<DateObj<Integer>>> fetchAllTimePeakPlayerCount(ServerUUID serverUUID) {
        return fetchPeakPlayerCount(serverUUID, 0, Long.MAX_VALUE);
    }

    public static Query<Optional<TPS>> fetchLatestTPSEntryForServer(ServerUUID serverUUID) {
        String sql = SELECT + "t.*" +
                FROM + TABLE_NAME + " t" +
                INNER_JOIN + ServerTable.TABLE_NAME + " s ON s." + ServerTable.ID + "=t." + SERVER_ID +
                WHERE + "s." + ServerTable.SERVER_UUID + "=?" +
                ORDER_BY + DATE + " DESC LIMIT 1";

        return new QueryStatement<>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
            }

            @Override
            public Optional<TPS> processResults(ResultSet set) throws SQLException {
                if (set.next()) {
                    return Optional.of(TPSBuilder.get()
                            .date(set.getLong(DATE))
                            .tps(set.getDouble(TPS))
                            .playersOnline(set.getInt(PLAYERS_ONLINE))
                            .usedCPU(set.getDouble(CPU_USAGE))
                            .usedMemory(set.getLong(RAM_USAGE))
                            .entities(set.getInt(ENTITIES))
                            .chunksLoaded(set.getInt(CHUNKS))
                            .freeDiskSpace(set.getLong(FREE_DISK))
                            .toTPS());
                }
                return Optional.empty();
            }
        };
    }

    public static Query<Double> averageTPS(long after, long before, ServerUUID serverUUID) {
        return averageTPS(after, before, Collections.singletonList(serverUUID));
    }

    public static Query<Double> averageTPS(long after, long before, List<ServerUUID> serverUUIDs) {
        if (serverUUIDs.isEmpty()) {
            return db -> -1.0;
        }

        String sql = SELECT + "AVG(" + TPS + ") as average" + FROM + TABLE_NAME + " t" +
                INNER_JOIN + ServerTable.TABLE_NAME + " s ON s." + ServerTable.ID + "=t." + SERVER_ID +
                WHERE + "s." + ServerTable.SERVER_UUID + " IN (" + ServerTable.uuids(serverUUIDs) + ")" +
                AND + TPS + ">=0" +
                AND + DATE + "<?" +
                AND + DATE + ">?";
        return new QueryStatement<>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setLong(1, before);
                statement.setLong(2, after);
            }

            @Override
            public Double processResults(ResultSet set) throws SQLException {
                return set.next() ? set.getDouble("average") : -1.0;
            }
        };
    }

    public static Query<Double> averageCPU(long after, long before, ServerUUID serverUUID) {
        String sql = SELECT + "AVG(" + CPU_USAGE + ") as average" + FROM + TABLE_NAME + " t" +
                INNER_JOIN + ServerTable.TABLE_NAME + " s ON s." + ServerTable.ID + "=t." + SERVER_ID +
                WHERE + "s." + ServerTable.SERVER_UUID + "=?" +
                AND + CPU_USAGE + ">=0" +
                AND + DATE + "<?" +
                AND + DATE + ">?";
        return new QueryStatement<>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
                statement.setLong(2, before);
                statement.setLong(3, after);
            }

            @Override
            public Double processResults(ResultSet set) throws SQLException {
                return set.next() ? set.getDouble("average") : -1.0;
            }
        };
    }

    public static Query<Long> averageRAM(long after, long before, ServerUUID serverUUID) {
        String sql = SELECT + "AVG(" + RAM_USAGE + ") as average" + FROM + TABLE_NAME + " t" +
                INNER_JOIN + ServerTable.TABLE_NAME + " s ON s." + ServerTable.ID + "=t." + SERVER_ID +
                WHERE + "s." + ServerTable.SERVER_UUID + "=?" +
                AND + RAM_USAGE + ">=0" +
                AND + DATE + "<?" +
                AND + DATE + ">?";
        return new QueryStatement<>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
                statement.setLong(2, before);
                statement.setLong(3, after);
            }

            @Override
            public Long processResults(ResultSet set) throws SQLException {
                return set.next() ? (long) set.getDouble("average") : -1L;
            }
        };
    }

    public static Query<Long> averageChunks(long after, long before, ServerUUID serverUUID) {
        String sql = SELECT + "AVG(" + CHUNKS + ") as average" + FROM + TABLE_NAME + " t" +
                INNER_JOIN + ServerTable.TABLE_NAME + " s ON s." + ServerTable.ID + "=t." + SERVER_ID +
                WHERE + "s." + ServerTable.SERVER_UUID + "=?" +
                AND + CHUNKS + ">=0" +
                AND + DATE + "<?" +
                AND + DATE + ">?";
        return new QueryStatement<>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
                statement.setLong(2, before);
                statement.setLong(3, after);
            }

            @Override
            public Long processResults(ResultSet set) throws SQLException {
                return set.next() ? (long) set.getDouble("average") : -1L;
            }
        };
    }

    public static Query<Long> averageEntities(long after, long before, ServerUUID serverUUID) {
        String sql = SELECT + "AVG(" + ENTITIES + ") as average" + FROM + TABLE_NAME + " t" +
                INNER_JOIN + ServerTable.TABLE_NAME + " s ON s." + ServerTable.ID + "=t." + SERVER_ID +
                WHERE + "s." + ServerTable.SERVER_UUID + "=?" +
                AND + ENTITIES + ">=0" +
                AND + DATE + "<?" +
                AND + DATE + ">?";
        return new QueryStatement<>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
                statement.setLong(2, before);
                statement.setLong(3, after);
            }

            @Override
            public Long processResults(ResultSet set) throws SQLException {
                return set.next() ? (long) set.getDouble("average") : -1L;
            }
        };
    }

    public static Query<Long> maxFreeDisk(long after, long before, ServerUUID serverUUID) {
        String sql = SELECT + "MAX(" + FREE_DISK + ") as free" + FROM + TABLE_NAME + " t" +
                INNER_JOIN + ServerTable.TABLE_NAME + " s ON s." + ServerTable.ID + "=t." + SERVER_ID +
                WHERE + "s." + ServerTable.SERVER_UUID + "=?" +
                AND + FREE_DISK + ">=0" +
                AND + DATE + "<?" +
                AND + DATE + ">?";
        return new QueryStatement<>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
                statement.setLong(2, before);
                statement.setLong(3, after);
            }

            @Override
            public Long processResults(ResultSet set) throws SQLException {
                return set.next() ? set.getLong("free") : -1L;
            }
        };
    }

    public static Query<Long> minFreeDisk(long after, long before, ServerUUID serverUUID) {
        String sql = SELECT + "MIN(" + FREE_DISK + ") as free" + FROM + TABLE_NAME + " t" +
                INNER_JOIN + ServerTable.TABLE_NAME + " s ON s." + ServerTable.ID + "=t." + SERVER_ID +
                WHERE + "s." + ServerTable.SERVER_UUID + "=?" +
                AND + FREE_DISK + ">=0" +
                AND + DATE + "<?" +
                AND + DATE + ">?";
        return new QueryStatement<>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
                statement.setLong(2, before);
                statement.setLong(3, after);
            }

            @Override
            public Long processResults(ResultSet set) throws SQLException {
                return set.next() ? set.getLong("free") : -1L;
            }
        };
    }

    public static Query<Long> averageFreeDisk(long after, long before, ServerUUID serverUUID) {
        String sql = SELECT + "AVG(" + FREE_DISK + ") as average" + FROM + TABLE_NAME + " t" +
                INNER_JOIN + ServerTable.TABLE_NAME + " s ON s." + ServerTable.ID + "=t." + SERVER_ID +
                WHERE + "s." + ServerTable.SERVER_UUID + "=?" +
                AND + FREE_DISK + ">=0" +
                AND + DATE + "<?" +
                AND + DATE + ">?";
        return new QueryStatement<>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
                statement.setLong(2, before);
                statement.setLong(3, after);
            }

            @Override
            public Long processResults(ResultSet set) throws SQLException {
                return set.next() ? (long) set.getDouble("average") : -1L;
            }
        };
    }

    public static Query<Optional<Long>> fetchLastStoredTpsDate(ServerUUID serverUUID) {
        @Language("SQL")
        String sql = SELECT + "MAX(" + DATE + ")" + FROM + TABLE_NAME + " t" +
                INNER_JOIN + ServerTable.TABLE_NAME + " s ON s." + ServerTable.ID + "=t." + SERVER_ID +
                WHERE + "s." + ServerTable.SERVER_UUID + "=?";
        return db -> db.queryOptional(sql, resultSet -> resultSet.getLong(1), serverUUID.toString());
    }

    public static Query<Map<Integer, List<TPS>>> fetchTPSDataOfServers(long after, long before, Collection<ServerUUID> serverUUIDs) {
        String sql = SELECT + "*" + FROM + TABLE_NAME +
                WHERE + SERVER_ID + " IN " + ServerTable.selectServerIds(serverUUIDs) +
                AND + DATE + ">=?" +
                AND + DATE + "<=?" +
                ORDER_BY + DATE;
        return new QueryStatement<>(sql, 50000) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setLong(1, after);
                statement.setLong(2, before);
            }

            @Override
            public Map<Integer, List<TPS>> processResults(ResultSet set) throws SQLException {
                Map<Integer, List<TPS>> data = new HashMap<>();
                while (set.next()) {
                    int serverId = set.getInt(SERVER_ID);
                    data.computeIfAbsent(serverId, Lists::create)
                            .add(extractTPS(set));
                }
                return data;
            }
        };
    }

    @Benchmark.Slow("1s")
    public static Query<Optional<Long>> fetchLatestServerStartTime(ServerUUID serverUUID, long dataGapThreshold) {
        String selectPreviousRowNumber = SELECT +
                "-1+ROW_NUMBER() over (ORDER BY " + DATE + ") AS previous_rn, " +
                SERVER_ID + ',' +
                DATE + " AS d1" +
                FROM + TABLE_NAME + " t1_in" +
                INNER_JOIN + ServerTable.TABLE_NAME + " s1_in ON s1_in." + ServerTable.ID + "=t1_in." + SERVER_ID +
                WHERE + "s1_in." + ServerTable.SERVER_UUID + "=?" +
                GROUP_BY + SERVER_ID + ',' + DATE +
                ORDER_BY + "d1 DESC";
        String selectRowNumber = SELECT +
                "ROW_NUMBER() over (ORDER BY " + DATE + ") AS rn, " +
                SERVER_ID + ',' +
                DATE + " AS previous_date" +
                FROM + TABLE_NAME + " t2_in" +
                INNER_JOIN + ServerTable.TABLE_NAME + " s2_in ON s2_in." + ServerTable.ID + "=t2_in." + SERVER_ID +
                WHERE + "s2_in." + ServerTable.SERVER_UUID + "=?" +
                GROUP_BY + SERVER_ID + ',' + DATE +
                ORDER_BY + "previous_date DESC";

        String selectFirstEntryDate = SELECT +
                "MIN(" + DATE + ") as start_time," +
                SERVER_ID + " as server_id" +
                FROM + TABLE_NAME + " t3_in" +
                INNER_JOIN + ServerTable.TABLE_NAME + " s3_in ON s3_in." + ServerTable.ID + "=t3_in." + SERVER_ID +
                WHERE + "s3_in." + ServerTable.SERVER_UUID + "=?" +
                GROUP_BY + SERVER_ID;

        // Finds the start time since difference between d1 and previous date is a gap,
        // so d1 is always first entry after a gap in the data. MAX finds the latest.
        // Union ensures if there are no gaps to use the first date recorded.
        String selectStartTime = SELECT +
                "MAX(d1) AS start_time," +
                "t1." + SERVER_ID + " as server_id" +
                FROM + "(" + selectPreviousRowNumber + ") t1" +
                INNER_JOIN +
                "(" + selectRowNumber + ") t2 ON t1.previous_rn=t2.rn AND t2." + SERVER_ID + "=t1." + SERVER_ID +
                WHERE + "d1 - previous_date > ?" +
                GROUP_BY + "t1." + SERVER_ID +
                UNION + selectFirstEntryDate;

        return new QueryStatement<>(selectStartTime) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
                statement.setString(2, serverUUID.toString());
                statement.setLong(3, dataGapThreshold);
                statement.setString(4, serverUUID.toString());
            }

            @Override
            public Optional<Long> processResults(ResultSet set) throws SQLException {
                long startTime = 0;
                while (set.next()) {
                    long gotStartTime = set.getLong("start_time");
                    if (!set.wasNull()) {
                        startTime = Math.max(startTime, gotStartTime);
                    }
                }
                return startTime != 0 ? Optional.of(startTime) : Optional.empty();
            }
        };
    }

    public static Query<List<Row>> fetchRows(int currentId, int rowLimit) {
        String sql = Select.all(TABLE_NAME)
                .where(TPSTable.ID + '>' + currentId)
                .orderBy(TPSTable.ID)
                .limit(rowLimit)
                .toString();
        return db -> db.queryList(sql, Row::extract);
    }
}