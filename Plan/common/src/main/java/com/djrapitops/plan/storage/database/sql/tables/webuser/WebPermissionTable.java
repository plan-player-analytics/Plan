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
package com.djrapitops.plan.storage.database.sql.tables.webuser;

import com.djrapitops.plan.storage.database.DBType;
import com.djrapitops.plan.storage.database.sql.building.CreateTableBuilder;
import com.djrapitops.plan.storage.database.sql.building.Sql;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Represents the plan_web_permission table.
 *
 * @author AuroraLS3
 */
public class WebPermissionTable {

    public static final String TABLE_NAME = "plan_web_permission";

    public static final String ID = "id";
    public static final String PERMISSION = "permission";

    public static final String INSERT_STATEMENT = "INSERT INTO " + TABLE_NAME + " (" + PERMISSION + ") VALUES (?)";
    public static final String SELECT_PERMISSION_ID = SELECT + ID + FROM + TABLE_NAME + WHERE + PERMISSION + "=?";

    private WebPermissionTable() {
        /* Static information class */
    }

    public static String safeInsertSQL(DBType dbType) {
        return dbType.getSql().insertOrIgnore() + TABLE_NAME + " (" + PERMISSION + ") VALUES (?)";
    }

    public static String createTableSQL(DBType dbType) {
        return CreateTableBuilder.create(TABLE_NAME, dbType)
                .column(ID, Sql.INT).primaryKey()
                .column(PERMISSION, Sql.varchar(100)).notNull().unique()
                .toString();
    }
}
