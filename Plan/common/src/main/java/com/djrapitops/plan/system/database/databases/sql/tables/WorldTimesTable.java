/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.system.database.databases.sql.tables;

import com.djrapitops.plan.api.exceptions.database.DBInitException;
import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.data.store.keys.SessionKeys;
import com.djrapitops.plan.data.time.GMTimes;
import com.djrapitops.plan.data.time.WorldTimes;
import com.djrapitops.plan.db.SQLDB;
import com.djrapitops.plan.db.patches.Version10Patch;
import com.djrapitops.plan.db.patches.WorldTimesOptimizationPatch;
import com.djrapitops.plan.db.patches.WorldTimesSeverIDPatch;
import com.djrapitops.plan.db.patches.WorldsServerIDPatch;
import com.djrapitops.plan.db.sql.parsing.Column;
import com.djrapitops.plan.db.sql.parsing.Sql;
import com.djrapitops.plan.db.sql.parsing.TableSqlParser;
import com.djrapitops.plan.system.database.databases.sql.processing.ExecStatement;
import com.djrapitops.plan.system.database.databases.sql.processing.QueryAllStatement;
import com.djrapitops.plan.system.database.databases.sql.processing.QueryStatement;
import com.djrapitops.plugin.utilities.Verify;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Table that is in charge of storing playtime data for each world in each GameMode.
 * <p>
 * Table Name: plan_world_times
 * <p>
 * For contained columns {@link Col}
 * <p>
 * Patches related to this table:
 * {@link Version10Patch}
 * {@link WorldTimesSeverIDPatch}
 * {@link WorldsServerIDPatch}
 * {@link WorldTimesOptimizationPatch}
 *
 * @author Rsl1122
 */
public class WorldTimesTable extends UserUUIDTable {

    public static final String TABLE_NAME = "plan_world_times";
    private final WorldTable worldTable;
    private final SessionsTable sessionsTable;
    private String insertStatement;

    public WorldTimesTable(SQLDB db) {
        super(TABLE_NAME, db);
        worldTable = db.getWorldTable();
        sessionsTable = db.getSessionsTable();
        insertStatement = "INSERT INTO " + tableName + " (" +
                Col.UUID + ", " +
                Col.WORLD_ID + ", " +
                Col.SERVER_UUID + ", " +
                Col.SESSION_ID + ", " +
                Col.SURVIVAL + ", " +
                Col.CREATIVE + ", " +
                Col.ADVENTURE + ", " +
                Col.SPECTATOR +
                ") VALUES (?, " +
                worldTable.statementSelectID + ", " +
                "?, ?, ?, ?, ?, ?)";
    }

    @Override
    public void createTable() throws DBInitException {
        createTable(TableSqlParser.createTable(tableName)
                .primaryKeyIDColumn(supportsMySQLQueries, Col.ID)
                .column(Col.UUID, Sql.varchar(36)).notNull()
                .column(Col.WORLD_ID, Sql.INT).notNull()
                .column(Col.SERVER_UUID, Sql.varchar(36)).notNull()
                .column(Col.SESSION_ID, Sql.INT).notNull()
                .column(Col.SURVIVAL, Sql.LONG).notNull().defaultValue("0")
                .column(Col.CREATIVE, Sql.LONG).notNull().defaultValue("0")
                .column(Col.ADVENTURE, Sql.LONG).notNull().defaultValue("0")
                .column(Col.SPECTATOR, Sql.LONG).notNull().defaultValue("0")
                .primaryKey(supportsMySQLQueries, Col.ID)
                .foreignKey(Col.WORLD_ID, worldTable.getTableName(), WorldTable.Col.ID)
                .foreignKey(Col.SESSION_ID, sessionsTable.getTableName(), SessionsTable.Col.ID)
                .toString()
        );
    }

    public void addWorldTimesToSessions(UUID uuid, Map<Integer, Session> sessions) {
        String worldIDColumn = worldTable + "." + WorldTable.Col.ID;
        String worldNameColumn = worldTable + "." + WorldTable.Col.NAME + " as world_name";
        String sql = "SELECT " +
                Col.SESSION_ID + ", " +
                Col.SURVIVAL + ", " +
                Col.CREATIVE + ", " +
                Col.ADVENTURE + ", " +
                Col.SPECTATOR + ", " +
                worldNameColumn +
                " FROM " + tableName +
                " INNER JOIN " + worldTable + " on " + worldIDColumn + "=" + Col.WORLD_ID +
                " WHERE " + Col.UUID + "=?";

        query(new QueryStatement<Object>(sql, 2000) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, uuid.toString());
            }

