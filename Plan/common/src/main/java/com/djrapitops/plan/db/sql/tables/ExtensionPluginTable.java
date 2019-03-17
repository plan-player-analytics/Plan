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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

import static com.djrapitops.plan.db.sql.parsing.Sql.*;

/**
 * Table information about 'plan_extension_plugins'.
 *
 * @author Rsl1122
 */
public class ExtensionPluginTable {

    public static final String TABLE_NAME = "plan_extension_plugins";

    public static final String ID = "id";
    public static final String PLUGIN_NAME = "name";
    public static final String LAST_UPDATED = "last_updated";
    public static final String SERVER_UUID = "server_uuid";
    public static final String ICON_ID = "icon_id";

    public static final String STATEMENT_SELECT_PLUGIN_ID = "(" + SELECT + ID +
            FROM + TABLE_NAME +
            WHERE + PLUGIN_NAME + "=?" +
            AND + SERVER_UUID + "=?)";

    public static void setPluginValuesToStatement(PreparedStatement statement, int parameterIndex, String pluginName, UUID serverUUID) throws SQLException {
        statement.setString(parameterIndex, pluginName);
        statement.setString(parameterIndex + 1, serverUUID.toString());
    }
}