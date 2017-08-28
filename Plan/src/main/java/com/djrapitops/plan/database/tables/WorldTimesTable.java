package main.java.com.djrapitops.plan.database.tables;

import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.api.exceptions.DbCreateTableException;
import main.java.com.djrapitops.plan.data.Session;
import main.java.com.djrapitops.plan.data.time.GMTimes;
import main.java.com.djrapitops.plan.data.time.WorldTimes;
import main.java.com.djrapitops.plan.database.databases.SQLDB;
import main.java.com.djrapitops.plan.database.sql.Sql;
import main.java.com.djrapitops.plan.database.sql.TableSqlParser;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Table class representing database table plan_world_times.
 *
 * @author Rsl1122
 * @since 4.0.0
 */
public class WorldTimesTable extends UserIDTable {

    private final String columnSessionID = "session_id";
    private final String columnWorldId = "world_id";
    private final String columnSurvival = "survival_time";
    private final String columnCreative = "creative_time";
    private final String columnAdventure = "adventure_time";
    private final String columnSpectator = "spectator_time";

    private final WorldTable worldTable;
    private final SessionsTable sessionsTable;

    /**
     * Constructor.
     *
     * @param db         Database this table is a part of.
     * @param usingMySQL Database is a MySQL database.
     */
    public WorldTimesTable(SQLDB db, boolean usingMySQL) {
        super("plan_world_times", db, usingMySQL);
        worldTable = db.getWorldTable();
        sessionsTable = db.getSessionsTable();
    }

    @Override
    public void createTable() throws DbCreateTableException {
        createTable(TableSqlParser.createTable(tableName)
                .column(columnUserID, Sql.INT).notNull()
                .column(columnWorldId, Sql.INT).notNull()
                .column(columnSessionID, Sql.INT).notNull()
                .column(columnSurvival, Sql.LONG).notNull().defaultValue("0")
                .column(columnCreative, Sql.LONG).notNull().defaultValue("0")
                .column(columnAdventure, Sql.LONG).notNull().defaultValue("0")
                .column(columnSpectator, Sql.LONG).notNull().defaultValue("0")
                .foreignKey(columnUserID, usersTable.getTableName(), usersTable.getColumnID())
                .foreignKey(columnWorldId, worldTable.getTableName(), worldTable.getColumnID())
                .foreignKey(columnSessionID, sessionsTable.getTableName(), sessionsTable.getColumnID())
                .toString()
        );
    }

    public void saveWorldTimes(UUID uuid, int sessionID, WorldTimes worldTimes) throws SQLException {
        Map<String, GMTimes> worldTimesMap = worldTimes.getWorldTimes();
        if (Verify.isEmpty(worldTimesMap)) {
            return;
        }

        Set<String> worldNames = worldTimesMap.keySet();
        db.getWorldTable().saveWorlds(worldNames);

        PreparedStatement statement = null;
        try {
            statement = prepareStatement("INSERT INTO " + tableName + " (" +
                    columnUserID + ", " +
                    columnWorldId + ", " +
                    columnSessionID + ", " +
                    columnSurvival + ", " +
                    columnCreative + ", " +
                    columnAdventure + ", " +
                    columnSpectator +
                    ") VALUES (" +
                    usersTable.statementSelectID + ", " +
                    worldTable.statementSelectID + ", " +
                    "?, ?, ?, ?, ?)");

            for (Map.Entry<String, GMTimes> entry : worldTimesMap.entrySet()) {
                String worldName = entry.getKey();
                GMTimes gmTimes = entry.getValue();
                statement.setString(1, uuid.toString());
                statement.setString(2, worldName);
                statement.setInt(3, sessionID);

                String[] gms = GMTimes.getGMKeyArray();
                statement.setLong(4, gmTimes.getTime(gms[0]));
                statement.setLong(5, gmTimes.getTime(gms[1]));
                statement.setLong(6, gmTimes.getTime(gms[2]));
                statement.setLong(7, gmTimes.getTime(gms[3]));
                statement.addBatch();
            }

            statement.executeBatch();
            commit(statement.getConnection());
        } finally {
            close(statement);
        }
    }

