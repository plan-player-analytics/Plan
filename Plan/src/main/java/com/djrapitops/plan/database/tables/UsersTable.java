package main.java.com.djrapitops.plan.database.tables;

import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.api.exceptions.DBCreateTableException;
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

    public final String statementSelectID;
    private final String columnID = "id";
    private final String columnUUID = "uuid";
    private final String columnRegistered = "registered";
    private final String columnName = "name";
    private final String columnTimesKicked = "times_kicked";

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
    public void createTable() throws DBCreateTableException {
        createTable(TableSqlParser.createTable(tableName)
                .primaryKeyIDColumn(usingMySQL, columnID)
                .column(columnUUID, Sql.varchar(36)).notNull().unique()
                .column(columnRegistered, Sql.LONG).notNull()
                .column(columnName, Sql.varchar(16)).notNull()
                .column(columnTimesKicked, Sql.INT).notNull().defaultValue("0")
                .primaryKey(usingMySQL, columnID)
                .toString()
        );
    }

    /**
     * @return a {@link Set} of the saved UUIDs.
     * @throws SQLException when an error at retrieving the UUIDs happens
     */
    public Set<UUID> getSavedUUIDs() throws SQLException {
        Set<UUID> uuids = new HashSet<>();

        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            statement = prepareStatement(Select.from(tableName, columnUUID).toString());
            statement.setFetchSize(2000);

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
     * @param uuid the UUID of the user that should be removed.
     * @return if the removal was successful.
     */
    @Override
    public void removeUser(UUID uuid) throws SQLException {
        PreparedStatement statement = null;
        try {
            statement = prepareStatement("DELETE FROM " + tableName + " WHERE (" + columnUUID + "=?)");
            statement.setString(1, uuid.toString());

            statement.execute();
            commit(statement.getConnection());
        } finally {
            close(statement);
        }
    }

    /**
     * @return the name of the column that inherits the ID.
     */
    public String getColumnID() {
        return columnID;
    }

    /**
     * @return the name of the column that inherits the UUID.
     */
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
            endTransaction(statement);
            close(set, statement);
        }
    }

    /**
     * Gets the names of the players which names or nicknames match {@code name}.
     *
     * @param name the name / nickname.
     * @return a list of distinct names.
     * @throws SQLException when an error at fetching the names happens.
     */
    public List<String> getMatchingNames(String name) throws SQLException {
        String searchString = "%" + name + "%";
        List<String> matchingNames = new ArrayList<>();

        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            NicknamesTable nicknamesTable = db.getNicknamesTable();
            statement = prepareStatement(
                    "SELECT " + columnName + " FROM " + tableName +
                            " WHERE " + columnName + " LIKE LOWER(?)" +
                            " UNION SELECT " + columnName + " FROM " + tableName +
                            " WHERE " + columnID + " =" +
                            " (SELECT " + columnID + " FROM " + nicknamesTable +
                            " WHERE " + nicknamesTable.getColumnNick() + " LIKE LOWER(?))"
            );
            statement.setString(1, searchString);
            statement.setString(2, searchString);

            set = statement.executeQuery();
            while (set.next()) {
                matchingNames.add(set.getString("name"));
            }
            return matchingNames;
        } finally {
            endTransaction(statement);
            close(set, statement);
        }
    }

    public String getColumnName() {
        return columnName;
    }
}
