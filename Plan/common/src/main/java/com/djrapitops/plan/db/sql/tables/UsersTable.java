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
import com.djrapitops.plan.data.store.Key;
import com.djrapitops.plan.data.store.containers.DataContainer;
import com.djrapitops.plan.data.store.keys.PlayerKeys;
import com.djrapitops.plan.db.DBType;
import com.djrapitops.plan.db.SQLDB;
import com.djrapitops.plan.db.access.ExecStatement;
import com.djrapitops.plan.db.access.QueryAllStatement;
import com.djrapitops.plan.db.access.QueryStatement;
import com.djrapitops.plan.db.sql.parsing.*;
import com.djrapitops.plugin.utilities.Verify;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Table that is in charge of storing common player data for all servers.
 * <p>
 * Table Name: plan_users
 * <p>
 * For contained columns {@link Col}
 *
 * @author Rsl1122
 */
public class UsersTable extends UserUUIDTable {

    public static final String TABLE_NAME = "plan_users";

    public static final String ID = "id";
    public static final String USER_UUID = "uuid";
    public static final String REGISTERED = "registered";
    public static final String USER_NAME = "name";
    public static final String TIMES_KICKED = "times_kicked";

    public UsersTable(SQLDB db) {
        super(TABLE_NAME, db);
        statementSelectID = "(" + Select.from(tableName, tableName + "." + Col.ID).where(Col.UUID + "=?").toString() + " LIMIT 1)";
        insertStatement = Insert.values(tableName,
                Col.UUID,
                Col.REGISTERED,
                Col.USER_NAME);
    }

    public final String statementSelectID;
    private String insertStatement;

    public static String createTableSQL(DBType dbType) {
        return CreateTableParser.create(TABLE_NAME, dbType)
                .column(ID, Sql.INT).primaryKey()
                .column(USER_UUID, Sql.varchar(36)).notNull().unique()
                .column(REGISTERED, Sql.LONG).notNull()
                .column(USER_NAME, Sql.varchar(16)).notNull()
                .column(TIMES_KICKED, Sql.INT).notNull().defaultValue("0")
                .toString();
    }

    @Override
    public void createTable() throws DBInitException {
        createTable(TableSqlParser.createTable(tableName)
                .primaryKeyIDColumn(supportsMySQLQueries, Col.ID)
                .column(Col.UUID, Sql.varchar(36)).notNull().unique()
                .column(Col.REGISTERED, Sql.LONG).notNull()
                .column(Col.USER_NAME, Sql.varchar(16)).notNull()
                .column(Col.TIMES_KICKED, Sql.INT).notNull().defaultValue("0")
                .primaryKey(supportsMySQLQueries, Col.ID)
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
        String searchString = "%" + name + "%";
        NicknamesTable nicknamesTable = db.getNicknamesTable();
        String sql = "SELECT DISTINCT " + Col.USER_NAME + " FROM " + tableName +
                " WHERE LOWER(" + Col.USER_NAME + ") LIKE LOWER(?)" +
                " UNION SELECT DISTINCT " + Col.USER_NAME + " FROM " + tableName +
                " INNER JOIN " + nicknamesTable + " on " + tableName + "." + USER_UUID + "=" + nicknamesTable + "." + NicknamesTable.USER_UUID +
                " WHERE LOWER(" + NicknamesTable.NICKNAME + ") LIKE LOWER(?)";

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

    /**
     * Gets the {@code UUID} and the name of the player mapped to the user ID
     *
     * @return a {@code Map<Integer, Map.Entry<UUID, String>>} where the key is the user ID
     * and the value is an {@code Map.Entry<UUID, String>>} of the player's {@code UUID} and name
     */
    public Map<Integer, Map.Entry<UUID, String>> getUUIDsAndNamesByID() {
        String sql = Select.from(tableName, Col.ID, Col.UUID, Col.USER_NAME).toString();
        return query(new QueryAllStatement<Map<Integer, Map.Entry<UUID, String>>>(sql, 20000) {
            @Override
            public Map<Integer, Map.Entry<UUID, String>> processResults(ResultSet set) throws SQLException {
                Map<Integer, Map.Entry<UUID, String>> uuidsAndNamesByID = new TreeMap<>();
                while (set.next()) {
                    int id = set.getInt(Col.ID.get());
                    UUID uuid = UUID.fromString(set.getString(Col.UUID.get()));
                    String name = set.getString(Col.USER_NAME.get());
                    uuidsAndNamesByID.put(id, new AbstractMap.SimpleEntry<>(uuid, name));
                }
                return uuidsAndNamesByID;
            }
        });
    }

    public DataContainer getUserInformation(UUID uuid) {
        Key<DataContainer> user_data = new Key<>(DataContainer.class, "plan_users_data");
        DataContainer returnValue = new DataContainer();

        returnValue.putSupplier(user_data, () -> getUserInformationDataContainer(uuid));
        returnValue.putRawData(PlayerKeys.UUID, uuid);
        returnValue.putSupplier(PlayerKeys.REGISTERED, () -> returnValue.getUnsafe(user_data).getValue(PlayerKeys.REGISTERED).orElse(null));
        returnValue.putSupplier(PlayerKeys.NAME, () -> returnValue.getUnsafe(user_data).getValue(PlayerKeys.NAME).orElse(null));
        returnValue.putSupplier(PlayerKeys.KICK_COUNT, () -> returnValue.getUnsafe(user_data).getValue(PlayerKeys.KICK_COUNT).orElse(null));
        return returnValue;
    }

    private DataContainer getUserInformationDataContainer(UUID uuid) {
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
    }

    @Deprecated
    public enum Col implements Column {
        @Deprecated
        ID("id"),
        @Deprecated
        UUID("uuid"),
        @Deprecated
        REGISTERED("registered"),
        @Deprecated
        USER_NAME("name"),
        @Deprecated
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
