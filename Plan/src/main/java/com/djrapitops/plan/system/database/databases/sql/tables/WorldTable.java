package com.djrapitops.plan.system.database.databases.sql.tables;

import com.djrapitops.plan.api.exceptions.database.DBInitException;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;
import com.djrapitops.plan.system.database.databases.sql.processing.ExecStatement;
import com.djrapitops.plan.system.database.databases.sql.processing.QueryAllStatement;
import com.djrapitops.plan.system.database.databases.sql.processing.QueryStatement;
import com.djrapitops.plan.system.database.databases.sql.statements.Column;
import com.djrapitops.plan.system.database.databases.sql.statements.Sql;
import com.djrapitops.plan.system.database.databases.sql.statements.TableSqlParser;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plugin.utilities.Verify;
import com.google.common.base.Objects;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Table class representing database table plan_worlds.
 * <p>
 * Used for storing id references to world names.
 *
 * @author Rsl1122
 * @since 3.6.0 / Database version 7
 */
public class WorldTable extends Table {

    public final String statementSelectID;
    private final ServerTable serverTable;

    public WorldTable(SQLDB db) {
        super("plan_worlds", db);
        serverTable = db.getServerTable();
        statementSelectID = "(SELECT " + Col.ID + " FROM " + tableName +
                " WHERE (" + Col.NAME + "=?)" +
                " AND (" + Col.SERVER_ID + "=" + serverTable.statementSelectServerID + ")" +
                " LIMIT 1)";
    }

    @Override
    public void createTable() throws DBInitException {
        createTable(TableSqlParser.createTable(tableName)
                .primaryKeyIDColumn(usingMySQL, Col.ID)
                .column(Col.NAME, Sql.varchar(100)).notNull()
                .column(Col.SERVER_ID, Sql.INT).notNull()
                .primaryKey(usingMySQL, Col.ID)
                .foreignKey(Col.SERVER_ID, ServerTable.TABLE_NAME, ServerTable.Col.SERVER_ID)
                .toString()
        );
    }

    /**
     * Used to get the available world names.
     *
     * @return List of all world names in the database.
     * @throws SQLException Database error occurs.
     */
    public List<String> getAllWorlds() throws SQLException {
        String sql = "SELECT * FROM " + tableName;

        return query(new QueryAllStatement<List<String>>(sql) {
            @Override
            public List<String> processResults(ResultSet set) throws SQLException {
                List<String> worldNames = new ArrayList<>();
                while (set.next()) {
                    String worldName = set.getString(Col.NAME.get());
                    worldNames.add(worldName);
                }
                return worldNames;
            }
        });
    }

    public Map<UUID, List<String>> getWorldsPerServer() throws SQLException {
        Map<Integer, UUID> serverUUIDsByID = serverTable.getServerUUIDsByID();
        String sql = "SELECT * FROM " + tableName;

        return query(new QueryAllStatement<Map<UUID, List<String>>>(sql, 1000) {
            @Override
            public Map<UUID, List<String>> processResults(ResultSet set) throws SQLException {
                Map<UUID, List<String>> worldsPerServer = new HashMap<>();
                while (set.next()) {
                    UUID serverUUID = serverUUIDsByID.get(set.getInt(Col.SERVER_ID.get()));
                    String worldName = set.getString(Col.NAME.get());
                    List<String> worlds = worldsPerServer.getOrDefault(serverUUID, new ArrayList<>());
                    worlds.add(worldName);
                    worldsPerServer.put(serverUUID, worlds);
                }
                return worldsPerServer;
            }
        });
    }

    public Map<Integer, UUID> getServerUUIDByWorldID() throws SQLException {
        Map<Integer, UUID> serverUUIDsByID = serverTable.getServerUUIDsByID();
        String sql = "SELECT DISTINCT " +
                Col.ID + ", " +
                Col.SERVER_ID +
                " FROM " + tableName;
        return query(new QueryAllStatement<Map<Integer, UUID>>(sql, 100) {
            @Override
            public Map<Integer, UUID> processResults(ResultSet set) throws SQLException {
                Map<Integer, UUID> idMap = new HashMap<>();
                while (set.next()) {
                    int worldId = set.getInt(Col.ID.get());
                    int serverId = set.getInt(Col.SERVER_ID.get());
                    UUID serverUUID = serverUUIDsByID.getOrDefault(serverId, ServerInfo.getServerUUID());
                    idMap.put(worldId, serverUUID);
                }
                return idMap;
            }
        });
    }

