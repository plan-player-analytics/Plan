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
package com.djrapitops.plan.storage.database.queries.filter.filters;

import com.djrapitops.plan.extension.implementation.storage.queries.ExtensionUUIDsInGroupQuery;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.queries.QueryAllStatement;
import com.djrapitops.plan.storage.database.queries.filter.FilterQuery;
import com.djrapitops.plan.storage.database.sql.tables.ExtensionGroupsTable;
import com.djrapitops.plan.storage.database.sql.tables.ExtensionPluginTable;
import com.djrapitops.plan.storage.database.sql.tables.ExtensionProviderTable;
import com.djrapitops.plan.utilities.java.Maps;

import javax.inject.Singleton;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

public class PluginGroupsFilter extends MultiOptionFilter {

    private final DBSystem dbSystem;
    private final String pluginName;
    private final String groupProvider;
    private final List<String> groups;

    public PluginGroupsFilter(
            DBSystem dbSystem,
            String pluginName,
            String groupProvider,
            List<String> groups
    ) {
        this.dbSystem = dbSystem;
        this.pluginName = pluginName;
        this.groupProvider = groupProvider;
        this.groups = groups;
    }

    @Override
    public String getKind() {
        return "pluginGroups-" + pluginName + "-" + groupProvider;
    }

    @Override
    public Map<String, Object> getOptions() {
        return Maps.builder(String.class, Object.class)
                .put("plugin", pluginName)
                .put("group", groupProvider)
                .put("options", groups)
                .build();
    }

    @Override
    public Set<UUID> getMatchingUUIDs(FilterQuery query) {
        return dbSystem.getDatabase().query(
                new ExtensionUUIDsInGroupQuery(pluginName, groupProvider, getSelected(query))
        );
    }

    @Singleton
    public static class PluginGroupsFilterQuery extends QueryAllStatement<Collection<PluginGroupsFilter>> {

        private final DBSystem dbSystem;

        public PluginGroupsFilterQuery(DBSystem dbSystem) {
            super(SELECT + DISTINCT + "pl." + ExtensionPluginTable.PLUGIN_NAME + " as plugin_name," +
                    "pr." + ExtensionProviderTable.PROVIDER_NAME + " as provider_name," +
                    "gr." + ExtensionGroupsTable.GROUP_NAME + " as group_name" +
                    FROM + ExtensionPluginTable.TABLE_NAME + " pl" +
                    INNER_JOIN + ExtensionProviderTable.TABLE_NAME + " pr  on pl." + ExtensionPluginTable.ID + "=pr." + ExtensionProviderTable.PLUGIN_ID +
                    INNER_JOIN + ExtensionGroupsTable.TABLE_NAME + " gr on pr." + ExtensionProviderTable.ID + "=gr." + ExtensionGroupsTable.PROVIDER_ID);

            this.dbSystem = dbSystem;
        }

        @Override
        public Collection<PluginGroupsFilter> processResults(ResultSet set) throws SQLException {
            Map<String, Map<String, List<String>>> byPlugin = new HashMap<>();
            while (set.next()) {
                String plugin = set.getString("plugin_name");
                String provider = set.getString("provider_name");
                String group = set.getString("group_name");

                Map<String, List<String>> byProvider = byPlugin.getOrDefault(plugin, new HashMap<>());
                List<String> groups = byProvider.getOrDefault(provider, new ArrayList<>());
                groups.add(group);
                byProvider.put(provider, groups);
                byPlugin.put(plugin, byProvider);
            }

            List<PluginGroupsFilter> filters = new ArrayList<>();
            for (Map.Entry<String, Map<String, List<String>>> providersOfPlugin : byPlugin.entrySet()) {
                for (Map.Entry<String, List<String>> groupsOfProvider : providersOfPlugin.getValue().entrySet()) {
                    filters.add(new PluginGroupsFilter(
                            dbSystem,
                            providersOfPlugin.getKey(),
                            groupsOfProvider.getKey(),
                            groupsOfProvider.getValue()
                    ));
                }
            }
            return filters;
        }
    }
}
