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

import com.djrapitops.plan.extension.implementation.builder.ComponentDataValue;
import com.djrapitops.plan.extension.implementation.builder.StringDataValue;
import com.djrapitops.plan.storage.database.DBType;
import com.djrapitops.plan.storage.database.sql.building.CreateTableBuilder;
import com.djrapitops.plan.storage.database.sql.building.Sql;

/**
 * Table information about 'plan_extension_user_values'.
 *
 * @author AuroraLS3
 */
public class ExtensionPlayerValueTable {

    public static final String TABLE_NAME = "plan_extension_user_values";

    public static final String ID = "id";
    public static final String PROVIDER_ID = "provider_id";
    public static final String USER_UUID = "uuid";

    public static final String BOOLEAN_VALUE = "boolean_value";
    public static final String DOUBLE_VALUE = "double_value";
    public static final String PERCENTAGE_VALUE = "percentage_value";
    public static final String LONG_VALUE = "long_value";
    public static final String STRING_VALUE = "string_value";
    public static final String COMPONENT_VALUE = "component_value";
    public static final String GROUP_VALUE = "group_value";

    private ExtensionPlayerValueTable() {
        /* Static information class */
    }

    public static String createTableSQL(DBType dbType) {
        return CreateTableBuilder.create(TABLE_NAME, dbType)
                .column(ID, Sql.INT).primaryKey()
                .column(BOOLEAN_VALUE, Sql.BOOL)
                .column(DOUBLE_VALUE, Sql.DOUBLE)
                .column(PERCENTAGE_VALUE, Sql.DOUBLE)
                .column(LONG_VALUE, Sql.LONG)
                .column(STRING_VALUE, Sql.varchar(StringDataValue.MAX_LENGTH))
                .column(COMPONENT_VALUE, Sql.varchar(ComponentDataValue.MAX_LENGTH))
                .column(GROUP_VALUE, Sql.varchar(50))
                .column(USER_UUID, Sql.varchar(36)).notNull()
                .column(PROVIDER_ID, Sql.INT).notNull()
                .foreignKey(PROVIDER_ID, ExtensionProviderTable.TABLE_NAME, ExtensionProviderTable.ID)
                .build();
    }

}