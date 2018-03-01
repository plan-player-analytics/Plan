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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Table class representing database table plan_worlds.
 * <p>
 * Used for storing id references to world names.
 *
 * @author Rsl1122
 * @since 3.6.0 / Database version 7
 */
public class WorldTable extends Table {

    public WorldTable(SQLDB db) {
        super("plan_worlds", db);
        statementSelectID = "(SELECT " + Col.ID + " FROM " + tableName + " WHERE (" + Col.NAME + "=?) LIMIT 1)";
    }

    public final String statementSelectID;

    @Override
    public void createTable() throws DBInitException {
        createTable(TableSqlParser.createTable(tableName)
                .primaryKeyIDColumn(usingMySQL, Col.ID)
                .column(Col.NAME, Sql.varchar(100)).notNull()
                .primaryKey(usingMySQL, Col.ID)
                .toString()
        );
    }

    /**
     * Used to get the available world names.
     *
     * @return List of all world names in the database.
     * @throws SQLException Database error occurs.
     */
    public List<String> getWorlds() throws SQLException {
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

    /**
     * Used to save a list of world names.
     * <p>
     * Already saved names will not be saved.
     *
     * @param worlds List of world names.
     * @throws SQLException Database error occurs.
     */
    public void saveWorlds(Collection<String> worlds) throws SQLException {
        Verify.nullCheck(worlds);
        Set<String> worldsToSave = new HashSet<>(worlds);

        List<String> saved = getWorlds();
        worldsToSave.removeAll(saved);
        if (Verify.isEmpty(worlds)) {
            return;
        }

        String sql = "INSERT INTO " + tableName + " ("
                + Col.NAME
                + ") VALUES (?)";

        executeBatch(new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                for (String world : worldsToSave) {
                    statement.setString(1, world);
                    statement.addBatch();
                }
            }
        });
    }

    public Set<String> getWorldNames(UUID serverUUID) throws SQLException {
        WorldTimesTable worldTimesTable = db.getWorldTimesTable();
        SessionsTable sessionsTable = db.getSessionsTable();
        ServerTable serverTable = db.getServerTable();

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

    public Set<String> getWorldNames() throws SQLException {
        return getWorldNames(ServerInfo.getServerUUID());
    }

    public Map<String, Integer> getWorldIds() throws SQLException {
        String sql = "SELECT DISTINCT " +
                Col.NAME + ", " +
                Col.ID + " FROM " +
                tableName;

        return query(new QueryAllStatement<Map<String, Integer>>(sql, 200) {
            @Override
            public Map<String, Integer> processResults(ResultSet set) throws SQLException {
                Map<String, Integer> worldIds = new HashMap<>();
                while (set.next()) {
                    String worldName = set.getString(Col.NAME.get());
                    int worldId = set.getInt(Col.ID.get());
                    worldIds.put(worldName, worldId);
                }
                return worldIds;
            }
        });
    }

    public enum Col implements Column {
        ID("id"),
        NAME("world_name");

        private final String column;

        Col(String column) {
            this.column = column;
        }

        public String get() {
            return toString();
        }

        @Override
        public String toString() {
            return column;
        }
    }
}
