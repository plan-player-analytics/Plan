/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.database.tables;

import main.java.com.djrapitops.plan.database.databases.SQLDB;
import main.java.com.djrapitops.plan.database.sql.Sql;
import main.java.com.djrapitops.plan.database.sql.TableSqlParser;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public class UserInfoTable extends UserIDTable {

    //TODO Server Specific Table
    private final String columnUserID = "user_ id";
    private final String columnRegistered = "registered";
    private final String columnOP = "opped";
    private final String columnBanned = "banned";
    private final String columnServerID = "server_id";

    private final ServerTable serverTable;

    public UserInfoTable(SQLDB db, boolean usingMySQL) {
        super("plan_user_info", db, usingMySQL);
        serverTable = db.getServerTable();
    }

    @Override
    public boolean createTable() {
        return createTable(TableSqlParser.createTable(tableName)
                .column(columnUserID, Sql.INT).notNull()
                .column(columnRegistered, Sql.LONG).notNull()
                .column(columnOP, Sql.BOOL).notNull().defaultValue(false)
                .column(columnBanned, Sql.BOOL).notNull().defaultValue(false)
                .column(columnServerID, Sql.INT).notNull()
                .foreignKey(columnUserID, usersTable.getTableName(), usersTable.getColumnID())
                .foreignKey(columnServerID, serverTable.getTableName(), serverTable.getColumnID())
                .toString());
    }
}