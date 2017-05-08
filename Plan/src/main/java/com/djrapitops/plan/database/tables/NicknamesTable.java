package main.java.com.djrapitops.plan.database.tables;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.database.databases.SQLDB;

/**
 *
 * @author Rsl1122
 */
public class NicknamesTable extends Table {

    private final String columnUserID;
    private final String columnNick;
    private final String columnCurrent;

    public NicknamesTable(SQLDB db, boolean usingMySQL) {
        super("plan_nicknames", db, usingMySQL);
        columnUserID = "user_id";
        columnNick = "nickname";
        columnCurrent = "current_nick";
    }

    @Override
    public boolean createTable() {
        UsersTable usersTable = db.getUsersTable();
        try {
            execute("CREATE TABLE IF NOT EXISTS " + tableName + " ("
                    + columnUserID + " integer NOT NULL, "
                    + columnNick + " varchar(75) NOT NULL, "
                    + columnCurrent + " boolean NOT NULL DEFAULT 0, "
                    + "FOREIGN KEY(" + columnUserID + ") REFERENCES " + usersTable.getTableName() + "(" + usersTable.getColumnID() + ")"
                    + ")"
            );
            if (getVersion() < 3) {
                alterTablesV3();
            }
            return true;
        } catch (SQLException ex) {
            Log.toLog(this.getClass().getName(), ex);
            return false;
        }
    }

    private void alterTablesV3() {
        String query;
        if (usingMySQL) {
            query = "ALTER TABLE " + tableName + " ADD " + columnCurrent + " boolean NOT NULL DEFAULT 0";

        } else {
            query = "ALTER TABLE " + tableName + " ADD COLUMN " + columnCurrent + " boolean NOT NULL DEFAULT 0";
        }
        try {
            execute(query);
        } catch (Exception e) {
        }
    }

    public boolean removeUserNicknames(int userId) {
        PreparedStatement statement = null;
        try {
            statement = prepareStatement("DELETE FROM " + tableName + " WHERE (" + columnUserID + "=?)");
            statement.setInt(1, userId);
            statement.execute();
            return true;
        } catch (SQLException ex) {
            Log.toLog(this.getClass().getName(), ex);
            return false;
        } finally {
            close(statement);
        }
    }

    public List<String> getNicknames(int userId) throws SQLException {
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            statement = prepareStatement("SELECT * FROM " + tableName + " WHERE (" + columnUserID + "=?)");
            statement.setInt(1, userId);
            set = statement.executeQuery();
            List<String> nicknames = new ArrayList<>();
            String lastNick = "";
            while (set.next()) {
                String nickname = set.getString(columnNick);
                if (nickname.isEmpty()) {
                    continue;
                }
                nicknames.add(nickname);
                if (set.getBoolean(columnCurrent)) {
                    lastNick = nickname;
                }
            }
            if (!lastNick.isEmpty()) {
                nicknames.remove(lastNick);
                nicknames.add(lastNick);
            }
            return nicknames;
        } finally {
            close(set);
            close(statement);
        }
    }

    public void saveNickList(int userId, Set<String> names, String lastNick) throws SQLException {
        if (names == null || names.isEmpty()) {
            return;
        }
        names.removeAll(getNicknames(userId));
        if (names.isEmpty()) {
            return;
        }
        PreparedStatement statement = null;
        try {
            statement = prepareStatement("INSERT INTO " + tableName + " ("
                    + columnUserID + ", "
                    + columnCurrent + ", "
                    + columnNick
                    + ") VALUES (?, ?, ?)");
            boolean commitRequired = false;
            for (String name : names) {
                statement.setInt(1, userId);
                statement.setInt(2, (name.equals(lastNick)) ? 1 : 0);
                statement.setString(3, name);
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
