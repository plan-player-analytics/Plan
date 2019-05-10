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

import com.djrapitops.plan.db.SQLDB;
import com.djrapitops.plan.db.access.Query;
import com.djrapitops.plan.db.access.QueryStatement;
import com.djrapitops.plan.db.sql.tables.ExtensionIconTable;
import com.djrapitops.plan.db.sql.tables.ExtensionPlayerValueTable;
import com.djrapitops.plan.db.sql.tables.ExtensionProviderTable;
import com.djrapitops.plan.db.sql.tables.ExtensionTabTable;
import com.djrapitops.plan.extension.ElementOrder;
import com.djrapitops.plan.extension.FormatType;
import com.djrapitops.plan.extension.icon.Color;
import com.djrapitops.plan.extension.icon.Family;
import com.djrapitops.plan.extension.icon.Icon;
import com.djrapitops.plan.extension.implementation.TabInformation;
import com.djrapitops.plan.extension.implementation.results.*;
import com.djrapitops.plan.extension.implementation.results.player.ExtensionPlayerData;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static com.djrapitops.plan.db.sql.parsing.Sql.*;

/**
 * Query all ExtensionPlayerData by Server UUIDs.
 * <p>
 * Returns Map: Server UUID - List of ExtensionPlayerData.
 * <p>
 * How it is done:
 * - Two queries are run, one that fetches all extensions and one that fetches all data of the player.
 * - Data query is sorted into a multi-map: PluginID - Tab Name - Tab Data
 * - (Tab Name can be empty.)
 * - Multi-map is sorted into ExtensionPlayerData objects by PluginID, one per ID
 * - This map is sorted into final Map: Server UUID - List of ExtensionPlayerData at the highest level.
 * <p>
 * There are multiple data extraction methods to make extracting the value query easier.
 *
 * @author Rsl1122
 */
public class ExtensionPlayerDataQuery implements Query<Map<UUID, List<ExtensionPlayerData>>> {

    private final UUID playerUUID;

