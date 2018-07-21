/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.database.databases.sql.tables;

import com.djrapitops.plan.api.exceptions.database.DBInitException;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;
import com.djrapitops.plan.system.database.databases.sql.processing.ExecStatement;
import com.djrapitops.plan.system.database.databases.sql.processing.QueryAllStatement;
import com.djrapitops.plan.system.database.databases.sql.processing.QueryStatement;
import com.djrapitops.plan.system.database.databases.sql.statements.*;
import com.djrapitops.plan.system.info.server.Server;
import com.djrapitops.plugin.utilities.Verify;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Table for managing multiple server's data in the database.
 * <p>
 * Table Name: plan_servers
 * <p>
 * For contained columns {@see Col}
 *
 * @author Rsl1122
 * @see Server
 */
public class ServerTable extends Table {

    public static final String TABLE_NAME = "plan_servers";
    public final String statementSelectServerID;
    public final String statementSelectServerNameID;
    private String insertStatement;
    public ServerTable(SQLDB db) {
        super(TABLE_NAME, db);
        statementSelectServerID = "(" + Select.from(tableName, tableName + "." + Col.SERVER_ID).where(tableName + "." + Col.SERVER_UUID + "=?").toString() + " LIMIT 1)";
        statementSelectServerNameID = "(" + Select.from(tableName, tableName + "." + Col.NAME).where(tableName + "." + Col.SERVER_ID + "=?").toString() + " LIMIT 1)";
        insertStatement = Insert.values(tableName,
                Col.SERVER_UUID,
                Col.NAME,
                Col.WEBSERVER_ADDRESS,
                Col.INSTALLED,
                Col.MAX_PLAYERS);
    }

    @Override
    public void createTable() throws DBInitException {
        createTable(TableSqlParser.createTable(tableName)
                .primaryKeyIDColumn(usingMySQL, Col.SERVER_ID)
                .column(Col.SERVER_UUID, Sql.varchar(36)).notNull().unique()
                .column(Col.NAME, Sql.varchar(100))
                .column(Col.WEBSERVER_ADDRESS, Sql.varchar(100))
                .column(Col.INSTALLED, Sql.BOOL).notNull().defaultValue(false)
                .column(Col.MAX_PLAYERS, Sql.INT).notNull().defaultValue("-1")
                .primaryKey(usingMySQL, Col.SERVER_ID)
                .toString()
        );
    }

    public void alterTableV11() {
        if (usingMySQL) {
            executeUnsafe("ALTER TABLE " + tableName + " MODIFY " + Col.MAX_PLAYERS + " INTEGER NOT NULL DEFAULT -1");
        }
    }