    public List<String> getWorlds() throws SQLException {
        return getWorlds(ServerInfo.getServerUUID());
    }

    public List<String> getWorlds(UUID serverUUID) throws SQLException {
        String sql = "SELECT * FROM " + tableName +
                " WHERE " + Col.SERVER_ID + "=" + serverTable.statementSelectServerID;

        return query(new QueryStatement<List<String>>(sql) {

            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
            }

            @Override
            public List<String> processResults(ResultSet set) throws SQLException {
                List<String> worldNames = new ArrayList<>();
                while (set.next()) {
                    String worldName = set.getString(Col.NAME.get());
                    worldNames.add(worldName);
                }
                return worldNames;
            }
        });
    }

    public void saveWorlds(Collection<String> worlds) throws SQLException {
        saveWorlds(worlds, ServerInfo.getServerUUID());
    }

    /**
     * Used to save a list of world names.
     * <p>
     * Already saved names will not be saved.
     *
     * @param worlds List of world names.
     * @throws SQLException Database error occurs.
     */
    public void saveWorlds(Collection<String> worlds, UUID serverUUID) throws SQLException {
        Verify.nullCheck(worlds);
        Set<String> worldsToSave = new HashSet<>(worlds);

        List<String> saved = getWorlds(serverUUID);
        worldsToSave.removeAll(saved);
        if (Verify.isEmpty(worlds)) {
            return;
        }

        String sql = "INSERT INTO " + tableName + " ("
                + Col.NAME + ", "
                + Col.SERVER_ID
                + ") VALUES (?, " + serverTable.statementSelectServerID + ")";

        executeBatch(new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                for (String world : worldsToSave) {
                    statement.setString(1, world);
                    statement.setString(2, ServerInfo.getServerUUID().toString());
                    statement.addBatch();
                }
            }
        });
    }

    public Set<String> getWorldNames(UUID serverUUID) throws SQLException {
        String sql = "SELECT DISTINCT " + Col.NAME + " FROM " + tableName +
                " WHERE " + Col.SERVER_ID + "=" + serverTable.statementSelectServerID;
        return query(new QueryStatement<Set<String>>(sql, 100) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
            }

            @Override
            public Set<String> processResults(ResultSet set) throws SQLException {
                Set<String> worldNames = new HashSet<>();
                while (set.next()) {
                    worldNames.add(set.getString(Col.NAME.get()));
                }
                return worldNames;
            }
        });
    }

    /**
     * Used to get world names for this server.
     *
     * @param serverUUID UUID of the Server
     * @return World names known for that server
     * @throws SQLException If DB Error occurs
     * @deprecated Use getWorldNames instead, this method is slower.
     */
    @Deprecated
    public Set<String> getWorldNamesOld(UUID serverUUID) throws SQLException {
        WorldTimesTable worldTimesTable = db.getWorldTimesTable();
        SessionsTable sessionsTable = db.getSessionsTable();

        String statementSelectServerID = serverTable.statementSelectServerID;

        String worldIDColumn = worldTimesTable + "." + WorldTimesTable.Col.WORLD_ID;
        String worldSessionIDColumn = worldTimesTable + "." + WorldTimesTable.Col.SESSION_ID;
        String sessionIDColumn = sessionsTable + "." + SessionsTable.Col.ID;
        String sessionServerIDColumn = sessionsTable + "." + SessionsTable.Col.SERVER_ID;

        String sql = "SELECT DISTINCT " +
                Col.NAME + " FROM " +
                tableName +
                " INNER JOIN " + worldTimesTable + " on " + worldIDColumn + "=" + tableName + "." + Col.ID +
                " INNER JOIN " + sessionsTable + " on " + worldSessionIDColumn + "=" + sessionIDColumn +
                " WHERE " + statementSelectServerID + "=" + sessionServerIDColumn;

        return query(new QueryStatement<Set<String>>(sql, 1000) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
            }

            @Override
            public Set<String> processResults(ResultSet set) throws SQLException {
                Set<String> worldNames = new HashSet<>();
                while (set.next()) {
                    worldNames.add(set.getString(Col.NAME.get()));
                }
                return worldNames;
            }
        });
    }

    private Map<Integer, Integer> getWorldIDServerIDRelation() throws SQLException {
        String sql = "SELECT " +
                Col.ID + ", " +
                Col.SERVER_ID +
                " FROM " + tableName;
        return query(new QueryAllStatement<Map<Integer, Integer>>(sql, 100) {
            @Override
            public Map<Integer, Integer> processResults(ResultSet set) throws SQLException {
                HashMap<Integer, Integer> idServerIdMap = new HashMap<>();
                while (set.next()) {
                    idServerIdMap.put(set.getInt(Col.ID.get()), set.getInt(Col.SERVER_ID.get()));
                }
                return idServerIdMap;
            }
        });
    }

    public void alterTableV16() throws SQLException {
        addColumns(Col.SERVER_ID + " integer NOT NULL DEFAULT 0");

        List<UUID> serverUUIDs = serverTable.getServerUUIDs();

        Map<UUID, Set<String>> worldsPerServer = new HashMap<>();
        for (UUID serverUUID : serverUUIDs) {
            worldsPerServer.put(serverUUID, getWorldNamesOld(serverUUID));
        }

        for (Map.Entry<UUID, Set<String>> entry : worldsPerServer.entrySet()) {
            UUID serverUUID = entry.getKey();
            Set<String> worlds = entry.getValue();

            saveWorlds(worlds, serverUUID);
        }

        updateWorldTimesTableWorldIDs();
        execute("DELETE FROM " + tableName + " WHERE " + Col.SERVER_ID + "=0");
    }

    private void updateWorldTimesTableWorldIDs() throws SQLException {
        List<WorldObj> worldObjects = getWorldObjects();
        Map<WorldObj, List<WorldObj>> oldToNewMap =
                worldObjects.stream()
                        .filter(worldObj -> worldObj.serverId == 0)
                        .collect(Collectors.toMap(
                                Function.identity(),
                                oldWorld -> worldObjects.stream()
                                        .filter(worldObj -> worldObj.equals(oldWorld))
                                        .collect(Collectors.toList()
                                        )));

        WorldTimesTable worldTimesTable = db.getWorldTimesTable();
        String sql = "UPDATE " + worldTimesTable + " SET " +
                WorldTimesTable.Col.WORLD_ID + "=?" +
                " WHERE " + WorldTimesTable.Col.WORLD_ID + "=?" +
                " AND " + WorldTimesTable.Col.SERVER_ID + "=?";
        executeBatch(new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                for (Map.Entry<WorldObj, List<WorldObj>> entry : oldToNewMap.entrySet()) {
                    WorldObj old = entry.getKey();
                    for (WorldObj newWorld : entry.getValue()) {
                        statement.setInt(1, newWorld.id);
                        statement.setInt(2, old.id);
                        statement.setInt(3, newWorld.serverId);
                        statement.addBatch();
                    }
                }
            }
        });
    }

    public Map<Integer, List<Integer>> getWorldIDsByServerIDs() throws SQLException {
        String sql = "SELECT " +
                Col.ID + ", " +
                Col.SERVER_ID +
                " FROM " + tableName;
        return query(new QueryAllStatement<Map<Integer, List<Integer>>>(sql, 100) {
            @Override
            public Map<Integer, List<Integer>> processResults(ResultSet set) throws SQLException {
                HashMap<Integer, List<Integer>> map = new HashMap<>();
                while (set.next()) {

                    int serverID = set.getInt(Col.SERVER_ID.get());
                    int worldID = set.getInt(Col.ID.get());
                    List<Integer> worldIDs = map.getOrDefault(serverID, new ArrayList<>());
                    worldIDs.add(worldID);
                    map.put(serverID, worldIDs);
                }
                return map;
            }
        });
    }

    public List<WorldObj> getWorldObjects() throws SQLException {
        String sql = "SELECT * FROM " + tableName;
        return query(new QueryAllStatement<List<WorldObj>>(sql, 100) {
            @Override
            public List<WorldObj> processResults(ResultSet set) throws SQLException {
                List<WorldObj> objects = new ArrayList<>();
                while (set.next()) {

                    int worldID = set.getInt(Col.ID.get());
                    int serverID = set.getInt(Col.SERVER_ID.get());
                    String worldName = set.getString(Col.NAME.get());
                    objects.add(new WorldObj(worldID, serverID, worldName));
                }
                return objects;
            }
        });
    }

    public enum Col implements Column {
        ID("id"),
        SERVER_ID("server_id"),
        NAME("world_name");

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

class WorldObj {
    final int id;
    final int serverId;
    final String name;

    public WorldObj(int id, int serverId, String name) {
        this.id = id;
        this.serverId = serverId;
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorldObj worldObj = (WorldObj) o;
        return Objects.equal(name, worldObj.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}
