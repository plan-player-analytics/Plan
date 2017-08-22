package main.java.com.djrapitops.plan.database.tables;

import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.database.databases.SQLDB;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Represents a Table that uses UsersTable IDs to get their data.
 *
 * @author Rsl1122
 * @since 3.7.0
 */
public abstract class UserIDTable extends Table {

    protected final String columnUserID = "user_id";
    protected final UsersTable usersTable;

    public UserIDTable(String name, SQLDB db, boolean usingMySQL) {
        super(name, db, usingMySQL);
        usersTable = db.getUsersTable();
    }

    protected boolean removeDataOf(int userID) {
        PreparedStatement statement = null;
        try {
            statement = prepareStatement("DELETE FROM " + tableName + " WHERE (" + columnUserID + "=?)");
            statement.setInt(1, userID);
            statement.execute();
            return true;
        } catch (SQLException ex) {
            Log.toLog(this.getClass().getName(), ex);
            return false;
        } finally {
            close(statement);
        }
    }
}
