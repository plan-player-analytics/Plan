package com.djrapitops.plan.system.database.databases.sql.tables;

import com.djrapitops.plan.system.database.databases.sql.SQLDB;
import com.djrapitops.plan.system.database.databases.sql.processing.ExecStatement;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Represents a Table that uses UsersTable IDs to get their data.
 *
 * @author Rsl1122
 * @since 3.7.0
 */
public abstract class UserIDTable extends Table {

    protected final String columnUserID = "user_id";
    protected final UsersTable usersTable;

    public UserIDTable(String name, SQLDB db) {
        super(name, db);
        usersTable = db.getUsersTable();
    }

    public void removeUser(UUID uuid) throws SQLException {
        String sql = "DELETE FROM " + tableName + " WHERE (" + columnUserID + "=" + usersTable.statementSelectID + ")";

        execute(new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, uuid.toString());
            }
        });
    }
}
