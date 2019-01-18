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
package com.djrapitops.plan.system.database.databases.sql.tables;

import com.djrapitops.plan.api.exceptions.database.DBInitException;
import com.djrapitops.plan.data.container.TPS;
import com.djrapitops.plan.data.container.builders.TPSBuilder;
import com.djrapitops.plan.db.SQLDB;
import com.djrapitops.plan.system.database.databases.sql.processing.ExecStatement;
import com.djrapitops.plan.system.database.databases.sql.processing.QueryAllStatement;
import com.djrapitops.plan.system.database.databases.sql.processing.QueryStatement;
import com.djrapitops.plan.system.database.databases.sql.statements.Column;
import com.djrapitops.plan.system.database.databases.sql.statements.Select;
import com.djrapitops.plan.system.database.databases.sql.statements.Sql;
import com.djrapitops.plan.system.database.databases.sql.statements.TableSqlParser;
import com.djrapitops.plan.system.info.server.Server;
import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.utilities.Verify;
import org.apache.commons.text.TextStringBuilder;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Table that is in charge of storing TPS, Players Online and Performance data.
 * <p>
 * Table Name: plan_tps
 * <p>
 * For contained columns {@link Col}
 *
 * @author Rsl1122
 */
public class TPSTable extends Table {

    public static final String TABLE_NAME = "plan_tps";

    public TPSTable(SQLDB db) {
        super(TABLE_NAME, db);
        serverTable = db.getServerTable();
        insertStatement = "INSERT INTO " + tableName + " ("
                + Col.SERVER_ID + ", "
                + Col.DATE + ", "
                + Col.TPS + ", "
                + Col.PLAYERS_ONLINE + ", "
                + Col.CPU_USAGE + ", "
                + Col.RAM_USAGE + ", "
                + Col.ENTITIES + ", "
                + Col.CHUNKS + ", "
                + Col.FREE_DISK
                + ") VALUES ("
                + serverTable.statementSelectServerID + ", "
                + "?, ?, ?, ?, ?, ?, ?, ?)";
    }

    private final ServerTable serverTable;
    private String insertStatement;

    @Override
    public void createTable() throws DBInitException {
        createTable(TableSqlParser.createTable(tableName)
                .column(Col.SERVER_ID, Sql.INT).notNull()
                .column(Col.DATE, Sql.LONG).notNull()
                .column(Col.TPS, Sql.DOUBLE).notNull()
                .column(Col.PLAYERS_ONLINE, Sql.INT).notNull()
                .column(Col.CPU_USAGE, Sql.DOUBLE).notNull()
                .column(Col.RAM_USAGE, Sql.LONG).notNull()
                .column(Col.ENTITIES, Sql.INT).notNull()
                .column(Col.CHUNKS, Sql.INT).notNull()
                .column(Col.FREE_DISK, Sql.LONG).notNull()
                .foreignKey(Col.SERVER_ID, serverTable.getTableName(), ServerTable.Col.SERVER_ID)
                .toString()
        );
    }

