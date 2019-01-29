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

import com.djrapitops.plan.db.DBType;
import com.djrapitops.plan.db.SQLDB;
import com.djrapitops.plan.db.patches.Version10Patch;
import com.djrapitops.plan.db.patches.WorldsOptimizationPatch;
import com.djrapitops.plan.db.patches.WorldsServerIDPatch;
import com.djrapitops.plan.db.sql.parsing.CreateTableParser;
import com.djrapitops.plan.db.sql.parsing.Sql;

import java.util.Objects;

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
 * @author Rsl1122
 */
public class WorldTable extends Table {

    public static final String TABLE_NAME = "plan_worlds";

    public static final String ID = "id";
    public static final String SERVER_UUID = "server_uuid";
    public static final String NAME = "world_name";

    public static final String INSERT_STATEMENT = "INSERT INTO " + TABLE_NAME + " ("
            + NAME + ", "
            + SERVER_UUID
            + ") VALUES (?, ?)";

    public static final String SELECT_WORLD_ID_STATEMENT = "(SELECT " + TABLE_NAME + "." + ID + " FROM " + TABLE_NAME +
            " WHERE (" + NAME + "=?)" +
            " AND (" + TABLE_NAME + "." + SERVER_UUID + "=?)" +
            " LIMIT 1)";

    public WorldTable(SQLDB db) {
        super(TABLE_NAME, db);
    }

    public static String createTableSQL(DBType dbType) {
        return CreateTableParser.create(TABLE_NAME, dbType)
                .column(ID, Sql.INT).primaryKey()
                .column(NAME, Sql.varchar(100)).notNull()
                .column(SERVER_UUID, Sql.varchar(36)).notNull()
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WorldTable)) return false;
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode());
    }
}

