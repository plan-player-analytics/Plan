package com.djrapitops.plan.system.database.databases.sql.tables;

import com.djrapitops.plan.api.exceptions.database.DBInitException;
import com.djrapitops.plan.data.container.PlayerKill;
import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;
import com.djrapitops.plan.system.database.databases.sql.processing.ExecStatement;
import com.djrapitops.plan.system.database.databases.sql.processing.QueryAllStatement;
import com.djrapitops.plan.system.database.databases.sql.processing.QueryStatement;
import com.djrapitops.plan.system.database.databases.sql.statements.Column;
import com.djrapitops.plan.system.database.databases.sql.statements.Sql;
import com.djrapitops.plan.system.database.databases.sql.statements.TableSqlParser;
import com.djrapitops.plugin.utilities.Verify;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Table that is in charge of storing kill data for each session.
 * <p>
 * Table Name: plan_kills
 * <p>
 * For contained columns {@see Col}
 *
 * @author Rsl1122
 */
public class KillsTable extends UserIDTable {

    public KillsTable(SQLDB db) {
        super("plan_kills", db);
        sessionsTable = db.getSessionsTable();
        insertStatement = "INSERT INTO " + tableName + " ("
                + Col.KILLER_ID + ", "
                + Col.VICTIM_ID + ", "
                + Col.SESSION_ID + ", "
                + Col.DATE + ", "
                + Col.WEAPON
                + ") VALUES ("
                + usersTable.statementSelectID + ", "
                + usersTable.statementSelectID + ", "
                + "?, ?, ?)";
    }

    private final SessionsTable sessionsTable;
    private String insertStatement;

    @Override
    public void createTable() throws DBInitException {
        createTable(TableSqlParser.createTable(tableName)
                .column(Col.KILLER_ID, Sql.INT).notNull()
                .column(Col.VICTIM_ID, Sql.INT).notNull()
                .column(Col.WEAPON, Sql.varchar(30)).notNull()
                .column(Col.DATE, Sql.LONG).notNull()
                .column(Col.SESSION_ID, Sql.INT).notNull()
                .foreignKey(Col.KILLER_ID, usersTable.getTableName(), UsersTable.Col.ID)
                .foreignKey(Col.VICTIM_ID, usersTable.getTableName(), UsersTable.Col.ID)
                .foreignKey(Col.SESSION_ID, sessionsTable.getTableName(), SessionsTable.Col.ID)
                .toString()
        );
    }

