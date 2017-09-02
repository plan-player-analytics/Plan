package main.java.com.djrapitops.plan.database.tables;

import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.api.exceptions.DBCreateTableException;
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
public class NicknamesTable extends UserIDTable {

    private final String columnNick = "nickname";
    private final String columnServerID = "server_id";

    private final ServerTable serverTable;
    private String insertStatement;

    /**
     * @param db         The database
     * @param usingMySQL if the server is using MySQL
     */
    public NicknamesTable(SQLDB db, boolean usingMySQL) {
        super("plan_nicknames", db, usingMySQL);
        serverTable = db.getServerTable();
        insertStatement = "INSERT INTO " + tableName + " (" +
                columnUserID + ", " +
                columnServerID + ", " +
                columnNick +
                ") VALUES (" +
                usersTable.statementSelectID + ", " +
                serverTable.statementSelectServerID + ", " +
                "?)";
    }

    /**
     * @return if the table was created successfully
     */
    @Override
    public void createTable() throws DBCreateTableException {
        createTable(TableSqlParser.createTable(tableName)
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
            statement = prepareStatement(insertStatement);
            statement.setString(1, uuid.toString());
            statement.setString(2, Plan.getServerUUID().toString());
            statement.setString(3, displayName);

            statement.execute();
            commit(statement.getConnection());
        } finally {
            close(statement);
        }
    }

    public Map<UUID, Map<UUID, List<String>>> getAllNicknames() throws SQLException {
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            String usersIDColumn = usersTable + "." + usersTable.getColumnID();
            String usersUUIDColumn = usersTable + "." + usersTable.getColumnUUID() + " as uuid";
            String serverIDColumn = serverTable + "." + serverTable.getColumnID();
            String serverUUIDColumn = serverTable + "." + serverTable.getColumnUUID() + " as s_uuid";
            statement = prepareStatement("SELECT " +
                    columnNick + ", " +
                    usersUUIDColumn + ", " +
                    serverUUIDColumn +
                    " FROM " + tableName +
                    " JOIN " + usersTable + " on " + usersIDColumn + "=" + columnUserID +
                    " JOIN " + serverTable + " on " + serverIDColumn + "=" + columnServerID
            );
            statement.setFetchSize(5000);
            set = statement.executeQuery();
            Map<UUID, Map<UUID, List<String>>> map = new HashMap<>();
            while (set.next()) {
                UUID serverUUID = UUID.fromString(set.getString("s_uuid"));
                UUID uuid = UUID.fromString(set.getString("uuid"));

                Map<UUID, List<String>> serverMap = map.getOrDefault(serverUUID, new HashMap<>());
                List<String> nicknames = serverMap.getOrDefault(uuid, new ArrayList<>());

                nicknames.add(set.getString(columnNick));

                serverMap.put(uuid, nicknames);
                map.put(serverUUID, serverMap);
            }
            return map;
        } finally {
            endTransaction(statement);
            close(set, statement);
        }
    }

    public void insertNicknames(Map<UUID, Map<UUID, List<String>>> allNicknames) throws SQLException {
        if (Verify.isEmpty(allNicknames)) {
            return;
        }
        PreparedStatement statement = null;
        try {
            statement = prepareStatement(insertStatement);

            // Every Server
            for (UUID serverUUID : allNicknames.keySet()) {
                // Every User
                for (Map.Entry<UUID, List<String>> entry : allNicknames.get(serverUUID).entrySet()) {
                    UUID uuid = entry.getKey();
                    // Every Nickname
                    List<String> nicknames = entry.getValue();
                    for (String nickname : nicknames) {
                        statement.setString(1, uuid.toString());
                        statement.setString(2, serverUUID.toString());
                        statement.setString(3, nickname);
                        statement.addBatch();
                    }
                }
            }

            statement.executeBatch();
            commit(statement.getConnection());
        } finally {
            close(statement);
        }
    }

    public String getColumnNick() {
        return columnNick;
    }
}
