package main.java.com.djrapitops.plan.database.tables;

import com.djrapitops.plugin.api.TimeAmount;
import main.java.com.djrapitops.plan.Plan;
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
import java.util.ArrayList;
import java.util.List;

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

    /**
     * @param db
     * @param usingMySQL
     */
    public TPSTable(SQLDB db, boolean usingMySQL) {
        super("plan_tps", db, usingMySQL);
        serverTable = db.getServerTable();
    }

    @Override
    public boolean createTable() {
        return createTable(TableSqlParser.createTable(tableName)
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
            statement.setFetchSize(5000);
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
            statement = prepareStatement("INSERT INTO " + tableName + " ("
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
                    + "?, ?, ?, ?, ?, ?, ?)");

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
        PreparedStatement statement = null;
        try {
            statement = prepareStatement("DELETE FROM " + tableName + " WHERE (" + columnDate + "<?)");
            // More than 2 Months ago.
            long fiveWeeks = TimeAmount.MONTH.ms() * 2L;
            statement.setLong(1, MiscUtils.getTime() - fiveWeeks);

            statement.execute();
            commit(statement.getConnection());
        } finally {
            close(statement);
        }
    }
}
