package main.java.com.djrapitops.plan.database.tables;

import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.data.KillData;
import main.java.com.djrapitops.plan.data.Session;
import main.java.com.djrapitops.plan.database.databases.SQLDB;
import main.java.com.djrapitops.plan.database.sql.Sql;
import main.java.com.djrapitops.plan.database.sql.TableSqlParser;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    /**
     * @param db
     * @param usingMySQL
     */
    public KillsTable(SQLDB db, boolean usingMySQL) {
        super("plan_kills", db, usingMySQL);
        sessionsTable = db.getSessionsTable();
    }

    /**
     * @return
     */
    @Override
    public boolean createTable() {
        return createTable(TableSqlParser.createTable(tableName)
                .column(columnKillerUserID, Sql.INT).notNull()
                .column(columnVictimUserID, Sql.INT).notNull()
                .column(columnWeapon, Sql.varchar(30)).notNull()
                .column(columnDate, Sql.LONG).notNull()
                .column(columnSessionID, Sql.LONG).notNull()
                .foreignKey(columnKillerUserID, usersTable.getTableName(), usersTable.getColumnID())
                .foreignKey(columnVictimUserID, usersTable.getTableName(), usersTable.getColumnID())
                .foreignKey(columnSessionID, sessionsTable.getTableName(), sessionsTable.getColumnID())
                .toString()
        );
    }

    @Override
    public boolean removeUser(UUID uuid) {
        PreparedStatement statement = null;
        try {
            statement = prepareStatement("DELETE FROM " + tableName +
                    " WHERE " + columnKillerUserID + " = " + usersTable.statementSelectID +
                    " OR " + columnVictimUserID + " = " + usersTable.statementSelectID);
            statement.setString(1, uuid.toString());
            statement.setString(2, uuid.toString());
            statement.execute();
            return true;
        } catch (SQLException ex) {
            Log.toLog(this.getClass().getName(), ex);
            return false;
        } finally {
            close(statement);
        }
    }

    public void savePlayerKills(UUID uuid, long sessionID, List<KillData> playerKills) throws SQLException {
        if (Verify.isEmpty(playerKills)) {
            return;
        }
        PreparedStatement statement = null;
        try {
            statement = prepareStatement("INSERT INTO " + tableName + " ("
                    + columnKillerUserID + ", "
                    + columnVictimUserID + ", "
                    + columnSessionID + ", "
                    + columnDate + ", "
                    + columnWeapon
                    + ") VALUES ("
                    + usersTable.statementSelectID + ", "
                    + usersTable.statementSelectID + ", "
                    + "?, ?, ?)");
            for (KillData kill : playerKills) {
                UUID victim = kill.getVictim();
                long date = kill.getTime();
                String weapon = kill.getWeapon();
                statement.setString(1, uuid.toString());
                statement.setString(2, victim.toString());
                statement.setLong(3, sessionID);
                statement.setLong(4, date);
                statement.setString(5, weapon);
                statement.addBatch();
            }
            statement.executeBatch();
        } finally {
            close(statement);
        }
    }

    public void addKillsToSessions(UUID uuid, Map<Long, Session> sessions) throws SQLException {
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
            statement.setString(1, uuid.toString());
            set = statement.executeQuery();
            while (set.next()) {
                long sessionID = set.getLong(columnSessionID);
                Session session = sessions.get(sessionID);
                if (session == null) {
                    continue;
                }
                String uuidS = set.getString("victim_uuid");
                UUID victim = UUID.fromString(uuidS);
                long date = set.getLong(columnDate);
                String weapon = set.getString(columnWeapon);
                session.getPlayerKills().add(new KillData(victim, weapon, date));
            }
        } finally {
            close(set, statement);
        }
    }

    // TODO getPlayerKills (UUID)
}