    public List<TPS> getTPSData(UUID serverUUID) {
        String sql = Select.all(tableName)
                .where(Col.SERVER_ID + "=" + serverTable.statementSelectServerID)
                .toString();

        return query(new QueryStatement<List<TPS>>(sql, 50000) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
            }

            @Override
            public List<TPS> processResults(ResultSet set) throws SQLException {
                List<TPS> data = new ArrayList<>();
                while (set.next()) {

                    TPS tps = TPSBuilder.get()
                            .date(set.getLong(Col.DATE.get()))
                            .tps(set.getDouble(Col.TPS.get()))
                            .playersOnline(set.getInt(Col.PLAYERS_ONLINE.get()))
                            .usedCPU(set.getDouble(Col.CPU_USAGE.get()))
                            .usedMemory(set.getLong(Col.RAM_USAGE.get()))
                            .entities(set.getInt(Col.ENTITIES.get()))
                            .chunksLoaded(set.getInt(Col.CHUNKS.get()))
                            .freeDiskSpace(set.getLong(Col.FREE_DISK.get()))
                            .toTPS();

                    data.add(tps);
                }
                return data;
            }
        });
    }

    public List<TPS> getTPSData() {
        return getTPSData(getServerUUID());
    }

    /**
     * Clean the TPS Table of old data.
     */
    public void clean() {
        Optional<TPS> allTimePeak = getAllTimePeak();
        int p = -1;
        if (allTimePeak.isPresent()) {
            p = allTimePeak.get().getPlayers();
        }
        final int pValue = p;

        String sql = "DELETE FROM " + tableName +
                " WHERE (" + Col.DATE + "<?)" +
                " AND (" + Col.PLAYERS_ONLINE + " != ?)";

        execute(new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                // More than 3 Months ago.
                long threeMonths = TimeAmount.MONTH.toMillis(3L);
                statement.setLong(1, System.currentTimeMillis() - threeMonths);
                statement.setInt(2, pValue);
            }
        });
    }

    public void insertTPS(TPS tps) {
        execute(new ExecStatement(insertStatement) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, getServerUUID().toString());
                statement.setLong(2, tps.getDate());
                statement.setDouble(3, tps.getTicksPerSecond());
                statement.setInt(4, tps.getPlayers());
                statement.setDouble(5, tps.getCPUUsage());
                statement.setLong(6, tps.getUsedMemory());
                statement.setDouble(7, tps.getEntityCount());
                statement.setDouble(8, tps.getChunksLoaded());
                statement.setLong(9, tps.getFreeDiskSpace());
            }
        });
    }

    public Optional<TPS> getPeakPlayerCount(UUID serverUUID, long afterDate) {
        String subStatement = "SELECT MAX(" + Col.PLAYERS_ONLINE + ") FROM " + tableName +
                " WHERE " + Col.SERVER_ID + "=" + serverTable.statementSelectServerID +
                " AND " + Col.DATE + ">= ?";
        String sql = Select.all(tableName)
                .where(Col.SERVER_ID + "=" + serverTable.statementSelectServerID)
                .and(Col.PLAYERS_ONLINE + "= (" + subStatement + ")")
                .and(Col.DATE + ">= ?")
                .toString();

        return query(new QueryStatement<Optional<TPS>>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
                statement.setString(2, serverUUID.toString());
                statement.setLong(3, afterDate);
                statement.setLong(4, afterDate);
            }

            @Override
            public Optional<TPS> processResults(ResultSet set) throws SQLException {
                if (set.next()) {
                    TPS tps = TPSBuilder.get()
                            .date(set.getLong(Col.DATE.get()))
                            .tps(set.getDouble(Col.TPS.get()))
                            .playersOnline(set.getInt(Col.PLAYERS_ONLINE.get()))
                            .usedCPU(set.getDouble(Col.CPU_USAGE.get()))
                            .usedMemory(set.getLong(Col.RAM_USAGE.get()))
                            .entities(set.getInt(Col.ENTITIES.get()))
                            .chunksLoaded(set.getInt(Col.CHUNKS.get()))
                            .freeDiskSpace(set.getLong(Col.FREE_DISK.get()))
                            .toTPS();

                    return Optional.of(tps);
                }
                return Optional.empty();
            }
        });
    }

    public Optional<TPS> getAllTimePeak(UUID serverUUID) {
        return getPeakPlayerCount(serverUUID, 0);
    }

    public Optional<TPS> getAllTimePeak() {
        return getPeakPlayerCount(0);
    }

    public Optional<TPS> getPeakPlayerCount(long afterDate) {
        return getPeakPlayerCount(getServerUUID(), afterDate);
    }

    public Map<UUID, List<TPS>> getAllTPS() {
        String serverIDColumn = serverTable + "." + ServerTable.Col.SERVER_ID;
        String serverUUIDColumn = serverTable + "." + ServerTable.Col.SERVER_UUID + " as s_uuid";
        String sql = "SELECT " +
                Col.DATE + ", " +
                Col.TPS + ", " +
                Col.PLAYERS_ONLINE + ", " +
                Col.CPU_USAGE + ", " +
                Col.RAM_USAGE + ", " +
                Col.ENTITIES + ", " +
                Col.CHUNKS + ", " +
                Col.FREE_DISK + ", " +
                serverUUIDColumn +
                " FROM " + tableName +
                " INNER JOIN " + serverTable + " on " + serverIDColumn + "=" + Col.SERVER_ID;

        return query(new QueryAllStatement<Map<UUID, List<TPS>>>(sql, 50000) {
            @Override
            public Map<UUID, List<TPS>> processResults(ResultSet set) throws SQLException {
                Map<UUID, List<TPS>> serverMap = new HashMap<>();
                while (set.next()) {
                    UUID serverUUID = UUID.fromString(set.getString("s_uuid"));

                    List<TPS> tpsList = serverMap.getOrDefault(serverUUID, new ArrayList<>());

                    TPS tps = TPSBuilder.get()
                            .date(set.getLong(Col.DATE.get()))
                            .tps(set.getDouble(Col.TPS.get()))
                            .playersOnline(set.getInt(Col.PLAYERS_ONLINE.get()))
                            .usedCPU(set.getDouble(Col.CPU_USAGE.get()))
                            .usedMemory(set.getLong(Col.RAM_USAGE.get()))
                            .entities(set.getInt(Col.ENTITIES.get()))
                            .chunksLoaded(set.getInt(Col.CHUNKS.get()))
                            .freeDiskSpace(set.getLong(Col.FREE_DISK.get()))
                            .toTPS();

                    tpsList.add(tps);
                    serverMap.put(serverUUID, tpsList);
                }
                return serverMap;
            }
        });
    }

    public List<TPS> getNetworkOnlineData() {
        Optional<Server> bungeeInfo = serverTable.getBungeeInfo();
        if (!bungeeInfo.isPresent()) {
            return new ArrayList<>();
        }
        UUID bungeeUUID = bungeeInfo.get().getUuid();

        String sql = "SELECT " +
                Col.DATE + ", " +
                Col.PLAYERS_ONLINE +
                " FROM " + tableName +
                " WHERE " + Col.SERVER_ID + "=" + serverTable.statementSelectServerID;

        return query(new QueryStatement<List<TPS>>(sql, 50000) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, bungeeUUID.toString());
            }

            @Override
            public List<TPS> processResults(ResultSet set) throws SQLException {
                List<TPS> tpsList = new ArrayList<>();
                while (set.next()) {

                    TPS tps = TPSBuilder.get()
                            .date(set.getLong(Col.DATE.get()))
                            .playersOnline(set.getInt(Col.PLAYERS_ONLINE.get()))
                            .toTPS();

                    tpsList.add(tps);
                }
                return tpsList;
            }
        });
    }

    public void insertAllTPS(Map<UUID, List<TPS>> allTPS) {
        if (Verify.isEmpty(allTPS)) {
            return;
        }

        executeBatch(new ExecStatement(insertStatement) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                // Every Server
                for (Map.Entry<UUID, List<TPS>> entry : allTPS.entrySet()) {
                    UUID serverUUID = entry.getKey();
                    // Every TPS Data point
                    List<TPS> tpsList = entry.getValue();
                    for (TPS tps : tpsList) {
                        statement.setString(1, serverUUID.toString());
                        statement.setLong(2, tps.getDate());
                        statement.setDouble(3, tps.getTicksPerSecond());
                        statement.setInt(4, tps.getPlayers());
                        statement.setDouble(5, tps.getCPUUsage());
                        statement.setLong(6, tps.getUsedMemory());
                        statement.setDouble(7, tps.getEntityCount());
                        statement.setDouble(8, tps.getChunksLoaded());
                        statement.setLong(9, tps.getFreeDiskSpace());
                        statement.addBatch();
                    }
                }
            }
        });
    }

    public Map<Integer, List<TPS>> getPlayersOnlineForServers(Collection<Server> servers) {
        if (servers.isEmpty()) {
            return new HashMap<>();
        }
        TextStringBuilder sql = new TextStringBuilder("SELECT ");
        sql.append(Col.SERVER_ID).append(", ")
                .append(Col.DATE).append(", ")
                .append(Col.PLAYERS_ONLINE)
                .append(" FROM ").append(tableName)
                .append(" WHERE ")
                .append(Col.DATE.get()).append(">").append(System.currentTimeMillis() - TimeAmount.WEEK.toMillis(2L))
                .append(" AND (");
        sql.appendWithSeparators(servers.stream().map(server -> Col.SERVER_ID + "=" + server.getId()).iterator(), " OR ");
        sql.append(")");

        return query(new QueryAllStatement<Map<Integer, List<TPS>>>(sql.toString(), 10000) {
            @Override
            public Map<Integer, List<TPS>> processResults(ResultSet set) throws SQLException {
                Map<Integer, List<TPS>> map = new HashMap<>();
                while (set.next()) {
                    int serverID = set.getInt(Col.SERVER_ID.get());
                    int playersOnline = set.getInt(Col.PLAYERS_ONLINE.get());
                    long date = set.getLong(Col.DATE.get());

                    List<TPS> tpsList = map.getOrDefault(serverID, new ArrayList<>());

                    TPS tps = TPSBuilder.get().date(date)
                            .playersOnline(playersOnline)
                            .toTPS();
                    tpsList.add(tps);

                    map.put(serverID, tpsList);
                }
                return map;
            }
        });
    }

    public enum Col implements Column {
        SERVER_ID("server_id"),
        DATE("date"),
        TPS("tps"),
        PLAYERS_ONLINE("players_online"),
        CPU_USAGE("cpu_usage"),
        RAM_USAGE("ram_usage"),
        ENTITIES("entities"),
        CHUNKS("chunks_loaded"),
        FREE_DISK("free_disk_space");

        private final String column;

        Col(String column) {
            this.column = column;
        }

        @Override
        public String get() {
            return toString();
        }

        @Override
        public String toString() {
            return column;
        }
    }
}
