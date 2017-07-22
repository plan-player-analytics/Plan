package main.java.com.djrapitops.plan.database.tables;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.database.databases.SQLDB;

/**
 *
 * @author Rsl1122
 */
@Deprecated
public class LocationsTable extends Table {

    private final String columnUserID;
    private final String columnID;
    private final String columnCoordinatesX;
    private final String columnCoordinatesZ;
    private final String columnWorld;

    @Override
    @Deprecated
    public boolean removeAllData() {
        try {
            execute("DELETE FROM " + tableName);
        } catch (Exception e) {
        }
        return true;
    }

    /**
     *
     * @param db
     * @param usingMySQL
     */
    @Deprecated
    public LocationsTable(SQLDB db, boolean usingMySQL) {
        super("plan_locations", db, usingMySQL);
        columnID = "id";
        columnUserID = "user_id";
        columnCoordinatesX = "x";
        columnCoordinatesZ = "z";
        columnWorld = "world_name";
    }

    /**
     *
     * @return
     */
    @Override
    @Deprecated
    public boolean createTable() {
        UsersTable usersTable = db.getUsersTable();
        try {
            execute("CREATE TABLE IF NOT EXISTS " + tableName + " ("
                    + columnID + " integer " + ((usingMySQL) ? "NOT NULL AUTO_INCREMENT" : "PRIMARY KEY") + ", "
                    + columnUserID + " integer NOT NULL, "
                    + columnCoordinatesX + " integer NOT NULL, "
                    + columnCoordinatesZ + " integer NOT NULL, "
                    + columnWorld + " varchar(64) NOT NULL, "
                    + (usingMySQL ? "PRIMARY KEY (" + usersTable.getColumnID() + "), " : "")
                    + "FOREIGN KEY(" + columnUserID + ") REFERENCES " + usersTable.getTableName() + "(" + usersTable.getColumnID() + ")"
                    + ")"
            );
            return true;
        } catch (SQLException ex) {
            Log.toLog(this.getClass().getName(), ex);
            return false;
        }
    }

    /**
     *
     * @param userId
     * @return
     */
    @Deprecated
    public boolean removeUserLocations(int userId) {
        PreparedStatement statement = null;
        try {
            statement = prepareStatement("DELETE FROM " + tableName + " WHERE (" + columnUserID + "=?)");
            statement.setInt(1, userId);
            statement.execute();
            return true;
        } catch (SQLException ex) {
            return true;
        } finally {
            close(statement);
        }
    }
}
