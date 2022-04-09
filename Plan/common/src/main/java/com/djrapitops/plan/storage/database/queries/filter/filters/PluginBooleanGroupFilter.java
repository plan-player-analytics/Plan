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
import com.djrapitops.plan.identification.Server;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.Query;
import com.djrapitops.plan.storage.database.queries.QueryAllStatement;
import com.djrapitops.plan.storage.database.queries.QueryStatement;
import com.djrapitops.plan.storage.database.queries.objects.ServerQueries;
import com.djrapitops.plan.storage.database.sql.tables.*;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

@Singleton
public class PluginBooleanGroupFilter extends MultiOptionFilter {

    private final DBSystem dbSystem;

    @Inject
    public PluginBooleanGroupFilter(DBSystem dbSystem) {
        this.dbSystem = dbSystem;
    }

    private static Query<List<PluginBooleanOption>> pluginBooleanOptionsQuery() {
        String selectOptions = SELECT + DISTINCT +
                "server." + ServerTable.ID + " as server_id," +
                "server." + ServerTable.NAME + " as server_name," +
                "plugin." + ExtensionPluginTable.PLUGIN_NAME + " as plugin_name," +
                "provider." + ExtensionProviderTable.TEXT + " as provider_text" +
                FROM + ServerTable.TABLE_NAME + " server" +
                INNER_JOIN + ExtensionPluginTable.TABLE_NAME + " plugin on plugin." + ExtensionPluginTable.SERVER_UUID + "=server." + ServerTable.SERVER_UUID +
                INNER_JOIN + ExtensionProviderTable.TABLE_NAME + " provider on provider." + ExtensionProviderTable.PLUGIN_ID + "=plugin." + ExtensionPluginTable.ID +
                INNER_JOIN + ExtensionPlayerValueTable.TABLE_NAME + " value on value." + ExtensionPlayerValueTable.PROVIDER_ID + "=provider." + ExtensionProviderTable.ID +
                WHERE + "value." + ExtensionPlayerValueTable.BOOLEAN_VALUE + " IS NOT NULL" +
                ORDER_BY + "server_name ASC, plugin_name ASC, provider_text ASC";
        return new QueryAllStatement<List<PluginBooleanOption>>(selectOptions) {
            @Override
            public List<PluginBooleanOption> processResults(ResultSet set) throws SQLException {
                List<PluginBooleanOption> options = new ArrayList<>();
                while (set.next()) {
                    int serverId = set.getInt("server_id");
                    String serverName = set.getString("server_name");
                    String pluginName = set.getString("plugin_name");
                    String providerText = set.getString("provider_text");
                    options.add(new PluginBooleanOption(
                            Server.getIdentifiableName(serverName, serverId),
                            pluginName,
                            providerText
                    ));
                }
                Collections.sort(options);
                return options;
            }
        };
    }

    private static Query<Set<Integer>> playersInGroups(
            Map<PluginBooleanOption, SelectedBoolean> selected,
            Map<String, ServerUUID> namesToUUIDs
    ) {
        return db -> {
            Set<Integer> userIds = new HashSet<>();
            for (Map.Entry<PluginBooleanOption, SelectedBoolean> option : selected.entrySet()) {
                PluginBooleanOption pluginBooleanOption = option.getKey();
                SelectedBoolean selectedBoolean = option.getValue();
                userIds.addAll(
                        db.query(playersInGroup(
                                namesToUUIDs.get(pluginBooleanOption.getServerName()),
                                pluginBooleanOption.getPluginName(),
                                pluginBooleanOption.getProviderText(),
                                selectedBoolean
                        ))
                );
            }
            return userIds;
        };
    }

    private static Query<Set<Integer>> playersInGroup(
            ServerUUID serverUUID, String pluginName, String providerText, SelectedBoolean selectedBoolean
    ) {
        String selectUUIDsWithBooleanValues = SELECT + DISTINCT + "u." + UsersTable.ID + " as id" +
                FROM + ExtensionPluginTable.TABLE_NAME + " plugin" +
                INNER_JOIN + ExtensionProviderTable.TABLE_NAME + " provider on provider." + ExtensionProviderTable.PLUGIN_ID + "=plugin." + ExtensionPluginTable.ID +
                INNER_JOIN + ExtensionPlayerValueTable.TABLE_NAME + " value on value." + ExtensionPlayerValueTable.PROVIDER_ID + "=provider." + ExtensionProviderTable.ID +
                INNER_JOIN + UsersTable.TABLE_NAME + " u on u." + UsersTable.USER_UUID + "=value." + ExtensionPlayerValueTable.USER_UUID +
                WHERE + "plugin." + ExtensionPluginTable.SERVER_UUID + "=?" +
                AND + "plugin." + ExtensionPluginTable.PLUGIN_NAME + "=?" +
                AND + "provider." + ExtensionProviderTable.TEXT + "=?" +
                AND + "value." + ExtensionPlayerValueTable.BOOLEAN_VALUE + (selectedBoolean == SelectedBoolean.BOTH ? "IS NOT NULL" : "=?");

        return new QueryStatement<Set<Integer>>(selectUUIDsWithBooleanValues) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
                statement.setString(2, pluginName);
                statement.setString(3, providerText);
                if (selectedBoolean != SelectedBoolean.BOTH) {
                    statement.setBoolean(4, selectedBoolean == SelectedBoolean.TRUE);
                }
            }