    public ExtensionPlayerDataQuery(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    @Override
    public Map<UUID, List<ExtensionPlayerData>> executeQuery(SQLDB db) {
        Map<UUID, List<ExtensionInformation>> extensionsByServerUUID = db.query(ExtensionInformationQueries.allExtensions());
        Map<Integer, ExtensionPlayerData.Factory> extensionDataByPluginID = db.query(fetchIncompletePlayerDataByPluginID());

        Map<Integer, ExtensionPlayerData.Factory> tableDataByPluginID = db.query(new ExtensionPlayerTablesQuery(playerUUID));
        combine(extensionDataByPluginID, tableDataByPluginID);

        return flatMapByServerUUID(extensionsByServerUUID, extensionDataByPluginID);
    }

    private void combine(
            Map<Integer, ExtensionPlayerData.Factory> extensionDataByPluginID,
            Map<Integer, ExtensionPlayerData.Factory> aggregates
    ) {
        for (Map.Entry<Integer, ExtensionPlayerData.Factory> entry : aggregates.entrySet()) {
            Integer pluginID = entry.getKey();
            ExtensionPlayerData.Factory data = entry.getValue();

            ExtensionPlayerData.Factory found = extensionDataByPluginID.get(pluginID);
            if (found == null) {
                extensionDataByPluginID.put(pluginID, data);
            } else {
                found.combine(data);
            }
        }
    }

    private Map<UUID, List<ExtensionPlayerData>> flatMapByServerUUID(Map<UUID, List<ExtensionInformation>> extensionsByServerUUID, Map<Integer, ExtensionPlayerData.Factory> extensionDataByPluginID) {
        Map<UUID, List<ExtensionPlayerData>> extensionDataByServerUUID = new HashMap<>();

        for (Map.Entry<UUID, List<ExtensionInformation>> entry : extensionsByServerUUID.entrySet()) {
            UUID serverUUID = entry.getKey();
            for (ExtensionInformation extensionInformation : entry.getValue()) {
                ExtensionPlayerData.Factory data = extensionDataByPluginID.get(extensionInformation.getId());
                if (data == null) {
                    continue;
                }
                List<ExtensionPlayerData> list = extensionDataByServerUUID.getOrDefault(serverUUID, new ArrayList<>());
                list.add(data.setInformation(extensionInformation).build());
                extensionDataByServerUUID.put(serverUUID, list);
            }
        }
        return extensionDataByServerUUID;
    }

    private Query<Map<Integer, ExtensionPlayerData.Factory>> fetchIncompletePlayerDataByPluginID() {
        String sql = SELECT +
                "v1." + ExtensionPlayerValueTable.BOOLEAN_VALUE + " as boolean_value," +
                "v1." + ExtensionPlayerValueTable.DOUBLE_VALUE + " as double_value," +
                "v1." + ExtensionPlayerValueTable.PERCENTAGE_VALUE + " as percentage_value," +
                "v1." + ExtensionPlayerValueTable.LONG_VALUE + " as long_value," +
                "v1." + ExtensionPlayerValueTable.STRING_VALUE + " as string_value," +
                "p1." + ExtensionProviderTable.PLUGIN_ID + " as plugin_id," +
                "p1." + ExtensionProviderTable.PROVIDER_NAME + " as provider_name," +
                "p1." + ExtensionProviderTable.TEXT + " as text," +
                "p1." + ExtensionProviderTable.DESCRIPTION + " as description," +
                "p1." + ExtensionProviderTable.PRIORITY + " as provider_priority," +
                "p1." + ExtensionProviderTable.FORMAT_TYPE + " as format_type," +
                "p1." + ExtensionProviderTable.IS_PLAYER_NAME + " as is_player_name," +
                "t1." + ExtensionTabTable.TAB_NAME + " as tab_name," +
                "t1." + ExtensionTabTable.TAB_PRIORITY + " as tab_priority," +
                "t1." + ExtensionTabTable.ELEMENT_ORDER + " as element_order," +
                "i1." + ExtensionIconTable.ICON_NAME + " as provider_icon_name," +
                "i1." + ExtensionIconTable.FAMILY + " as provider_icon_family," +
                "i1." + ExtensionIconTable.COLOR + " as provider_icon_color," +
                "i2." + ExtensionIconTable.ICON_NAME + " as tab_icon_name," +
                "i2." + ExtensionIconTable.FAMILY + " as tab_icon_family," +
                "i2." + ExtensionIconTable.COLOR + " as tab_icon_color" +
                FROM + ExtensionPlayerValueTable.TABLE_NAME + " v1" +
                INNER_JOIN + ExtensionProviderTable.TABLE_NAME + " p1 on p1." + ExtensionProviderTable.ID + "=v1." + ExtensionPlayerValueTable.PROVIDER_ID +
                LEFT_JOIN + ExtensionTabTable.TABLE_NAME + " t1 on t1." + ExtensionTabTable.ID + "=p1." + ExtensionProviderTable.TAB_ID +
                LEFT_JOIN + ExtensionIconTable.TABLE_NAME + " i1 on i1." + ExtensionIconTable.ID + "=p1." + ExtensionProviderTable.ICON_ID +
                LEFT_JOIN + ExtensionIconTable.TABLE_NAME + " i2 on i2." + ExtensionIconTable.ID + "=p1." + ExtensionTabTable.ICON_ID +
                WHERE + ExtensionPlayerValueTable.USER_UUID + "=?" +
                AND + "p1." + ExtensionProviderTable.HIDDEN + "=?";

        return new QueryStatement<Map<Integer, ExtensionPlayerData.Factory>>(sql, 1000) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, playerUUID.toString());
                statement.setBoolean(2, false); // Don't select hidden values
            }

