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

import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.DBType;
import com.djrapitops.plan.storage.database.sql.building.CreateTableBuilder;
import com.djrapitops.plan.storage.database.sql.building.Sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Table information about 'plan_extension_providers'.
 *
 * @author AuroraLS3
 */
public class ExtensionProviderTable {

    public static final String TABLE_NAME = "plan_extension_providers";

    public static final String ID = "id";
    public static final String PROVIDER_NAME = "name";
    public static final String TEXT = "text";
    public static final String DESCRIPTION = "description"; // Can be null
    public static final String PRIORITY = "priority";
    public static final String GROUPABLE = "groupable"; // default false
    public static final String CONDITION = "condition_name"; // Can be null, related to @Conditional
    public static final String PLUGIN_ID = "plugin_id";
    public static final String ICON_ID = "icon_id";
    public static final String TAB_ID = "tab_id"; // Can be null, related to @Tab
    public static final String SHOW_IN_PLAYERS_TABLE = "show_in_players_table"; // default false

    public static final String HIDDEN = "hidden"; // default false, related to @BooleanProvider
    public static final String PROVIDED_CONDITION = "provided_condition"; // Can be null, related to @BooleanProvider
    public static final String FORMAT_TYPE = "format_type"; // Can be null,  related to @NumberProvider
    public static final String IS_PLAYER_NAME = "player_name"; // default false, related to @StringProvider

    public static final String STATEMENT_SELECT_PROVIDER_ID = '(' + SELECT + ID + FROM + TABLE_NAME +
            WHERE + PROVIDER_NAME + "=?" +
            AND + PLUGIN_ID + "=" + ExtensionPluginTable.STATEMENT_SELECT_PLUGIN_ID + " LIMIT 1)";

    public static void set3PluginValuesToStatement(PreparedStatement statement, int parameterIndex, String providerName, String pluginName, ServerUUID serverUUID) throws SQLException {
        statement.setString(parameterIndex, providerName);
        ExtensionPluginTable.set2PluginValuesToStatement(statement, parameterIndex + 1, pluginName, serverUUID);
    }

    private ExtensionProviderTable() {
        /* Static information class */
    }

    public static String createTableSQL(DBType dbType) {
        return CreateTableBuilder.create(TABLE_NAME, dbType)
                .column(ID, INT).primaryKey()
                .column(PROVIDER_NAME, Sql.varchar(50)).notNull()
                .column(TEXT, Sql.varchar(50)).notNull()
                .column(DESCRIPTION, Sql.varchar(150))
                .column(PRIORITY, INT).notNull().defaultValue("0")
                .column(SHOW_IN_PLAYERS_TABLE, BOOL).notNull().defaultValue(false)
                .column(GROUPABLE, BOOL).notNull().defaultValue(false)
                .column(CONDITION, Sql.varchar(54)) // 50 + 4 for "not_"
                .column(PROVIDED_CONDITION, Sql.varchar(50))
                .column(FORMAT_TYPE, Sql.varchar(25))
                .column(HIDDEN, BOOL).notNull().defaultValue(false)
                .column(IS_PLAYER_NAME, BOOL).notNull().defaultValue(false)
                .column(PLUGIN_ID, INT).notNull()
                .column(ICON_ID, INT).notNull()
                .column(TAB_ID, INT)
                .foreignKey(PLUGIN_ID, ExtensionPluginTable.TABLE_NAME, ExtensionPluginTable.ID)
                .foreignKey(ICON_ID, ExtensionIconTable.TABLE_NAME, ExtensionIconTable.ID)
                .foreignKey(TAB_ID, ExtensionTabTable.TABLE_NAME, ExtensionTabTable.ID)
                .build();
    }
}