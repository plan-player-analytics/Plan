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
 * Table information about 'plan_extension_plugins'.
 *
 * @author AuroraLS3
 */
public class ExtensionPluginTable {

    public static final String TABLE_NAME = "plan_extension_plugins";

    public static final String ID = "id";
    public static final String PLUGIN_NAME = "name";
    public static final String LAST_UPDATED = "last_updated";
    public static final String SERVER_UUID = "server_uuid";
    public static final String ICON_ID = "icon_id";

    public static final String STATEMENT_SELECT_PLUGIN_ID = '(' + SELECT + ID +
            FROM + TABLE_NAME +
            WHERE + PLUGIN_NAME + "=?" +
            AND + SERVER_UUID + "=? LIMIT 1)";

    public static void set2PluginValuesToStatement(PreparedStatement statement, int parameterIndex, String pluginName, ServerUUID serverUUID) throws SQLException {
        statement.setString(parameterIndex, pluginName);
        statement.setString(parameterIndex + 1, serverUUID.toString());
    }

    private ExtensionPluginTable() {
        /* Static information class */
    }

    public static String createTableSQL(DBType dbType) {
        return CreateTableBuilder.create(TABLE_NAME, dbType)
                .column(ID, INT).primaryKey()
                .column(PLUGIN_NAME, Sql.varchar(50)).notNull()
                .column(LAST_UPDATED, LONG).notNull()
                .column(SERVER_UUID, Sql.varchar(36)).notNull()
                .column(ICON_ID, INT).notNull()
                .foreignKey(ICON_ID, ExtensionIconTable.TABLE_NAME, ExtensionIconTable.ID)
                .build();
    }
}