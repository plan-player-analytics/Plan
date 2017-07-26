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
        //TODO add new columns
    }

    @Override
    public boolean createTable() {
        try {
            execute("CREATE TABLE IF NOT EXISTS " + tableName + " ("
                    + columnDate + " bigint NOT NULL, "
                    + columnTPS + " double NOT NULL, "
                    + columnPlayers + " integer NOT NULL, "
                    + columnCPUUsage + " double NOT NULL"
                    + ")"
                    //TODO add new columns
            );
            int version = getVersion();
            if (version < 6) {
                alterTablesV6();
            }
            return true;
        } catch (SQLException ex) {
            Log.toLog(this.getClass().getName(), ex);
            return false;
        }
    }

    private void alterTablesV6() {
        try {
            if (usingMySQL) {
                execute("ALTER TABLE " + tableName + " ADD " + columnCPUUsage + " double NOT NULL DEFAULT 0");
            } else {
                execute("ALTER TABLE " + tableName + " ADD COLUMN " + columnCPUUsage + " double NOT NULL DEFAULT 0");
            }
        } catch (SQLException e) {

        }
    }

    //TODO alterTablesV7

    /**
     * @return @throws SQLException
     */
    public List<TPS> getTPSData() throws SQLException {
        Benchmark.start("Database: Get TPS");
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
                //TODO add new data
                data.add(new TPS(date, tps, players, cpuUsage, 0, 0, 0));
            }
            return data;
        } finally {
            close(set);
            close(statement);
            Benchmark.stop("Database: Get TPS");
        }
    }

    /**
     * @param data
     * @throws SQLException
     */
    public void saveTPSData(List<TPS> data) throws SQLException {
        List<List<TPS>> batches = DBUtils.splitIntoBatches(data);
        for (List<TPS> batch : batches) {
            saveTPSBatch(batch);
        }
    }

    private void saveTPSBatch(List<TPS> batch) throws SQLException {
        PreparedStatement statement = null;
        try {
            statement = prepareStatement("INSERT INTO " + tableName + " ("
                    + columnDate + ", "
                    + columnTPS + ", "
                    + columnPlayers + ", "
                    + columnCPUUsage
                    + ") VALUES (?, ?, ?, ?)");

            boolean commitRequired = false;
            int i = 0;
            for (TPS tps : batch) {
                statement.setLong(1, tps.getDate());
                statement.setDouble(2, tps.getTps());
                statement.setInt(3, tps.getPlayers());
                statement.setDouble(4, tps.getCPUUsage());
                statement.addBatch();
                commitRequired = true;
                i++;
            }
            if (commitRequired) {
                Log.debug("Executing tps batch: " + i);
                statement.executeBatch();
            }
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
