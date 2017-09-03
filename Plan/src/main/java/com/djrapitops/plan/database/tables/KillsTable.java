package main.java.com.djrapitops.plan.database.tables;

import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.api.exceptions.DBCreateTableException;
import main.java.com.djrapitops.plan.data.PlayerKill;
import main.java.com.djrapitops.plan.data.Session;
import main.java.com.djrapitops.plan.data.time.GMTimes;
import main.java.com.djrapitops.plan.database.databases.SQLDB;
import main.java.com.djrapitops.plan.database.sql.Sql;
import main.java.com.djrapitops.plan.database.sql.TableSqlParser;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * @author Rsl1122
 */
public class KillsTable extends UserIDTable {

    private final String columnKillerUserID = "killer_id";
    private final String columnVictimUserID = "victim_id";
    private final String columnWeapon = "weapon";
    private final String columnDate = "date";
    private final String columnSessionID = "session_id";

    private final SessionsTable sessionsTable;
    private String insertStatement;

    /**
     * @param db
     * @param usingMySQL
     */
    public KillsTable(SQLDB db, boolean usingMySQL) {
        super("plan_kills", db, usingMySQL);
        sessionsTable = db.getSessionsTable();
        insertStatement = "INSERT INTO " + tableName + " ("
                + columnKillerUserID + ", "
                + columnVictimUserID + ", "
                + columnSessionID + ", "
                + columnDate + ", "
                + columnWeapon
                + ") VALUES ("
                + usersTable.statementSelectID + ", "
                + usersTable.statementSelectID + ", "
                + "?, ?, ?)";
    }

    /**
     * @return
     */
    @Override
    public void createTable() throws DBCreateTableException {
        createTable(TableSqlParser.createTable(tableName)
                .column(columnKillerUserID, Sql.INT).notNull()
                .column(columnVictimUserID, Sql.INT).notNull()
                .column(columnWeapon, Sql.varchar(30)).notNull()
                .column(columnDate, Sql.LONG).notNull()
                .column(columnSessionID, Sql.INT).notNull()
                .foreignKey(columnKillerUserID, usersTable.getTableName(), usersTable.getColumnID())
                .foreignKey(columnVictimUserID, usersTable.getTableName(), usersTable.getColumnID())
                .foreignKey(columnSessionID, sessionsTable.getTableName(), sessionsTable.getColumnID())
                .toString()
        );
    }

    @Override
    public void removeUser(UUID uuid) throws SQLException {
        PreparedStatement statement = null;
        try {
            statement = prepareStatement("DELETE FROM " + tableName +
                    " WHERE " + columnKillerUserID + " = " + usersTable.statementSelectID +
                    " OR " + columnVictimUserID + " = " + usersTable.statementSelectID);
            statement.setString(1, uuid.toString());
            statement.setString(2, uuid.toString());

            statement.execute();
            commit(statement.getConnection());
        } finally {
            close(statement);
        }
    }

    public void savePlayerKills(UUID uuid, int sessionID, List<PlayerKill> playerKills) throws SQLException {
        if (Verify.isEmpty(playerKills)) {
            return;
        }
        PreparedStatement statement = null;
        try {
            statement = prepareStatement(insertStatement);
            for (PlayerKill kill : playerKills) {
                UUID victim = kill.getVictim();
                long date = kill.getTime();
                String weapon = kill.getWeapon();
                statement.setString(1, uuid.toString());
                statement.setString(2, victim.toString());
                statement.setInt(3, sessionID);
                statement.setLong(4, date);
                statement.setString(5, weapon);
                statement.addBatch();
            }

            statement.executeBatch();
            commit(statement.getConnection());
        } finally {
            close(statement);
        }
    }

