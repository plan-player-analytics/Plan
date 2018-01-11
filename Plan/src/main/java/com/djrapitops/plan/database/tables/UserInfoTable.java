/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.database.tables;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.api.exceptions.DBCreateTableException;
import com.djrapitops.plan.data.container.UserInfo;
import com.djrapitops.plan.database.databases.SQLDB;
import com.djrapitops.plan.database.processing.ExecStatement;
import com.djrapitops.plan.database.processing.QueryAllStatement;
import com.djrapitops.plan.database.processing.QueryStatement;
import com.djrapitops.plan.database.sql.Select;
import com.djrapitops.plan.database.sql.Sql;
import com.djrapitops.plan.database.sql.TableSqlParser;
import com.djrapitops.plan.database.sql.Update;
import com.djrapitops.plan.utilities.MiscUtils;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.utilities.Verify;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Server Specific user information table.
 * <p>
 * Represents plan_user_info.
 *
 * @author Rsl1122
 */
public class UserInfoTable extends UserIDTable {

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
    public void createTable() throws DBCreateTableException {
        createTable(TableSqlParser.createTable(tableName)
                .column(columnUserID, Sql.INT).notNull()
                .column(columnRegistered, Sql.LONG).notNull()
                .column(columnOP, Sql.BOOL).notNull().defaultValue(false)
                .column(columnBanned, Sql.BOOL).notNull().defaultValue(false)
                .column(columnServerID, Sql.INT).notNull()
                .foreignKey(columnUserID, usersTable.getTableName(), usersTable.getColumnID())
                .foreignKey(columnServerID, serverTable.getTableName(), serverTable.getColumnID())
                .toString());
    }

