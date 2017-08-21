/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.database.tables;

import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.data.server.ServerInfo;
import main.java.com.djrapitops.plan.database.databases.SQLDB;
import main.java.com.djrapitops.plan.database.sql.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    private final String columnInstalled;

    public ServerTable(String name, SQLDB db, boolean usingMySQL) {
        super("plan_servers", db, usingMySQL);
        columnServerID = "id";
        columnServerUUID = "uuid";
        columnServerName = "name";
        columnWebserverAddress = "web_address";
        columnInstalled = "is_installed";
    }

    @Override
    public boolean createTable() {
        try {
            execute(TableSqlParser.createTable(tableName)
                    .primaryKeyIDColumn(usingMySQL, columnServerID, Sql.INT)
                    .column(columnServerUUID, Sql.varchar(36)).notNull().unique()
                    .column(columnServerName, Sql.varchar(100))
                    .column(columnWebserverAddress, Sql.varchar(100))
                    .column(columnInstalled, Sql.BOOL).notNull().defaultValue(false)
                    .primaryKey(usingMySQL, columnServerID)
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

    private void updateServerInfo(ServerInfo info) throws SQLException {
        PreparedStatement statement = null;
        try {
            statement = prepareStatement(Update.values(tableName,
                    columnServerUUID,
                    columnServerName,
                    columnWebserverAddress,
                    columnInstalled)
                    .where(columnServerID + "=?")
                    .toString()
            );
            statement.setString(1, info.getUuid().toString());
            statement.setString(2, info.getName());
            statement.setString(3, info.getWebAddress());
            statement.setBoolean(4, true);
            statement.setInt(5, info.getId());
            statement.executeUpdate();
        } finally {
            close(statement);
        }
    }

    /**
     * Inserts new row for a server into the table.
     *
     * @param info Info to instert (All variables should be present.
     * @throws IllegalStateException if one of the ServerInfo variables is null
     * @throws SQLException
     */
    private void saveNewServerInfo(ServerInfo info) throws SQLException {
        UUID uuid = info.getUuid();
        String name = info.getName();
        String webAddress = info.getWebAddress();
        Verify.nullCheck(uuid, name, webAddress);
        PreparedStatement statement = null;
        try {
            statement = prepareStatement(Insert.values(tableName,
                    columnServerUUID,
                    columnServerName,
                    columnWebserverAddress,
                    columnInstalled));

            statement.setString(1, uuid.toString());
            statement.setString(2, name);
            statement.setString(3, webAddress);
            statement.setBoolean(4, true);
            statement.execute();
        } finally {
            close(statement);
        }
    }

    /**
     * Returns server ID for a matching UUID
     *
     * @param serverUUID UUID of the server.
     * @return ID or or empty optional.
     * @throws SQLException
     */
    public Optional<Integer> getServerID(UUID serverUUID) throws SQLException {
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            statement = prepareStatement(Select.from(tableName,
                    columnServerID)
                    .where(columnServerUUID + "=?")
                    .toString());
            statement.setString(1, serverUUID.toString());
            set = statement.executeQuery();
            if (set.next()) {
                return Optional.of(set.getInt(columnServerID));
            } else {
                return Optional.empty();
            }
        } finally {
            close(set, statement);
        }
    }

    /**
     * Returns server Name for a matching UUID
     *
     * @param serverUUID UUID of the server.
     * @return Name or empty optional.
     * @throws SQLException
     */
    public Optional<String> getServerName(UUID serverUUID) throws SQLException {
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            statement = prepareStatement(Select.from(tableName,
                    columnServerName)
                    .where(columnServerUUID + "=?")
                    .toString());
            statement.setString(1, serverUUID.toString());
            set = statement.executeQuery();
            if (set.next()) {
                return Optional.of(set.getString(columnServerName));
            } else {
                return Optional.empty();
            }
        } finally {
            close(set, statement);
        }
    }

    /**
     * Used to get BungeeCord WebServer info if present.
     *
     * @return information about Bungee server.
     * @throws SQLException
     */
    public Optional<ServerInfo> getBungeeInfo() throws SQLException {
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            statement = prepareStatement(Select.from(tableName, "*")
                    .where(columnServerName + "=?")
                    .toString());
            statement.setString(1, "BungeeCord");
            set = statement.executeQuery();
            if (set.next()) {
                return Optional.of(new ServerInfo(
                        set.getInt(columnServerID),
                        UUID.fromString(set.getString(columnServerUUID)),
                        set.getString(columnServerName),
                        set.getString(columnWebserverAddress)));
            } else {
                return Optional.empty();
            }
        } finally {
            close(set, statement);
        }
    }

    public List<ServerInfo> getBukkitServers() throws SQLException {
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            statement = prepareStatement(Select.from(tableName, "*")
                    .where(columnServerName + "!=?")
                    .toString());
            statement.setString(1, "BungeeCord");
            set = statement.executeQuery();
            List<ServerInfo> servers = new ArrayList<>();
            while (set.next()) {
                servers.add(new ServerInfo(
                        set.getInt(columnServerID),
                        UUID.fromString(set.getString(columnServerUUID)),
                        set.getString(columnServerName),
                        set.getString(columnWebserverAddress)));
            }
            return servers;
        } finally {
            close(set, statement);
        }
    }
}