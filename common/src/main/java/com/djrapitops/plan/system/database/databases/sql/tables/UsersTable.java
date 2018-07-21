package com.djrapitops.plan.system.database.databases.sql.tables;

import com.djrapitops.plan.api.exceptions.database.DBInitException;
import com.djrapitops.plan.data.container.UserInfo;
import com.djrapitops.plan.data.store.Key;
import com.djrapitops.plan.data.store.containers.DataContainer;
import com.djrapitops.plan.data.store.keys.PlayerKeys;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;
import com.djrapitops.plan.system.database.databases.sql.processing.ExecStatement;
import com.djrapitops.plan.system.database.databases.sql.processing.QueryAllStatement;
import com.djrapitops.plan.system.database.databases.sql.processing.QueryStatement;
import com.djrapitops.plan.system.database.databases.sql.statements.*;
import com.djrapitops.plugin.utilities.Verify;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Supplier;

/**
 * Table that is in charge of storing common player data for all servers.
 * <p>
 * Table Name: plan_users
 * <p>
 * For contained columns {@see Col}
 *
 * @author Rsl1122
 */
public class UsersTable extends UserIDTable {

    public final String statementSelectID;
    private String insertStatement;
    public UsersTable(SQLDB db) {
        super("plan_users", db);
        statementSelectID = "(" + Select.from(tableName, tableName + "." + Col.ID).where(Col.UUID + "=?").toString() + " LIMIT 1)";
        insertStatement = Insert.values(tableName,
                Col.UUID,
                Col.REGISTERED,
                Col.USER_NAME);
    }

    @Override
    public void createTable() throws DBInitException {
        createTable(TableSqlParser.createTable(tableName)
                .primaryKeyIDColumn(usingMySQL, Col.ID)
                .column(Col.UUID, Sql.varchar(36)).notNull().unique()
                .column(Col.REGISTERED, Sql.LONG).notNull()
                .column(Col.USER_NAME, Sql.varchar(16)).notNull()
                .column(Col.TIMES_KICKED, Sql.INT).notNull().defaultValue("0")
                .primaryKey(usingMySQL, Col.ID)
                .toString()
        );
    }

    /**
     * @return a {@link Set} of the saved UUIDs.
     */
    public Set<UUID> getSavedUUIDs() {
        String sql = Select.from(tableName, Col.UUID).toString();

        return query(new QueryAllStatement<Set<UUID>>(sql, 50000) {
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

    /**
     * Remove a user from Users Table.
     *
     * @param uuid the UUID of the user that should be removed.
     */
    @Override
    public void removeUser(UUID uuid) {
        String sql = "DELETE FROM " + tableName + " WHERE (" + Col.UUID + "=?)";

        execute(new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, uuid.toString());
            }
        });
    }

    /**
     * Get UUID of a player.
     *
     * @param playerName Name of a player
     * @return UUID of the player
     */
    public UUID getUuidOf(String playerName) {
        String sql = Select.from(tableName, Col.UUID)
                .where("UPPER(" + Col.USER_NAME + ")=UPPER(?)")
                .toString();

        return query(new QueryStatement<UUID>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, playerName);
            }

