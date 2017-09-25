package main.java.com.djrapitops.plan.database.tables;

import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.api.exceptions.DBCreateTableException;
import main.java.com.djrapitops.plan.data.TPS;
import main.java.com.djrapitops.plan.database.databases.SQLDB;
import main.java.com.djrapitops.plan.database.sql.Select;
import main.java.com.djrapitops.plan.database.sql.Sql;
import main.java.com.djrapitops.plan.database.sql.TableSqlParser;
import main.java.com.djrapitops.plan.utilities.Benchmark;
import main.java.com.djrapitops.plan.utilities.MiscUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Class representing database table plan_tps
 *
 * @author Rsl1122
 * @since 3.5.0
 */
public class TPSTable extends Table {

    private final String columnServerID = "server_id";
    private final String columnDate = "date";
    private final String columnTPS = "tps";
    private final String columnPlayers = "players_online";
    private final String columnCPUUsage = "cpu_usage";
    private final String columnRAMUsage = "ram_usage";
    private final String columnEntities = "entities";
    private final String columnChunksLoaded = "chunks_loaded";

    private final ServerTable serverTable;
    private String insertStatement;

    /**
     * @param db
     * @param usingMySQL
     */
    public TPSTable(SQLDB db, boolean usingMySQL) {
        super("plan_tps", db, usingMySQL);
        serverTable = db.getServerTable();
        insertStatement = "INSERT INTO " + tableName + " ("
                + columnServerID + ", "
                + columnDate + ", "
                + columnTPS + ", "
                + columnPlayers + ", "
                + columnCPUUsage + ", "
                + columnRAMUsage + ", "
                + columnEntities + ", "
                + columnChunksLoaded
                + ") VALUES ("
                + serverTable.statementSelectServerID + ", "
                + "?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    public void createTable() throws DBCreateTableException {
        createTable(TableSqlParser.createTable(tableName)
                .column(columnServerID, Sql.INT).notNull()
                .column(columnDate, Sql.LONG).notNull()
                .column(columnTPS, Sql.DOUBLE).notNull()
                .column(columnPlayers, Sql.INT).notNull()
                .column(columnCPUUsage, Sql.DOUBLE).notNull()
                .column(columnRAMUsage, Sql.LONG).notNull()
                .column(columnEntities, Sql.INT).notNull()
                .column(columnChunksLoaded, Sql.INT).notNull()
                .foreignKey(columnServerID, serverTable.getTableName(), serverTable.getColumnID())
                .toString()
        );
    }

