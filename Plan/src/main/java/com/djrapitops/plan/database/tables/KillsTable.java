package main.java.com.djrapitops.plan.database.tables;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.data.KillData;
import main.java.com.djrapitops.plan.database.databases.SQLDB;

/**
 *
 * @author Rsl1122
 */
public class KillsTable extends Table {

    private final String columnKillerUserID;
    private final String columnVictimUserID;
    private final String columnWeapon;
    private final String columnDate;

    public KillsTable(SQLDB db, boolean usingMySQL) {
        super("plan_kills", db, usingMySQL);
        columnWeapon = "weapon";
        columnDate = "date";
        columnKillerUserID = "killer_id";
        columnVictimUserID = "victim_id";
    }

    @Override
    public boolean createTable() {
        UsersTable usersTable = db.getUsersTable();
        try {
            execute("CREATE TABLE IF NOT EXISTS " + tableName + " ("
                    + columnKillerUserID + " integer NOT NULL, "
                    + columnVictimUserID + " integer NOT NULL, "
                    + columnWeapon + " varchar(30) NOT NULL, "
                    + columnDate + " bigint NOT NULL, "
                    + "FOREIGN KEY(" + columnKillerUserID + ") REFERENCES " + usersTable.getTableName() + "(" + usersTable.getColumnID() + "), "
                    + "FOREIGN KEY(" + columnVictimUserID + ") REFERENCES " + usersTable.getTableName() + "(" + usersTable.getColumnID() + ")"
                    + ")"
            );
            return true;
        } catch (SQLException ex) {
            Log.toLog(this.getClass().getName(), ex);
            return false;
        }
    }

    public boolean removeUserKillsAndVictims(int userId) {
        PreparedStatement statement = null;
        try {
            statement = prepareStatement("DELETE FROM " + tableName + " WHERE " + columnKillerUserID + " = ? OR " + columnVictimUserID + " = ?");
            statement.setInt(1, userId);
            statement.setInt(2, userId);
            statement.execute();
            return true;
        } catch (SQLException ex) {
            Log.toLog(this.getClass().getName(), ex);
            return false;
        } finally {
            close(statement);
        }
    }

    public List<KillData> getPlayerKills(int userId) throws SQLException {
        UsersTable usersTable = db.getUsersTable();
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            statement = prepareStatement("SELECT * FROM " + tableName + " WHERE (" + columnKillerUserID + "=?)");
            statement.setInt(1, userId);
            set = statement.executeQuery();
            List<KillData> killData = new ArrayList<>();
            while (set.next()) {
                int victimID = set.getInt(columnVictimUserID);
                UUID victimUUID = usersTable.getUserUUID(victimID + "");
                killData.add(new KillData(victimUUID, victimID, set.getString(columnWeapon), set.getLong(columnDate)));
            }
            return killData;
        } finally {
            close(set);
            close(statement);
        }
    }

    public void savePlayerKills(int userId, List<KillData> kills) throws SQLException {
        if (kills == null) {
            return;
        }
        kills.removeAll(getPlayerKills(userId));
        if (kills.isEmpty()) {
            return;
        }
        PreparedStatement statement = null;
        try {
            statement = prepareStatement("INSERT INTO " + tableName + " ("
                    + columnKillerUserID + ", "
                    + columnVictimUserID + ", "
                    + columnWeapon + ", "
                    + columnDate
                    + ") VALUES (?, ?, ?, ?)");
            boolean commitRequired = false;
            for (KillData kill : kills) {
                if (kill == null) {
                    continue;
                }
                statement.setInt(1, userId);
                statement.setInt(2, kill.getVictimUserID());
                statement.setString(3, kill.getWeapon());
                statement.setLong(4, kill.getDate());
                statement.addBatch();
                commitRequired = true;
            }
            if (commitRequired) {
                statement.executeBatch();
            }
        } finally {
            close(statement);
        }
    }
}
