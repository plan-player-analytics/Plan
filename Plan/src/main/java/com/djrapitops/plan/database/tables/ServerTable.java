/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.database.tables;

import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.data.server.ServerInfo;
import main.java.com.djrapitops.plan.database.databases.SQLDB;
import main.java.com.djrapitops.plan.database.sql.Insert;
import main.java.com.djrapitops.plan.database.sql.Sql;
import main.java.com.djrapitops.plan.database.sql.TableSqlParser;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public class ServerTable extends Table {

    private final String columnServerID;
    private final String columnServerUUID;
    private final String columnServerName;
    private final String columnWebserverAddress;
    private final String columnWebserverPort;

    public ServerTable(String name, SQLDB db, boolean usingMySQL) {
        super("plan_servers", db, usingMySQL);
        columnServerID = "id";
        columnServerUUID = "uuid";
        columnServerName = "name";
        columnWebserverAddress = "web_address";
        columnWebserverPort = "web_port";
    }

    @Override
    public boolean createTable() {
        try {
            execute(TableSqlParser.createTable(tableName)
                    .primaryKeyIDColumn(usingMySQL, columnServerID, Sql.INT)
                    .column(columnServerUUID, Sql.varchar(36)).notNull().unique()
                    .column(columnServerName, Sql.varchar(100))
                    .column(columnWebserverAddress, Sql.varchar(100))
                    .column(columnWebserverPort, Sql.INT)
                    .toString());
            return true;
        } catch (SQLException ex) {
            Log.toLog(this.getClass().getName(), ex);
            return false;
        }
    }

    public void saveCurrentServerInfo(ServerInfo info) throws SQLException {
        if (info.getId() == -1) {
            saveNewServerInfo(info);
        } else {
            updateServerInfo(info);
        }

    }

    private void updateServerInfo(ServerInfo info) {
        //TODO Continue here, create Update SqlParser.
    }

    public void saveNewServerInfo(ServerInfo info) throws SQLException {
        PreparedStatement statement = null;
        try {
            statement = prepareStatement(Insert.values(tableName,
                    columnServerUUID,
                    columnServerName,
                    columnWebserverAddress,
                    columnWebserverPort));
            statement.setString(1, info.getUuid().toString());
            statement.setString(2, info.getName());
            statement.setString(3, info.getWebAddress());
            statement.setInt(4, info.getPort());
            statement.execute();
        } finally {
            close(statement);
        }
    }
}