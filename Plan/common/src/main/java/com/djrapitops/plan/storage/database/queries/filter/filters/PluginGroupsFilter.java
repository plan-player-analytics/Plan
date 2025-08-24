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

import com.djrapitops.plan.delivery.domain.datatransfer.InputFilterDto;
import com.djrapitops.plan.extension.implementation.providers.ProviderIdentifier;
import com.djrapitops.plan.extension.implementation.storage.queries.ExtensionUserIdsInGroupQuery;
import com.djrapitops.plan.identification.Server;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.queries.QueryAllStatement;
import com.djrapitops.plan.storage.database.sql.tables.ServerTable;
import com.djrapitops.plan.storage.database.sql.tables.extension.ExtensionGroupsTable;
import com.djrapitops.plan.storage.database.sql.tables.extension.ExtensionPluginTable;
import com.djrapitops.plan.storage.database.sql.tables.extension.ExtensionProviderTable;
import com.djrapitops.plan.utilities.dev.Untrusted;
import com.djrapitops.plan.utilities.java.Maps;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

public class PluginGroupsFilter extends MultiOptionFilter {

    private final DBSystem dbSystem;
    private final List<String> groups;
    private final ProviderIdentifier identifier;
    private final String serverName;

    public PluginGroupsFilter(DBSystem dbSystem, ProviderIdentifier identifier, List<String> groups) {
        this.dbSystem = dbSystem;
        this.identifier = identifier;
        this.groups = groups;
        this.serverName = identifier.getServerName().orElse("?");
    }

    @Override
    public String getKind() {
        return "pluginGroups-" + serverName + " " + identifier.getPluginName() + " " + identifier.getProviderName();
    }

    @Override
    public Map<String, Object> getOptions() {
        return Maps.builder(String.class, Object.class)
                .put("plugin", identifier.getPluginName())
                .put("group", identifier.getProviderName())
                .put("options", groups)
                .build();
    }

    @Override
    public Set<Integer> getMatchingUserIds(@Untrusted InputFilterDto query) {
        return dbSystem.getDatabase().query(
                new ExtensionUserIdsInGroupQuery(identifier.getPluginName(), identifier.getProviderName(), identifier.getServerUUID(), getSelected(query))
        );
    }

    @Singleton
    public static class PluginGroupsFilterQuery extends QueryAllStatement<Collection<PluginGroupsFilter>> {

        private final DBSystem dbSystem;

        @Inject
        public PluginGroupsFilterQuery(DBSystem dbSystem, ServerInfo serverInfo) {
            super(SELECT + DISTINCT +
                    "pl." + ExtensionPluginTable.PLUGIN_NAME + " as plugin_name," +
                    "s." + ServerTable.NAME + " as server_name," +
                    "s." + ServerTable.ID + " as server_id," +
                    "s." + ServerTable.PROXY + " as is_proxy," +
                    "pl." + ExtensionPluginTable.SERVER_UUID + " as server_uuid," +
                    "pr." + ExtensionProviderTable.PROVIDER_NAME + " as provider_name," +
                    "gr." + ExtensionGroupsTable.GROUP_NAME + " as group_name" +
                    FROM + ExtensionPluginTable.TABLE_NAME + " pl" +
                    INNER_JOIN + ServerTable.TABLE_NAME + " s on s." + ServerTable.SERVER_UUID + "=pl." + ExtensionPluginTable.SERVER_UUID +
                    INNER_JOIN + ExtensionProviderTable.TABLE_NAME + " pr on pl." + ExtensionPluginTable.ID + "=pr." + ExtensionProviderTable.PLUGIN_ID +
                    INNER_JOIN + ExtensionGroupsTable.TABLE_NAME + " gr on pr." + ExtensionProviderTable.ID + "=gr." + ExtensionGroupsTable.PROVIDER_ID);

            this.dbSystem = dbSystem;
        }

        @Override
        public Collection<PluginGroupsFilter> processResults(ResultSet set) throws SQLException {
            Map<ProviderIdentifier, List<String>> byProvider = new HashMap<>();
            while (set.next()) {
                String plugin = set.getString("plugin_name");
                String provider = set.getString("provider_name");
                ServerUUID serverUUID = ServerUUID.fromString(set.getString("server_uuid"));
                ProviderIdentifier identifier = new ProviderIdentifier(serverUUID, plugin, provider);
                identifier.setServerName(Server.getIdentifiableName(
                        set.getString("server_name"),
                        set.getInt("server_id"),
                        set.getBoolean("is_proxy")
                ));

                String group = set.getString("group_name");

                List<String> groups = byProvider.getOrDefault(identifier, new ArrayList<>());
                groups.add(group);
                byProvider.put(identifier, groups);
            }

            List<PluginGroupsFilter> filters = new ArrayList<>();
            for (Map.Entry<ProviderIdentifier, List<String>> groupsOfProvider : byProvider.entrySet()) {
                filters.add(new PluginGroupsFilter(
                        dbSystem,
                        groupsOfProvider.getKey(),
                        groupsOfProvider.getValue()
                ));
            }
            return filters;
        }
    }
}