    public void addKillsToSessions(UUID uuid, Map<Integer, Session> sessions) throws SQLException {
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            String usersIDColumn = usersTable + "." + usersTable.getColumnID();
            String usersUUIDColumn = usersTable + "." + usersTable.getColumnUUID() + " as victim_uuid";
            statement = prepareStatement("SELECT " +
                    columnSessionID + ", " +
                    columnDate + ", " +
                    columnWeapon + ", " +
                    usersUUIDColumn +
                    " FROM " + tableName +
                    " JOIN " + usersTable + " on " + usersIDColumn + "=" + columnVictimUserID +
                    " WHERE " + columnKillerUserID + "=" + usersTable.statementSelectID);

            statement.setFetchSize(10000);
            statement.setString(1, uuid.toString());

            set = statement.executeQuery();

            while (set.next()) {
                int sessionID = set.getInt(columnSessionID);
                Session session = sessions.get(sessionID);
                if (session == null) {
                    continue;
                }
                String uuidS = set.getString("victim_uuid");
                UUID victim = UUID.fromString(uuidS);
                long date = set.getLong(columnDate);
                String weapon = set.getString(columnWeapon);
                session.getPlayerKills().add(new PlayerKill(victim, weapon, date));
            }
        } finally {
            close(set, statement);
        }
    }

    public Map<UUID, List<PlayerKill>> getPlayerKills() throws SQLException {
        return getPlayerKills(Plan.getServerUUID());
    }

    public Map<UUID, List<PlayerKill>> getPlayerKills(UUID serverUUID) throws SQLException {
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            String usersVictimIDColumn = usersTable + "." + usersTable.getColumnID();
            String usersKillerIDColumn = "a." + usersTable.getColumnID();
            String usersVictimUUIDColumn = usersTable + "." + usersTable.getColumnUUID() + " as victim_uuid";
            String usersKillerUUIDColumn = "a." + usersTable.getColumnUUID() + " as killer_uuid";
            statement = prepareStatement("SELECT " +
                    columnDate + ", " +
                    columnWeapon + ", " +
                    usersVictimUUIDColumn + ", " +
                    usersKillerUUIDColumn +
                    " FROM " + tableName +
                    " JOIN " + usersTable + " on " + usersVictimIDColumn + "=" + columnVictimUserID +
                    " JOIN " + usersTable + " a on " + usersKillerIDColumn + "=" + columnKillerUserID);

            statement.setFetchSize(10000);
            set = statement.executeQuery();

            Map<UUID, List<PlayerKill>> allKills = new HashMap<>();
            while (set.next()) {
                UUID killer = UUID.fromString(set.getString("killer_uuid"));
                UUID victim = UUID.fromString(set.getString("victim_uuid"));
                long date = set.getLong(columnDate);
                String weapon = set.getString(columnWeapon);
                List<PlayerKill> kills = allKills.getOrDefault(killer, new ArrayList<>());
                kills.add(new PlayerKill(victim, weapon, date));
                allKills.put(killer, kills);
            }
            return allKills;
        } finally {
            close(set, statement);
        }
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
        PreparedStatement statement = null;
        try {
            statement = prepareStatement(insertStatement);
            String[] gms = GMTimes.getGMKeyArray();
            for (UUID serverUUID : allSessions.keySet()) {
                for (Map.Entry<UUID, List<Session>> entry : allSessions.get(serverUUID).entrySet()) {
                    UUID killer = entry.getKey();
                    List<Session> sessions = entry.getValue();

                    for (Session session : sessions) {
                        int sessionID = session.getSessionID();
                        System.out.println(usersTable.getSavedUUIDs());
                        for (PlayerKill kill : session.getPlayerKills()) {
                            System.out.println(kill);
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
            statement.executeBatch();
            commit(statement.getConnection());
        } finally {
            close(statement);
        }
    }

    public Map<Integer, List<PlayerKill>> getAllPlayerKillsBySessionID() throws SQLException {
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            String usersIDColumn = usersTable + "." + usersTable.getColumnID();
            String usersUUIDColumn = usersTable + "." + usersTable.getColumnUUID() + " as victim_uuid";
            statement = prepareStatement("SELECT " +
                    columnSessionID + ", " +
                    columnDate + ", " +
                    columnWeapon + ", " +
                    usersUUIDColumn +
                    " FROM " + tableName +
                    " JOIN " + usersTable + " on " + usersIDColumn + "=" + columnVictimUserID);

            statement.setFetchSize(10000);

            set = statement.executeQuery();

            Map<Integer, List<PlayerKill>> allPlayerKills = new HashMap<>();
            while (set.next()) {
                int sessionID = set.getInt(columnSessionID);

                List<PlayerKill> playerKills = allPlayerKills.getOrDefault(sessionID, new ArrayList<>());

                String uuidS = set.getString("victim_uuid");
                UUID victim = UUID.fromString(uuidS);
                long date = set.getLong(columnDate);
                String weapon = set.getString(columnWeapon);
                playerKills.add(new PlayerKill(victim, weapon, date));

                allPlayerKills.put(sessionID, playerKills);
            }
            return allPlayerKills;
        } finally {
            close(set, statement);
        }
    }
}
