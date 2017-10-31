package main.java.com.djrapitops.plan.database.tables;

import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.api.exceptions.DBCreateTableException;
import main.java.com.djrapitops.plan.data.Session;
import main.java.com.djrapitops.plan.data.time.GMTimes;
import main.java.com.djrapitops.plan.data.time.WorldTimes;
import main.java.com.djrapitops.plan.database.databases.SQLDB;
import main.java.com.djrapitops.plan.database.processing.ExecStatement;
import main.java.com.djrapitops.plan.database.processing.QueryAllStatement;
import main.java.com.djrapitops.plan.database.processing.QueryStatement;
import main.java.com.djrapitops.plan.database.sql.Sql;
import main.java.com.djrapitops.plan.database.sql.TableSqlParser;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

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
    private String insertStatement;

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
        insertStatement = "INSERT INTO " + tableName + " (" +
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
                "?, ?, ?, ?, ?)";
    }

    @Override
    public void createTable() throws DBCreateTableException {
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

        executeBatch(new ExecStatement(insertStatement) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
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
            }
        });
    }

    public void addWorldTimesToSessions(UUID uuid, Map<Integer, Session> sessions) throws SQLException {
        String worldIDColumn = worldTable + "." + worldTable.getColumnID();
        String worldNameColumn = worldTable + "." + worldTable.getColumnWorldName() + " as world_name";
        String sql = "SELECT " +
                columnSessionID + ", " +
                columnSurvival + ", " +
                columnCreative + ", " +
                columnAdventure + ", " +
                columnSpectator + ", " +
                worldNameColumn +
                " FROM " + tableName +
                " JOIN " + worldTable + " on " + worldIDColumn + "=" + columnWorldId +
                " WHERE " + columnUserID + "=" + usersTable.statementSelectID;

        query(new QueryStatement<Object>(sql, 2000) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, uuid.toString());
            }

            @Override
            public Object processResults(ResultSet set) throws SQLException {
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
                return null;
            }
        });
    }

    public WorldTimes getWorldTimesOfServer() throws SQLException {
        return getWorldTimesOfServer(Plan.getServerUUID());
    }

    public WorldTimes getWorldTimesOfServer(UUID serverUUID) throws SQLException {
        String worldIDColumn = worldTable + "." + worldTable.getColumnID();
        String worldNameColumn = worldTable + "." + worldTable.getColumnWorldName() + " as world_name";
        String sessionIDColumn = sessionsTable + "." + sessionsTable.getColumnID();
        String sessionServerIDColumn = sessionsTable + ".server_id";
        String sql = "SELECT " +
                "SUM(" + columnSurvival + ") as survival, " +
                "SUM(" + columnCreative + ") as creative, " +
                "SUM(" + columnAdventure + ") as adventure, " +
                "SUM(" + columnSpectator + ") as spectator, " +
                worldNameColumn +
                " FROM " + tableName +
                " JOIN " + worldTable + " on " + worldIDColumn + "=" + columnWorldId +
                " JOIN " + sessionsTable + " on " + sessionIDColumn + "=" + columnSessionID +
                " WHERE " + sessionServerIDColumn + "=" + db.getServerTable().statementSelectServerID +
                " GROUP BY " + columnWorldId;

        return query(new QueryStatement<WorldTimes>(sql, 1000) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
            }

            @Override
            public WorldTimes processResults(ResultSet set) throws SQLException {
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
            }
        });
    }

    public WorldTimes getWorldTimesOfUser(UUID uuid) throws SQLException {
        String worldIDColumn = worldTable + "." + worldTable.getColumnID();
        String worldNameColumn = worldTable + "." + worldTable.getColumnWorldName() + " as world_name";
        String sql = "SELECT " +
                "SUM(" + columnSurvival + ") as survival, " +
                "SUM(" + columnCreative + ") as creative, " +
                "SUM(" + columnAdventure + ") as adventure, " +
                "SUM(" + columnSpectator + ") as spectator, " +
                worldNameColumn +
                " FROM " + tableName +
                " JOIN " + worldTable + " on " + worldIDColumn + "=" + columnWorldId +
                " WHERE " + columnUserID + "=" + usersTable.statementSelectID +
                " GROUP BY " + columnWorldId;

        return query(new QueryStatement<WorldTimes>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, uuid.toString());
            }

            @Override
            public WorldTimes processResults(ResultSet set) throws SQLException {
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
            }
        });
    }

    public void addWorldTimesToSessions(Map<UUID, Map<UUID, List<Session>>> map) throws SQLException {
        Map<Integer, WorldTimes> worldTimesBySessionID = getAllWorldTimesBySessionID();

        for (UUID serverUUID : map.keySet()) {
            for (List<Session> sessions : map.get(serverUUID).values()) {
                for (Session session : sessions) {
                    WorldTimes worldTimes = worldTimesBySessionID.get(session.getSessionID());
                    if (worldTimes != null) {
                        session.setWorldTimes(worldTimes);
                    }
                }
            }
        }
    }

    public void saveWorldTimes(Map<UUID, Map<UUID, List<Session>>> allSessions) throws SQLException {
        if (Verify.isEmpty(allSessions)) {
            return;
        }
        List<String> worldNames = allSessions.values().stream()
                .map(Map::values)
                .flatMap(Collection::stream)
                .flatMap(Collection::stream)
                .map(Session::getWorldTimes)
                .map(WorldTimes::getWorldTimes)
                .map(Map::keySet)
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.toList());
        db.getWorldTable().saveWorlds(worldNames);

        executeBatch(new ExecStatement(insertStatement) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                String[] gms = GMTimes.getGMKeyArray();
                // Every Server
                for (Map<UUID, List<Session>> serverSessions : allSessions.values()) {
                    // Every User
                    for (Map.Entry<UUID, List<Session>> entry : serverSessions.entrySet()) {
                        UUID uuid = entry.getKey();
                        List<Session> sessions = entry.getValue();
                        // Every Session
                        for (Session session : sessions) {
                            int sessionID = session.getSessionID();
                            // Every WorldTimes
                            for (Map.Entry<String, GMTimes> worldTimesEntry : session.getWorldTimes().getWorldTimes().entrySet()) {
                                String worldName = worldTimesEntry.getKey();
                                GMTimes gmTimes = worldTimesEntry.getValue();
                                statement.setString(1, uuid.toString());
                                statement.setString(2, worldName);
                                statement.setInt(3, sessionID);
                                statement.setLong(4, gmTimes.getTime(gms[0]));
                                statement.setLong(5, gmTimes.getTime(gms[1]));
                                statement.setLong(6, gmTimes.getTime(gms[2]));
                                statement.setLong(7, gmTimes.getTime(gms[3]));
                                statement.addBatch();
                            }
                        }
                    }
                }
            }
        });
    }

    public Map<Integer, WorldTimes> getAllWorldTimesBySessionID() throws SQLException {
        String worldIDColumn = worldTable + "." + worldTable.getColumnID();
        String worldNameColumn = worldTable + "." + worldTable.getColumnWorldName() + " as world_name";
        String sql = "SELECT " +
                columnSessionID + ", " +
                columnSurvival + ", " +
                columnCreative + ", " +
                columnAdventure + ", " +
                columnSpectator + ", " +
                worldNameColumn +
                " FROM " + tableName +
                " JOIN " + worldTable + " on " + worldIDColumn + "=" + columnWorldId;

        return query(new QueryAllStatement<Map<Integer, WorldTimes>>(sql, 50000) {
            @Override
            public Map<Integer, WorldTimes> processResults(ResultSet set) throws SQLException {
                String[] gms = GMTimes.getGMKeyArray();

                Map<Integer, WorldTimes> worldTimes = new HashMap<>();
                while (set.next()) {
                    int sessionID = set.getInt(columnSessionID);

                    String worldName = set.getString("world_name");

                    Map<String, Long> gmMap = new HashMap<>();
                    gmMap.put(gms[0], set.getLong(columnSurvival));
                    gmMap.put(gms[1], set.getLong(columnCreative));
                    gmMap.put(gms[2], set.getLong(columnAdventure));
                    gmMap.put(gms[3], set.getLong(columnSpectator));
                    GMTimes gmTimes = new GMTimes(gmMap);

                    WorldTimes worldTOfSession = worldTimes.getOrDefault(sessionID, new WorldTimes(new HashMap<>()));
                    worldTOfSession.setGMTimesForWorld(worldName, gmTimes);
                    worldTimes.put(sessionID, worldTOfSession);
                }
                return worldTimes;
            }
        });
    }

    String getColumnWorldId() {
        return columnWorldId;
    }

    String getColumnSessionID() {
        return columnSessionID;
    }
}
