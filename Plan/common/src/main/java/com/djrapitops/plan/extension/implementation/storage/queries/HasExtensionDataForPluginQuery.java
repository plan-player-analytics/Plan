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
package com.djrapitops.plan.extension.implementation.storage.queries;

import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.queries.HasMoreThanZeroQueryStatement;
import com.djrapitops.plan.storage.database.sql.tables.extension.ExtensionPluginTable;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

public class HasExtensionDataForPluginQuery extends HasMoreThanZeroQueryStatement {

    private final String pluginName;
    private final ServerUUID serverUUID;

    public HasExtensionDataForPluginQuery(String pluginName, ServerUUID serverUUID) {
        super(sql());
        this.pluginName = pluginName;
        this.serverUUID = serverUUID;
    }

    private static String sql() {
        return SELECT + "COUNT(1) as c" +
                FROM + ExtensionPluginTable.TABLE_NAME +
                WHERE + ExtensionPluginTable.PLUGIN_NAME + "=?" +
                AND + ExtensionPluginTable.SERVER_UUID + "=?";
    }

    @Override
    public void prepare(PreparedStatement statement) throws SQLException {
        statement.setString(1, pluginName);
        statement.setString(2, serverUUID.toString());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        HasExtensionDataForPluginQuery that = (HasExtensionDataForPluginQuery) o;
        return Objects.equals(pluginName, that.pluginName) && Objects.equals(serverUUID, that.serverUUID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), pluginName, serverUUID);
    }
}
