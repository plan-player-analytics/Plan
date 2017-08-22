package main.java.com.djrapitops.plan.database.tables;

import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.database.databases.SQLDB;
import main.java.com.djrapitops.plan.database.sql.Sql;
import main.java.com.djrapitops.plan.database.sql.TableSqlParser;
import main.java.com.djrapitops.plan.utilities.Benchmark;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * @author Rsl1122
 */
public class NicknamesTable extends UserIDTable {

    private final String columnNick = "nickname";
    private final String columnCurrent = "current_nick";
    private final String columnServerID = "server_id"; //TODO

    /**
     * @param db         The database
     * @param usingMySQL if the server is using MySQL
     */
    public NicknamesTable(SQLDB db, boolean usingMySQL) {
        super("plan_nicknames", db, usingMySQL);
    }

    /**
     * @return if the table was created successfully
     */
    @Override
    public boolean createTable() {
        UsersTable usersTable = db.getUsersTable();
        try {
            execute(TableSqlParser.createTable(tableName)
                    .column(columnUserID, Sql.INT).notNull()
                    .column(columnNick, Sql.varchar(75)).notNull()
                    .column(columnCurrent, Sql.BOOL).notNull().defaultValue(false)
                    .foreignKey(columnUserID, usersTable.getTableName(), usersTable.getColumnID())
                    .toString()
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
     * @param userId The User ID from which the nicknames should be removed from
     * @return if the removal was successful
     */
    public boolean removeUserNicknames(int userId) {
        return super.removeDataOf(userId);
    }

    /**
     * @param userId The User ID from which the nicknames should be retrieved from
     * @return The nicknames of the User
     * @throws SQLException when an error at retrieval happens
     */
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
                nicknames.set(nicknames.size() - 1, lastNick);
            }

            return nicknames;
        } finally {
            close(set, statement);
        }
    }

    /**
     * @param userId   The User ID for which the nicknames should be saved for
     * @param names    The nicknames
     * @param lastNick The latest nickname
     * @throws SQLException when an error at saving happens
     */
    public void saveNickList(int userId, Set<String> names, String lastNick) throws SQLException {
        if (Verify.isEmpty(names)) {
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

            for (String name : names) {
                statement.setInt(1, userId);
                statement.setInt(2, name.equals(lastNick) ? 1 : 0);
                statement.setString(3, name);
                statement.addBatch();
            }

            statement.executeBatch();
        } finally {
            close(statement);
        }
    }

    /**
     * @param ids The User IDs for which the nicknames should be retrieved for
     * @return The User ID corresponding with the nicknames
     * @throws SQLException when an error at retrieval happens
     */
    public Map<Integer, List<String>> getNicknames(Collection<Integer> ids) throws SQLException {
        if (Verify.isEmpty(ids)) {
            return new HashMap<>();
        }

        Benchmark.start("Get Nicknames Multiple");

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
                String nickname = set.getString(columnNick);

                if (!ids.contains(id) || nickname.isEmpty()) {
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
            close(set, statement);
            Benchmark.stop("Database", "Get Nicknames Multiple");
        }
    }

    /**
     * @param nicknames The User ID corresponding to the nicknames
     * @param lastNicks The User ID corresponding with the last nick they inherited
     * @throws SQLException when an error at saving happens
     */
    public void saveNickLists(Map<Integer, Set<String>> nicknames, Map<Integer, String> lastNicks) throws SQLException {
        if (Verify.isEmpty(nicknames)) {
            return;
        }

        Benchmark.start("Save Nicknames Multiple");

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
            Benchmark.stop("Database", "Save Nicknames Multiple");
        }
    }
}
