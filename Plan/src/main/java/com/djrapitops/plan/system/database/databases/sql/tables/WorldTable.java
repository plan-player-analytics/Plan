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

    public static final String TABLE_NAME = "plan_worlds";
    public final String statementSelectID;
    private final ServerTable serverTable;

    public WorldTable(SQLDB db) {
        super(TABLE_NAME, db);
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
     */
    public List<String> getAllWorlds() {
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

    public List<String> getWorlds() {
        return getWorlds(ServerInfo.getServerUUID());
    }

    public List<String> getWorlds(UUID serverUUID) {
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

    public void saveWorlds(Collection<String> worlds) {
        saveWorlds(worlds, ServerInfo.getServerUUID());
    }

    /**
     * Used to save a list of world names.
     * <p>
     * Already saved names will not be saved.
     *
     * @param worlds List of world names.
     */
    public void saveWorlds(Collection<String> worlds, UUID serverUUID) {
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
                    statement.setString(2, serverUUID.toString());
                    statement.addBatch();
                }
            }
        });
    }

    public Set<String> getWorldNames(UUID serverUUID) {
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

