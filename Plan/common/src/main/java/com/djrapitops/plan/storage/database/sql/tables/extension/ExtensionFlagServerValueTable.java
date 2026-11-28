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
package com.djrapitops.plan.storage.database.sql.tables.extension;

import com.djrapitops.plan.storage.database.DBType;
import com.djrapitops.plan.storage.database.sql.building.CreateTableBuilder;
import com.djrapitops.plan.storage.database.sql.building.Insert;
import com.djrapitops.plan.storage.database.sql.building.Sql;
import com.djrapitops.plan.storage.database.sql.tables.ServerTable;

public class ExtensionFlagServerValueTable {

    public static final String TABLE_NAME = "plan_extension_server_flag_value";

    public static final String ID = "id";
    public static final String FLAG_ID = "flag_id";
    public static final String SERVER_ID = "server_id";
    public static final String VALUE = "value";

    public static final String INSERT_STATEMENT = Insert.values(TABLE_NAME, FLAG_ID, SERVER_ID, VALUE);

    private ExtensionFlagServerValueTable() {
        /* Static information class */
    }

    public static String createTableSQL(DBType dbType) {
        return CreateTableBuilder.create(TABLE_NAME, dbType)
                .column(ID, Sql.INT).primaryKey()
                .column(FLAG_ID, Sql.INT).notNull()
                .column(SERVER_ID, Sql.INT).notNull()
                .column(VALUE, Sql.BOOL).notNull()
                .foreignKey(FLAG_ID, ExtensionFlagTable.TABLE_NAME, ExtensionFlagTable.ID)
                .foreignKey(SERVER_ID, ServerTable.TABLE_NAME, ServerTable.ID)
                .toString();
    }
}