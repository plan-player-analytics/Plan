package com.djrapitops.plan.system.database.tables;

import com.djrapitops.plan.api.exceptions.DBCreateTableException;
import com.djrapitops.plan.data.container.UserInfo;
import com.djrapitops.plan.system.database.databases.SQLDB;
import com.djrapitops.plan.system.database.processing.ExecStatement;
import com.djrapitops.plan.system.database.processing.QueryAllStatement;
import com.djrapitops.plan.system.database.processing.QueryStatement;
import com.djrapitops.plan.system.database.sql.*;
import com.djrapitops.plugin.utilities.Verify;

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
    private String insertStatement;

    public UsersTable(SQLDB db, boolean usingMySQL) {
        super("plan_users", db, usingMySQL);
        statementSelectID = "(" + Select.from(tableName, tableName + "." + columnID).where(columnUUID + "=?").toString() + ")";
        insertStatement = Insert.values(tableName,
                columnUUID,
                columnRegistered,
                columnName);
    }

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
        String sql = Select.from(tableName, columnUUID).toString();

        return query(new QueryAllStatement<Set<UUID>>(sql, 50000) {
            @Override
            public Set<UUID> processResults(ResultSet set) throws SQLException {
                Set<UUID> uuids = new HashSet<>();
                while (set.next()) {
                    UUID uuid = UUID.fromString(set.getString(columnUUID));
                    uuids.add(uuid);
                }
                return uuids;
            }
        });
    }

    /**
     * Remove a user from Users Table.
     *
     * @param uuid the UUID of the user that should be removed.
     * @throws SQLException DB Error
     */
    @Override
    public void removeUser(UUID uuid) throws SQLException {
        String sql = "DELETE FROM " + tableName + " WHERE (" + columnUUID + "=?)";

        execute(new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, uuid.toString());
            }
        });
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
     * Get UUID of a player.
     *
     * @param playerName Name of a player
     * @return UUID of the player
     * @throws SQLException DB Error
     */
    public UUID getUuidOf(String playerName) throws SQLException {
        String sql = Select.from(tableName, columnUUID)
                .where("UPPER(" + columnName + ")=UPPER(?)")
                .toString();

        return query(new QueryStatement<UUID>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, playerName);
            }

            @Override
            public UUID processResults(ResultSet set) throws SQLException {
                if (set.next()) {
                    String uuidS = set.getString(columnUUID);
                    return UUID.fromString(uuidS);
                }
                return null;
            }
        });
    }

    public List<Long> getRegisterDates() throws SQLException {
        String sql = Select.from(tableName, columnRegistered).toString();

        return query(new QueryAllStatement<List<Long>>(sql, 50000) {
            @Override
            public List<Long> processResults(ResultSet set) throws SQLException {
                List<Long> registerDates = new ArrayList<>();
                while (set.next()) {
                    registerDates.add(set.getLong(columnRegistered));
                }
                return registerDates;
            }
        });
    }

    /**
     * Register a new user (UUID) to the database.
     *
     * @param uuid       UUID of the player.
     * @param registered Register date.
     * @param name       Name of the player.
     * @throws SQLException             DB Error
     * @throws IllegalArgumentException If uuid or name are null.
     */
    public void registerUser(UUID uuid, long registered, String name) throws SQLException {
        Verify.nullCheck(uuid, name);

        execute(new ExecStatement(insertStatement) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, uuid.toString());
                statement.setLong(2, registered);
                statement.setString(3, name);
            }
        });
    }

    public boolean isRegistered(UUID uuid) throws SQLException {
        String sql = Select.from(tableName, "COUNT(" + columnID + ") as c")
                .where(columnUUID + "=?")
                .toString();

        return query(new QueryStatement<Boolean>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, uuid.toString());
            }

            @Override
            public Boolean processResults(ResultSet set) throws SQLException {
                return set.next() && set.getInt("c") >= 1;
            }
        });
    }

    public void updateName(UUID uuid, String name) throws SQLException {
        String sql = Update.values(tableName, columnName)
                .where(columnUUID + "=?")
                .toString();

        execute(new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, name);
                statement.setString(2, uuid.toString());
            }
        });
    }

    public int getTimesKicked(UUID uuid) throws SQLException {
        String sql = Select.from(tableName, columnTimesKicked)
                .where(columnUUID + "=?")
                .toString();

        return query(new QueryStatement<Integer>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, uuid.toString());
            }

            @Override
            public Integer processResults(ResultSet set) throws SQLException {
                if (set.next()) {
                    return set.getInt(columnTimesKicked);
                }
                return 0;
            }
        });
    }

    public void kicked(UUID uuid) throws SQLException {
        String sql = "UPDATE " + tableName + " SET "
                + columnTimesKicked + "=" + columnTimesKicked + "+ 1" +
                " WHERE " + columnUUID + "=?";

        execute(new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, uuid.toString());
            }
        });
    }

    public String getPlayerName(UUID uuid) throws SQLException {
        String sql = Select.from(tableName, columnName).where(columnUUID + "=?").toString();

        return query(new QueryStatement<String>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, uuid.toString());
            }

            @Override
            public String processResults(ResultSet set) throws SQLException {
                if (set.next()) {
                    return set.getString(columnName);
                }
                return null;
            }
        });
    }

    /**
     * Gets the names of the players which names or nicknames match {@code name}.
     *
     * @param name the name / nickname.
     * @return a list of distinct names.
     * @throws SQLException when an error at fetching the names happens.
     */
    public List<String> getMatchingNames(String name) throws SQLException {
        String searchString = "%" + name.toLowerCase() + "%";
        NicknamesTable nicknamesTable = db.getNicknamesTable();
        String sql = "SELECT DISTINCT " + columnName + " FROM " + tableName +
                " WHERE " + columnName + " LIKE ?" +
                " UNION SELECT DISTINCT " + columnName + " FROM " + tableName +
                " JOIN " + nicknamesTable + " on " + columnID + "=" + nicknamesTable + "." + nicknamesTable.getColumnUserID() +
                " WHERE " + nicknamesTable.getColumnNick() + " LIKE ?";

        return query(new QueryStatement<List<String>>(sql, 5000) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, searchString);
                statement.setString(2, searchString);
            }

            @Override
            public List<String> processResults(ResultSet set) throws SQLException {
                List<String> matchingNames = new ArrayList<>();
                while (set.next()) {
                    String match = set.getString("name");
                    if (!matchingNames.contains(match)) {
                        matchingNames.add(match);
                    }
                }
                return matchingNames;
            }
        });
    }

    public String getColumnName() {
        return columnName;
    }

    /**
     * Inserts UUIDs, Register dates and Names to the table.
     * <p>
     * This method is for batch operations, and should not be used to add information of users.
     * Use UserInfoTable instead.
     *
     * @param users Users to insert
     * @throws SQLException DB Error
     */
    public void insertUsers(Map<UUID, UserInfo> users) throws SQLException {
        if (Verify.isEmpty(users)) {
            return;
        }

        executeBatch(new ExecStatement(insertStatement) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                for (Map.Entry<UUID, UserInfo> entry : users.entrySet()) {
                    UUID uuid = entry.getKey();
                    UserInfo info = entry.getValue();
                    long registered = info.getRegistered();
                    String name = info.getName();

                    statement.setString(1, uuid.toString());
                    statement.setLong(2, registered);
                    statement.setString(3, name);
                    statement.addBatch();
                }
            }
        });
    }

    public Map<UUID, UserInfo> getUsers() throws SQLException {
        String sql = Select.all(tableName).toString();

        return query(new QueryAllStatement<Map<UUID, UserInfo>>(sql, 20000) {
            @Override
            public Map<UUID, UserInfo> processResults(ResultSet set) throws SQLException {
                Map<UUID, UserInfo> users = new HashMap<>();
                while (set.next()) {
                    UUID uuid = UUID.fromString(set.getString(columnUUID));
                    String name = set.getString(columnName);
                    long registered = set.getLong(columnRegistered);

                    users.put(uuid, new UserInfo(uuid, name, registered, false, false));
                }
                return users;
            }
        });
    }

    public void updateKicked(Map<UUID, Integer> timesKicked) throws SQLException {
        if (Verify.isEmpty(timesKicked)) {
            return;
        }

        String sql = "UPDATE " + tableName + " SET " + columnTimesKicked + "=? WHERE " + columnUUID + "=?";

        executeBatch(new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                for (Map.Entry<UUID, Integer> entry : timesKicked.entrySet()) {
                    UUID uuid = entry.getKey();
                    int kickCount = entry.getValue();
                    statement.setInt(1, kickCount);
                    statement.setString(2, uuid.toString());
                    statement.addBatch();
                }
            }
        });
    }

    public Map<UUID, Integer> getAllTimesKicked() throws SQLException {
        String sql = Select.from(tableName, columnUUID, columnTimesKicked).toString();

        return query(new QueryAllStatement<Map<UUID, Integer>>(sql, 20000) {
            @Override
            public Map<UUID, Integer> processResults(ResultSet set) throws SQLException {
                Map<UUID, Integer> timesKicked = new HashMap<>();
                while (set.next()) {
                    UUID uuid = UUID.fromString(set.getString(columnUUID));
                    int kickCount = set.getInt(columnTimesKicked);

                    timesKicked.put(uuid, kickCount);
                }
                return timesKicked;
            }
        });
    }

    public Map<UUID, String> getPlayerNames() throws SQLException {
        String sql = Select.from(tableName, columnUUID, columnName).toString();

        return query(new QueryAllStatement<Map<UUID, String>>(sql, 20000) {
            @Override
            public Map<UUID, String> processResults(ResultSet set) throws SQLException {
                Map<UUID, String> names = new HashMap<>();
                while (set.next()) {
                    UUID uuid = UUID.fromString(set.getString(columnUUID));
                    String name = set.getString(columnName);

                    names.put(uuid, name);
                }
                return names;
            }
        });
    }

    public int getPlayerCount() throws SQLException {
        String sql = "SELECT COUNT(*) AS player_count FROM " + tableName;

        return query(new QueryAllStatement<Integer>(sql) {
            @Override
            public Integer processResults(ResultSet set) throws SQLException {
                if (set.next()) {
                    return set.getInt("player_count");
                }
                return 0;
            }
        });
    }

    public Optional<Long> getRegisterDate(UUID uuid) throws SQLException {
        String sql = Select.from(tableName, columnRegistered).where(columnUUID + "=?").toString();

        return query(new QueryStatement<Optional<Long>>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, uuid.toString());
            }

            @Override
            public Optional<Long> processResults(ResultSet set) throws SQLException {
                if (set.next()) {
                    return Optional.of(set.getLong(columnRegistered));
                }
                return Optional.empty();
            }
        });
    }
}
