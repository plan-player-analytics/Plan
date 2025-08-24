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
import com.djrapitops.plan.storage.database.queries.QueryStatement;
import com.djrapitops.plan.storage.database.sql.tables.UsersTable;
import com.djrapitops.plan.storage.database.sql.tables.extension.ExtensionGroupsTable;
import com.djrapitops.plan.storage.database.sql.tables.extension.ExtensionProviderTable;
import com.djrapitops.plan.utilities.dev.Untrusted;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

public class ExtensionUserIdsInGroupQuery extends QueryStatement<Set<Integer>> {

    private final String pluginName;
    private final String groupProvider;
    private final ServerUUID serverUUID;
    @Untrusted
    private final List<String> inGroups;

    public ExtensionUserIdsInGroupQuery(String pluginName, String groupProvider, ServerUUID serverUUID, @Untrusted List<String> inGroups) {
        super(buildSQL(inGroups), 100);
        this.pluginName = pluginName;
        this.groupProvider = groupProvider;
        this.serverUUID = serverUUID;
        this.inGroups = inGroups;
    }

    private static String buildSQL(@Untrusted Collection<String> inGroups) {
        return SELECT + DISTINCT + "u." + UsersTable.ID +
                FROM + ExtensionGroupsTable.TABLE_NAME + " g" +
                INNER_JOIN + UsersTable.TABLE_NAME + " u on u." + UsersTable.USER_UUID + "=g." + ExtensionGroupsTable.USER_UUID +
                WHERE + ExtensionGroupsTable.PROVIDER_ID + "=" + ExtensionProviderTable.STATEMENT_SELECT_PROVIDER_ID +
                AND + ExtensionGroupsTable.GROUP_NAME + " IN (" + nParameters(inGroups.size()) + ")";
    }

    @Override
    public void prepare(PreparedStatement statement) throws SQLException {
        ExtensionProviderTable.set3PluginValuesToStatement(statement, 1, groupProvider, pluginName, serverUUID);
        int index = 4;
        for (@Untrusted String group : inGroups) {
            setStringOrNull(statement, index, group == null || "null".equalsIgnoreCase(group) ? null : group);
            index++;
        }
    }

    @Override
    public Set<Integer> processResults(ResultSet set) throws SQLException {
        Set<Integer> userIds = new HashSet<>();
        while (set.next()) {
            userIds.add(set.getInt(UsersTable.ID));
        }
        return userIds;
    }
}
