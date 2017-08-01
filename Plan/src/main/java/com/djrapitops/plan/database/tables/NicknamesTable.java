package main.java.com.djrapitops.plan.database.tables;

import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.database.databases.SQLDB;
import main.java.com.djrapitops.plan.utilities.Benchmark;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * @author Rsl1122
 */
public class NicknamesTable extends Table {

    private final String columnUserID;
    private final String columnNick;
    private final String columnCurrent;

    /**
     * @param db
     * @param usingMySQL
     */
    public NicknamesTable(SQLDB db, boolean usingMySQL) {
        super("plan_nicknames", db, usingMySQL);
        columnUserID = "user_id";
        columnNick = "nickname";
        columnCurrent = "current_nick";
    }

    /**
     * @return
     */
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
        addColumns(columnCurrent + " boolean NOT NULL DEFAULT 0");
    }

    /**
     * @param userId
     * @return
     */
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

    /**
     * @param userId
     * @return
     * @throws SQLException
     */
    public List<String> getNicknames(int userId) throws SQLException {
        Benchmark.start("Database: Get Nicknames");
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
            Benchmark.stop("Database: Get Nicknames");
        }
    }

    /**
     * @param userId
     * @param names
     * @param lastNick
     * @throws SQLException
     */
    public void saveNickList(int userId, Set<String> names, String lastNick) throws SQLException {
        if (names == null || names.isEmpty()) {
            return;
        }
        Benchmark.start("Database: Save Nicknames");
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
            int i = 0;
            for (String name : names) {
                statement.setInt(1, userId);
                statement.setInt(2, (name.equals(lastNick)) ? 1 : 0);
                statement.setString(3, name);
                statement.addBatch();
                commitRequired = true;
                i++;
            }
            if (commitRequired) {
                Log.debug("Executing nicknames batch: " + i);
                statement.executeBatch();

            }
        } finally {
            close(statement);
            Benchmark.stop("Database: Save Nicknames");
        }
    }

    /**
     * @param ids
     * @return
     * @throws SQLException
     */
    public Map<Integer, List<String>> getNicknames(Collection<Integer> ids) throws SQLException {
        if (ids == null || ids.isEmpty()) {
            return new HashMap<>();
        }
        Benchmark.start("Database: Get Nicknames Multiple");
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            Map<Integer, List<String>> nicks = new HashMap<>();
            Map<Integer, String> lastNicks = new HashMap<>();
            for (Integer id : ids) {
                nicks.put(id, new ArrayList<>());
            }
            statement = prepareStatement("SELECT * FROM " + tableName);
            set = statement.executeQuery();
            while (set.next()) {

                Integer id = set.getInt(columnUserID);
                if (!ids.contains(id)) {
                    continue;
                }
                String nickname = set.getString(columnNick);
                if (nickname.isEmpty()) {
                    continue;
                }
                nicks.get(id).add(nickname);
                if (set.getBoolean(columnCurrent)) {
                    lastNicks.put(id, nickname);
                }
            }

            for (Map.Entry<Integer, String> entry : lastNicks.entrySet()) {
                Integer id = entry.getKey();
                String lastNick = entry.getValue();

                List<String> list = nicks.get(id);

                // Moves the last known nickname to the end of the List.
                // This is due to the way nicknames are added to UserData,
                // Nicknames are stored as a Set and last Nickname is a separate String.
                list.set(list.size() - 1, lastNick);
            }

            return nicks;
        } finally {
            close(set);
            close(statement);
            Benchmark.stop("Database: Get Nicknames Multiple");
        }
    }

    /**
     * @param nicknames
     * @param lastNicks
     * @throws SQLException
     */
    public void saveNickLists(Map<Integer, Set<String>> nicknames, Map<Integer, String> lastNicks) throws SQLException {
        if (nicknames == null || nicknames.isEmpty()) {
            return;
        }

        Benchmark.start("Database: Save Nicknames Multiple");

        Map<Integer, List<String>> saved = getNicknames(nicknames.keySet());
        PreparedStatement statement = null;
        try {
            boolean commitRequired = false;
            statement = prepareStatement("INSERT INTO " + tableName + " ("
                    + columnUserID + ", "
                    + columnCurrent + ", "
                    + columnNick
                    + ") VALUES (?, ?, ?)");

            for (Map.Entry<Integer, Set<String>> entrySet : nicknames.entrySet()) {
                Integer id = entrySet.getKey();
                Set<String> newNicks = entrySet.getValue();

                String lastNick = lastNicks.get(id);
                List<String> s = saved.get(id);

                if (s != null) {
                    newNicks.removeAll(s);
                }

                if (newNicks.isEmpty()) {
                    continue;
                }

                for (String name : newNicks) {
                    statement.setInt(1, id);
                    statement.setInt(2, (name.equals(lastNick)) ? 1 : 0);
                    statement.setString(3, name);
                    statement.addBatch();
                    commitRequired = true;
                }
            }

            if (commitRequired) {
                statement.executeBatch();
            }
        } finally {
            close(statement);
            Benchmark.stop("Database: Save Nicknames Multiple");
        }
    }
}
