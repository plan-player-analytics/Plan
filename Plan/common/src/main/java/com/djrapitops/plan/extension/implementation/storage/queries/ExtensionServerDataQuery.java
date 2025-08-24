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

import com.djrapitops.plan.extension.ElementOrder;
import com.djrapitops.plan.extension.FormatType;
import com.djrapitops.plan.extension.icon.Color;
import com.djrapitops.plan.extension.icon.Family;
import com.djrapitops.plan.extension.icon.Icon;
import com.djrapitops.plan.extension.implementation.TabInformation;
import com.djrapitops.plan.extension.implementation.results.*;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.SQLDB;
import com.djrapitops.plan.storage.database.queries.Query;
import com.djrapitops.plan.storage.database.queries.QueryStatement;
import com.djrapitops.plan.storage.database.sql.tables.extension.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Query ExtensionData of a server.
 * <p>
 * Returns List of ExtensionData.
 * <p>
 * Following utilities are used to query the data more easily.
 * - {@link ExtensionData.Builder}
 * - {@link QueriedTabData}
 * - {@link QueriedTables}
 * These utilities allow combining incomplete information.
 *
 * @author AuroraLS3
 */
public class ExtensionServerDataQuery implements Query<List<ExtensionData>> {

    private final ServerUUID serverUUID;

    public ExtensionServerDataQuery(ServerUUID serverUUID) {
        this.serverUUID = serverUUID;
    }

    @Override
    public List<ExtensionData> executeQuery(SQLDB db) {
        List<ExtensionInformation> extensionsOfServer = db.query(ExtensionInformationQueries.extensionsOfServer(serverUUID));
        Map<Integer, ExtensionData.Builder> extensionDataByPluginID = db.query(fetchIncompleteServerDataByPluginID());

        combine(extensionDataByPluginID, db.query(new ExtensionAggregateBooleansQuery(serverUUID)));
        combine(extensionDataByPluginID, db.query(new ExtensionAggregateDoublesQuery(serverUUID)));
        combine(extensionDataByPluginID, db.query(new ExtensionAggregateNumbersQuery(serverUUID)));
        combine(extensionDataByPluginID, db.query(new ExtensionAggregatePercentagesQuery(serverUUID)));
        combine(extensionDataByPluginID, db.query(new ExtensionServerTablesQuery(serverUUID)));
        combine(extensionDataByPluginID, db.query(new ExtensionAggregateGroupsQuery(serverUUID)));

        return combineWithExtensionInfo(extensionsOfServer, extensionDataByPluginID);
    }

    private void combine(
            Map<Integer, ExtensionData.Builder> extensionDataByPluginID,
            Map<Integer, ExtensionData.Builder> aggregates
    ) {
        for (Map.Entry<Integer, ExtensionData.Builder> entry : aggregates.entrySet()) {
            Integer pluginID = entry.getKey();
            ExtensionData.Builder data = entry.getValue();

            ExtensionData.Builder found = extensionDataByPluginID.get(pluginID);
            if (found == null) {
                extensionDataByPluginID.put(pluginID, data);
            } else {
                found.combine(data);
            }
        }
    }

    private List<ExtensionData> combineWithExtensionInfo(
            List<ExtensionInformation> extensionsOfServer,
            Map<Integer, ExtensionData.Builder> extensionDataByPluginID
    ) {
        List<ExtensionData> extensionData = new ArrayList<>();

        for (ExtensionInformation extensionInformation : extensionsOfServer) {
            ExtensionData.Builder data = extensionDataByPluginID.get(extensionInformation.getId());
            if (data == null) {
                continue;
            }
            extensionData.add(data.setInformation(extensionInformation).build());
        }
        return extensionData;
    }

    private Query<Map<Integer, ExtensionData.Builder>> fetchIncompleteServerDataByPluginID() {
        String sql = SELECT +
                "v1." + ExtensionServerValueTable.BOOLEAN_VALUE + " as boolean_value," +
                "v1." + ExtensionServerValueTable.DOUBLE_VALUE + " as double_value," +
                "v1." + ExtensionServerValueTable.PERCENTAGE_VALUE + " as percentage_value," +
                "v1." + ExtensionServerValueTable.LONG_VALUE + " as long_value," +
                "v1." + ExtensionServerValueTable.STRING_VALUE + " as string_value," +
                "v1." + ExtensionServerValueTable.COMPONENT_VALUE + " as component_value," +
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
                FROM + ExtensionServerValueTable.TABLE_NAME + " v1" +
                INNER_JOIN + ExtensionProviderTable.TABLE_NAME + " p1 on p1." + ExtensionProviderTable.ID + "=v1." + ExtensionServerValueTable.PROVIDER_ID +
                INNER_JOIN + ExtensionPluginTable.TABLE_NAME + " e1 on p1." + ExtensionProviderTable.PLUGIN_ID + "=e1." + ExtensionPluginTable.ID +
                LEFT_JOIN + ExtensionTabTable.TABLE_NAME + " t1 on t1." + ExtensionTabTable.ID + "=p1." + ExtensionProviderTable.TAB_ID +
                LEFT_JOIN + ExtensionIconTable.TABLE_NAME + " i1 on i1." + ExtensionIconTable.ID + "=p1." + ExtensionProviderTable.ICON_ID +
                LEFT_JOIN + ExtensionIconTable.TABLE_NAME + " i2 on i2." + ExtensionIconTable.ID + "=t1." + ExtensionTabTable.ICON_ID +
                WHERE + ExtensionPluginTable.SERVER_UUID + "=?" +
                AND + "p1." + ExtensionProviderTable.HIDDEN + "=?";

        return new QueryStatement<>(sql, 1000) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
                statement.setBoolean(2, false); // Don't select hidden values
            }