    /**
     * @return @throws SQLException
     */
    public List<TPS> getTPSData() throws SQLException {
        Benchmark.start("Get TPS");
        List<TPS> data = new ArrayList<>();
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            statement = prepareStatement(Select.all(tableName)
                    .where(columnServerID + "=" + serverTable.statementSelectServerID)
                    .toString());
            statement.setFetchSize(20000);
            statement.setString(1, Plan.getServerUUID().toString());
            set = statement.executeQuery();
            while (set.next()) {
                long date = set.getLong(columnDate);
                double tps = set.getDouble(columnTPS);
                int players = set.getInt(columnPlayers);
                double cpuUsage = set.getDouble(columnCPUUsage);
                long ramUsage = set.getLong(columnRAMUsage);
                int entities = set.getInt(columnEntities);
                int chunksLoaded = set.getInt(columnChunksLoaded);
                data.add(new TPS(date, tps, players, cpuUsage, ramUsage, entities, chunksLoaded));
            }
            return data;
        } finally {
            endTransaction(statement);
            close(set, statement);
            Benchmark.stop("Database", "Get TPS");
        }
    }

    public void insertTPS(TPS tps) throws SQLException {
        PreparedStatement statement = null;
        try {
            statement = prepareStatement(insertStatement);

            statement.setString(1, Plan.getServerUUID().toString());
            statement.setLong(2, tps.getDate());
            statement.setDouble(3, tps.getTicksPerSecond());
            statement.setInt(4, tps.getPlayers());
            statement.setDouble(5, tps.getCPUUsage());
            statement.setLong(6, tps.getUsedMemory());
            statement.setDouble(7, tps.getEntityCount());
            statement.setDouble(8, tps.getChunksLoaded());

            statement.execute();
            commit(statement.getConnection());
        } finally {
            close(statement);
        }
    }

    /**
     * @throws SQLException
     */
    public void clean() throws SQLException {
        Optional<TPS> allTimePeak = getAllTimePeak();
        int p = -1;
        if (allTimePeak.isPresent()) {
            p = allTimePeak.get().getPlayers();
        }
        PreparedStatement statement = null;
        try {
            statement = prepareStatement("DELETE FROM " + tableName +
                    " WHERE (" + columnDate + "<?)" +
                    " AND (" + columnPlayers + "" +
                    " != ?)");
            statement.setInt(1, p);
            // More than 2 Months ago.
            long fiveWeeks = TimeAmount.MONTH.ms() * 2L;
            statement.setLong(2, MiscUtils.getTime() - fiveWeeks);

            statement.execute();
            commit(statement.getConnection());
        } finally {
            close(statement);
        }
    }

    public Optional<TPS> getAllTimePeak(UUID serverUUID) throws SQLException {
        return getPeakPlayerCount(serverUUID, 0);
    }

    public Optional<TPS> getAllTimePeak() throws SQLException {
        return getPeakPlayerCount(0);
    }

    public Optional<TPS> getPeakPlayerCount(long afterDate) throws SQLException {
        return getPeakPlayerCount(MiscUtils.getIPlan().getServerUuid(), afterDate);
    }

    public Optional<TPS> getPeakPlayerCount(UUID serverUUID, long afterDate) throws SQLException {
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            statement = prepareStatement(Select.all(tableName)
                    .where(columnServerID + "=" + serverTable.statementSelectServerID)
                    .and(columnPlayers + "= (SELECT MAX(" + columnPlayers + ") FROM " + tableName + ")")
                    .and(columnDate + ">= ?")
                    .toString());
            statement.setString(1, serverUUID.toString());
            statement.setLong(2, afterDate);
            set = statement.executeQuery();
            if (set.next()) {
                long date = set.getLong(columnDate);
                double tps = set.getDouble(columnTPS);
                int players = set.getInt(columnPlayers);
                double cpuUsage = set.getDouble(columnCPUUsage);
                long ramUsage = set.getLong(columnRAMUsage);
                int entities = set.getInt(columnEntities);
                int chunksLoaded = set.getInt(columnChunksLoaded);
                return Optional.of(new TPS(date, tps, players, cpuUsage, ramUsage, entities, chunksLoaded));
            }
            return Optional.empty();
        } finally {
            endTransaction(statement);
            close(set, statement);
        }
    }

    public Map<UUID, List<TPS>> getAllTPS() throws SQLException {
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            String serverIDColumn = serverTable + "." + serverTable.getColumnID();
            String serverUUIDColumn = serverTable + "." + serverTable.getColumnUUID() + " as s_uuid";
            statement = prepareStatement("SELECT " +
                    columnDate + ", " +
                    columnTPS + ", " +
                    columnPlayers + ", " +
                    columnCPUUsage + ", " +
                    columnRAMUsage + ", " +
                    columnEntities + ", " +
                    columnChunksLoaded + ", " +
                    serverUUIDColumn +
                    " FROM " + tableName +
                    " JOIN " + serverTable + " on " + serverIDColumn + "=" + columnServerID
            );
            statement.setFetchSize(20000);
            set = statement.executeQuery();
            Map<UUID, List<TPS>> serverMap = new HashMap<>();
            while (set.next()) {
                UUID serverUUID = UUID.fromString(set.getString("s_uuid"));

                List<TPS> tpsList = serverMap.getOrDefault(serverUUID, new ArrayList<>());

                long date = set.getLong(columnDate);
                double tps = set.getDouble(columnTPS);
                int players = set.getInt(columnPlayers);
                double cpuUsage = set.getDouble(columnCPUUsage);
                long ramUsage = set.getLong(columnRAMUsage);
                int entities = set.getInt(columnEntities);
                int chunksLoaded = set.getInt(columnChunksLoaded);

                tpsList.add(new TPS(date, tps, players, cpuUsage, ramUsage, entities, chunksLoaded));
                serverMap.put(serverUUID, tpsList);
            }
            return serverMap;
        } finally {
            endTransaction(statement);
            close(set, statement);
        }
    }

    public void insertAllTPS(Map<UUID, List<TPS>> allTPS) throws SQLException {
        if (Verify.isEmpty(allTPS)) {
            return;
        }
        PreparedStatement statement = null;
        try {
            statement = prepareStatement(insertStatement);

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

            statement.executeBatch();
            commit(statement.getConnection());
        } finally {
            close(statement);
        }
    }

    public List<TPS> getNetworkOnlineData() throws SQLException {
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            statement = prepareStatement("SELECT " +
                    columnDate + ", " +
                    "SUM(" + columnPlayers + ") as players" +
                    " FROM " + tableName +
                    " GROUP BY " + columnDate
            );
            statement.setFetchSize(50000);
            set = statement.executeQuery();
            List<TPS> tpsList = new ArrayList<>();
            while (set.next()) {
                long date = set.getLong(columnDate);
                int players = set.getInt("players");

                tpsList.add(new TPS(date, 0, players, 0, 0, 0, 0));
            }
            return tpsList;
        } finally {
            endTransaction(statement);
            close(set, statement);
        }
    }
}