    private void updateServerInfo(Server info) {
        String sql = Update.values(tableName,
                Col.SERVER_UUID,
                Col.NAME,
                Col.WEBSERVER_ADDRESS,
                Col.INSTALLED,
                Col.MAX_PLAYERS)
                .where(Col.SERVER_ID + "=?")
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

    public void saveCurrentServerInfo(Server info) {
        if (getServerID(info.getUuid()).isPresent()) {
            updateServerInfo(info);
        } else {
            saveNewServerInfo(info);
        }
    }

    /**
     * Returns server ID for a matching UUID
     *
     * @param serverUUID UUID of the server.
     * @return ID or or empty optional.
     */
    public Optional<Integer> getServerID(UUID serverUUID) {
        String sql = Select.from(tableName,
                Col.SERVER_ID)
                .where(Col.SERVER_UUID + "=?")
                .toString();

        return query(new QueryStatement<Optional<Integer>>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
            }

            @Override
            public Optional<Integer> processResults(ResultSet set) throws SQLException {
                if (set.next()) {
                    return Optional.of(set.getInt(Col.SERVER_ID.get()));
                } else {
                    return Optional.empty();
                }
            }
        });
    }

    /**
     * Inserts new row for a server into the table.
     *
     * @param info Info to instert (All variables should be present.
     * @throws IllegalStateException if one of the Server variables is null
     */
    private void saveNewServerInfo(Server info) {
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
     * Returns server Name for a matching UUID
     *
     * @param serverUUID UUID of the server.
     * @return Name or empty optional.
     */
    public Optional<String> getServerName(UUID serverUUID) {
        String sql = Select.from(tableName,
                Col.NAME)
                .where(Col.SERVER_UUID + "=?")
                .toString();

        return query(new QueryStatement<Optional<String>>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
            }

            @Override
            public Optional<String> processResults(ResultSet set) throws SQLException {
                if (set.next()) {
                    return Optional.of(set.getString(Col.NAME.get()));
                } else {
                    return Optional.empty();
                }
            }
        });
    }

    public Map<Integer, String> getServerNamesByID() {
        String sql = Select.from(tableName,
                Col.SERVER_ID, Col.NAME)
                .toString();

        return query(new QueryAllStatement<Map<Integer, String>>(sql) {
            @Override
            public Map<Integer, String> processResults(ResultSet set) throws SQLException {
                Map<Integer, String> names = new HashMap<>();
                while (set.next()) {
                    int id = set.getInt(Col.SERVER_ID.get());
                    names.put(id, set.getString(Col.NAME.get()));
                }
                return names;
            }
        });
    }

    public Map<UUID, String> getServerNames() {
        String sql = Select.from(tableName,
                Col.SERVER_UUID, Col.NAME)
                .toString();

        return query(new QueryAllStatement<Map<UUID, String>>(sql) {
            @Override
            public Map<UUID, String> processResults(ResultSet set) throws SQLException {
                Map<UUID, String> names = new HashMap<>();
                while (set.next()) {
                    UUID serverUUID = UUID.fromString(set.getString(Col.SERVER_UUID.get()));
                    names.put(serverUUID, set.getString(Col.NAME.get()));
                }
                return names;
            }
        });
    }

    public Map<Integer, UUID> getServerUUIDsByID() {
        String sql = Select.from(tableName,
                Col.SERVER_ID, Col.SERVER_UUID)
                .toString();

        return query(new QueryAllStatement<Map<Integer, UUID>>(sql) {
            @Override
            public Map<Integer, UUID> processResults(ResultSet set) throws SQLException {
                Map<Integer, UUID> uuids = new HashMap<>();
                while (set.next()) {
                    int id = set.getInt(Col.SERVER_ID.get());
                    uuids.put(id, UUID.fromString(set.getString(Col.SERVER_UUID.get())));
                }
                return uuids;
            }
        });
    }

    /**
     * Used to get BungeeCord WebServer info if present.
     *
     * @return information about Bungee server.
     */
    public Optional<Server> getBungeeInfo() {
        String sql = Select.from(tableName, "*")
                .where(Col.NAME + "=?")
                .toString();

        return query(new QueryStatement<Optional<Server>>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, "BungeeCord");
            }

            @Override
            public Optional<Server> processResults(ResultSet set) throws SQLException {
                if (set.next()) {
                    return Optional.of(new Server(
                            set.getInt(Col.SERVER_ID.get()),
                            UUID.fromString(set.getString(Col.SERVER_UUID.get())),
                            set.getString(Col.NAME.get()),
                            set.getString(Col.WEBSERVER_ADDRESS.get()),
                            set.getInt(Col.MAX_PLAYERS.get())));
                } else {
                    return Optional.empty();
                }
            }
        });
    }

    public Map<UUID, Server> getBukkitServers() {
        String sql = Select.from(tableName, "*")
                .where(Col.NAME + "!=?")
                .toString();

        return query(new QueryStatement<Map<UUID, Server>>(sql, 100) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, "BungeeCord");
            }

            @Override
            public Map<UUID, Server> processResults(ResultSet set) throws SQLException {
                Map<UUID, Server> servers = new HashMap<>();
                while (set.next()) {
                    UUID serverUUID = UUID.fromString(set.getString(Col.SERVER_UUID.get()));
                    servers.put(serverUUID, new Server(
                            set.getInt(Col.SERVER_ID.get()),
                            serverUUID,
                            set.getString(Col.NAME.get()),
                            set.getString(Col.WEBSERVER_ADDRESS.get()),
                            set.getInt(Col.MAX_PLAYERS.get())));
                }
                return servers;
            }
        });
    }

    public List<UUID> getServerUUIDs() {
        String sql = Select.from(tableName, Col.SERVER_UUID)
                .toString();

        return query(new QueryAllStatement<List<UUID>>(sql) {
            @Override
            public List<UUID> processResults(ResultSet set) throws SQLException {
                List<UUID> uuids = new ArrayList<>();
                while (set.next()) {
                    uuids.add(UUID.fromString(set.getString(Col.SERVER_UUID.get())));
                }
                return uuids;
            }
        });
    }

    public void insertAllServers(List<Server> allServer) {
        if (Verify.isEmpty(allServer)) {
            return;
        }

        executeBatch(new ExecStatement(insertStatement) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                for (Server info : allServer) {
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

    public Optional<UUID> getServerUUID(String serverName) {
        String sql = Select.from(tableName,
                Col.SERVER_UUID)
                .where(Col.NAME + "=?")
                .toString();

        return query(new QueryStatement<Optional<UUID>>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverName);
            }

            @Override
            public Optional<UUID> processResults(ResultSet set) throws SQLException {
                if (set.next()) {
                    return Optional.of(UUID.fromString(set.getString(Col.SERVER_UUID.get())));
                } else {
                    return Optional.empty();
                }
            }
        });
    }

    public Optional<Server> getServerInfo(UUID serverUUID) {
        String sql = Select.from(tableName, "*")
                .where(Col.SERVER_UUID + "=?")
                .toString();

        return query(new QueryStatement<Optional<Server>>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
            }

            @Override
            public Optional<Server> processResults(ResultSet set) throws SQLException {
                if (set.next()) {
                    return Optional.of(new Server(
                            set.getInt(Col.SERVER_ID.get()),
                            UUID.fromString(set.getString(Col.SERVER_UUID.get())),
                            set.getString(Col.NAME.get()),
                            set.getString(Col.WEBSERVER_ADDRESS.get()),
                            set.getInt(Col.MAX_PLAYERS.get())));
                }
                return Optional.empty();
            }
        });
    }

    public int getMaxPlayers() {
        String sql = "SELECT SUM(" + Col.MAX_PLAYERS + ") AS max FROM " + tableName;

        return query(new QueryAllStatement<Integer>(sql) {
            @Override
            public Integer processResults(ResultSet set) throws SQLException {
                if (set.next()) {
                    return set.getInt("max");
                }
                return 0;
            }
        });
    }

    public enum Col implements Column {
        SERVER_ID("id"),
        SERVER_UUID("uuid"),
        NAME("name"),
        WEBSERVER_ADDRESS("web_address"),
        INSTALLED("is_installed"),
        MAX_PLAYERS("max_players");

        private final String column;

        Col(String column) {
            this.column = column;
        }

        @Override
        public String get() {
            return toString();
        }

        @Override
        public String toString() {
            return column;
        }
    }
}
