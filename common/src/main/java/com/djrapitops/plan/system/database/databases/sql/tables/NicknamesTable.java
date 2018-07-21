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
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.task.RunnableFactory;
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

    private final ServerTable serverTable;
    private final String updateStatement;
    private String insertStatement;

    public NicknamesTable(SQLDB db) {
        super("plan_nicknames", db);
        serverTable = db.getServerTable();
        insertStatement = "INSERT INTO " + tableName + " (" +
                Col.USER_ID + ", " +
                Col.SERVER_ID + ", " +
                Col.NICKNAME + ", " +
                Col.LAST_USED +
                ") VALUES (" +
                usersTable.statementSelectID + ", " +
                serverTable.statementSelectServerID + ", " +
                "?, ?)";
        updateStatement = "UPDATE " + tableName + " SET " + Col.LAST_USED + "=?" +
                " WHERE " + Col.NICKNAME + "=?" +
                " AND " + Col.USER_ID + "=" + usersTable.statementSelectID +
                " AND " + Col.SERVER_ID + "=" + serverTable.statementSelectServerID;
    }

    @Override
    public void createTable() throws DBInitException {
        createTable(TableSqlParser.createTable(tableName)
                .column(Col.USER_ID, Sql.INT).notNull()
                .column(Col.NICKNAME, Sql.varchar(75)).notNull()
                .column(Col.SERVER_ID, Sql.INT).notNull()
                .column(Col.LAST_USED, Sql.LONG).notNull()
                .foreignKey(Col.USER_ID, usersTable.getTableName(), UsersTable.Col.ID)
                .foreignKey(Col.SERVER_ID, serverTable.getTableName(), ServerTable.Col.SERVER_ID)
                .toString()
        );
    }

    public void alterTableV19() {
        addColumns(Col.LAST_USED + " bigint NOT NULL DEFAULT '0'");

        RunnableFactory.createNew(new AbsRunnable("DB version 18->19") {
            @Override
            public void run() {
                // Create actions table if version 18 transfer is run concurrently.
                execute("CREATE TABLE IF NOT EXISTS plan_actions " +
                        "(action_id integer, date bigint, server_id integer, user_id integer, additional_info varchar(1))");

                Map<Integer, UUID> serverUUIDsByID = serverTable.getServerUUIDsByID();
                Map<UUID, Integer> serverIDsByUUID = new HashMap<>();
                for (Map.Entry<Integer, UUID> entry : serverUUIDsByID.entrySet()) {
                    serverIDsByUUID.put(entry.getValue(), entry.getKey());
                }

                String fetchSQL = "SELECT * FROM plan_actions WHERE action_id=3 ORDER BY date DESC";
                Map<Integer, Set<Nickname>> nicknames = query(new QueryAllStatement<Map<Integer, Set<Nickname>>>(fetchSQL, 10000) {
                    @Override
                    public Map<Integer, Set<Nickname>> processResults(ResultSet set) throws SQLException {
                        Map<Integer, Set<Nickname>> map = new HashMap<>();

                        while (set.next()) {
                            long date = set.getLong("date");
                            int userID = set.getInt(UserIDTable.Col.USER_ID.get());
                            int serverID = set.getInt("server_id");
                            UUID serverUUID = serverUUIDsByID.get(serverID);
                            Nickname nick = new Nickname(set.getString("additional_info"), date, serverUUID);
                            Set<Nickname> nicknames = map.getOrDefault(userID, new HashSet<>());
                            if (serverUUID == null || nicknames.contains(nick)) {
                                continue;
                            }
                            nicknames.add(nick);
                            map.put(userID, nicknames);
                        }

                        return map;
                    }
                });

                String updateSQL = "UPDATE " + tableName + " SET " + Col.LAST_USED + "=?" +
                        " WHERE " + Col.NICKNAME + "=?" +
                        " AND " + Col.USER_ID + "=?" +
                        " AND " + Col.SERVER_ID + "=?";

                executeBatch(new ExecStatement(updateSQL) {
                    @Override
                    public void prepare(PreparedStatement statement) throws SQLException {
                        for (Map.Entry<Integer, Set<Nickname>> entry : nicknames.entrySet()) {
                            Integer userId = entry.getKey();
                            Set<Nickname> nicks = entry.getValue();
                            for (Nickname nick : nicks) {
                                Integer serverID = serverIDsByUUID.get(nick.getServerUUID());
                                statement.setLong(1, nick.getDate());
                                statement.setString(2, nick.getName());
                                statement.setInt(3, userId);
                                statement.setInt(4, serverID);
                                statement.addBatch();
                            }
                        }
                    }
                });

                db.setVersion(19);
                executeUnsafe("DROP TABLE plan_actions");
            }
        }).runTaskAsynchronously();
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

    public Map<UUID, Map<UUID, List<Nickname>>> getAllNicknames() {
        String usersIDColumn = usersTable + "." + UsersTable.Col.ID;
        String usersUUIDColumn = usersTable + "." + UsersTable.Col.UUID + " as uuid";
        String serverIDColumn = serverTable + "." + ServerTable.Col.SERVER_ID;
        String serverUUIDColumn = serverTable + "." + ServerTable.Col.SERVER_UUID + " as s_uuid";
        String sql = "SELECT " +
                Col.NICKNAME + ", " +
                Col.LAST_USED + ", " +
                usersUUIDColumn + ", " +
                serverUUIDColumn +
                " FROM " + tableName +
                " INNER JOIN " + usersTable + " on " + usersIDColumn + "=" + Col.USER_ID +
                " INNER JOIN " + serverTable + " on " + serverIDColumn + "=" + Col.SERVER_ID;

        return query(new QueryAllStatement<Map<UUID, Map<UUID, List<Nickname>>>>(sql, 5000) {
            @Override
            public Map<UUID, Map<UUID, List<Nickname>>> processResults(ResultSet set) throws SQLException {
                Map<UUID, Map<UUID, List<Nickname>>> map = new HashMap<>();
                while (set.next()) {
                    UUID serverUUID = UUID.fromString(set.getString("s_uuid"));
                    UUID uuid = UUID.fromString(set.getString("uuid"));

                    Map<UUID, List<Nickname>> serverMap = map.getOrDefault(serverUUID, new HashMap<>());
                    List<Nickname> nicknames = serverMap.getOrDefault(uuid, new ArrayList<>());

                    nicknames.add(new Nickname(
                            set.getString(Col.NICKNAME.get()), set.getLong(Col.LAST_USED.get()), serverUUID
                    ));

                    serverMap.put(uuid, nicknames);
                    map.put(serverUUID, serverMap);
                }
                return map;
            }
        });
    }

    public void saveUserName(UUID uuid, Nickname name) {
        List<Nickname> saved = getNicknameInformation(uuid);
        if (saved.contains(name)) {
            updateNickname(uuid, name);
        } else {
            insertNickname(uuid, name);
        }
    }

    private void updateNickname(UUID uuid, Nickname name) {
        execute(new ExecStatement(updateStatement) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setLong(1, name.getDate());
                statement.setString(2, name.getName());
                statement.setString(3, uuid.toString());
                statement.setString(4, ServerInfo.getServerUUID().toString());
            }
        });
    }

    private void insertNickname(UUID uuid, Nickname name) {
        execute(new ExecStatement(insertStatement) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, uuid.toString());
                statement.setString(2, ServerInfo.getServerUUID().toString());
                statement.setString(3, name.getName());
                statement.setLong(4, name.getDate());
            }
        });
    }

    public List<Nickname> getNicknameInformation(UUID uuid) {
        String serverIDColumn = serverTable + "." + ServerTable.Col.SERVER_ID;
        String serverUUIDColumn = serverTable + "." + ServerTable.Col.SERVER_UUID + " as s_uuid";
        String sql = "SELECT " +
                Col.NICKNAME + ", " +
                Col.LAST_USED + ", " +
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
                    nicknames.add(new Nickname(nickname, set.getLong(Col.LAST_USED.get()), serverUUID));
                }
                return nicknames;
            }
        });
    }

    public void insertNicknames(Map<UUID, Map<UUID, List<Nickname>>> allNicknames) {
        if (Verify.isEmpty(allNicknames)) {
            return;
        }

        executeBatch(new ExecStatement(insertStatement) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                // Every Server
                for (UUID serverUUID : allNicknames.keySet()) {
                    // Every User
                    for (Map.Entry<UUID, List<Nickname>> entry : allNicknames.get(serverUUID).entrySet()) {
                        UUID uuid = entry.getKey();
                        // Every Nickname
                        List<Nickname> nicknames = entry.getValue();
                        for (Nickname nickname : nicknames) {
                            statement.setString(1, uuid.toString());
                            statement.setString(2, serverUUID.toString());
                            statement.setString(3, nickname.getName());
                            statement.setLong(4, nickname.getDate());
                            statement.addBatch();
                        }
                    }
                }
            }
        });
    }

    public enum Col implements Column {
        USER_ID(UserIDTable.Col.USER_ID.get()),
        SERVER_ID("server_id"),
        NICKNAME("nickname"),
        LAST_USED("last_used");

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
