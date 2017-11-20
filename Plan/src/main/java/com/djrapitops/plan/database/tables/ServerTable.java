/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.database.tables;

import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.api.exceptions.DBCreateTableException;
import main.java.com.djrapitops.plan.database.databases.SQLDB;
import main.java.com.djrapitops.plan.database.processing.ExecStatement;
import main.java.com.djrapitops.plan.database.processing.QueryAllStatement;
import main.java.com.djrapitops.plan.database.processing.QueryStatement;
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
                .column(columnMaxPlayers, Sql.INT).notNull().defaultValue("-1")
                .primaryKey(usingMySQL, columnServerID)
                .toString()
        );
    }

    public void alterTableV11() {
        if (usingMySQL) {
            executeUnsafe("ALTER TABLE " + tableName + " MODIFY " + columnMaxPlayers + " INTEGER NOT NULL DEFAULT -1");
        }
    }

    public void saveCurrentServerInfo(ServerInfo info) throws SQLException {
        if (getServerID(info.getUuid()).isPresent()) {
            updateServerInfo(info);
        } else {
            saveNewServerInfo(info);
        }
    }

    private void updateServerInfo(ServerInfo info) throws SQLException {
        String sql = Update.values(tableName,
                columnServerUUID,
                columnServerName,
                columnWebserverAddress,
                columnInstalled,
                columnMaxPlayers)
                .where(columnServerID + "=?")
                .toString();

        execute(new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, info.getUuid().toString());
                statement.setString(2, info.getName());
                statement.setString(3, info.getWebAddress());
                statement.setBoolean(4, true);
                statement.setInt(5, info.getMaxPlayers());
                statement.setInt(6, info.getId());
            }
        });
    }

    /**
     * Inserts new row for a server into the table.
     *
     * @param info Info to instert (All variables should be present.
     * @throws IllegalStateException if one of the ServerInfo variables is null
     * @throws SQLException          DB Error
     */
    private void saveNewServerInfo(ServerInfo info) throws SQLException {
        UUID uuid = info.getUuid();
        String name = info.getName();
        String webAddress = info.getWebAddress();
        Verify.nullCheck(uuid, name, webAddress);

        execute(new ExecStatement(insertStatement) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, uuid.toString());
                statement.setString(2, name);
                statement.setString(3, webAddress);
                statement.setBoolean(4, true);
                statement.setInt(5, info.getMaxPlayers());
            }
        });
    }

    /**
     * Returns server ID for a matching UUID
     *
     * @param serverUUID UUID of the server.
     * @return ID or or empty optional.
     * @throws SQLException DB Error
     */
    public Optional<Integer> getServerID(UUID serverUUID) throws SQLException {
        String sql = Select.from(tableName,
                columnServerID)
                .where(columnServerUUID + "=?")
                .toString();

        return query(new QueryStatement<Optional<Integer>>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
            }

            @Override
            public Optional<Integer> processResults(ResultSet set) throws SQLException {
                if (set.next()) {
                    return Optional.of(set.getInt(columnServerID));
                } else {
                    return Optional.empty();
                }
            }
        });
    }

    /**
     * Returns server Name for a matching UUID
     *
     * @param serverUUID UUID of the server.
     * @return Name or empty optional.
     * @throws SQLException DB Error
     */
    public Optional<String> getServerName(UUID serverUUID) throws SQLException {
        String sql = Select.from(tableName,
                columnServerName)
                .where(columnServerUUID + "=?")
                .toString();

        return query(new QueryStatement<Optional<String>>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
            }

            @Override
            public Optional<String> processResults(ResultSet set) throws SQLException {
                if (set.next()) {
                    return Optional.of(set.getString(columnServerName));
                } else {
                    return Optional.empty();
                }
            }
        });
    }

    public Map<Integer, String> getServerNamesByID() throws SQLException {
        String sql = Select.from(tableName,
                columnServerID, columnServerName)
                .toString();

        return query(new QueryAllStatement<Map<Integer, String>>(sql) {
            @Override
            public Map<Integer, String> processResults(ResultSet set) throws SQLException {
                Map<Integer, String> names = new HashMap<>();
                while (set.next()) {
                    int id = set.getInt(columnServerID);
                    names.put(id, set.getString(columnServerName));
                }
                return names;
            }
        });
    }

    public Map<UUID, String> getServerNames() throws SQLException {
        String sql = Select.from(tableName,
                columnServerUUID, columnServerName)
                .toString();

        return query(new QueryAllStatement<Map<UUID, String>>(sql) {
            @Override
            public Map<UUID, String> processResults(ResultSet set) throws SQLException {
                Map<UUID, String> names = new HashMap<>();
                while (set.next()) {
                    UUID serverUUID = UUID.fromString(set.getString(columnServerUUID));
                    names.put(serverUUID, set.getString(columnServerName));
                }
                return names;
            }
        });
    }

    public Map<Integer, UUID> getServerUuids() throws SQLException {
        String sql = Select.from(tableName,
                columnServerID, columnServerUUID)
                .toString();

        return query(new QueryAllStatement<Map<Integer, UUID>>(sql) {
            @Override
            public Map<Integer, UUID> processResults(ResultSet set) throws SQLException {
                Map<Integer, UUID> uuids = new HashMap<>();
                while (set.next()) {
                    int id = set.getInt(columnServerID);
                    uuids.put(id, UUID.fromString(set.getString(columnServerUUID)));
                }
                return uuids;
            }
        });
    }

    /**
     * Used to get BungeeCord WebServer info if present.
     *
     * @return information about Bungee server.
     * @throws SQLException DB Error
     */
    public Optional<ServerInfo> getBungeeInfo() throws SQLException {
        String sql = Select.from(tableName, "*")
                .where(columnServerName + "=?")
                .toString();

        return query(new QueryStatement<Optional<ServerInfo>>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, "BungeeCord");
            }

            @Override
            public Optional<ServerInfo> processResults(ResultSet set) throws SQLException {
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
            }
        });
    }

    public List<ServerInfo> getBukkitServers() throws SQLException {
        String sql = Select.from(tableName, "*")
                .where(columnServerName + "!=?")
                .toString();

        return query(new QueryStatement<List<ServerInfo>>(sql, 100) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, "BungeeCord");
            }

            @Override
            public List<ServerInfo> processResults(ResultSet set) throws SQLException {
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
            }
        });
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

        executeBatch(new ExecStatement(insertStatement) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
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
            }
        });
    }

    public List<UUID> getServerUUIDs() throws SQLException {
        String sql = Select.from(tableName, columnServerUUID)
                .toString();

        return query(new QueryAllStatement<List<UUID>>(sql) {
            @Override
            public List<UUID> processResults(ResultSet set) throws SQLException {
                List<UUID> uuids = new ArrayList<>();
                while (set.next()) {
                    uuids.add(UUID.fromString(set.getString(columnServerUUID)));
                }
                return uuids;
            }
        });
    }

    public Optional<UUID> getServerUUID(String serverName) throws SQLException {
        String sql = Select.from(tableName,
                columnServerUUID)
                .where(columnServerName + "=?")
                .toString();

        return query(new QueryStatement<Optional<UUID>>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverName);
            }

            @Override
            public Optional<UUID> processResults(ResultSet set) throws SQLException {
                if (set.next()) {
                    return Optional.of(UUID.fromString(set.getString(columnServerUUID)));
                } else {
                    return Optional.empty();
                }
            }
        });
    }

    public Optional<ServerInfo> getServerInfo(UUID serverUUID) throws SQLException {
        String sql = Select.from(tableName, "*")
                .where(columnServerUUID + "=?")
                .toString();

        return query(new QueryStatement<Optional<ServerInfo>>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
            }

            @Override
            public Optional<ServerInfo> processResults(ResultSet set) throws SQLException {
                if (set.next()) {
                    return Optional.of(new ServerInfo(
                            set.getInt(columnServerID),
                            UUID.fromString(set.getString(columnServerUUID)),
                            set.getString(columnServerName),
                            set.getString(columnWebserverAddress),
                            set.getInt(columnMaxPlayers)));
                }
                return Optional.empty();
            }
        });
    }

    public int getMaxPlayers() throws SQLException {
        String sql = "SELECT SUM(" + columnMaxPlayers + ") AS max FROM " + tableName;

        return query(new QueryStatement<Integer>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {

            }

            @Override
            public Integer processResults(ResultSet set) throws SQLException {
                if (set.next()) {
                    return set.getInt("max");
                }
                return 0;
            }
        });
    }
}