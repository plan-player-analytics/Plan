package main.java.com.djrapitops.plan.database.tables;

import com.djrapitops.plugin.api.TimeAmount;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.data.TPS;
import main.java.com.djrapitops.plan.database.DBUtils;
import main.java.com.djrapitops.plan.database.databases.SQLDB;
import main.java.com.djrapitops.plan.utilities.Benchmark;
import main.java.com.djrapitops.plan.utilities.MiscUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class representing database table plan_tps
 *
 * @author Rsl1122
 * @since 3.5.0
 */
public class TPSTable extends Table {

    private final String columnDate;
    private final String columnTPS;
    private final String columnPlayers;
    private final String columnCPUUsage;
    private final String columnRAMUsage;
    private final String columnEntities;
    private final String columnChunksLoaded;

    /**
     * @param db
     * @param usingMySQL
     */
    public TPSTable(SQLDB db, boolean usingMySQL) {
        super("plan_tps", db, usingMySQL);
        columnDate = "date";
        columnTPS = "tps";
        columnPlayers = "players_online";
        columnCPUUsage = "cpu_usage";
        columnRAMUsage = "ram_usage";
        columnEntities = "entities";
        columnChunksLoaded = "chunks_loaded";
    }

    @Override
    public boolean createTable() {
        try {
            execute("CREATE TABLE IF NOT EXISTS " + tableName + " ("
                    + columnDate + " bigint NOT NULL, "
                    + columnTPS + " double NOT NULL, "
                    + columnPlayers + " integer NOT NULL, "
                    + columnCPUUsage + " double NOT NULL, "
                    + columnRAMUsage + " bigint NOT NULL, "
                    + columnEntities + " integer NOT NULL, "
                    + columnChunksLoaded + " integer NOT NULL"
                    + ")"
            );
            int version = getVersion();
            if (version < 6) {
                alterTablesV6();
            }
            if (version < 7) {
                alterTablesV7();
            }
            return true;
        } catch (SQLException ex) {
            Log.toLog(this.getClass().getName(), ex);
            return false;
        }
    }

    private void alterTablesV6() {
        addColumns(columnCPUUsage + " double NOT NULL DEFAULT 0");
    }

    private void alterTablesV7() {
        addColumns(
                columnRAMUsage + " bigint NOT NULL DEFAULT 0",
                columnEntities + " integer NOT NULL DEFAULT 0",
                columnChunksLoaded + " integer NOT NULL DEFAULT 0"
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
            statement = prepareStatement("SELECT * FROM " + tableName);
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
            close(set);
            close(statement);
            Benchmark.stop("Database", "Get TPS");
        }
    }

    /**
     * @param data
     * @throws SQLException
     */
    public void saveTPSData(List<TPS> data) throws SQLException {
        List<List<TPS>> batches = DBUtils.splitIntoBatches(data);
        batches.forEach(batch -> {
            try {
                saveTPSBatch(batch);
            } catch (SQLException e) {
                Log.toLog("UsersTable.saveUserDataInformationBatch", e);
            }
        });
        db.setAvailable();
        commit();
    }

    private void saveTPSBatch(List<TPS> batch) throws SQLException {
        if (batch.isEmpty()) {
            return;
        }

        int batchSize = batch.size();
        Log.debug("Database", "Preparing insertion of TPS - Batch Size: " + batchSize);

        PreparedStatement statement = null;
        try {
            statement = prepareStatement("INSERT INTO " + tableName + " ("
                    + columnDate + ", "
                    + columnTPS + ", "
                    + columnPlayers + ", "
                    + columnCPUUsage + ", "
                    + columnRAMUsage + ", "
                    + columnEntities + ", "
                    + columnChunksLoaded
                    + ") VALUES (?, ?, ?, ?, ?, ?, ?)");

            for (TPS tps : batch) {
                statement.setLong(1, tps.getDate());
                statement.setDouble(2, tps.getTps());
                statement.setInt(3, tps.getPlayers());
                statement.setDouble(4, tps.getCPUUsage());
                statement.setLong(5, tps.getUsedMemory());
                statement.setDouble(6, tps.getEntityCount());
                statement.setDouble(7, tps.getChunksLoaded());
                statement.addBatch();
            }

            Log.debug("Database", "Executing tps batch: " + batchSize);
            statement.executeBatch();
        } finally {
            close(statement);
        }
    }

    /**
     * @throws SQLException
     */
    public void clean() throws SQLException {
        PreparedStatement statement = null;
        try {
            statement = prepareStatement("DELETE FROM " + tableName + " WHERE (" + columnDate + "<?)");
            // More than 5 Weeks ago.
            long fiveWeeks = TimeAmount.WEEK.ms() * 5L;
            statement.setLong(1, MiscUtils.getTime() - fiveWeeks);
            statement.execute();
        } finally {
            close(statement);
        }
    }
}
