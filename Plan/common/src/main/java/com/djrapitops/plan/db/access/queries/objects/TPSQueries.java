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
package com.djrapitops.plan.db.access.queries.objects;

import com.djrapitops.plan.data.container.TPS;
import com.djrapitops.plan.data.container.builders.TPSBuilder;
import com.djrapitops.plan.data.store.objects.DateObj;
import com.djrapitops.plan.db.access.Query;
import com.djrapitops.plan.db.access.QueryAllStatement;
import com.djrapitops.plan.db.access.QueryStatement;
import com.djrapitops.plan.db.sql.parsing.Select;
import com.djrapitops.plan.db.sql.tables.ServerTable;
import com.djrapitops.plan.system.info.server.Server;
import com.djrapitops.plugin.api.TimeAmount;
import org.apache.commons.text.TextStringBuilder;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static com.djrapitops.plan.db.sql.parsing.Sql.*;
import static com.djrapitops.plan.db.sql.tables.TPSTable.*;

/**
 * Queries for {@link com.djrapitops.plan.data.container.TPS} objects.
 *
 * @author Rsl1122
 */
public class TPSQueries {

    private TPSQueries() {
        /* Static method class */
    }

    public static Query<List<TPS>> fetchTPSDataOfServer(UUID serverUUID) {
        String sql = Select.all(TABLE_NAME)
                .where(SERVER_ID + "=" + ServerTable.STATEMENT_SELECT_SERVER_ID)
                .toString();

        return new QueryStatement<List<TPS>>(sql, 50000) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
            }