            @Override
            public Map<Integer, ExtensionPlayerData.Factory> processResults(ResultSet set) throws SQLException {
                Map<Integer, Map<String, ExtensionTabData.Factory>> tabDataByPluginID = extractTabDataByPluginID(set);
                return flatMapToPlayerData(tabDataByPluginID);
            }
        };
    }

    private Map<Integer, ExtensionPlayerData.Factory> flatMapToPlayerData(Map<Integer, Map<String, ExtensionTabData.Factory>> tabDataByPluginID) {
        Map<Integer, ExtensionPlayerData.Factory> dataByPluginID = new HashMap<>();
        for (Map.Entry<Integer, Map<String, ExtensionTabData.Factory>> entry : tabDataByPluginID.entrySet()) {
            Integer pluginID = entry.getKey();
            ExtensionPlayerData.Factory data = dataByPluginID.getOrDefault(pluginID, new ExtensionPlayerData.Factory(pluginID));
            for (ExtensionTabData.Factory tabData : entry.getValue().values()) {
                data.addTab(tabData.build());
            }
            dataByPluginID.put(pluginID, data);
        }
        return dataByPluginID;
    }

    private Map<Integer, Map<String, ExtensionTabData.Factory>> extractTabDataByPluginID(ResultSet set) throws SQLException {
        Map<Integer, Map<String, ExtensionTabData.Factory>> tabDataByPluginID = new HashMap<>();

        while (set.next()) {
            int pluginID = set.getInt("plugin_id");
            Map<String, ExtensionTabData.Factory> tabData = tabDataByPluginID.getOrDefault(pluginID, new HashMap<>());

            String tabName = Optional.ofNullable(set.getString("tab_name")).orElse("");
            ExtensionTabData.Factory inMap = tabData.get(tabName);
            ExtensionTabData.Factory extensionTab = inMap != null ? inMap : extractTab(tabName, set, tabData);

            ExtensionDescriptive extensionDescriptive = extractDescriptive(set);
            extractAndPutDataTo(extensionTab, extensionDescriptive, set);

            tabData.put(tabName, extensionTab);
            tabDataByPluginID.put(pluginID, tabData);
        }
        return tabDataByPluginID;
    }

    private void extractAndPutDataTo(ExtensionTabData.Factory extensionTab, ExtensionDescriptive descriptive, ResultSet set) throws SQLException {
        boolean booleanValue = set.getBoolean(ExtensionPlayerValueTable.BOOLEAN_VALUE);
        if (!set.wasNull()) {
            extensionTab.putBooleanData(new ExtensionBooleanData(descriptive, booleanValue));
            return;
        }

        double doubleValue = set.getDouble(ExtensionPlayerValueTable.DOUBLE_VALUE);
        if (!set.wasNull()) {
            extensionTab.putDoubleData(new ExtensionDoubleData(descriptive, doubleValue));
            return;
        }

        double percentageValue = set.getDouble(ExtensionPlayerValueTable.PERCENTAGE_VALUE);
        if (!set.wasNull()) {
            extensionTab.putPercentageData(new ExtensionDoubleData(descriptive, percentageValue));
            return;
        }

        long numberValue = set.getLong(ExtensionPlayerValueTable.LONG_VALUE);
        if (!set.wasNull()) {
            FormatType formatType = FormatType.getByName(set.getString(ExtensionProviderTable.FORMAT_TYPE)).orElse(FormatType.NONE);
            extensionTab.putNumberData(new ExtensionNumberData(descriptive, formatType, numberValue));
            return;
        }

        String stringValue = set.getString(ExtensionPlayerValueTable.STRING_VALUE);
        if (stringValue != null) {
            boolean isPlayerName = set.getBoolean("is_player_name");
            extensionTab.putStringData(new ExtensionStringData(descriptive, isPlayerName, stringValue));
        }
    }

    private ExtensionDescriptive extractDescriptive(ResultSet set) throws SQLException {
        String name = set.getString("provider_name");
        String text = set.getString(ExtensionProviderTable.TEXT);
        String description = set.getString(ExtensionProviderTable.DESCRIPTION);
        int priority = set.getInt("provider_priority");

        String iconName = set.getString("provider_icon_name");
        Family family = Family.getByName(set.getString("provider_icon_family")).orElse(Family.SOLID);
        Color color = Color.getByName(set.getString("provider_icon_color")).orElse(Color.NONE);
        Icon icon = new Icon(family, iconName, color);

        return new ExtensionDescriptive(name, text, description, icon, priority);
    }

    private ExtensionTabData.Factory extractTab(String tabName, ResultSet set, Map<String, ExtensionTabData.Factory> tabData) throws SQLException {
        Optional<Integer> tabPriority = Optional.of(set.getInt("tab_priority"));
        if (set.wasNull()) {
            tabPriority = Optional.empty();
        }
        Optional<ElementOrder[]> elementOrder = Optional.ofNullable(set.getString(ExtensionTabTable.ELEMENT_ORDER)).map(ElementOrder::deserialize);

        Icon tabIcon = extractTabIcon(set);

        return tabData.getOrDefault(tabName, new ExtensionTabData.Factory(new TabInformation(
                tabName,
                tabIcon,
                elementOrder.orElse(ElementOrder.values()),
                tabPriority.orElse(100)
        )));
    }

    private Icon extractTabIcon(ResultSet set) throws SQLException {
        Optional<String> iconName = Optional.ofNullable(set.getString("tab_icon_name"));
        if (iconName.isPresent()) {
            Family iconFamily = Family.getByName(set.getString("tab_icon_family")).orElse(Family.SOLID);
            Color iconColor = Color.getByName(set.getString("tab_icon_color")).orElse(Color.NONE);
            return new Icon(iconFamily, iconName.get(), iconColor);
        } else {
            return TabInformation.defaultIcon();
        }
    }
}