package main.java.com.djrapitops.plan.database.tables;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.database.databases.SQLDB;
import main.java.com.djrapitops.plan.utilities.Benchmark;

/**
 *
 * @author Rsl1122
 */
public class NicknamesTable extends Table {

    private final String columnUserID;
    private final String columnNick;
    private final String columnCurrent;

    /**
     *
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
     *
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

    /**
     *
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
     *
     * @param userId
     * @return
     * @throws SQLException
     */
    public List<String> getNicknames(int userId) throws SQLException {
        Benchmark.start("Get Nicknames");
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
            Benchmark.stop("Get Nicknames");
        }
    }

    /**
     *
     * @param userId
     * @param names
     * @param lastNick
     * @throws SQLException
     */
    public void saveNickList(int userId, Set<String> names, String lastNick) throws SQLException {
        if (names == null || names.isEmpty()) {
            return;
        }
        Benchmark.start("Save Nicknames");
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
                Log.debug("Executing nicknames batch: "+i);
                statement.executeBatch();

            }
        } finally {
            close(statement);
            Benchmark.stop("Save Nicknames");
        }
    }

    /**
     *
     * @param ids
     * @return
     * @throws SQLException
     */
    public Map<Integer, List<String>> getNicknames(Collection<Integer> ids) throws SQLException {
        if (ids == null || ids.isEmpty()) {
            return new HashMap<>();
        }
        Benchmark.start("Get Nicknames Multiple "+ids.size());
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
            for (Integer id : lastNicks.keySet()) {
                String lastNick = lastNicks.get(id);
                List<String> list = nicks.get(id);
                list.remove(lastNick);
                list.add(lastNick);
            }

            return nicks;
        } finally {
            close(set);
            close(statement);
            Benchmark.stop("Get Nicknames Multiple "+ids.size());
        }
    }

    /**
     *
     * @param nicknames
     * @param lastNicks
     * @throws SQLException
     */
    public void saveNickLists(Map<Integer, Set<String>> nicknames, Map<Integer, String> lastNicks) throws SQLException {
        if (nicknames == null || nicknames.isEmpty()) {
            return;
        }
        Benchmark.start("Save Nicknames Multiple "+nicknames.size());
        Map<Integer, List<String>> saved = getNicknames(nicknames.keySet());
        PreparedStatement statement = null;
        try {
            boolean commitRequired = false;
            statement = prepareStatement("INSERT INTO " + tableName + " ("
                    + columnUserID + ", "
                    + columnCurrent + ", "
                    + columnNick
                    + ") VALUES (?, ?, ?)");
            for (Integer id : nicknames.keySet()) {
                Set<String> newNicks = nicknames.get(id);
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
            Benchmark.stop("Save Nicknames Multiple "+nicknames.size());
        }
    }
}
