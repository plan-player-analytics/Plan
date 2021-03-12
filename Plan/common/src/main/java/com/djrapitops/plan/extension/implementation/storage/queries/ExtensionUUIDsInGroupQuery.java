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
import com.djrapitops.plan.storage.database.sql.tables.ExtensionGroupsTable;
import com.djrapitops.plan.storage.database.sql.tables.ExtensionProviderTable;
import org.apache.commons.text.TextStringBuilder;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

public class ExtensionUUIDsInGroupQuery extends QueryStatement<Set<UUID>> {

    private final String pluginName;
    private final String groupProvider;
    private final ServerUUID serverUUID;
    private final List<String> inGroups;

    public ExtensionUUIDsInGroupQuery(String pluginName, String groupProvider, ServerUUID serverUUID, List<String> inGroups) {
        super(buildSQL(inGroups), 100);
        this.pluginName = pluginName;
        this.groupProvider = groupProvider;
        this.serverUUID = serverUUID;
        this.inGroups = inGroups;
    }

    private static String buildSQL(Collection<String> inGroups) {
        TextStringBuilder dynamicInClauseAllocation = new TextStringBuilder();
        dynamicInClauseAllocation.appendWithSeparators(inGroups.stream().map(group -> "?").toArray(), ",");
        return SELECT + DISTINCT + ExtensionGroupsTable.USER_UUID +
                FROM + ExtensionGroupsTable.TABLE_NAME +
                WHERE + ExtensionGroupsTable.PROVIDER_ID + "=" + ExtensionProviderTable.STATEMENT_SELECT_PROVIDER_ID +
                AND + ExtensionGroupsTable.GROUP_NAME + " IN (" + dynamicInClauseAllocation.build() + ")";
    }

    @Override
    public void prepare(PreparedStatement statement) throws SQLException {
        ExtensionProviderTable.set3PluginValuesToStatement(statement, 1, groupProvider, pluginName, serverUUID);
        int index = 4;
        for (String group : inGroups) {
            statement.setString(index, group);
            index++;
        }
    }

    @Override
    public Set<UUID> processResults(ResultSet set) throws SQLException {
        Set<UUID> uuids = new HashSet<>();
        while (set.next()) {
            uuids.add(UUID.fromString(set.getString(ExtensionGroupsTable.USER_UUID)));
        }
        return uuids;
    }
}
