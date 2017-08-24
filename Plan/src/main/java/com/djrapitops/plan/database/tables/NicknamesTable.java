package main.java.com.djrapitops.plan.database.tables;

import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.database.databases.SQLDB;
import main.java.com.djrapitops.plan.database.sql.Sql;
import main.java.com.djrapitops.plan.database.sql.TableSqlParser;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Rsl1122
 */
public class NicknamesTable extends UserIDTable {

    private final String columnNick = "nickname";
    private final String columnServerID = "server_id";

    private final ServerTable serverTable;

    /**
     * @param db         The database
     * @param usingMySQL if the server is using MySQL
     */
    public NicknamesTable(SQLDB db, boolean usingMySQL) {
        super("plan_nicknames", db, usingMySQL);
        serverTable = db.getServerTable();
    }

    /**
     * @return if the table was created successfully
     */
    @Override
    public boolean createTable() {
        return createTable(TableSqlParser.createTable(tableName)
                .column(columnUserID, Sql.INT).notNull()
                .column(columnNick, Sql.varchar(75)).notNull()
                .column(columnServerID, Sql.INT).notNull()
                .foreignKey(columnUserID, usersTable.getTableName(), usersTable.getColumnID())
                .foreignKey(columnServerID, serverTable.getTableName(), serverTable.getColumnID())
                .toString()
        );
    }

    /**
     * Get ALL nicknames of the user.
     * <p>
     * Get's nicknames from other servers as well.
     *
     * @param uuid UUID of the Player
     * @return The nicknames of the User
     * @throws SQLException when an error at retrieval happens
     */
    public List<String> getAllNicknames(UUID uuid) throws SQLException {
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            statement = prepareStatement("SELECT " + columnNick + " FROM " + tableName +
                    " WHERE (" + columnUserID + "=" + usersTable.statementSelectID + ")");
            statement.setString(1, uuid.toString());
            set = statement.executeQuery();

            List<String> nicknames = new ArrayList<>();
            while (set.next()) {
                String nickname = set.getString(columnNick);
                if (nickname.isEmpty()) {
                    continue;
                }
                if (!nicknames.contains(nickname)) {
                    nicknames.add(nickname);
                }
            }
            return nicknames;
        } finally {
            endTransaction(statement);
            close(set, statement);
        }
    }

    /**
     * Get nicknames of the user on THIS server.
     * <p>
     * Get's nicknames from other servers as well.
     *
     * @param uuid UUID of the Player
     * @return The nicknames of the User
     * @throws SQLException when an error at retrieval happens
     */
    public List<String> getNicknames(UUID uuid) throws SQLException {
        return getNicknames(uuid, Plan.getServerUUID());
    }

    /**
     * Get nicknames of the user on a server.
     * <p>
     * Get's nicknames from other servers as well.
     *
     * @param uuid       UUID of the Player
     * @param serverUUID UUID of the server
     * @return The nicknames of the User
     * @throws SQLException when an error at retrieval happens
     */
    public List<String> getNicknames(UUID uuid, UUID serverUUID) throws SQLException {
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            statement = prepareStatement("SELECT " + columnNick + " FROM " + tableName +
                    " WHERE (" + columnUserID + "=" + usersTable.statementSelectID + ")" +
                    " AND " + columnServerID + "=" + serverTable.statementSelectServerID
            );
            statement.setString(1, uuid.toString());
            statement.setString(2, serverUUID.toString());
            set = statement.executeQuery();

            List<String> nicknames = new ArrayList<>();
            while (set.next()) {
                String nickname = set.getString(columnNick);
                if (nickname.isEmpty()) {
                    continue;
                }
                if (!nicknames.contains(nickname)) {
                    nicknames.add(nickname);
                }
            }
            return nicknames;
        } finally {
            endTransaction(statement);
            close(set, statement);
        }
    }

    public void saveUserName(UUID uuid, String displayName) throws SQLException {
        List<String> saved = getNicknames(uuid);
        if (saved.contains(displayName)) {
            return;
        }

        PreparedStatement statement = null;
        try {
            statement = prepareStatement("INSERT INTO " + tableName + " (" +
                    columnUserID + ", " +
                    columnServerID + ", " +
                    columnNick +
                    ") VALUES (" +
                    usersTable.statementSelectID + ", " +
                    serverTable.statementSelectServerID + ", " +
                    "?)");
            statement.setString(1, uuid.toString());
            statement.setString(2, Plan.getServerUUID().toString());
            statement.setString(3, displayName);

            statement.execute();
            commit(statement.getConnection());
        } finally {
            close(statement);
        }
    }
}
