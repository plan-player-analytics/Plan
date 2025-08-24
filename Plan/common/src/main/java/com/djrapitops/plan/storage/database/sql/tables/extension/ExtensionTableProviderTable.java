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

import com.djrapitops.plan.extension.icon.Color;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.DBType;
import com.djrapitops.plan.storage.database.sql.building.CreateTableBuilder;
import com.djrapitops.plan.storage.database.sql.building.Sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Table information about 'plan_extension_tables'.
 *
 * @author AuroraLS3
 */
public class ExtensionTableProviderTable {

    public static final String TABLE_NAME = "plan_extension_tables";

    public static final String ID = "id";
    public static final String PROVIDER_NAME = "name";
    public static final String COLOR = "color";
    public static final String VALUES_FOR = "values_for";
    public static final String CONDITION = "condition_name"; // Can be null, related to @Conditional
    public static final String PLUGIN_ID = "plugin_id";
    public static final String TAB_ID = "tab_id"; // Can be null, related to @Tab

    // All columns can be null
    public static final String COL_1 = "col_1_name";
    public static final String COL_2 = "col_2_name";
    public static final String COL_3 = "col_3_name";
    public static final String COL_4 = "col_4_name";
    public static final String COL_5 = "col_5_name";

    // All icons can be null
    public static final String ICON_1_ID = "icon_1_id";
    public static final String ICON_2_ID = "icon_2_id";
    public static final String ICON_3_ID = "icon_3_id";
    public static final String ICON_4_ID = "icon_4_id";
    public static final String ICON_5_ID = "icon_5_id";

    public static final String FORMAT_1 = "format_1";
    public static final String FORMAT_2 = "format_2";
    public static final String FORMAT_3 = "format_3";
    public static final String FORMAT_4 = "format_4";
    public static final String FORMAT_5 = "format_5";

    public static final int VALUES_FOR_PLAYER = 0;
    public static final int VALUES_FOR_SERVER = 1;

    public static final String STATEMENT_SELECT_TABLE_ID = '(' + SELECT + ID + FROM + TABLE_NAME +
            WHERE + PROVIDER_NAME + "=?" +
            AND + PLUGIN_ID + "=" + ExtensionPluginTable.STATEMENT_SELECT_PLUGIN_ID + " LIMIT 1)";

    private ExtensionTableProviderTable() {
        /* Static information class */
    }

    public static void set3PluginValuesToStatement(PreparedStatement statement, int parameterIndex, String providerName, String pluginName, ServerUUID serverUUID) throws SQLException {
        statement.setString(parameterIndex, providerName);
        ExtensionPluginTable.set2PluginValuesToStatement(statement, parameterIndex + 1, pluginName, serverUUID);
    }

    public static String createTableSQL(DBType dbType) {
        return CreateTableBuilder.create(TABLE_NAME, dbType)
                .column(ID, INT).primaryKey()
                .column(PROVIDER_NAME, Sql.varchar(50)).notNull()
                .column(COLOR, Sql.varchar(25)).notNull().defaultValue("'" + Color.NONE.name() + "'")
                .column(VALUES_FOR, INT).defaultValue("0")
                .column(CONDITION, Sql.varchar(54)) // 50 + 4 for "not_"
                .column(COL_1, Sql.varchar(50))
                .column(COL_2, Sql.varchar(50))
                .column(COL_3, Sql.varchar(50))
                .column(COL_4, Sql.varchar(50))
                .column(COL_5, Sql.varchar(50))
                .column(PLUGIN_ID, INT).notNull()
                .column(ICON_1_ID, INT)
                .column(ICON_2_ID, INT)
                .column(ICON_3_ID, INT)
                .column(ICON_4_ID, INT)
                .column(ICON_5_ID, INT)
                .column(FORMAT_1, Sql.varchar(15))
                .column(FORMAT_2, Sql.varchar(15))
                .column(FORMAT_3, Sql.varchar(15))
                .column(FORMAT_4, Sql.varchar(15))
                .column(FORMAT_5, Sql.varchar(15))
                .column(TAB_ID, INT)
                .foreignKey(PLUGIN_ID, ExtensionPluginTable.TABLE_NAME, ExtensionPluginTable.ID)
                .foreignKey(ICON_1_ID, ExtensionIconTable.TABLE_NAME, ExtensionIconTable.ID)
                .foreignKey(ICON_2_ID, ExtensionIconTable.TABLE_NAME, ExtensionIconTable.ID)
                .foreignKey(ICON_3_ID, ExtensionIconTable.TABLE_NAME, ExtensionIconTable.ID)
                .foreignKey(ICON_4_ID, ExtensionIconTable.TABLE_NAME, ExtensionIconTable.ID)
                .foreignKey(ICON_5_ID, ExtensionIconTable.TABLE_NAME, ExtensionIconTable.ID)
                .foreignKey(TAB_ID, ExtensionTabTable.TABLE_NAME, ExtensionTabTable.ID)
                .build();
    }
}