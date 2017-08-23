package main.java.com.djrapitops.plan.database.tables;

import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.database.databases.SQLDB;
import main.java.com.djrapitops.plan.database.sql.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * @author Rsl1122
 */
public class UsersTable extends UserIDTable {

    private final String columnID = "id";
    private final String columnUUID = "uuid";
    private final String columnRegistered = "registered";
    private final String columnName = "name";
    private final String columnTimesKicked = "times_kicked";

    public final String statementSelectID;

    /**
     * @param db
     * @param usingMySQL
     */
    public UsersTable(SQLDB db, boolean usingMySQL) {
        super("plan_users", db, usingMySQL);
        statementSelectID = "(" + Select.from(tableName, tableName + "." + columnID).where(columnUUID + "=?").toString() + ")";
    }

    /**
     * @return
     */
    @Override
    public boolean createTable() {
        return createTable(TableSqlParser.createTable(tableName)
                .primaryKeyIDColumn(usingMySQL, columnID, Sql.INT)
                .column(columnUUID, Sql.varchar(36)).notNull().unique()
                .column(columnRegistered, Sql.LONG).notNull()
                .column(columnName, Sql.varchar(16)).notNull()
                .column(columnTimesKicked, Sql.INT).notNull().defaultValue("0")
                .primaryKey(usingMySQL, columnID)
                .toString()
        );
    }

    /**
     * @return @throws SQLException
     */
    public Set<UUID> getSavedUUIDs() throws SQLException {
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            Set<UUID> uuids = new HashSet<>();
            statement = prepareStatement(Select.from(tableName, columnUUID).toString());
            set = statement.executeQuery();
            while (set.next()) {
                UUID uuid = UUID.fromString(set.getString(columnUUID));
                uuids.add(uuid);
            }
            return uuids;
        } finally {
            endTransaction(statement);
            close(set, statement);
        }
    }

    /**
     * @param uuid
     * @return
     */
    @Override
    public boolean removeUser(UUID uuid) {
        PreparedStatement statement = null;
        try {
            statement = prepareStatement("DELETE FROM " + tableName + " WHERE (" + columnUUID + "=?)");
            statement.setString(1, uuid.toString());

            statement.execute();
            commit(statement.getConnection());
            return true;
        } catch (SQLException ex) {
            return false;
        } finally {
            close(statement);
        }
    }

    /**
     * @return
     */
    public String getColumnID() {
        return columnID;
    }

    public String getColumnUUID() {
        return columnUUID;
    }

    /**
     * @param playername
     * @return
     * @throws SQLException
     */
    public UUID getUuidOf(String playername) throws SQLException {
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            statement = prepareStatement(Select.from(tableName, columnUUID)
                    .where("UPPER(" + columnName + ")=UPPER(?)")
                    .toString());
            statement.setString(1, playername);
            set = statement.executeQuery();
            if (set.next()) {
                String uuidS = set.getString(columnUUID);
                return UUID.fromString(uuidS);
            }
            return null;
        } finally {
            endTransaction(statement);
            close(set, statement);
        }
    }

    public List<Long> getRegisterDates() throws SQLException {
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            statement = prepareStatement(Select.from(tableName, columnRegistered).toString());
            set = statement.executeQuery();
            List<Long> registerDates = new ArrayList<>();
            while (set.next()) {
                registerDates.add(set.getLong(columnRegistered));
            }
            return registerDates;
        } finally {
            endTransaction(statement);
            close(set, statement);
        }
    }

    /**
     * Register a new user (UUID) to the database.
     *
     * @param uuid       UUID of the player.
     * @param registered Register date.
     * @param name       Name of the player.
     * @throws SQLException
     * @throws IllegalArgumentException If uuid or name are null.
     */
    public void registerUser(UUID uuid, long registered, String name) throws SQLException {
        Verify.nullCheck(uuid, name);

        PreparedStatement statement = null;
        try {
            statement = prepareStatement(Insert.values(tableName,
                    columnUUID,
                    columnRegistered,
                    columnName));
            statement.setString(1, uuid.toString());
            statement.setLong(2, registered);
            statement.setString(3, name);

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
            statement = prepareStatement(Select.from(tableName, columnID)
                    .where(columnUUID + "=?")
                    .toString());
            statement.setString(1, uuid.toString());
            set = statement.executeQuery();
            return set.next();
        } finally {
            endTransaction(statement);
            close(set, statement);
        }
    }

    public void updateName(UUID uuid, String name) throws SQLException {
        PreparedStatement statement = null;
        try {
            statement = prepareStatement(Update.values(tableName, columnName)
                    .where(columnUUID + "=?")
                    .toString());
            statement.setString(1, name);
            statement.setString(2, uuid.toString());

            statement.execute();
            commit(statement.getConnection());
        } finally {
            close(statement);
        }
    }

    public int getTimesKicked(UUID uuid) throws SQLException {
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            statement = prepareStatement(Select.from(tableName, columnTimesKicked)
                    .where(columnUUID + "=?")
                    .toString());
            statement.setString(1, uuid.toString());
            set = statement.executeQuery();
            if (set.next()) {
                return set.getInt(columnTimesKicked);
            }
            return 0;
        } finally {
            endTransaction(statement);
            close(set, statement);
        }
    }

    public void kicked(UUID uuid) throws SQLException {
        PreparedStatement statement = null;
        try {
            statement = prepareStatement("UPDATE " + tableName + " SET "
                    + columnTimesKicked + "=" + columnTimesKicked + "+ 1" +
                    " WHERE " + columnUUID + "=?");
            statement.setString(1, uuid.toString());

            statement.execute();
            commit(statement.getConnection());
        } finally {
            close(statement);
        }
    }

    public String getPlayerName(UUID uuid) throws SQLException {
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            statement = prepareStatement(Select.from(tableName, columnName)
                    .where(columnUUID + "=?")
                    .toString());
            statement.setString(1, uuid.toString());
            set = statement.executeQuery();
            if (set.next()) {
                return set.getString(columnName);
            }
            return null;
        } finally {
            close(set, statement);
        }
    }

    public String getColumnName() {
        return columnName;
    }
}
