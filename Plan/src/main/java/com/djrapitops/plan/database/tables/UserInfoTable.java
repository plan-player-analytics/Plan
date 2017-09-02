/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.database.tables;

import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.api.exceptions.DBCreateTableException;
import main.java.com.djrapitops.plan.data.UserInfo;
import main.java.com.djrapitops.plan.database.databases.SQLDB;
import main.java.com.djrapitops.plan.database.sql.Select;
import main.java.com.djrapitops.plan.database.sql.Sql;
import main.java.com.djrapitops.plan.database.sql.TableSqlParser;
import main.java.com.djrapitops.plan.database.sql.Update;

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

        PreparedStatement statement = null;
        try {
            statement = prepareStatement("INSERT INTO " + tableName + " (" +
                    columnUserID + ", " +
                    columnRegistered + ", " +
                    columnServerID +
                    ") VALUES (" +
                    usersTable.statementSelectID + ", " +
                    "?, " +
                    serverTable.statementSelectServerID + ")");
            statement.setString(1, uuid.toString());
            statement.setLong(2, registered);
            statement.setString(3, Plan.getServerUUID().toString());

            statement.execute();
            commit(statement.getConnection());
        } finally {
            close(statement);
        }
    }

    public boolean isRegistered(UUID uuid) throws SQLException {
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            statement = prepareStatement(Select.from(tableName, columnUserID)
                    .where(columnUserID + "=" + usersTable.statementSelectID)
                    .toString());
            statement.setString(1, uuid.toString());
            set = statement.executeQuery();
            return set.next();
        } finally {
            endTransaction(statement);
            close(set, statement);
        }
    }

    public void updateOpAndBanStatus(UUID uuid, boolean opped, boolean banned) throws SQLException {
        PreparedStatement statement = null;
        try {
            statement = prepareStatement(Update.values(tableName, columnOP, columnBanned)
                    .where(columnUserID + "=" + usersTable.statementSelectID)
                    .toString());
            statement.setBoolean(1, opped);
            statement.setBoolean(2, banned);
            statement.setString(3, uuid.toString());

            statement.execute();
            commit(statement.getConnection());
        } finally {
            close(statement);
        }
    }

    public UserInfo getUserInfo(UUID uuid) throws SQLException {
        return getUserInfo(uuid, Plan.getServerUUID());
    }

    public UserInfo getUserInfo(UUID uuid, UUID serverUUID) throws SQLException {
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            String usersIDColumn = usersTable + "." + usersTable.getColumnID();
            String usersNameColumn = usersTable + "." + usersTable.getColumnName() + " as name";
            statement = prepareStatement("SELECT " +
                    tableName + "." + columnRegistered + ", " +
                    columnOP + ", " +
                    columnBanned + ", " +
                    usersNameColumn +
                    " FROM " + tableName +
                    " JOIN " + usersTable + " on " + usersIDColumn + "=" + columnUserID +
                    " WHERE " + columnUserID + "=" + usersTable.statementSelectID +
                    " AND " + columnServerID + "=" + serverTable.statementSelectServerID
            );
            statement.setString(1, uuid.toString());
            statement.setString(2, serverUUID.toString());
            set = statement.executeQuery();
            if (set.next()) {
                long registered = set.getLong(columnRegistered);
                boolean opped = set.getBoolean(columnOP);
                boolean banned = set.getBoolean(columnBanned);
                String name = set.getString("name");
                return new UserInfo(uuid, name, registered, opped, banned);
            }
            return null;
        } finally {
            endTransaction(statement);
            close(set, statement);
        }
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
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            List<UserInfo> userInfo = new ArrayList<>();
            String usersIDColumn = usersTable + "." + usersTable.getColumnID();
            String usersUUIDColumn = usersTable + "." + usersTable.getColumnUUID() + " as uuid";
            String usersNameColumn = usersTable + "." + usersTable.getColumnName() + " as name";
            statement = prepareStatement("SELECT " +
                    tableName + "." + columnRegistered + ", " +
                    columnOP + ", " +
                    columnBanned + ", " +
                    usersNameColumn + ", " +
                    usersUUIDColumn +
                    " FROM " + tableName +
                    " JOIN " + usersTable + " on " + usersIDColumn + "=" + columnUserID +
                    " WHERE " + columnServerID + "=" + serverTable.statementSelectServerID
            );
            statement.setFetchSize(2000);
            statement.setString(1, serverUUID.toString());
            set = statement.executeQuery();
            while (set.next()) {
                long registered = set.getLong(columnRegistered);
                boolean opped = set.getBoolean(columnOP);
                boolean banned = set.getBoolean(columnBanned);
                String name = set.getString("name");
                UUID uuid = UUID.fromString(set.getString("uuid"));
                userInfo.add(new UserInfo(uuid, name, registered, opped, banned));
            }
            return userInfo;
        } finally {
            close(set, statement);
        }
    }

    public Map<UUID, List<UserInfo>> getAllUserInfo() throws SQLException {
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            String usersIDColumn = usersTable + "." + usersTable.getColumnID();
            String usersUUIDColumn = usersTable + "." + usersTable.getColumnUUID() + " as uuid";
            String serverIDColumn = serverTable + "." + serverTable.getColumnID();
            String serverUUIDColumn = serverTable + "." + serverTable.getColumnUUID() + " as s_uuid";
            statement = prepareStatement("SELECT " +
                    tableName + "." + columnRegistered + ", " +
                    columnBanned + ", " +
                    columnOP + ", " +
                    usersUUIDColumn + ", " +
                    serverUUIDColumn +
                    " FROM " + tableName +
                    " JOIN " + usersTable + " on " + usersIDColumn + "=" + columnUserID +
                    " JOIN " + serverTable + " on " + serverIDColumn + "=" + columnServerID
            );
            statement.setFetchSize(5000);
            set = statement.executeQuery();
            Map<UUID, List<UserInfo>> serverMap = new HashMap<>();
            while (set.next()) {
                UUID serverUUID = UUID.fromString(set.getString("s_uuid"));
                UUID uuid = UUID.fromString(set.getString("uuid"));

                List<UserInfo> userInfos = serverMap.getOrDefault(serverUUID, new ArrayList<>());

                long registered = set.getLong(columnRegistered);
                boolean banned = set.getBoolean(columnBanned);
                boolean op = set.getBoolean(columnOP);

                userInfos.add(new UserInfo(uuid, "", registered, op, banned));

                serverMap.put(uuid, userInfos);
            }
            return serverMap;
        } finally {
            endTransaction(statement);
            close(set, statement);
        }
    }

    public void insertUserInfo(Map<UUID, List<UserInfo>> allUserInfos) throws SQLException {
        if (Verify.isEmpty(allUserInfos)) {
            return;
        }
        PreparedStatement statement = null;
        try {
            statement = prepareStatement("INSERT INTO " + tableName + " (" +
                    columnUserID + ", " +
                    columnRegistered + ", " +
                    columnServerID + ", " +
                    columnBanned + ", " +
                    columnOP +
                    ") VALUES (" +
                    usersTable.statementSelectID + ", " +
                    "?, " +
                    serverTable.statementSelectServerID + ", ?, ?)");

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

            statement.executeBatch();
            commit(statement.getConnection());
        } finally {
            close(statement);
        }
    }
}