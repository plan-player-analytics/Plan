/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.database.tables;

import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.api.exceptions.DBCreateTableException;
import main.java.com.djrapitops.plan.database.databases.SQLDB;
import main.java.com.djrapitops.plan.database.sql.*;
import main.java.com.djrapitops.plan.systems.info.server.ServerInfo;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Table for managing multiple server's data in the database.
 * <p>
 * plan_servers contains columns:
 * <ul>
 * <li>id</li>
 * <li>uuid</li>
 * <li>name</li>
 * <li>web_address</li>
 * <li>is_installed</li>
 * </ul>
 * Columns refer to Server Information.
 *
 * @author Rsl1122
 */
public class ServerTable extends Table {

    public final String statementSelectServerID;
    public final String statementSelectServerNameID;
    private final String columnServerID = "id";
    private final String columnServerUUID = "uuid";
    private final String columnServerName = "name";
    private final String columnWebserverAddress = "web_address";
    private final String columnInstalled = "is_installed";
    private final String columnMaxPlayers = "max_players";
    private String insertStatement;

    public ServerTable(SQLDB db, boolean usingMySQL) {
        super("plan_servers", db, usingMySQL);
        statementSelectServerID = "(" + Select.from(tableName, tableName + "." + columnServerID).where(columnServerUUID + "=?").toString() + ")";
        statementSelectServerNameID = "(" + Select.from(tableName, tableName + "." + columnServerName).where(columnServerID + "=?").toString() + ")";
        insertStatement = Insert.values(tableName,
                columnServerUUID,
                columnServerName,
                columnWebserverAddress,
                columnInstalled,
                columnMaxPlayers);
    }

    @Override
    public void createTable() throws DBCreateTableException {
        createTable(TableSqlParser.createTable(tableName)
                .primaryKeyIDColumn(usingMySQL, columnServerID)
                .column(columnServerUUID, Sql.varchar(36)).notNull().unique()
                .column(columnServerName, Sql.varchar(100))
                .column(columnWebserverAddress, Sql.varchar(100))
                .column(columnInstalled, Sql.BOOL).notNull().defaultValue(false)
                .column(columnMaxPlayers, Sql.BOOL).notNull().defaultValue("-1")
                .primaryKey(usingMySQL, columnServerID)
                .toString()
        );
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
                    columnInstalled,
                    columnMaxPlayers)
                    .where(columnServerID + "=?")
                    .toString()
            );
            statement.setString(1, info.getUuid().toString());
            statement.setString(2, info.getName());
            statement.setString(3, info.getWebAddress());
            statement.setBoolean(4, true);
            statement.setInt(5, info.getMaxPlayers());
            statement.setInt(6, info.getId());
            statement.executeUpdate();

            commit(statement.getConnection());
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
            statement = prepareStatement(insertStatement);

            statement.setString(1, uuid.toString());
            statement.setString(2, name);
            statement.setString(3, webAddress);
            statement.setBoolean(4, true);
            statement.setInt(5, info.getMaxPlayers());
            statement.execute();

            commit(statement.getConnection());
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
            endTransaction(statement);
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
            endTransaction(statement);
            close(set, statement);
        }
    }

    public Map<Integer, String> getServerNames() throws SQLException {
        Map<Integer, String> names = new HashMap<>();
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            statement = prepareStatement(Select.from(tableName,
                    columnServerID, columnServerName)
                    .toString());
            set = statement.executeQuery();
            while (set.next()) {
                int id = set.getInt(columnServerID);
                names.put(id, set.getString(columnServerName));
            }
            return names;
        } finally {
            endTransaction(statement);
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
                        set.getString(columnWebserverAddress),
                        set.getInt(columnMaxPlayers)));
            } else {
                return Optional.empty();
            }
        } finally {
            endTransaction(statement);
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
                        set.getString(columnWebserverAddress),
                        set.getInt(columnMaxPlayers)));
            }
            return servers;
        } finally {
            endTransaction(statement);
            close(set, statement);
        }
    }

    public String getColumnID() {
        return columnServerID;
    }

    public String getColumnUUID() {
        return columnServerUUID;
    }

    public void insertAllServers(List<ServerInfo> allServerInfo) throws SQLException {
        if (Verify.isEmpty(allServerInfo)) {
            return;
        }
        PreparedStatement statement = null;
        try {
            statement = prepareStatement(insertStatement);

            for (ServerInfo info : allServerInfo) {
                UUID uuid = info.getUuid();
                String name = info.getName();
                String webAddress = info.getWebAddress();

                if (uuid == null) {
                    continue;
                }

                statement.setString(1, uuid.toString());
                statement.setString(2, name);
                statement.setString(3, webAddress);
                statement.setBoolean(4, true);
                statement.setInt(5, info.getMaxPlayers());
                statement.addBatch();
            }

            statement.executeBatch();
            commit(statement.getConnection());
        } finally {
            close(statement);
        }
    }

    public List<UUID> getServerUUIDs() throws SQLException {
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            statement = prepareStatement(Select.from(tableName, columnServerUUID)
                    .toString());
            set = statement.executeQuery();
            List<UUID> uuids = new ArrayList<>();
            while (set.next()) {
                uuids.add(UUID.fromString(set.getString(columnServerUUID)));
            }
            return uuids;
        } finally {
            endTransaction(statement);
            close(set, statement);
        }
    }

    public Optional<UUID> getServerUUID(String serverName) throws SQLException {
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            statement = prepareStatement(Select.from(tableName,
                    columnServerUUID)
                    .where(columnServerName + "=?")
                    .toString());
            statement.setString(1, serverName);
            set = statement.executeQuery();
            if (set.next()) {
                return Optional.of(UUID.fromString(set.getString(columnServerUUID)));
            } else {
                return Optional.empty();
            }
        } finally {
            endTransaction(statement);
            close(set, statement);
        }
    }

    public Optional<ServerInfo> getServerInfo(UUID serverUUID) throws SQLException {
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            statement = prepareStatement(Select.from(tableName, "*")
                    .where(columnServerUUID + "=?")
                    .toString());
            statement.setString(1, serverUUID.toString());
            set = statement.executeQuery();
            if (set.next()) {
                return Optional.of(new ServerInfo(
                        set.getInt(columnServerID),
                        UUID.fromString(set.getString(columnServerUUID)),
                        set.getString(columnServerName),
                        set.getString(columnWebserverAddress),
                        set.getInt(columnMaxPlayers)));
            }
            return Optional.empty();
        } finally {
            endTransaction(statement);
            close(set, statement);
        }
    }

    public int getMaxPlayers() throws SQLException {
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            statement = prepareStatement("SELECT SUM(" + columnMaxPlayers + ") AS max FROM " + tableName);
            statement.setFetchSize(5000);

            set = statement.executeQuery();
            if (set.next()) {
                return set.getInt("max");
            }
            return 0;
        } finally {
            endTransaction(statement);
            close(set, statement);
        }
    }
}