            @Override
            public UUID processResults(ResultSet set) throws SQLException {
                if (set.next()) {
                    String uuidS = set.getString(Col.UUID.get());
                    return UUID.fromString(uuidS);
                }
                return null;
            }
        });
    }

    public List<Long> getRegisterDates() {
        String sql = Select.from(tableName, Col.REGISTERED).toString();

        return query(new QueryAllStatement<List<Long>>(sql, 50000) {
            @Override
            public List<Long> processResults(ResultSet set) throws SQLException {
                List<Long> registerDates = new ArrayList<>();
                while (set.next()) {
                    registerDates.add(set.getLong(Col.REGISTERED.get()));
                }
                return registerDates;
            }
        });
    }

    public boolean isRegistered(UUID uuid) {
        String sql = Select.from(tableName, "COUNT(" + Col.ID + ") as c")
                .where(Col.UUID + "=?")
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

    /**
     * Register a new user (UUID) to the database.
     *
     * @param uuid       UUID of the player.
     * @param registered Register date.
     * @param name       Name of the player.
     * @throws IllegalArgumentException If uuid or name are null.
     */
    public void registerUser(UUID uuid, long registered, String name) {
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

    public void updateName(UUID uuid, String name) {
        String sql = Update.values(tableName, Col.USER_NAME.get())
                .where(Col.UUID + "=?")
                .toString();

        execute(new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, name);
                statement.setString(2, uuid.toString());
            }
        });
    }

    public int getTimesKicked(UUID uuid) {
        String sql = Select.from(tableName, Col.TIMES_KICKED)
                .where(Col.UUID + "=?")
                .toString();

        return query(new QueryStatement<Integer>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, uuid.toString());
            }

            @Override
            public Integer processResults(ResultSet set) throws SQLException {
                if (set.next()) {
                    return set.getInt(Col.TIMES_KICKED.get());
                }
                return 0;
            }
        });
    }

    public void kicked(UUID uuid) {
        String sql = "UPDATE " + tableName + " SET "
                + Col.TIMES_KICKED + "=" + Col.TIMES_KICKED + "+ 1" +
                " WHERE " + Col.UUID + "=?";

        execute(new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, uuid.toString());
            }
        });
    }

    public String getPlayerName(UUID uuid) {
        String sql = Select.from(tableName, Col.USER_NAME).where(Col.UUID + "=?").toString();

        return query(new QueryStatement<String>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, uuid.toString());
            }

            @Override
            public String processResults(ResultSet set) throws SQLException {
                if (set.next()) {
                    return set.getString(Col.USER_NAME.get());
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
     */
    public List<String> getMatchingNames(String name) {
        String searchString = "%" + name.toLowerCase() + "%";
        NicknamesTable nicknamesTable = db.getNicknamesTable();
        String sql = "SELECT DISTINCT " + Col.USER_NAME + " FROM " + tableName +
                " WHERE " + Col.USER_NAME + " LIKE ?" +
                " UNION SELECT DISTINCT " + Col.USER_NAME + " FROM " + tableName +
                " INNER JOIN " + nicknamesTable + " on " + Col.ID + "=" + nicknamesTable + "." + NicknamesTable.Col.USER_ID +
                " WHERE " + NicknamesTable.Col.NICKNAME + " LIKE ?";

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

    public Map<UUID, UserInfo> getUsers() {
        String sql = Select.all(tableName).toString();

        return query(new QueryAllStatement<Map<UUID, UserInfo>>(sql, 20000) {
            @Override
            public Map<UUID, UserInfo> processResults(ResultSet set) throws SQLException {
                Map<UUID, UserInfo> users = new HashMap<>();
                while (set.next()) {
                    UUID uuid = UUID.fromString(set.getString(Col.UUID.get()));
                    String name = set.getString(Col.USER_NAME.get());
                    long registered = set.getLong(Col.REGISTERED.get());

                    users.put(uuid, new UserInfo(uuid, name, registered, false, false));
                }
                return users;
            }
        });
    }

    /**
     * Inserts UUIDs, Register dates and Names to the table.
     * <p>
     * This method is for batch operations, and should not be used to add information of users.
     * Use UserInfoTable instead.
     *
     * @param users Users to insert
     */
    public void insertUsers(Map<UUID, UserInfo> users) {
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

    public void updateKicked(Map<UUID, Integer> timesKicked) {
        if (Verify.isEmpty(timesKicked)) {
            return;
        }

        String sql = "UPDATE " + tableName + " SET " + Col.TIMES_KICKED + "=? WHERE " + Col.UUID + "=?";

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

    public Map<UUID, Integer> getAllTimesKicked() {
        String sql = Select.from(tableName, Col.UUID, Col.TIMES_KICKED).toString();

        return query(new QueryAllStatement<Map<UUID, Integer>>(sql, 20000) {
            @Override
            public Map<UUID, Integer> processResults(ResultSet set) throws SQLException {
                Map<UUID, Integer> timesKicked = new HashMap<>();
                while (set.next()) {
                    UUID uuid = UUID.fromString(set.getString(Col.UUID.get()));
                    int kickCount = set.getInt(Col.TIMES_KICKED.get());

                    timesKicked.put(uuid, kickCount);
                }
                return timesKicked;
            }
        });
    }

    public Map<UUID, String> getPlayerNames() {
        String sql = Select.from(tableName, Col.UUID, Col.USER_NAME).toString();

        return query(new QueryAllStatement<Map<UUID, String>>(sql, 20000) {
            @Override
            public Map<UUID, String> processResults(ResultSet set) throws SQLException {
                Map<UUID, String> names = new HashMap<>();
                while (set.next()) {
                    UUID uuid = UUID.fromString(set.getString(Col.UUID.get()));
                    String name = set.getString(Col.USER_NAME.get());

                    names.put(uuid, name);
                }
                return names;
            }
        });
    }

    public Optional<Long> getRegisterDate(UUID uuid) {
        String sql = Select.from(tableName, Col.REGISTERED).where(Col.UUID + "=?").toString();

        return query(new QueryStatement<Optional<Long>>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, uuid.toString());
            }

            @Override
            public Optional<Long> processResults(ResultSet set) throws SQLException {
                if (set.next()) {
                    return Optional.of(set.getLong(Col.REGISTERED.get()));
                }
                return Optional.empty();
            }
        });
    }

    public int getPlayerCount() {
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

    public Map<Integer, UUID> getUUIDsByID() {
        String sql = Select.from(tableName, Col.ID, Col.UUID).toString();

        return query(new QueryAllStatement<Map<Integer, UUID>>(sql, 20000) {
            @Override
            public Map<Integer, UUID> processResults(ResultSet set) throws SQLException {
                Map<Integer, UUID> uuidsByID = new TreeMap<>();

                while (set.next()) {
                    int id = set.getInt(Col.ID.get());
                    UUID uuid = UUID.fromString(set.getString(Col.UUID.get()));
                    uuidsByID.put(id, uuid);
                }

                return uuidsByID;
            }
        });
    }

    public DataContainer getUserInformation(UUID uuid) {
        Key<DataContainer> key = new Key<>(DataContainer.class, "plan_users_data");
        DataContainer returnValue = new DataContainer();

        Supplier<DataContainer> usersTableResults = () -> {
            String sql = "SELECT * FROM " + tableName + " WHERE " + Col.UUID + "=?";

            return query(new QueryStatement<DataContainer>(sql) {
                @Override
                public void prepare(PreparedStatement statement) throws SQLException {
                    statement.setString(1, uuid.toString());
                }

                @Override
                public DataContainer processResults(ResultSet set) throws SQLException {
                    DataContainer container = new DataContainer();

                    if (set.next()) {
                        long registered = set.getLong(Col.REGISTERED.get());
                        String name = set.getString(Col.USER_NAME.get());
                        int timesKicked = set.getInt(Col.TIMES_KICKED.get());

                        container.putRawData(PlayerKeys.REGISTERED, registered);
                        container.putRawData(PlayerKeys.NAME, name);
                        container.putRawData(PlayerKeys.KICK_COUNT, timesKicked);
                    }

                    return container;
                }
            });
        };

        returnValue.putSupplier(key, usersTableResults);
        returnValue.putRawData(PlayerKeys.UUID, uuid);
        returnValue.putSupplier(PlayerKeys.REGISTERED, () -> returnValue.getUnsafe(key).getUnsafe(PlayerKeys.REGISTERED));
        returnValue.putSupplier(PlayerKeys.NAME, () -> returnValue.getUnsafe(key).getUnsafe(PlayerKeys.NAME));
        returnValue.putSupplier(PlayerKeys.KICK_COUNT, () -> returnValue.getUnsafe(key).getUnsafe(PlayerKeys.KICK_COUNT));
        return returnValue;
    }

    public enum Col implements Column {
        ID("id"),
        UUID("uuid"),
        REGISTERED("registered"),
        USER_NAME("name"),
        TIMES_KICKED("times_kicked");

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
