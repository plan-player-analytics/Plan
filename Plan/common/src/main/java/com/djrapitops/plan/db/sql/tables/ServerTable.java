/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.db.sql.tables;

import com.djrapitops.plan.api.exceptions.database.DBInitException;
import com.djrapitops.plan.db.DBType;
import com.djrapitops.plan.db.SQLDB;
import com.djrapitops.plan.db.access.ExecStatement;
import com.djrapitops.plan.db.access.QueryAllStatement;
import com.djrapitops.plan.db.access.QueryStatement;
import com.djrapitops.plan.db.sql.parsing.*;
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
 * For contained columns {@link Col}
 *
 * @author Rsl1122
 * @see Server
 */
public class ServerTable extends Table {

    public static final String TABLE_NAME = "plan_servers";

    public static final String SERVER_ID = "id";
    public static final String SERVER_UUID = "uuid";
    public static final String NAME = "name";
    public static final String WEB_ADDRESS = "web_address";
    public static final String INSTALLED = "is_installed";
    public static final String MAX_PLAYERS = "max_players";

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

    public final String statementSelectServerID;
    public final String statementSelectServerNameID;
    private String insertStatement;

    public static String createTableSQL(DBType dbType) {
        return CreateTableParser.create(TABLE_NAME, dbType)
                .column(SERVER_ID, Sql.INT).primaryKey()
                .column(SERVER_UUID, Sql.varchar(36)).notNull().unique()
                .column(NAME, Sql.varchar(100))
                .column(WEB_ADDRESS, Sql.varchar(100))
                .column(INSTALLED, Sql.BOOL).notNull().defaultValue(true)
                .column(MAX_PLAYERS, Sql.INT).notNull().defaultValue("-1")
                .toString();
    }

    @Override
    public void createTable() throws DBInitException {
        createTable(TableSqlParser.createTable(tableName)
                .primaryKeyIDColumn(supportsMySQLQueries, Col.SERVER_ID)
                .column(Col.SERVER_UUID, Sql.varchar(36)).notNull().unique()
                .column(Col.NAME, Sql.varchar(100))
                .column(Col.WEBSERVER_ADDRESS, Sql.varchar(100))
                .column(Col.INSTALLED, Sql.BOOL).notNull().defaultValue(true)
                .column(Col.MAX_PLAYERS, Sql.INT).notNull().defaultValue("-1")
                .primaryKey(supportsMySQLQueries, Col.SERVER_ID)
                .toString()
        );
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

    public void insertAllServers(Collection<Server> allServer) {
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

    public void setAsUninstalled(UUID serverUUID) {
        String sql = "UPDATE " + tableName + " SET " + Col.INSTALLED + "=? WHERE " + Col.SERVER_UUID + "=?";

        execute(new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setBoolean(1, false);
                statement.setString(2, serverUUID.toString());
            }
        });
    }

    @Deprecated
    public enum Col implements Column {
        @Deprecated
        SERVER_ID("id"),
        @Deprecated
        SERVER_UUID("uuid"),
        @Deprecated
        NAME("name"),
        @Deprecated
        WEBSERVER_ADDRESS("web_address"),
        @Deprecated
        INSTALLED("is_installed"),
        @Deprecated
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