    public void addWorldTimesToSessions(UUID uuid, Map<Integer, Session> sessions) throws SQLException {
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            String worldIDColumn = worldTable + "." + worldTable.getColumnID();
            String worldNameColumn = worldTable + "." + worldTable.getColumnWorldName() + " as world_name";
            statement = prepareStatement("SELECT " +
                    columnSessionID + ", " +
                    columnSurvival + ", " +
                    columnCreative + ", " +
                    columnAdventure + ", " +
                    columnSpectator + ", " +
                    worldNameColumn +
                    " FROM " + tableName +
                    " JOIN " + worldTable + " on " + worldIDColumn + "=" + columnWorldId +
                    " WHERE " + columnUserID + "=" + usersTable.statementSelectID
            );
            statement.setString(1, uuid.toString());
            set = statement.executeQuery();
            String[] gms = GMTimes.getGMKeyArray();

            while (set.next()) {
                int sessionID = set.getInt(columnSessionID);
                Session session = sessions.get(sessionID);

                if (session == null) {
                    continue;
                }

                String worldName = set.getString("world_name");

                Map<String, Long> gmMap = new HashMap<>();
                gmMap.put(gms[0], set.getLong(columnSurvival));
                gmMap.put(gms[1], set.getLong(columnCreative));
                gmMap.put(gms[2], set.getLong(columnAdventure));
                gmMap.put(gms[3], set.getLong(columnSpectator));
                GMTimes gmTimes = new GMTimes(gmMap);

                session.getWorldTimes().setGMTimesForWorld(worldName, gmTimes);
            }
        } finally {
            endTransaction(statement);
            close(set, statement);
        }
    }

    public WorldTimes getWorldTimesOfServer() throws SQLException {
        return getWorldTimesOfServer(Plan.getServerUUID());
    }

    public WorldTimes getWorldTimesOfServer(UUID serverUUID) throws SQLException {
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            String worldIDColumn = worldTable + "." + worldTable.getColumnID();
            String worldNameColumn = worldTable + "." + worldTable.getColumnWorldName() + " as world_name";
            String sessionIDColumn = sessionsTable + "." + sessionsTable.getColumnID();
            String sessionServerIDColumn = sessionsTable + ".server_id";
            statement = prepareStatement("SELECT " +
                    "SUM(" + columnSurvival + ") as survival, " +
                    "SUM(" + columnCreative + ") as creative, " +
                    "SUM(" + columnAdventure + ") as adventure, " +
                    "SUM(" + columnSpectator + ") as spectator, " +
                    worldNameColumn +
                    " FROM " + tableName +
                    " JOIN " + worldTable + " on " + worldIDColumn + "=" + columnWorldId +
                    " JOIN " + sessionsTable + " on " + sessionIDColumn + "=" + columnSessionID +
                    " WHERE " + sessionServerIDColumn + "=" + db.getServerTable().statementSelectServerID
            );
            statement.setString(1, serverUUID.toString());
            set = statement.executeQuery();
            String[] gms = GMTimes.getGMKeyArray();

            WorldTimes worldTimes = new WorldTimes(new HashMap<>());
            while (set.next()) {
                String worldName = set.getString("world_name");

                Map<String, Long> gmMap = new HashMap<>();
                gmMap.put(gms[0], set.getLong("survival"));
                gmMap.put(gms[1], set.getLong("creative"));
                gmMap.put(gms[2], set.getLong("adventure"));
                gmMap.put(gms[3], set.getLong("spectator"));
                GMTimes gmTimes = new GMTimes(gmMap);

                worldTimes.setGMTimesForWorld(worldName, gmTimes);
            }
            return worldTimes;
        } finally {
            endTransaction(statement);
            close(set, statement);
        }
    }
}
