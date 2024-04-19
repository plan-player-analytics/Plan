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

import com.djrapitops.plan.storage.database.DBType;
import com.djrapitops.plan.storage.database.sql.building.CreateTableBuilder;
import com.djrapitops.plan.storage.database.sql.building.Sql;
import org.intellij.lang.annotations.Language;

/**
 * Represents plan_allowlist_bounce table.
 *
 * @author AuroraLS3
 */
public class AllowlistBounceTable {

    public static final String TABLE_NAME = "plan_allowlist_bounce";

    public static final String ID = "id";
    public static final String UUID = "uuid";
    public static final String USER_NAME = "name";
    public static final String SERVER_ID = "server_id";
    public static final String TIMES = "times";
    public static final String LAST_BOUNCE = "last_bounce";

    @Language("SQL")
    public static final String INSERT_STATEMENT = "INSERT INTO " + TABLE_NAME + " (" +
            UUID + ',' +
            USER_NAME + ',' +
            SERVER_ID + ',' +
            TIMES + ',' +
            LAST_BOUNCE +
            ") VALUES (?,?," + ServerTable.SELECT_SERVER_ID + ",?,?)";

    @Language("SQL")
    public static final String INCREMENT_TIMES_STATEMENT = "UPDATE " + TABLE_NAME +
            " SET " + TIMES + "=" + TIMES + "+1, " + LAST_BOUNCE + "=?" +
            " WHERE " + UUID + "=?" +
            " AND " + SERVER_ID + "=" + ServerTable.SELECT_SERVER_ID;

    private AllowlistBounceTable() {
        /* Static information class */
    }

    public static String createTableSQL(DBType dbType) {
        return CreateTableBuilder.create(TABLE_NAME, dbType)
                .column(ID, Sql.INT).primaryKey()
                .column(UUID, Sql.varchar(36)).notNull().unique()
                .column(USER_NAME, Sql.varchar(36)).notNull()
                .column(SERVER_ID, Sql.INT).notNull()
                .column(TIMES, Sql.INT).notNull().defaultValue("0")
                .column(LAST_BOUNCE, Sql.LONG).notNull()
                .foreignKey(SERVER_ID, ServerTable.TABLE_NAME, ServerTable.ID)
                .toString();
    }
}
