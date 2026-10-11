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
package com.djrapitops.plan.extension.implementation.storage.queries.statistics;

import com.djrapitops.plan.extension.ElementOrder;
import com.djrapitops.plan.extension.icon.Color;
import com.djrapitops.plan.extension.icon.Family;
import com.djrapitops.plan.extension.icon.Icon;
import com.djrapitops.plan.extension.implementation.TabInformation;
import com.djrapitops.plan.extension.implementation.results.ExtensionData;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.SQLDB;
import com.djrapitops.plan.storage.database.queries.Query;
import com.djrapitops.plan.storage.database.queries.QueryStatement;
import com.djrapitops.plan.storage.database.sql.tables.ServerTable;
import com.djrapitops.plan.storage.database.sql.tables.extension.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Query to get server statistics from plan_extension_server_statistic_value.
 *
 * @author AuroraLS3
 */
public class ExtensionServerStatisticsQuery implements Query<Map<Integer, ExtensionData.Builder>> {

    private final ServerUUID serverUUID;

    public ExtensionServerStatisticsQuery(ServerUUID serverUUID) {
        this.serverUUID = serverUUID;
    }

    @Override
    public Map<Integer, ExtensionData.Builder> executeQuery(SQLDB db) {
        String sql = SELECT +
                "p1." + ExtensionProviderTable.PLUGIN_ID + " as plugin_id," +
                "p1." + ExtensionProviderTable.ID + " as provider_id," +
                "p1." + ExtensionProviderTable.PROVIDER_NAME + " as provider_name," +
                "i1." + ExtensionIconTable.ICON_NAME + " as provider_icon_name," +
                "i1." + ExtensionIconTable.FAMILY + " as provider_icon_family," +
                "i1." + ExtensionIconTable.COLOR + " as provider_icon_color," +
                "t1." + ExtensionTabTable.TAB_NAME + " as tab_name," +
                "t1." + ExtensionTabTable.TAB_PRIORITY + " as tab_priority," +
                "t1." + ExtensionTabTable.ELEMENT_ORDER + " as element_order," +
                "i2." + ExtensionIconTable.ICON_NAME + " as tab_icon_name," +
                "i2." + ExtensionIconTable.FAMILY + " as tab_icon_family," +
                "i2." + ExtensionIconTable.COLOR + " as tab_icon_color," +
                "s1." + ExtensionStatisticTable.STATISTIC_NAME + " as statistic_name," +
                "v1." + ExtensionStatisticServerValueTable.VALUE + " as statistic_value" +
                FROM + ExtensionStatisticServerValueTable.TABLE_NAME + " v1" +
                INNER_JOIN + ServerTable.TABLE_NAME + " s1_server on s1_server." + ServerTable.ID + "=v1." + ExtensionStatisticServerValueTable.SERVER_ID +
                INNER_JOIN + ExtensionStatisticTable.TABLE_NAME + " s1 on s1." + ExtensionStatisticTable.ID + "=v1." + ExtensionStatisticServerValueTable.STATISTIC_ID +
                INNER_JOIN + ExtensionProviderTable.TABLE_NAME + " p1 on p1." + ExtensionProviderTable.ID + "=s1." + ExtensionStatisticTable.PROVIDER_ID +
                INNER_JOIN + ExtensionPluginTable.TABLE_NAME + " e1 on e1." + ExtensionPluginTable.ID + "=p1." + ExtensionProviderTable.PLUGIN_ID +
                LEFT_JOIN + ExtensionTabTable.TABLE_NAME + " t1 on t1." + ExtensionTabTable.ID + "=p1." + ExtensionProviderTable.TAB_ID +
                LEFT_JOIN + ExtensionIconTable.TABLE_NAME + " i1 on i1." + ExtensionIconTable.ID + "=p1." + ExtensionProviderTable.ICON_ID +
                LEFT_JOIN + ExtensionIconTable.TABLE_NAME + " i2 on i2." + ExtensionIconTable.ID + "=t1." + ExtensionTabTable.ICON_ID +
                WHERE + "s1_server." + ServerTable.SERVER_UUID + "=?" +
                AND + "e1." + ExtensionPluginTable.SERVER_UUID + "=?" +
                ORDER_BY + "provider_id ASC, statistic_name ASC";

        return db.query(new QueryStatement<>(sql, 1000) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
                statement.setString(2, serverUUID.toString());
            }

            @Override
            public Map<Integer, ExtensionData.Builder> processResults(ResultSet set) throws SQLException {
                return extractStatistics(set).toQueriedTabs().toExtensionDataByPluginID();
            }
        });
    }

    private QueriedStatistics extractStatistics(ResultSet set) throws SQLException {
        QueriedStatistics statistics = new QueriedStatistics();

        while (set.next()) {
            int pluginID = set.getInt("plugin_id");
            int providerID = set.getInt("provider_id");

            if (!statistics.contains(pluginID, providerID)) {
                String providerName = set.getString("provider_name");
                Icon icon = extractProviderIcon(set);
                TabInformation tabInformation = extractTabInformation(set);
                statistics.put(pluginID, providerID, new QueriedStatistics.QueriedStatistic(providerName, icon, tabInformation));
            }
            statistics.addStatistic(pluginID, providerID, set.getString("statistic_name"), set.getLong("statistic_value"));
        }
        return statistics;
    }

    private Icon extractProviderIcon(ResultSet set) throws SQLException {
        String iconName = set.getString("provider_icon_name");
        Family family = Family.getByName(set.getString("provider_icon_family")).orElse(Family.SOLID);
        Color color = Color.getByName(set.getString("provider_icon_color")).orElse(Color.NONE);
        return new Icon(family, iconName, color);
    }

    private TabInformation extractTabInformation(ResultSet set) throws SQLException {
        String tabName = Optional.ofNullable(set.getString("tab_name")).orElse("");
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