            @Override
            public Object processResults(ResultSet set) throws SQLException {
                String[] gms = GMTimes.getGMKeyArray();

                while (set.next()) {
                    int sessionID = set.getInt(Col.SESSION_ID.get());
                    Session session = sessions.get(sessionID);

                    if (session == null) {
                        continue;
                    }

                    String worldName = set.getString("world_name");

                    Map<String, Long> gmMap = new HashMap<>();
                    gmMap.put(gms[0], set.getLong(Col.SURVIVAL.get()));
                    gmMap.put(gms[1], set.getLong(Col.CREATIVE.get()));
                    gmMap.put(gms[2], set.getLong(Col.ADVENTURE.get()));
                    gmMap.put(gms[3], set.getLong(Col.SPECTATOR.get()));
                    GMTimes gmTimes = new GMTimes(gmMap);

                    session.getUnsafe(SessionKeys.WORLD_TIMES).setGMTimesForWorld(worldName, gmTimes);
                }
                return null;
            }
        });
    }

    public void saveWorldTimes(UUID uuid, int sessionID, WorldTimes worldTimes) {
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
                    String serverUUID = getServerUUID().toString();
                    statement.setString(3, serverUUID);
                    statement.setString(4, serverUUID);
                    statement.setInt(5, sessionID);

                    String[] gms = GMTimes.getGMKeyArray();
                    statement.setLong(6, gmTimes.getTime(gms[0]));
                    statement.setLong(7, gmTimes.getTime(gms[1]));
                    statement.setLong(8, gmTimes.getTime(gms[2]));
                    statement.setLong(9, gmTimes.getTime(gms[3]));
                    statement.addBatch();
                }
            }
        });
    }

    public WorldTimes getWorldTimesOfServer(UUID serverUUID) {
        String worldIDColumn = worldTable + "." + WorldTable.Col.ID;
        String worldNameColumn = worldTable + "." + WorldTable.Col.NAME + " as world";
        String sql = "SELECT " +
                "SUM(" + Col.SURVIVAL + ") as survival, " +
                "SUM(" + Col.CREATIVE + ") as creative, " +
                "SUM(" + Col.ADVENTURE + ") as adventure, " +
                "SUM(" + Col.SPECTATOR + ") as spectator, " +
                worldNameColumn +
                " FROM " + tableName +
                " INNER JOIN " + worldTable + " on " + worldIDColumn + "=" + Col.WORLD_ID +
                " WHERE " + tableName + "." + Col.SERVER_UUID + "=?" +
                " GROUP BY world";

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
                    String worldName = set.getString("world");

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

    public WorldTimes getWorldTimesOfUser(UUID uuid) {
        String worldIDColumn = worldTable + "." + WorldTable.Col.ID;
        String worldNameColumn = worldTable + "." + WorldTable.Col.NAME + " as world";
        String sql = "SELECT " +
                "SUM(" + Col.SURVIVAL + ") as survival, " +
                "SUM(" + Col.CREATIVE + ") as creative, " +
                "SUM(" + Col.ADVENTURE + ") as adventure, " +
                "SUM(" + Col.SPECTATOR + ") as spectator, " +
                worldNameColumn +
                " FROM " + tableName +
                " INNER JOIN " + worldTable + " on " + worldIDColumn + "=" + Col.WORLD_ID +
                " WHERE " + Col.UUID + "=?" +
                " GROUP BY world";

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
                    String worldName = set.getString("world");

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

    public Map<Integer, WorldTimes> getAllWorldTimesBySessionID() {
        String worldIDColumn = worldTable + "." + WorldTable.Col.ID;
        String worldNameColumn = worldTable + "." + WorldTable.Col.NAME + " as world";
        String sql = "SELECT " +
                Col.SESSION_ID + ", " +
                Col.SURVIVAL + ", " +
                Col.CREATIVE + ", " +
                Col.ADVENTURE + ", " +
                Col.SPECTATOR + ", " +
                worldNameColumn +
                " FROM " + tableName +
                " INNER JOIN " + worldTable + " on " + worldIDColumn + "=" + Col.WORLD_ID;

        return query(new QueryAllStatement<Map<Integer, WorldTimes>>(sql, 50000) {
            @Override
            public Map<Integer, WorldTimes> processResults(ResultSet set) throws SQLException {
                String[] gms = GMTimes.getGMKeyArray();

                Map<Integer, WorldTimes> worldTimes = new HashMap<>();
                while (set.next()) {
                    int sessionID = set.getInt(Col.SESSION_ID.get());

                    String worldName = set.getString("world");

                    Map<String, Long> gmMap = new HashMap<>();
                    gmMap.put(gms[0], set.getLong(Col.SURVIVAL.get()));
                    gmMap.put(gms[1], set.getLong(Col.CREATIVE.get()));
                    gmMap.put(gms[2], set.getLong(Col.ADVENTURE.get()));
                    gmMap.put(gms[3], set.getLong(Col.SPECTATOR.get()));
                    GMTimes gmTimes = new GMTimes(gmMap);

                    WorldTimes worldTOfSession = worldTimes.getOrDefault(sessionID, new WorldTimes(new HashMap<>()));
                    worldTOfSession.setGMTimesForWorld(worldName, gmTimes);
                    worldTimes.put(sessionID, worldTOfSession);
                }
                return worldTimes;
            }
        });
    }

    public void addWorldTimesToSessions(Map<UUID, Map<UUID, List<Session>>> map) {
        Map<Integer, WorldTimes> worldTimesBySessionID = getAllWorldTimesBySessionID();

        for (Map.Entry<UUID, Map<UUID, List<Session>>> entry : map.entrySet()) {
            for (List<Session> sessions : entry.getValue().values()) {
                for (Session session : sessions) {
                    WorldTimes worldTimes = worldTimesBySessionID.get(session.getUnsafe(SessionKeys.DB_ID));
                    if (worldTimes != null) {
                        session.setWorldTimes(worldTimes);
                    }
                }
            }
        }
    }

    public void saveWorldTimes(Map<UUID, Map<UUID, List<Session>>> allSessions) {
        if (Verify.isEmpty(allSessions)) {
            return;
        }
        List<String> worldNames = allSessions.values().stream()
                .map(Map::values)
                .flatMap(Collection::stream)
                .flatMap(Collection::stream)
                .map(s -> s.getUnsafe(SessionKeys.WORLD_TIMES))
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
                for (Map.Entry<UUID, Map<UUID, List<Session>>> serverSessions : allSessions.entrySet()) {
                    UUID serverUUID = serverSessions.getKey();
                    // Every User
                    for (Map.Entry<UUID, List<Session>> entry : serverSessions.getValue().entrySet()) {
                        UUID uuid = entry.getKey();
                        List<Session> sessions = entry.getValue();
                        // Every Session
                        for (Session session : sessions) {
                            int sessionID = session.getUnsafe(SessionKeys.DB_ID);
                            // Every WorldTimes
                            for (Map.Entry<String, GMTimes> worldTimesEntry : session.getUnsafe(SessionKeys.WORLD_TIMES)
                                    .getWorldTimes().entrySet()) {
                                String worldName = worldTimesEntry.getKey();
                                GMTimes gmTimes = worldTimesEntry.getValue();
                                statement.setString(1, uuid.toString());
                                statement.setString(2, worldName);
                                statement.setString(3, serverUUID.toString());
                                statement.setString(4, serverUUID.toString());
                                statement.setInt(5, sessionID);
                                statement.setLong(6, gmTimes.getTime(gms[0]));
                                statement.setLong(7, gmTimes.getTime(gms[1]));
                                statement.setLong(8, gmTimes.getTime(gms[2]));
                                statement.setLong(9, gmTimes.getTime(gms[3]));
                                statement.addBatch();
                            }
                        }
                    }
                }
            }
        });
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WorldTimesTable)) return false;
        if (!super.equals(o)) return false;
        WorldTimesTable that = (WorldTimesTable) o;
        return Objects.equals(worldTable, that.worldTable) &&
                Objects.equals(sessionsTable, that.sessionsTable) &&
                Objects.equals(insertStatement, that.insertStatement);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), worldTable, sessionsTable, insertStatement);
    }

    public enum Col implements Column {
        ID("id"),
        UUID(UserUUIDTable.Col.UUID.get()),
        SERVER_UUID("server_uuid"),
        SESSION_ID("session_id"),
        WORLD_ID("world_id"),
        SURVIVAL("survival_time"),
        CREATIVE("creative_time"),
        ADVENTURE("adventure_time"),
        SPECTATOR("spectator_time");

        private final String column;

        Col(String column) {
            this.column = column;
        }

        @Override
        public String get() {
            return toString();
        }

        @Override
        public String toString() {
            return column;
        }
    }
}
