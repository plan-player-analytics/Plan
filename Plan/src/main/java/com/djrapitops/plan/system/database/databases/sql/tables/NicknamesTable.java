package com.djrapitops.plan.system.database.databases.sql.tables;

import com.djrapitops.plan.api.exceptions.database.DBInitException;
import com.djrapitops.plan.data.store.objects.Nickname;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;
import com.djrapitops.plan.system.database.databases.sql.processing.ExecStatement;
import com.djrapitops.plan.system.database.databases.sql.processing.QueryAllStatement;
import com.djrapitops.plan.system.database.databases.sql.processing.QueryStatement;
import com.djrapitops.plan.system.database.databases.sql.statements.Column;
import com.djrapitops.plan.system.database.databases.sql.statements.Sql;
import com.djrapitops.plan.system.database.databases.sql.statements.TableSqlParser;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plugin.utilities.Verify;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Table that is in charge of storing nickname data.
 * <p>
 * Table Name: plan_nicknames
 * <p>
 * For contained columns {@see Col}
 *
 * @author Rsl1122
 */
public class NicknamesTable extends UserIDTable {

    // TODO Add last used

    public NicknamesTable(SQLDB db) {
        super("plan_nicknames", db);
        serverTable = db.getServerTable();
        insertStatement = "INSERT INTO " + tableName + " (" +
                Col.USER_ID + ", " +
                Col.SERVER_ID + ", " +
                Col.NICKNAME +
                ") VALUES (" +
                usersTable.statementSelectID + ", " +
                serverTable.statementSelectServerID + ", " +
                "?)";
    }

    private final ServerTable serverTable;
    private String insertStatement;

    @Override
    public void createTable() throws DBInitException {
        createTable(TableSqlParser.createTable(tableName)
                .column(Col.USER_ID, Sql.INT).notNull()
                .column(Col.NICKNAME, Sql.varchar(75)).notNull()
                .column(Col.SERVER_ID, Sql.INT).notNull()
                .foreignKey(Col.USER_ID, usersTable.getTableName(), UsersTable.Col.ID)
                .foreignKey(Col.SERVER_ID, serverTable.getTableName(), ServerTable.Col.SERVER_ID)
                .toString()
        );
    }

