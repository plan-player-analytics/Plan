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
package com.djrapitops.plan.system.database.databases.sql.tables;

import com.djrapitops.plan.api.exceptions.database.DBInitException;
import com.djrapitops.plan.data.container.UserInfo;
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
 * Table that is in charge of storing server specific player data.
 * <p>
 * Table Name: plan_user_info
 * <p>
 * For contained columns {@see Col}
 *
 * @author Rsl1122
 */
public class UserInfoTable extends UserIDTable {

    @Override
    public void createTable() throws DBInitException {
        createTable(TableSqlParser.createTable(tableName)
                .column(Col.USER_ID, Sql.INT).notNull()
                .column(Col.REGISTERED, Sql.LONG).notNull()
                .column(Col.OP, Sql.BOOL).notNull().defaultValue(false)
                .column(Col.BANNED, Sql.BOOL).notNull().defaultValue(false)
                .column(Col.SERVER_ID, Sql.INT).notNull()
                .foreignKey(Col.USER_ID, usersTable.getTableName(), UsersTable.Col.ID)
                .foreignKey(Col.SERVER_ID, serverTable.getTableName(), ServerTable.Col.SERVER_ID)
                .toString());
    }

    private final ServerTable serverTable;

    public UserInfoTable(SQLDB db) {
        super("plan_user_info", db);
        serverTable = db.getServerTable();
    }

