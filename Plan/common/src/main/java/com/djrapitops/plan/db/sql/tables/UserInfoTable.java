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
import com.djrapitops.plan.data.container.UserInfo;
import com.djrapitops.plan.db.SQLDB;
import com.djrapitops.plan.db.access.ExecStatement;
import com.djrapitops.plan.db.access.QueryAllStatement;
import com.djrapitops.plan.db.access.QueryStatement;
import com.djrapitops.plan.db.patches.UserInfoOptimizationPatch;
import com.djrapitops.plan.db.patches.Version10Patch;
import com.djrapitops.plan.db.sql.parsing.*;
import com.djrapitops.plan.system.info.server.Server;
import com.djrapitops.plugin.utilities.Verify;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Table that is in charge of storing server specific player data.
 * <p>
 * Table Name: plan_user_info
 * <p>
 * Patches related to this table:
 * {@link Version10Patch}
 * {@link UserInfoOptimizationPatch}
 *
 * @author Rsl1122
 */
public class UserInfoTable extends UserUUIDTable {

    public static final String TABLE_NAME = "plan_user_info";

    private final String insertStatement;

    private final UsersTable usersTable;

    public UserInfoTable(SQLDB db) {
        super(TABLE_NAME, db);
        usersTable = db.getUsersTable();
        insertStatement = "INSERT INTO " + tableName + " (" +
                Col.UUID + ", " +
                Col.REGISTERED + ", " +
                Col.SERVER_UUID + ", " +
                Col.BANNED + ", " +
                Col.OP +
                ") VALUES (?, ?, ?, ?, ?)";
    }

    @Override
    public void createTable() throws DBInitException {
        createTable(TableSqlParser.createTable(tableName)
                .primaryKeyIDColumn(supportsMySQLQueries, Col.ID)
                .column(Col.UUID, Sql.varchar(36)).notNull()
                .column(Col.SERVER_UUID, Sql.varchar(36)).notNull()
                .column(Col.REGISTERED, Sql.LONG).notNull()
                .column(Col.OP, Sql.BOOL).notNull().defaultValue(false)
                .column(Col.BANNED, Sql.BOOL).notNull().defaultValue(false)
                .primaryKey(supportsMySQLQueries, Col.ID)
                .toString());
    }