    public void registerUserInfo(UUID uuid, long registered) throws SQLException {
        if (!usersTable.isRegistered(uuid)) {
            usersTable.registerUser(uuid, registered, "Waiting for Update..");
        }

        String sql = "INSERT INTO " + tableName + " (" +
                columnUserID + ", " +
                columnRegistered + ", " +
                columnServerID +
                ") VALUES (" +
                usersTable.statementSelectID + ", " +
                "?, " +
                serverTable.statementSelectServerID + ")";

        execute(new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, uuid.toString());
                statement.setLong(2, registered);
                statement.setString(3, Plan.getServerUUID().toString());
            }
        });
    }

    public boolean isRegistered(UUID uuid) throws SQLException {
        return isRegistered(uuid, PlanPlugin.getInstance().getServerUuid());
    }

    public boolean isRegistered(UUID uuid, UUID serverUUID) throws SQLException {
        String sql = Select.from(tableName, "COUNT(" + columnUserID + ") as c")
                .where(columnUserID + "=" + usersTable.statementSelectID)
                .and(columnServerID + "=" + serverTable.statementSelectServerID)
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

    public void updateOpAndBanStatus(UUID uuid, boolean opped, boolean banned) throws SQLException {
        String sql = Update.values(tableName, columnOP, columnBanned)
                .where(columnUserID + "=" + usersTable.statementSelectID)
                .toString();

        execute(new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setBoolean(1, opped);
                statement.setBoolean(2, banned);
                statement.setString(3, uuid.toString());
            }
        });
    }

    public UserInfo getUserInfo(UUID uuid) throws SQLException {
        return getAllUserInfo(uuid).get(PlanPlugin.getInstance().getServerUuid());
    }

    public Map<UUID, UserInfo> getAllUserInfo(UUID uuid) throws SQLException {
        String usersIDColumn = usersTable + "." + usersTable.getColumnID();
        String serverIDColumn = serverTable + "." + serverTable.getColumnID();
        String usersNameColumn = usersTable + "." + usersTable.getColumnName() + " as name";
        String serverUUIDColumn = serverTable + "." + serverTable.getColumnUUID() + " as s_uuid";
        String sql = "SELECT " +
                tableName + "." + columnRegistered + ", " +
                columnBanned + ", " +
                columnOP + ", " +
                usersNameColumn + ", " +
                serverUUIDColumn +
                " FROM " + tableName +
                " JOIN " + usersTable + " on " + usersIDColumn + "=" + columnUserID +
                " JOIN " + serverTable + " on " + serverIDColumn + "=" + columnServerID +
                " WHERE " + columnUserID + "=" + usersTable.statementSelectID;

        return query(new QueryStatement<Map<UUID, UserInfo>>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, uuid.toString());
            }

            @Override
            public Map<UUID, UserInfo> processResults(ResultSet set) throws SQLException {
                Map<UUID, UserInfo> map = new HashMap<>();
                while (set.next()) {
                    long registered = set.getLong(columnRegistered);
                    boolean opped = set.getBoolean(columnOP);
                    boolean banned = set.getBoolean(columnBanned);
                    String name = set.getString("name");

                    UUID serverUUID = UUID.fromString(set.getString("s_uuid"));
                    map.put(serverUUID, new UserInfo(uuid, name, registered, opped, banned));
                }
                return map;
            }
        });
    }

    /**
     * Used for getting info of all users on THIS server.
     *
     * @return List of UserInfo objects.
     */
    public List<UserInfo> getServerUserInfo() throws SQLException {
        return getServerUserInfo(Plan.getServerUUID());
    }

    public List<UserInfo> getServerUserInfo(UUID serverUUID) throws SQLException {
        String usersIDColumn = usersTable + "." + usersTable.getColumnID();
        String usersUUIDColumn = usersTable + "." + usersTable.getColumnUUID() + " as uuid";
        String usersNameColumn = usersTable + "." + usersTable.getColumnName() + " as name";
        String sql = "SELECT " +
                tableName + "." + columnRegistered + ", " +
                columnOP + ", " +
                columnBanned + ", " +
                usersNameColumn + ", " +
                usersUUIDColumn +
                " FROM " + tableName +
                " JOIN " + usersTable + " on " + usersIDColumn + "=" + columnUserID +
                " WHERE " + columnServerID + "=" + serverTable.statementSelectServerID;

        return query(new QueryStatement<List<UserInfo>>(sql, 20000) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
            }

            @Override
            public List<UserInfo> processResults(ResultSet set) throws SQLException {
                List<UserInfo> userInfo = new ArrayList<>();
                while (set.next()) {
                    long registered = set.getLong(columnRegistered);
                    boolean opped = set.getBoolean(columnOP);
                    boolean banned = set.getBoolean(columnBanned);
                    String name = set.getString("name");
                    UUID uuid = UUID.fromString(set.getString("uuid"));
                    UserInfo info = new UserInfo(uuid, name, registered, opped, banned);
                    if (!userInfo.contains(info)) {
                        userInfo.add(info);
                    }
                }
                return userInfo;
            }
        });
    }

    public Map<UUID, List<UserInfo>> getAllUserInfo() throws SQLException {
        String usersIDColumn = usersTable + "." + usersTable.getColumnID();
        String usersUUIDColumn = usersTable + "." + usersTable.getColumnUUID() + " as uuid";
        String serverIDColumn = serverTable + "." + serverTable.getColumnID();
        String serverUUIDColumn = serverTable + "." + serverTable.getColumnUUID() + " as s_uuid";
        String sql = "SELECT " +
                tableName + "." + columnRegistered + ", " +
                columnBanned + ", " +
                columnOP + ", " +
                usersUUIDColumn + ", " +
                serverUUIDColumn +
                " FROM " + tableName +
                " JOIN " + usersTable + " on " + usersIDColumn + "=" + columnUserID +
                " JOIN " + serverTable + " on " + serverIDColumn + "=" + columnServerID;

        return query(new QueryAllStatement<Map<UUID, List<UserInfo>>>(sql, 50000) {
            @Override
            public Map<UUID, List<UserInfo>> processResults(ResultSet set) throws SQLException {
                Map<UUID, List<UserInfo>> serverMap = new HashMap<>();
                while (set.next()) {
                    UUID serverUUID = UUID.fromString(set.getString("s_uuid"));
                    UUID uuid = UUID.fromString(set.getString("uuid"));

                    List<UserInfo> userInfos = serverMap.getOrDefault(serverUUID, new ArrayList<>());

                    long registered = set.getLong(columnRegistered);
                    boolean banned = set.getBoolean(columnBanned);
                    boolean op = set.getBoolean(columnOP);

                    userInfos.add(new UserInfo(uuid, "", registered, op, banned));

                    serverMap.put(serverUUID, userInfos);
                }
                return serverMap;
            }
        });
    }

    public void insertUserInfo(Map<UUID, List<UserInfo>> allUserInfos) throws SQLException {
        if (Verify.isEmpty(allUserInfos)) {
            return;
        }

        String sql = "INSERT INTO " + tableName + " (" +
                columnUserID + ", " +
                columnRegistered + ", " +
                columnServerID + ", " +
                columnBanned + ", " +
                columnOP +
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
                        statement.setBoolean(5, user.isOpped());
                        statement.addBatch();
                    }
                }
            }
        });
    }

    public Map<UUID, Set<UUID>> getSavedUUIDs() throws SQLException {
        String usersIDColumn = usersTable + "." + usersTable.getColumnID();
        String usersUUIDColumn = usersTable + "." + usersTable.getColumnUUID() + " as uuid";
        String serverIDColumn = serverTable + "." + serverTable.getColumnID();
        String serverUUIDColumn = serverTable + "." + serverTable.getColumnUUID() + " as s_uuid";
        String sql = "SELECT " +
                usersUUIDColumn + ", " +
                serverUUIDColumn +
                " FROM " + tableName +
                " JOIN " + usersTable + " on " + usersIDColumn + "=" + columnUserID +
                " JOIN " + serverTable + " on " + serverIDColumn + "=" + columnServerID;

        return query(new QueryAllStatement<Map<UUID, Set<UUID>>>(sql, 50000) {
            @Override
            public Map<UUID, Set<UUID>> processResults(ResultSet set) throws SQLException {
                Map<UUID, Set<UUID>> serverMap = new HashMap<>();
                while (set.next()) {
                    UUID serverUUID = UUID.fromString(set.getString("s_uuid"));
                    UUID uuid = UUID.fromString(set.getString("uuid"));

                    Set<UUID> uuids = serverMap.getOrDefault(serverUUID, new HashSet<>());
                    uuids.add(uuid);

                    serverMap.put(serverUUID, uuids);
                }
                return serverMap;
            }
        });
    }

    public int getServerUserCount(UUID serverUUID) {
        try {
            String sql = "SELECT " +
                    " COUNT(" + columnRegistered + ") as c" +
                    " FROM " + tableName +
                    " WHERE " + columnServerID + "=" + serverTable.statementSelectServerID;

            return query(new QueryStatement<Integer>(sql, 20000) {
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
        } catch (SQLException e) {
            Log.toLog(this.getClass().getName(), e);
            return 0;
        }
    }
}