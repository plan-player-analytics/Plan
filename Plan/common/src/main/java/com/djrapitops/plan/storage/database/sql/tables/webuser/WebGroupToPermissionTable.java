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
public class WebGroupToPermissionTable {

    public static final String TABLE_NAME = "plan_web_group_to_permission";

    public static final String ID = "id";
    public static final String GROUP_ID = "group_id";
    public static final String PERMISSION_ID = "permission_id";

    public static final String INSERT_STATEMENT = INSERT_INTO + TABLE_NAME + " (" + GROUP_ID + ',' + PERMISSION_ID + ") VALUES (?,?)";
    public static final String SELECT_IDS = SELECT + GROUP_ID + ',' + PERMISSION_ID + FROM + TABLE_NAME + ORDER_BY + GROUP_ID + ',' + PERMISSION_ID;

    private WebGroupToPermissionTable() {
        /* Static information class */
    }

    public static String createTableSQL(DBType dbType) {
        return CreateTableBuilder.create(TABLE_NAME, dbType)
                .column(ID, Sql.INT).primaryKey()
                .column(GROUP_ID, Sql.INT).notNull()
                .column(PERMISSION_ID, Sql.INT).notNull()
                .foreignKey(GROUP_ID, WebGroupTable.TABLE_NAME, WebGroupTable.ID)
                .foreignKey(PERMISSION_ID, WebPermissionTable.TABLE_NAME, WebPermissionTable.ID)
                .toString();
    }
}