            @Override
            public Set<Integer> processResults(ResultSet set) throws SQLException {
                Set<Integer> userIds = new HashSet<>();
                while (set.next()) {
                    userIds.add(set.getInt("id"));
                }
                return userIds;
            }
        };
    }

    @Override
    public String getKind() {
        return "pluginsBooleanGroups";
    }

    private List<String> getOptionList() {
        Database database = dbSystem.getDatabase();
        List<PluginBooleanOption> pluginBooleanOptions = database.query(pluginBooleanOptionsQuery());

        List<String> options = new ArrayList<>();
        for (PluginBooleanOption pluginBooleanOption : pluginBooleanOptions) {
            String names = pluginBooleanOption.format();
            options.add(names + ": true");
            options.add(names + ": false");
        }

        return options;
    }

    @Override
    public Map<String, Object> getOptions() {
        return Collections.singletonMap("options", getOptionList());
    }

    @Override
    public Set<Integer> getMatchingUserIds(InputFilterDto query) {
        Map<PluginBooleanOption, SelectedBoolean> selectedBooleanOptions = new HashMap<>();
        for (String selected : getSelected(query)) {
            String[] optionAndBoolean = StringUtils.split(selected, ":", 2);
            PluginBooleanOption pluginBooleanOption = PluginBooleanOption.parse(optionAndBoolean[0].trim());
            String selectedBoolean = optionAndBoolean[1].trim().toUpperCase();
            selectedBooleanOptions.computeIfPresent(pluginBooleanOption, (key, existing) -> SelectedBoolean.BOTH);
            selectedBooleanOptions.computeIfAbsent(pluginBooleanOption, key -> SelectedBoolean.valueOf(selectedBoolean));
        }

        Database db = dbSystem.getDatabase();
        Map<String, ServerUUID> namesToUUIDs = db.query(ServerQueries.fetchServerNamesToUUIDs());
        return db.query(playersInGroups(selectedBooleanOptions, namesToUUIDs));
    }

    public enum SelectedBoolean {
        TRUE,
        FALSE,
        BOTH
    }

    public static class PluginBooleanOption implements Comparable<PluginBooleanOption> {
        private final String serverName;
        private final String pluginName;
        private final String providerText;

        public PluginBooleanOption(String serverName, String pluginName, String providerText) {
            this.serverName = serverName;
            this.pluginName = pluginName;
            this.providerText = providerText;
        }

        public static PluginBooleanOption parse(String fromFormatted) {
            String[] split1 = StringUtils.split(fromFormatted, ",", 2);
            String[] split2 = StringUtils.split(split1[1], "-", 2);
            String serverName = split1[0].trim();
            String pluginName = split2[0].trim();
            String providerName = split2[1].trim();
            return new PluginBooleanOption(serverName, pluginName, providerName);
        }

        public String getServerName() {
            return serverName;
        }

        public String getPluginName() {
            return pluginName;
        }

        public String getProviderText() {
            return providerText;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PluginBooleanOption that = (PluginBooleanOption) o;
            return Objects.equals(getServerName(), that.getServerName()) && Objects.equals(getPluginName(), that.getPluginName()) && Objects.equals(getProviderText(), that.getProviderText());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getServerName(), getPluginName(), getProviderText());
        }

        @Override
        public int compareTo(PluginBooleanOption o) {
            int serverNameAlphabetical = String.CASE_INSENSITIVE_ORDER.compare(serverName, o.serverName);
            if (serverNameAlphabetical != 0) return serverNameAlphabetical;

            int pluginNameAlphabetical = String.CASE_INSENSITIVE_ORDER.compare(pluginName, o.pluginName);
            if (pluginNameAlphabetical != 0) return pluginNameAlphabetical;

            return String.CASE_INSENSITIVE_ORDER.compare(providerText, o.providerText);
        }

        public String format() {
            return serverName + ", " + pluginName + " - " + providerText;
        }
    }
}
