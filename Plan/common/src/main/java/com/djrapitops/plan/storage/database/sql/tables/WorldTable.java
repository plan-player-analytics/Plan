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
package com.djrapitops.plan.storage.database.sql.tables;

import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.DBType;
import com.djrapitops.plan.storage.database.queries.objects.lookup.ServerUUIDIdentifiable;
import com.djrapitops.plan.storage.database.sql.building.CreateTableBuilder;
import com.djrapitops.plan.storage.database.sql.building.Sql;
import com.djrapitops.plan.storage.database.transactions.patches.Version10Patch;
import com.djrapitops.plan.storage.database.transactions.patches.WorldsOptimizationPatch;
import com.djrapitops.plan.storage.database.transactions.patches.WorldsServerIDPatch;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Table information about 'plan_worlds'.
 * <p>
 * Patches related to this table:
 * {@link Version10Patch}
 * {@link WorldsServerIDPatch}
 * {@link WorldsOptimizationPatch}
 *
 * @author AuroraLS3
 */
public class WorldTable {

    public static final String TABLE_NAME = "plan_worlds";

    public static final String ID = "id";
    public static final String SERVER_UUID = "server_uuid";
    public static final String NAME = "world_name";

    public static final String INSERT_STATEMENT = "INSERT INTO " + TABLE_NAME + " ("
            + NAME + ','
            + SERVER_UUID
            + ") VALUES (?, ?)";

    public static final String SELECT_WORLD_ID_STATEMENT = '(' +
            SELECT + TABLE_NAME + '.' + ID + FROM + TABLE_NAME +
            WHERE + NAME + "=?" +
            AND + TABLE_NAME + '.' + SERVER_UUID + "=?" +
            " LIMIT 1)";

    private WorldTable() {
        /* Static information class */
    }

    public static String createTableSQL(DBType dbType) {
        return CreateTableBuilder.create(TABLE_NAME, dbType)
                .column(ID, Sql.INT).primaryKey()
                .column(NAME, Sql.varchar(100)).notNull()
                .column(SERVER_UUID, Sql.varchar(36)).notNull()
                .toString();
    }

    public static class Row implements ServerUUIDIdentifiable {
        public int id;
        public String name;
        public ServerUUID serverUUID;

        public static Row extract(ResultSet set) throws SQLException {
            Row row = new Row();
            row.id = set.getInt(ID);
            row.name = set.getString(NAME);
            row.serverUUID = ServerUUID.fromString(set.getString(SERVER_UUID));
            return row;
        }

        @Override
        public ServerUUID getServerUUID() {
            return serverUUID;
        }

        @Override
        public void setServerUUID(ServerUUID serverUUID) {
            this.serverUUID = serverUUID;
        }

        public void insert(PreparedStatement statement) throws SQLException {
            statement.setString(1, name);
            statement.setString(2, serverUUID.toString());
        }
    }
}