    public void registerUserInfo(UUID uuid, long registered) {
        execute(new ExecStatement(insertStatement) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, uuid.toString());
                statement.setLong(2, registered);
                statement.setString(3, getServerUUID().toString());
                statement.setBoolean(4, false);
                statement.setBoolean(5, false);
            }
        });
    }

    public boolean isRegistered(UUID uuid, UUID serverUUID) {
        String sql = Select.from(tableName, "COUNT(" + Col.UUID + ") as c")
                .where(Col.UUID + "=?")
                .and(Col.SERVER_UUID + "=?")
                .toString();

        return query(new QueryStatement<Boolean>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, uuid.toString());
                statement.setString(2, serverUUID.toString());
            }

            @Override
            public Boolean processResults(ResultSet set) throws SQLException {
                return set.next() && set.getInt("c") >= 1;
            }
        });
    }

    public boolean isRegistered(UUID uuid) {
        return isRegistered(uuid, getServerUUID());
    }

    public void updateOpStatus(UUID uuid, boolean op) {
        String sql = Update.values(tableName, Col.OP)
                .where(Col.UUID + "=?")
                .toString();

        execute(new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setBoolean(1, op);
                statement.setString(2, uuid.toString());
            }
        });
    }

    public void updateBanStatus(UUID uuid, boolean banned) {
        String sql = Update.values(tableName, Col.BANNED)
                .where(Col.UUID + "=?")
                .toString();

        execute(new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setBoolean(1, banned);
                statement.setString(2, uuid.toString());
            }
        });
    }

    public Map<UUID, UserInfo> getAllUserInfo(UUID uuid) {
        String usersUUIDColumn = usersTable + "." + UsersTable.Col.UUID;
        String usersNameColumn = usersTable + "." + UsersTable.Col.USER_NAME + " as name";

        String sql = "SELECT " +
                tableName + "." + Col.REGISTERED + ", " +
                Col.BANNED + ", " +
                Col.OP + ", " +
                usersNameColumn + ", " +
                Col.SERVER_UUID +
                " FROM " + tableName +
                " INNER JOIN " + usersTable + " on " + usersUUIDColumn + "=" + tableName + "." + Col.UUID +
                " WHERE " + tableName + "." + Col.UUID + "=?";

        return query(new QueryStatement<Map<UUID, UserInfo>>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, uuid.toString());
            }

            @Override
            public Map<UUID, UserInfo> processResults(ResultSet set) throws SQLException {
                Map<UUID, UserInfo> map = new HashMap<>();
                while (set.next()) {
                    long registered = set.getLong(Col.REGISTERED.get());
                    boolean op = set.getBoolean(Col.OP.get());
                    boolean banned = set.getBoolean(Col.BANNED.get());
                    String name = set.getString("name");

                    UUID serverUUID = UUID.fromString(set.getString(Col.SERVER_UUID.get()));
                    map.put(serverUUID, new UserInfo(uuid, name, registered, op, banned));
                }
                return map;
            }
        });
    }

    public UserInfo getUserInfo(UUID uuid) {
        return getAllUserInfo(uuid).get(getServerUUID());
    }

    public List<UserInfo> getServerUserInfo(UUID serverUUID) {
        String usersUUIDColumn = usersTable + "." + UsersTable.Col.UUID;
        String usersNameColumn = usersTable + "." + UsersTable.Col.USER_NAME + " as name";

        String sql = "SELECT " +
                tableName + "." + Col.REGISTERED + ", " +
                Col.BANNED + ", " +
                Col.OP + ", " +
                usersNameColumn + ", " +
                tableName + "." + Col.UUID +
                " FROM " + tableName +
                " INNER JOIN " + usersTable + " on " + usersUUIDColumn + "=" + tableName + "." + Col.UUID +
                " WHERE " + Col.SERVER_UUID + "=?";

        return query(new QueryStatement<List<UserInfo>>(sql, 20000) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
            }

            @Override
            public List<UserInfo> processResults(ResultSet set) throws SQLException {
                List<UserInfo> userInfo = new ArrayList<>();
                while (set.next()) {
                    UUID uuid = UUID.fromString(set.getString(Col.UUID.get()));
                    String name = set.getString("name");
                    long registered = set.getLong(Col.REGISTERED.get());
                    boolean op = set.getBoolean(Col.OP.get());
                    boolean banned = set.getBoolean(Col.BANNED.get());

                    UserInfo info = new UserInfo(uuid, name, registered, op, banned);
                    if (!userInfo.contains(info)) {
                        userInfo.add(info);
                    }
                }
                return userInfo;
            }
        });
    }

    /**
     * Used for getting info of all users on THIS server.
     *
     * @return List of UserInfo objects.
     */
    public List<UserInfo> getServerUserInfo() {
        return getServerUserInfo(getServerUUID());
    }

    public void insertUserInfo(Map<UUID, List<UserInfo>> allUserInfos) {
        if (Verify.isEmpty(allUserInfos)) {
            return;
        }

        executeBatch(new ExecStatement(insertStatement) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                // Every Server
                for (Map.Entry<UUID, List<UserInfo>> entry : allUserInfos.entrySet()) {
                    UUID serverUUID = entry.getKey();
                    // Every User
                    for (UserInfo user : entry.getValue()) {
                        statement.setString(1, user.getUuid().toString());
                        statement.setLong(2, user.getRegistered());
                        statement.setString(3, serverUUID.toString());
                        statement.setBoolean(4, user.isBanned());
                        statement.setBoolean(5, user.isOperator());
                        statement.addBatch();
                    }
                }
            }
        });
    }

    public int getServerUserCount(UUID serverUUID) {
        String sql = "SELECT " +
                " COUNT(" + Col.REGISTERED + ") as c" +
                " FROM " + tableName +
                " WHERE " + Col.SERVER_UUID + "=?";

        return query(new QueryAllStatement<Integer>(sql, 20000) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
            }

            @Override
            public Integer processResults(ResultSet set) throws SQLException {
                if (set.next()) {
                    return set.getInt("c");
                }
                return 0;
            }
        });
    }

    public boolean isRegisteredOnThisServer(UUID player) {
        return isRegistered(player, getServerUUID());
    }

    public Map<UUID, Integer> getPlayersRegisteredForServers(Collection<Server> servers) {
        if (servers.isEmpty()) {
            return new HashMap<>();
        }

        String sql = "SELECT " + Col.SERVER_UUID + ", " +
                "COUNT(" + Col.REGISTERED + ") AS count" +
                " FROM " + tableName +
                " GROUP BY " + Col.SERVER_UUID;
        return query(new QueryAllStatement<Map<UUID, Integer>>(sql, 10000) {
            @Override
            public Map<UUID, Integer> processResults(ResultSet set) throws SQLException {
                Map<UUID, Integer> map = new HashMap<>();
                while (set.next()) {
                    UUID serverUUID = UUID.fromString(set.getString(Col.SERVER_UUID.get()));
                    int count = set.getInt("count");
                    map.put(serverUUID, count);
                }
                return map;
            }
        });

    }

    public Set<UUID> getSavedUUIDs(UUID serverUUID) {
        String sql = "SELECT " + Col.UUID + " FROM " + tableName + " WHERE " + Col.SERVER_UUID + "=?";

        return query(new QueryStatement<Set<UUID>>(sql, 50000) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
            }

            @Override
            public Set<UUID> processResults(ResultSet set) throws SQLException {
                Set<UUID> uuids = new HashSet<>();
                while (set.next()) {
                    UUID uuid = UUID.fromString(set.getString(Col.UUID.get()));
                    uuids.add(uuid);
                }
                return uuids;
            }
        });
    }

    public enum Col implements Column {
        ID("id"),
        UUID(UserUUIDTable.Col.UUID.get()),
        SERVER_UUID("server_uuid"),
        REGISTERED("registered"),
        OP("opped"),
        BANNED("banned");

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