    @Override
    public void removeUser(UUID uuid) throws SQLException {
        String sql = "DELETE FROM " + tableName +
                " WHERE " + Col.KILLER_ID + " = " + usersTable.statementSelectID +
                " OR " + Col.VICTIM_ID + " = " + usersTable.statementSelectID;

        execute(new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, uuid.toString());
                statement.setString(2, uuid.toString());
            }
        });
    }

    public void addKillsToSessions(UUID uuid, Map<Integer, Session> sessions) throws SQLException {
        String usersIDColumn = usersTable + "." + UsersTable.Col.ID;
        String usersUUIDColumn = usersTable + "." + UsersTable.Col.UUID + " as victim_uuid";
        String sql = "SELECT " +
                Col.SESSION_ID + ", " +
                Col.DATE + ", " +
                Col.WEAPON + ", " +
                usersUUIDColumn +
                " FROM " + tableName +
                " INNER JOIN " + usersTable + " on " + usersIDColumn + "=" + Col.VICTIM_ID +
                " WHERE " + Col.KILLER_ID + "=" + usersTable.statementSelectID;

        query(new QueryStatement<Object>(sql, 50000) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, uuid.toString());
            }

            @Override
            public Object processResults(ResultSet set) throws SQLException {
                while (set.next()) {
                    int sessionID = set.getInt(Col.SESSION_ID.get());
                    Session session = sessions.get(sessionID);
                    if (session == null) {
                        continue;
                    }
                    String uuidS = set.getString("victim_uuid");
                    UUID victim = UUID.fromString(uuidS);
                    long date = set.getLong(Col.DATE.get());
                    String weapon = set.getString(Col.WEAPON.get());
                    session.getPlayerKills().add(new PlayerKill(victim, weapon, date));
                }
                return null;
            }
        });
    }

    public void savePlayerKills(UUID uuid, int sessionID, List<PlayerKill> playerKills) throws SQLException {
        if (Verify.isEmpty(playerKills)) {
            return;
        }

        executeBatch(new ExecStatement(insertStatement) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                for (PlayerKill kill : playerKills) {
                    UUID victim = kill.getVictim();
                    long date = kill.getTime();
                    String weapon = kill.getWeapon();
                    if (Verify.containsNull(victim, uuid)) {
                        continue;
                    }

                    statement.setString(1, uuid.toString());
                    statement.setString(2, victim.toString());
                    statement.setInt(3, sessionID);
                    statement.setLong(4, date);
                    statement.setString(5, weapon);
                    statement.addBatch();
                }
            }
        });
    }

    public Map<UUID, List<PlayerKill>> getPlayerKills() throws SQLException {
        String usersVictimIDColumn = usersTable + "." + UsersTable.Col.ID;
        String usersKillerIDColumn = "a." + UsersTable.Col.ID;
        String usersVictimUUIDColumn = usersTable + "." + UsersTable.Col.UUID + " as victim_uuid";
        String usersKillerUUIDColumn = "a." + UsersTable.Col.UUID + " as killer_uuid";
        String sql = "SELECT " +
                Col.DATE + ", " +
                Col.WEAPON + ", " +
                usersVictimUUIDColumn + ", " +
                usersKillerUUIDColumn +
                " FROM " + tableName +
                " INNER JOIN " + usersTable + " on " + usersVictimIDColumn + "=" + Col.VICTIM_ID +
                " INNER JOIN " + usersTable + " a on " + usersKillerIDColumn + "=" + Col.KILLER_ID;

        return query(new QueryAllStatement<Map<UUID, List<PlayerKill>>>(sql, 50000) {
            @Override
            public Map<UUID, List<PlayerKill>> processResults(ResultSet set) throws SQLException {
                Map<UUID, List<PlayerKill>> allKills = new HashMap<>();
                while (set.next()) {
                    UUID killer = UUID.fromString(set.getString("killer_uuid"));
                    UUID victim = UUID.fromString(set.getString("victim_uuid"));
                    long date = set.getLong(Col.DATE.get());
                    String weapon = set.getString(Col.WEAPON.get());
                    List<PlayerKill> kills = allKills.getOrDefault(killer, new ArrayList<>());
                    kills.add(new PlayerKill(victim, weapon, date));
                    allKills.put(killer, kills);
                }
                return allKills;
            }
        });
    }

    public Map<Integer, List<PlayerKill>> getAllPlayerKillsBySessionID() throws SQLException {
        String usersIDColumn = usersTable + "." + UsersTable.Col.ID;
        String usersUUIDColumn = usersTable + "." + UsersTable.Col.UUID + " as victim_uuid";
        String sql = "SELECT " +
                Col.SESSION_ID + ", " +
                Col.DATE + ", " +
                Col.WEAPON + ", " +
                usersUUIDColumn +
                " FROM " + tableName +
                " INNER JOIN " + usersTable + " on " + usersIDColumn + "=" + Col.VICTIM_ID;

        return query(new QueryAllStatement<Map<Integer, List<PlayerKill>>>(sql, 50000) {
            @Override
            public Map<Integer, List<PlayerKill>> processResults(ResultSet set) throws SQLException {
                Map<Integer, List<PlayerKill>> allPlayerKills = new HashMap<>();
                while (set.next()) {
                    int sessionID = set.getInt(Col.SESSION_ID.get());

                    List<PlayerKill> playerKills = allPlayerKills.getOrDefault(sessionID, new ArrayList<>());

                    String uuidS = set.getString("victim_uuid");
                    UUID victim = UUID.fromString(uuidS);
                    long date = set.getLong(Col.DATE.get());
                    String weapon = set.getString(Col.WEAPON.get());
                    playerKills.add(new PlayerKill(victim, weapon, date));

                    allPlayerKills.put(sessionID, playerKills);
                }
                return allPlayerKills;
            }
        });
    }

    public void addKillsToSessions(Map<UUID, Map<UUID, List<Session>>> map) throws SQLException {
        Map<Integer, List<PlayerKill>> playerKillsBySessionID = getAllPlayerKillsBySessionID();
        for (UUID serverUUID : map.keySet()) {
            for (List<Session> sessions : map.get(serverUUID).values()) {
                for (Session session : sessions) {
                    List<PlayerKill> playerKills = playerKillsBySessionID.get(session.getSessionID());
                    if (playerKills != null) {
                        session.setPlayerKills(playerKills);
                    }
                }
            }
        }
    }

    public void savePlayerKills(Map<UUID, Map<UUID, List<Session>>> allSessions) throws SQLException {
        if (Verify.isEmpty(allSessions)) {
            return;
        }

        executeBatch(new ExecStatement(insertStatement) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                // Every server
                for (UUID serverUUID : allSessions.keySet()) {
                    // Every player
                    for (Map.Entry<UUID, List<Session>> entry : allSessions.get(serverUUID).entrySet()) {
                        UUID killer = entry.getKey();
                        List<Session> sessions = entry.getValue();
                        // Every session
                        for (Session session : sessions) {
                            int sessionID = session.getSessionID();
                            // Every kill
                            for (PlayerKill kill : session.getPlayerKills()) {
                                UUID victim = kill.getVictim();
                                long date = kill.getTime();
                                String weapon = kill.getWeapon();
                                statement.setString(1, killer.toString());
                                statement.setString(2, victim.toString());
                                statement.setInt(3, sessionID);
                                statement.setLong(4, date);
                                statement.setString(5, weapon);
                                statement.addBatch();
                            }
                        }
                    }
                }
            }
        });
    }

    public enum Col implements Column {
        KILLER_ID("killer_id"),
        VICTIM_ID("victim_id"),
        SESSION_ID("session_id"),
        WEAPON("weapon"),
        DATE("date");

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