            @Override
            public Map<Integer, ExtensionData.Builder> processResults(ResultSet set) throws SQLException {
                return extractTabDataByPluginID(set).toExtensionDataByPluginID();
            }
        };
    }

    private QueriedTabData extractTabDataByPluginID(ResultSet set) throws SQLException {
        QueriedTabData tabData = new QueriedTabData();

        while (set.next()) {
            int pluginID = set.getInt("plugin_id");
            String tabName = Optional.ofNullable(set.getString("tab_name")).orElse("");
            ExtensionTabData.Builder extensionTab = tabData.getTab(pluginID, tabName, () -> extractTabInformation(tabName, set));

            ExtensionDescription extensionDescription = extractDescription(set);
            extractAndPutDataTo(extensionTab, extensionDescription, set);
        }
        return tabData;
    }

    private TabInformation extractTabInformation(String tabName, ResultSet set) throws SQLException {
        Optional<Integer> tabPriority = Optional.of(set.getInt("tab_priority"));
        if (set.wasNull()) {
            tabPriority = Optional.empty();
        }
        Optional<ElementOrder[]> elementOrder = Optional.ofNullable(set.getString(ExtensionTabTable.ELEMENT_ORDER)).map(ElementOrder::deserialize);

        Icon tabIcon = extractTabIcon(set);

        return new TabInformation(
                tabName,
                tabIcon,
                elementOrder.orElse(ElementOrder.values()),
                tabPriority.orElse(100)
        );
    }

    private void extractAndPutDataTo(ExtensionTabData.Builder extensionTab, ExtensionDescription description, ResultSet set) throws SQLException {
        boolean booleanValue = set.getBoolean(ExtensionServerValueTable.BOOLEAN_VALUE);
        if (!set.wasNull()) {
            extensionTab.putBooleanData(new ExtensionBooleanData(description, booleanValue));
            return;
        }

        double doubleValue = set.getDouble(ExtensionServerValueTable.DOUBLE_VALUE);
        if (!set.wasNull()) {
            extensionTab.putDoubleData(new ExtensionDoubleData(description, doubleValue));
            return;
        }

        double percentageValue = set.getDouble(ExtensionServerValueTable.PERCENTAGE_VALUE);
        if (!set.wasNull()) {
            extensionTab.putPercentageData(new ExtensionDoubleData(description, percentageValue));
            return;
        }

        long numberValue = set.getLong(ExtensionServerValueTable.LONG_VALUE);
        if (!set.wasNull()) {
            FormatType formatType = FormatType.getByName(set.getString(ExtensionProviderTable.FORMAT_TYPE)).orElse(FormatType.NONE);
            extensionTab.putNumberData(new ExtensionNumberData(description, formatType, numberValue));
            return;
        }

        String stringValue = set.getString(ExtensionServerValueTable.STRING_VALUE);
        if (stringValue != null) {
            boolean isPlayerName = set.getBoolean("is_player_name");
            extensionTab.putStringData(new ExtensionStringData(description, isPlayerName, stringValue));
        }

        String componentValue = set.getString(ExtensionServerValueTable.COMPONENT_VALUE);
        if (componentValue != null) {
            extensionTab.putComponentData(new ExtensionComponentData(description, componentValue));
        }
    }

    private ExtensionDescription extractDescription(ResultSet set) throws SQLException {
        String name = set.getString("provider_name");
        String text = set.getString(ExtensionProviderTable.TEXT);
        String description = set.getString(ExtensionProviderTable.DESCRIPTION);
        int priority = set.getInt("provider_priority");

        String iconName = set.getString("provider_icon_name");
        Family family = Family.getByName(set.getString("provider_icon_family")).orElse(Family.SOLID);
        Color color = Color.getByName(set.getString("provider_icon_color")).orElse(Color.NONE);
        Icon icon = new Icon(family, iconName, color);

        return new ExtensionDescription(name, text, description, icon, priority);
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