    public void registerUserInfo(UUID uuid, long registered) {
        if (!usersTable.isRegistered(uuid)) {
            usersTable.registerUser(uuid, registered, "waitingForUpdate");
        }

        String sql = "INSERT INTO " + tableName + " (" +
                Col.USER_ID + ", " +
                Col.REGISTERED + ", " +
                Col.SERVER_ID +
                ") VALUES (" +
                usersTable.statementSelectID + ", " +
                "?, " +
                serverTable.statementSelectServerID + ")";

        execute(new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, uuid.toString());
                statement.setLong(2, registered);
                statement.setString(3, getServerUUID().toString());
            }
        });
    }

    public boolean isRegistered(UUID uuid, UUID serverUUID) {
        String sql = Select.from(tableName, "COUNT(" + Col.USER_ID + ") as c")
                .where(Col.USER_ID + "=" + usersTable.statementSelectID)
                .and(Col.SERVER_ID + "=" + serverTable.statementSelectServerID)
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
                .where(Col.USER_ID + "=" + usersTable.statementSelectID)
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
                .where(Col.USER_ID + "=" + usersTable.statementSelectID)
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
        String usersIDColumn = usersTable + "." + UsersTable.Col.ID;
        String serverIDColumn = serverTable + "." + ServerTable.Col.SERVER_ID;
        String usersNameColumn = usersTable + "." + UsersTable.Col.USER_NAME + " as name";
        String serverUUIDColumn = serverTable + "." + ServerTable.Col.SERVER_UUID + " as s_uuid";
        String sql = "SELECT " +
                tableName + "." + Col.REGISTERED + ", " +
                Col.BANNED + ", " +
                Col.OP + ", " +
                usersNameColumn + ", " +
                serverUUIDColumn +
                " FROM " + tableName +
                " INNER JOIN " + usersTable + " on " + usersIDColumn + "=" + Col.USER_ID +
                " INNER JOIN " + serverTable + " on " + serverIDColumn + "=" + Col.SERVER_ID +
                " WHERE " + Col.USER_ID + "=" + usersTable.statementSelectID;

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

                    UUID serverUUID = UUID.fromString(set.getString("s_uuid"));
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
        Optional<Integer> serverID = serverTable.getServerID(serverUUID);
        if (!serverID.isPresent()) {
            return new ArrayList<>();
        }

        Map<Integer, Map.Entry<UUID, String>> uuidsAndNamesByID = usersTable.getUUIDsAndNamesByID();

        String sql = "SELECT * FROM " + tableName +
                " WHERE " + Col.SERVER_ID + "=?";

        return query(new QueryStatement<List<UserInfo>>(sql, 20000) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setInt(1, serverID.get());
            }

            @Override
            public List<UserInfo> processResults(ResultSet set) throws SQLException {
                List<UserInfo> userInfo = new ArrayList<>();
                while (set.next()) {
                    long registered = set.getLong(Col.REGISTERED.get());
                    boolean op = set.getBoolean(Col.OP.get());
                    boolean banned = set.getBoolean(Col.BANNED.get());
                    int userId = set.getInt(Col.USER_ID.get());

                    Map.Entry<UUID, String> uuidNameEntry = uuidsAndNamesByID.get(userId);
                    UUID uuid = uuidNameEntry.getKey();
                    String name = uuidNameEntry.getValue();

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

    public Map<UUID, List<UserInfo>> getAllUserInfo() {
        String usersIDColumn = usersTable + "." + UsersTable.Col.ID;
        String usersUUIDColumn = usersTable + "." + UsersTable.Col.UUID + " as uuid";
        String serverIDColumn = serverTable + "." + ServerTable.Col.SERVER_ID;
        String serverUUIDColumn = serverTable + "." + ServerTable.Col.SERVER_UUID + " as s_uuid";
        String sql = "SELECT " +
                tableName + "." + Col.REGISTERED + ", " +
                Col.BANNED + ", " +
                Col.OP + ", " +
                usersUUIDColumn + ", " +
                serverUUIDColumn +
                " FROM " + tableName +
                " INNER JOIN " + usersTable + " on " + usersIDColumn + "=" + Col.USER_ID +
                " INNER JOIN " + serverTable + " on " + serverIDColumn + "=" + Col.SERVER_ID;

        return query(new QueryAllStatement<Map<UUID, List<UserInfo>>>(sql, 50000) {
            @Override
            public Map<UUID, List<UserInfo>> processResults(ResultSet set) throws SQLException {
                Map<UUID, List<UserInfo>> serverMap = new HashMap<>();
                while (set.next()) {
                    UUID serverUUID = UUID.fromString(set.getString("s_uuid"));
                    UUID uuid = UUID.fromString(set.getString("uuid"));

                    List<UserInfo> userInfos = serverMap.getOrDefault(serverUUID, new ArrayList<>());

                    long registered = set.getLong(Col.REGISTERED.get());
                    boolean banned = set.getBoolean(Col.BANNED.get());
                    boolean op = set.getBoolean(Col.OP.get());

                    userInfos.add(new UserInfo(uuid, "", registered, op, banned));

                    serverMap.put(serverUUID, userInfos);
                }
                return serverMap;
            }
        });
    }

    public void insertUserInfo(Map<UUID, List<UserInfo>> allUserInfos) {
        if (Verify.isEmpty(allUserInfos)) {
            return;
        }

        String sql = "INSERT INTO " + tableName + " (" +
                Col.USER_ID + ", " +
                Col.REGISTERED + ", " +
                Col.SERVER_ID + ", " +
                Col.BANNED + ", " +
                Col.OP +
                ") VALUES (" +
                usersTable.statementSelectID + ", " +
                "?, " +
                serverTable.statementSelectServerID + ", ?, ?)";

        executeBatch(new ExecStatement(sql) {
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
                " WHERE " + Col.SERVER_ID + "=" + serverTable.statementSelectServerID;

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

    public Map<Integer, Integer> getPlayersRegisteredForServers(Collection<Server> servers) {
        if (servers.isEmpty()) {
            return new HashMap<>();
        }

        String sql = "SELECT " + Col.SERVER_ID + ", " +
                "COUNT(" + Col.REGISTERED + ") AS count" +
                " FROM " + tableName +
                " GROUP BY " + Col.SERVER_ID;
        return query(new QueryAllStatement<Map<Integer, Integer>>(sql, 10000) {
            @Override
            public Map<Integer, Integer> processResults(ResultSet set) throws SQLException {
                Map<Integer, Integer> map = new HashMap<>();
                while (set.next()) {
                    int serverID = set.getInt(Col.SERVER_ID.get());
                    int count = set.getInt("count");
                    map.put(serverID, count);
                }
                return map;
            }
        });

    }

    public enum Col implements Column {
        USER_ID(UserIDTable.Col.USER_ID.get()),
        SERVER_ID("server_id"),
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

    public Set<UUID> getSavedUUIDs(UUID serverUUID) {
        String usersIDColumn = usersTable + "." + UsersTable.Col.ID;
        String usersUUIDColumn = usersTable + "." + UsersTable.Col.UUID + " as uuid";
        String serverIDColumn = serverTable + "." + ServerTable.Col.SERVER_ID;
        String serverUUIDColumn = serverTable + "." + ServerTable.Col.SERVER_UUID + " as s_uuid";
        String sql = "SELECT " +
                usersUUIDColumn + ", " +
                serverUUIDColumn +
                " FROM " + tableName +
                " INNER JOIN " + usersTable + " on " + usersIDColumn + "=" + Col.USER_ID +
                " INNER JOIN " + serverTable + " on " + serverIDColumn + "=" + Col.SERVER_ID +
                " WHERE s_uuid=?";

        return query(new QueryStatement<Set<UUID>>(sql, 50000) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
            }

            @Override
            public Set<UUID> processResults(ResultSet set) throws SQLException {
                Set<UUID> uuids = new HashSet<>();
                while (set.next()) {
                    UUID uuid = UUID.fromString(set.getString("uuid"));
                    uuids.add(uuid);
                }
                return uuids;
            }
        });
    }
}
