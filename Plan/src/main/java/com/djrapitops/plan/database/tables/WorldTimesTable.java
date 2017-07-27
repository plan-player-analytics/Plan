package main.java.com.djrapitops.plan.database.tables;

import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.database.databases.SQLDB;

import java.sql.SQLException;

/**
 * Table class representing database table plan_world_times.
 *
 * @author Rsl1122
 * @since 3.6.0 / Database version 7
 */
public class WorldTimesTable extends Table {

    private final WorldTable worldTable;

    private final String columnWorldId;
    private final String columnUserId;
    private final String columnPlaytime;

    /**
     * Constructor.
     *
     * @param db         Database this table is a part of.
     * @param usingMySQL Database is a MySQL database.
     */
    public WorldTimesTable(SQLDB db, boolean usingMySQL) {
        super("plan_world_times", db, usingMySQL);
        worldTable = db.getWorldTable();
        columnWorldId = "world_id";
        columnUserId = "user_id";
        columnPlaytime = "playtime";
    }

    @Override
    public boolean createTable() {
        UsersTable usersTable = db.getUsersTable();
        try {
            execute("CREATE TABLE IF NOT EXISTS " + tableName + " ("
                    + columnUserId + " integer NOT NULL, "
                    + columnWorldId + " integer NOT NULL, "
                    + columnPlaytime + " bigint NOT NULL, "
                    + "FOREIGN KEY(" + columnUserId + ") REFERENCES " + usersTable.getTableName() + "(" + usersTable.getColumnID() + "), "
                    + "FOREIGN KEY(" + columnWorldId + ") REFERENCES " + worldTable.getTableName() + "(" + worldTable.getColumnID() + ")"
                    + ")"
            );
            return true;
        } catch (SQLException ex) {
            Log.toLog(this.getClass().getName(), ex);
            return false;
        }
    }
}
