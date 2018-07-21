package com.djrapitops.plan.system.database.databases.sql.tables;

import com.djrapitops.plan.api.exceptions.database.DBInitException;
import com.djrapitops.plan.data.container.TPS;
import com.djrapitops.plan.data.container.builders.TPSBuilder;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;
import com.djrapitops.plan.system.database.databases.sql.processing.ExecStatement;
import com.djrapitops.plan.system.database.databases.sql.processing.QueryAllStatement;
import com.djrapitops.plan.system.database.databases.sql.processing.QueryStatement;
import com.djrapitops.plan.system.database.databases.sql.statements.Column;
import com.djrapitops.plan.system.database.databases.sql.statements.Select;
import com.djrapitops.plan.system.database.databases.sql.statements.Sql;
import com.djrapitops.plan.system.database.databases.sql.statements.TableSqlParser;
import com.djrapitops.plan.system.info.server.Server;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.utilities.Verify;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Table that is in charge of storing TPS, Players Online & Performance data.
 * <p>
 * Table Name: plan_tps
 * <p>
 * For contained columns {@see Col}
 *
 * @author Rsl1122
 */
public class TPSTable extends Table {

    private final ServerTable serverTable;
    private String insertStatement;
    public TPSTable(SQLDB db) {
        super("plan_tps", db);
        serverTable = db.getServerTable();
        insertStatement = "INSERT INTO " + tableName + " ("
                + Col.SERVER_ID + ", "
                + Col.DATE + ", "
                + Col.TPS + ", "
                + Col.PLAYERS_ONLINE + ", "
                + Col.CPU_USAGE + ", "
                + Col.RAM_USAGE + ", "
                + Col.ENTITIES + ", "
                + Col.CHUNKS
                + ") VALUES ("
                + serverTable.statementSelectServerID + ", "
                + "?, ?, ?, ?, ?, ?, ?)";
    }

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
                            .toTPS();

                    data.add(tps);
                }
                return data;
            }
        });
    }

    /**
     * @return @throws SQLException
     */
    public List<TPS> getTPSData() {
        return getTPSData(ServerInfo.getServerUUID());
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
                long threeMonths = TimeAmount.MONTH.ms() * 3L;
                statement.setLong(1, System.currentTimeMillis() - threeMonths);
                statement.setInt(2, pValue);
            }
        });
    }

    public void insertTPS(TPS tps) {
        execute(new ExecStatement(insertStatement) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, ServerInfo.getServerUUID().toString());
                statement.setLong(2, tps.getDate());
                statement.setDouble(3, tps.getTicksPerSecond());
                statement.setInt(4, tps.getPlayers());
                statement.setDouble(5, tps.getCPUUsage());
                statement.setLong(6, tps.getUsedMemory());
                statement.setDouble(7, tps.getEntityCount());
                statement.setDouble(8, tps.getChunksLoaded());
            }
        });
    }

    public Optional<TPS> getPeakPlayerCount(UUID serverUUID, long afterDate) {
        String sql = Select.all(tableName)
                .where(Col.SERVER_ID + "=" + serverTable.statementSelectServerID)
                .and(Col.PLAYERS_ONLINE + "= (SELECT MAX(" + Col.PLAYERS_ONLINE + ") FROM " + tableName + ")")
                .and(Col.DATE + ">= ?")
                .toString();

        return query(new QueryStatement<Optional<TPS>>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
                statement.setLong(2, afterDate);
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
        return getPeakPlayerCount(ServerInfo.getServerUUID(), afterDate);
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
                            .skipTPS()
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
                        statement.addBatch();
                    }
                }
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
        CHUNKS("chunks_loaded");

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
