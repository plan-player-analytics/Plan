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
package com.djrapitops.plan.db.sql.tables;

import com.djrapitops.plan.api.exceptions.database.DBInitException;
import com.djrapitops.plan.db.DBType;
import com.djrapitops.plan.db.SQLDB;
import com.djrapitops.plan.db.access.ExecStatement;
import com.djrapitops.plan.db.access.QueryStatement;
import com.djrapitops.plan.db.patches.Version10Patch;
import com.djrapitops.plan.db.patches.WorldsOptimizationPatch;
import com.djrapitops.plan.db.patches.WorldsServerIDPatch;
import com.djrapitops.plan.db.sql.parsing.Column;
import com.djrapitops.plan.db.sql.parsing.CreateTableParser;
import com.djrapitops.plan.db.sql.parsing.Sql;
import com.djrapitops.plugin.utilities.Verify;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Table class representing database table plan_worlds.
 * <p>
 * Used for storing id references to world names.
 * <p>
 * Patches related to this table:
 * {@link Version10Patch}
 * {@link WorldsServerIDPatch}
 * {@link WorldsOptimizationPatch}
 *
 * @author Rsl1122 / Database version 7
 */
public class WorldTable extends Table {

    public static final String TABLE_NAME = "plan_worlds";

    public static final String ID = "id";
    public static final String SERVER_UUID = "server_uuid";
    public static final String NAME = "world_name";

    public final String statementSelectID;
    private final ServerTable serverTable;

    public WorldTable(SQLDB db) {
        super(TABLE_NAME, db);
        serverTable = db.getServerTable();
        statementSelectID = "(SELECT " + Col.ID + " FROM " + tableName +
                " WHERE (" + Col.NAME + "=?)" +
                " AND (" + Col.SERVER_UUID + "=?)" +
                " LIMIT 1)";
    }

    public static String createTableSQL(DBType dbType) {
        return CreateTableParser.create(TABLE_NAME, dbType)
                .column(ID, Sql.INT).primaryKey()
                .column(NAME, Sql.varchar(100)).notNull()
                .column(SERVER_UUID, Sql.varchar(36)).notNull()
                .toString();
    }

    @Override
    public void createTable() throws DBInitException {
        createTable(createTableSQL(db.getType()));
    }

    public List<String> getWorlds() {
        return getWorlds(getServerUUID());
    }

    public List<String> getWorlds(UUID serverUUID) {
        String sql = "SELECT * FROM " + tableName +
                " WHERE " + Col.SERVER_UUID + "=?";

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

    public void saveWorlds(Map<UUID, Collection<String>> worldMap) {
        String sql = "INSERT INTO " + tableName + " ("
                + Col.NAME + ", "
                + Col.SERVER_UUID
                + ") VALUES (?, ?)";

        executeBatch(new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                for (Map.Entry<UUID, Collection<String>> entry : worldMap.entrySet()) {
                    UUID serverUUID = entry.getKey();
                    for (String world : entry.getValue()) {
                        statement.setString(1, world);
                        statement.setString(2, serverUUID.toString());
                        statement.addBatch();
                    }
                }
            }
        });
    }

    public void saveWorlds(Collection<String> worlds) {
        saveWorlds(worlds, getServerUUID());
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
                + Col.SERVER_UUID
                + ") VALUES (?, ?)";

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
                " WHERE " + Col.SERVER_UUID + "=?";
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

    @Deprecated
    public enum Col implements Column {
        @Deprecated ID("id"),
        @Deprecated SERVER_UUID("server_uuid"),
        @Deprecated NAME("world_name");

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WorldTable)) return false;
        if (!super.equals(o)) return false;
        WorldTable that = (WorldTable) o;
        return Objects.equals(statementSelectID, that.statementSelectID) &&
                Objects.equals(serverTable, that.serverTable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), statementSelectID, serverTable);
    }
}