    /**
     * Get ALL nicknames of the user by Server UUID.
     * <p>
     * Get's nicknames from other servers as well.
     *
     * @param uuid UUID of the Player
     * @return The nicknames of the User in a map by ServerUUID
     */
    public Map<UUID, List<String>> getAllNicknames(UUID uuid) {
        String serverIDColumn = serverTable + "." + ServerTable.Col.SERVER_ID;
        String serverUUIDColumn = serverTable + "." + ServerTable.Col.SERVER_UUID + " as s_uuid";
        String sql = "SELECT " +
                Col.NICKNAME + ", " +
                serverUUIDColumn +
                " FROM " + tableName +
                " INNER JOIN " + serverTable + " on " + serverIDColumn + "=" + Col.SERVER_ID +
                " WHERE (" + Col.USER_ID + "=" + usersTable.statementSelectID + ")";

        return query(new QueryStatement<Map<UUID, List<String>>>(sql, 5000) {

            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, uuid.toString());
            }

            @Override
            public Map<UUID, List<String>> processResults(ResultSet set) throws SQLException {
                Map<UUID, List<String>> map = new HashMap<>();
                while (set.next()) {
                    UUID serverUUID = UUID.fromString(set.getString("s_uuid"));

                    List<String> nicknames = map.getOrDefault(serverUUID, new ArrayList<>());

                    nicknames.add(set.getString(Col.NICKNAME.get()));

                    map.put(serverUUID, nicknames);
                }
                return map;
            }
        });
    }

    /**
     * Get nicknames of the user on a server.
     * <p>
     * Get's nicknames from other servers as well.
     *
     * @param uuid       UUID of the Player
     * @param serverUUID UUID of the server
     * @return The nicknames of the User
     */
    public List<String> getNicknames(UUID uuid, UUID serverUUID) {
        String sql = "SELECT " + Col.NICKNAME + " FROM " + tableName +
                " WHERE (" + Col.USER_ID + "=" + usersTable.statementSelectID + ")" +
                " AND " + Col.SERVER_ID + "=" + serverTable.statementSelectServerID;

        return query(new QueryStatement<List<String>>(sql, 1000) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, uuid.toString());
                statement.setString(2, serverUUID.toString());
            }

            @Override
            public List<String> processResults(ResultSet set) throws SQLException {
                List<String> nicknames = new ArrayList<>();
                while (set.next()) {
                    String nickname = set.getString(Col.NICKNAME.get());
                    if (nickname.isEmpty()) {
                        continue;
                    }
                    if (!nicknames.contains(nickname)) {
                        nicknames.add(nickname);
                    }
                }
                return nicknames;
            }
        });
    }

    /**
     * Get nicknames of the user on THIS server.
     * <p>
     * Get's nicknames from other servers as well.
     *
     * @param uuid UUID of the Player
     * @return The nicknames of the User
     */
    public List<String> getNicknames(UUID uuid) {
        return getNicknames(uuid, ServerInfo.getServerUUID());
    }

    public Map<UUID, Map<UUID, List<String>>> getAllNicknames() {
        String usersIDColumn = usersTable + "." + UsersTable.Col.ID;
        String usersUUIDColumn = usersTable + "." + UsersTable.Col.UUID + " as uuid";
        String serverIDColumn = serverTable + "." + ServerTable.Col.SERVER_ID;
        String serverUUIDColumn = serverTable + "." + ServerTable.Col.SERVER_UUID + " as s_uuid";
        String sql = "SELECT " +
                Col.NICKNAME + ", " +
                usersUUIDColumn + ", " +
                serverUUIDColumn +
                " FROM " + tableName +
                " INNER JOIN " + usersTable + " on " + usersIDColumn + "=" + Col.USER_ID +
                " INNER JOIN " + serverTable + " on " + serverIDColumn + "=" + Col.SERVER_ID;

        return query(new QueryAllStatement<Map<UUID, Map<UUID, List<String>>>>(sql, 5000) {
            @Override
            public Map<UUID, Map<UUID, List<String>>> processResults(ResultSet set) throws SQLException {
                Map<UUID, Map<UUID, List<String>>> map = new HashMap<>();
                while (set.next()) {
                    UUID serverUUID = UUID.fromString(set.getString("s_uuid"));
                    UUID uuid = UUID.fromString(set.getString("uuid"));

                    Map<UUID, List<String>> serverMap = map.getOrDefault(serverUUID, new HashMap<>());
                    List<String> nicknames = serverMap.getOrDefault(uuid, new ArrayList<>());

                    nicknames.add(set.getString(Col.NICKNAME.get()));

                    serverMap.put(uuid, nicknames);
                    map.put(serverUUID, serverMap);
                }
                return map;
            }
        });
    }

    public void saveUserName(UUID uuid, String displayName) {
        List<String> saved = getNicknames(uuid);
        if (saved.contains(displayName)) {
            return;
        }

        execute(new ExecStatement(insertStatement) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, uuid.toString());
                statement.setString(2, ServerInfo.getServerUUID().toString());
                statement.setString(3, displayName);
            }
        });
    }

    public List<Nickname> getNicknameInformation(UUID uuid) {
        String serverIDColumn = serverTable + "." + ServerTable.Col.SERVER_ID;
        String serverUUIDColumn = serverTable + "." + ServerTable.Col.SERVER_UUID + " as s_uuid";
        String sql = "SELECT " +
                Col.NICKNAME + ", " +
                serverUUIDColumn +
                " FROM " + tableName +
                " INNER JOIN " + serverTable + " on " + serverIDColumn + "=" + Col.SERVER_ID +
                " WHERE (" + Col.USER_ID + "=" + usersTable.statementSelectID + ")";

        return query(new QueryStatement<List<Nickname>>(sql, 5000) {

            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, uuid.toString());
            }

            @Override
            public List<Nickname> processResults(ResultSet set) throws SQLException {
                List<Nickname> nicknames = new ArrayList<>();
                while (set.next()) {
                    UUID serverUUID = UUID.fromString(set.getString("s_uuid"));
                    String nickname = set.getString(Col.NICKNAME.get());
                    nicknames.add(new Nickname(nickname, -1, serverUUID)); // TODO Add last used
                }
                return nicknames;
            }
        });
    }

    public enum Col implements Column {
        USER_ID(UserIDTable.Col.USER_ID.get()),
        SERVER_ID("server_id"),
        NICKNAME("nickname");

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

    public void insertNicknames(Map<UUID, Map<UUID, List<String>>> allNicknames) {
        if (Verify.isEmpty(allNicknames)) {
            return;
        }

        executeBatch(new ExecStatement(insertStatement) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                // Every Server
                for (UUID serverUUID : allNicknames.keySet()) {
                    // Every User
                    for (Map.Entry<UUID, List<String>> entry : allNicknames.get(serverUUID).entrySet()) {
                        UUID uuid = entry.getKey();
                        // Every Nickname
                        List<String> nicknames = entry.getValue();
                        for (String nickname : nicknames) {
                            statement.setString(1, uuid.toString());
                            statement.setString(2, serverUUID.toString());
                            statement.setString(3, nickname);
                            statement.addBatch();
                        }
                    }
                }
            }
        });
    }
}