            @Override
            public List<TPS> processResults(ResultSet set) throws SQLException {
                List<TPS> data = new ArrayList<>();
                while (set.next()) {

                    TPS tps = TPSBuilder.get()
                            .date(set.getLong(DATE))
                            .tps(set.getDouble(TPS))
                            .playersOnline(set.getInt(PLAYERS_ONLINE))
                            .usedCPU(set.getDouble(CPU_USAGE))
                            .usedMemory(set.getLong(RAM_USAGE))
                            .entities(set.getInt(ENTITIES))
                            .chunksLoaded(set.getInt(CHUNKS))
                            .freeDiskSpace(set.getLong(FREE_DISK))
                            .toTPS();

                    data.add(tps);
                }
                return data;
            }
        };
    }

    public static Query<List<TPS>> fetchTPSDataOfServer(long after, long before, UUID serverUUID) {
        String sql = Select.all(TABLE_NAME)
                .where(SERVER_ID + "=" + ServerTable.STATEMENT_SELECT_SERVER_ID)
                .and(DATE + ">=?").and(DATE + "<=?")
                .toString();

        return new QueryStatement<List<TPS>>(sql, 50000) {
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

                    TPS tps = TPSBuilder.get()
                            .date(set.getLong(DATE))
                            .tps(set.getDouble(TPS))
                            .playersOnline(set.getInt(PLAYERS_ONLINE))
                            .usedCPU(set.getDouble(CPU_USAGE))
                            .usedMemory(set.getLong(RAM_USAGE))
                            .entities(set.getInt(ENTITIES))
                            .chunksLoaded(set.getInt(CHUNKS))
                            .freeDiskSpace(set.getLong(FREE_DISK))
                            .toTPS();

                    data.add(tps);
                }
                return data;
            }
        };
    }

    public static Query<List<DateObj<Integer>>> fetchPlayersOnlineOfServer(long after, long before, UUID serverUUID) {
        String sql = SELECT + ServerTable.SERVER_UUID + ',' + DATE + ',' + PLAYERS_ONLINE +
                FROM + TABLE_NAME +
                INNER_JOIN + ServerTable.TABLE_NAME + " on " + ServerTable.TABLE_NAME + '.' + ServerTable.SERVER_ID + '=' + SERVER_ID +
                WHERE + ServerTable.SERVER_UUID + "=?" +
                AND + DATE + "<?" +
                AND + DATE + ">?";
        return new QueryStatement<List<DateObj<Integer>>>(sql, 1000) {
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

    public static Query<Map<UUID, List<DateObj<Integer>>>> fetchPlayersOnlineOfAllServersBut(long after, long before, UUID leaveOut) {
        String sql = SELECT + ServerTable.SERVER_UUID + ',' + DATE + ',' + PLAYERS_ONLINE +
                FROM + TABLE_NAME +
                INNER_JOIN + ServerTable.TABLE_NAME + " on " + ServerTable.TABLE_NAME + '.' + ServerTable.SERVER_ID + '=' + SERVER_ID +
                WHERE + ServerTable.SERVER_UUID + "!=?" +
                AND + ServerTable.INSTALLED + "=?" +
                AND + DATE + "<?" +
                AND + DATE + ">?";
        return new QueryStatement<Map<UUID, List<DateObj<Integer>>>>(sql, 1000) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, leaveOut.toString());
                statement.setBoolean(2, true);
                statement.setLong(3, before);
                statement.setLong(4, after);
            }

            @Override
            public Map<UUID, List<DateObj<Integer>>> processResults(ResultSet set) throws SQLException {
                Map<UUID, List<DateObj<Integer>>> byServer = new HashMap<>();

                while (set.next()) {
                    UUID serverUUID = UUID.fromString(set.getString(ServerTable.SERVER_UUID));
                    List<DateObj<Integer>> ofServer = byServer.getOrDefault(serverUUID, new ArrayList<>());
                    ofServer.add(new DateObj<>(set.getLong(DATE), set.getInt(PLAYERS_ONLINE)));
                    byServer.put(serverUUID, ofServer);
                }

                return byServer;
            }
        };
    }

    public static Query<Map<Integer, List<TPS>>> fetchPlayerOnlineDataOfServers(Collection<Server> servers) {
        if (servers.isEmpty()) {
            return db -> new HashMap<>();
        }

        TextStringBuilder sql = new TextStringBuilder(SELECT);
        sql.append(SERVER_ID).append(',')
                .append(DATE).append(',')
                .append(PLAYERS_ONLINE)
                .append(FROM).append(TABLE_NAME)
                .append(WHERE).append(DATE).append(">").append(System.currentTimeMillis() - TimeAmount.WEEK.toMillis(2L))
                .append(AND).append('(');
        sql.appendWithSeparators(servers.stream().map(server -> SERVER_ID + "=" + server.getId()).iterator(), OR);
        sql.append(')');

        return new QueryAllStatement<Map<Integer, List<TPS>>>(sql.toString(), 10000) {
            @Override
            public Map<Integer, List<TPS>> processResults(ResultSet set) throws SQLException {
                Map<Integer, List<TPS>> map = new HashMap<>();
                while (set.next()) {
                    int serverID = set.getInt(SERVER_ID);
                    int playersOnline = set.getInt(PLAYERS_ONLINE);
                    long date = set.getLong(DATE);

                    List<TPS> tpsList = map.getOrDefault(serverID, new ArrayList<>());

                    TPS tps = TPSBuilder.get().date(date)
                            .playersOnline(playersOnline)
                            .toTPS();
                    tpsList.add(tps);

                    map.put(serverID, tpsList);
                }
                return map;
            }
        };
    }

    public static Query<Optional<DateObj<Integer>>> fetchPeakPlayerCount(UUID serverUUID, long afterDate) {
        String subQuery = '(' + SELECT + "MAX(" + PLAYERS_ONLINE + ')' + FROM + TABLE_NAME + WHERE + SERVER_ID + "=" + ServerTable.STATEMENT_SELECT_SERVER_ID +
                AND + DATE + ">= ?)";
        String sql = SELECT +
                DATE + ',' + PLAYERS_ONLINE +
                FROM + TABLE_NAME +
                WHERE + SERVER_ID + "=" + ServerTable.STATEMENT_SELECT_SERVER_ID +
                AND + DATE + ">= ?" +
                AND + PLAYERS_ONLINE + "=" + subQuery +
                ORDER_BY + DATE + " DESC LIMIT 1";

        return new QueryStatement<Optional<DateObj<Integer>>>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
                statement.setLong(2, afterDate);
                statement.setString(3, serverUUID.toString());
                statement.setLong(4, afterDate);
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

    public static Query<Optional<DateObj<Integer>>> fetchAllTimePeakPlayerCount(UUID serverUUID) {
        return fetchPeakPlayerCount(serverUUID, 0);
    }

    public static Query<Optional<TPS>> fetchLatestTPSEntryForServer(UUID serverUUID) {
        String sql = SELECT + "*" +
                FROM + TABLE_NAME +
                WHERE + SERVER_ID + '=' + ServerTable.STATEMENT_SELECT_SERVER_ID +
                ORDER_BY + DATE + " DESC LIMIT 1";

        return new QueryStatement<Optional<TPS>>(sql) {
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
}