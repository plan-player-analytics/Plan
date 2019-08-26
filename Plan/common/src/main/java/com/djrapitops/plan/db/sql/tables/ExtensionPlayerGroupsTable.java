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
import com.djrapitops.plan.db.sql.parsing.CreateTableParser;
import com.djrapitops.plan.db.sql.parsing.Sql;

/**
 * Table information about 'plan_extension_player_groups'.
 *
 * @author Rsl1122
 */
public class ExtensionPlayerGroupsTable {

    public static final String TABLE_NAME = "plan_extension_player_groups";

    public static final String ID = "id";
    public static final String GROUP_ID = "group_id";
    public static final String USER_UUID = "uuid";

    private ExtensionPlayerGroupsTable() {
        /* Static information class */
    }

    public static String createTableSQL(DBType dbType) {
        return CreateTableParser.create(TABLE_NAME, dbType)
                .column(ID, Sql.INT).primaryKey()
                .column(GROUP_ID, Sql.INT).notNull()
                .column(USER_UUID, Sql.varchar(36)).notNull()
                .foreignKey(GROUP_ID, ExtensionGroupsTable.TABLE_NAME, ExtensionGroupsTable.ID)
                .build();
    }

}