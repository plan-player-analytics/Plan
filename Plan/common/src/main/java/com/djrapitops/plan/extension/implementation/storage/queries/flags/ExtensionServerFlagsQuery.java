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
package com.djrapitops.plan.extension.implementation.storage.queries.flags;

import com.djrapitops.plan.extension.ElementOrder;
import com.djrapitops.plan.extension.icon.Color;
import com.djrapitops.plan.extension.icon.Family;
import com.djrapitops.plan.extension.icon.Icon;
import com.djrapitops.plan.extension.implementation.TabInformation;
import com.djrapitops.plan.extension.implementation.results.ExtensionData;
import com.djrapitops.plan.storage.database.SQLDB;
import com.djrapitops.plan.storage.database.queries.Query;
import com.djrapitops.plan.storage.database.queries.QueryStatement;
import com.djrapitops.plan.storage.database.sql.tables.ServerTable;
import com.djrapitops.plan.storage.database.sql.tables.extension.*;
import com.djrapitops.plan.identification.ServerUUID;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

public class ExtensionServerFlagsQuery implements Query<Map<Integer, ExtensionData.Builder>> {

    private final ServerUUID serverUUID;

    public ExtensionServerFlagsQuery(ServerUUID serverUUID) {
        this.serverUUID = serverUUID;
    }

    @Override
    public Map<Integer, ExtensionData.Builder> executeQuery(SQLDB db) {
        String sql = SELECT +
                "pl." + ExtensionPluginTable.ID + " as plugin_id," +
                "p." + ExtensionProviderTable.ID + " as provider_id," +
                "p." + ExtensionProviderTable.PROVIDER_NAME + " as provider_name," +
                "p." + ExtensionProviderTable.TEXT + " as provider_text," +
                "i1." + ExtensionIconTable.ICON_NAME + " as provider_icon_name," +
                "i1." + ExtensionIconTable.FAMILY + " as provider_icon_family," +
                "i1." + ExtensionIconTable.COLOR + " as provider_icon_color," +
                "t." + ExtensionTabTable.TAB_NAME + " as tab_name," +
                "t." + ExtensionTabTable.TAB_PRIORITY + " as tab_priority," +
                "t." + ExtensionTabTable.ELEMENT_ORDER + " as element_order," +
                "i2." + ExtensionIconTable.ICON_NAME + " as tab_icon_name," +
                "i2." + ExtensionIconTable.FAMILY + " as tab_icon_family," +
                "i2." + ExtensionIconTable.COLOR + " as tab_icon_color," +
                "f." + ExtensionFlagTable.FLAG_NAME + " as flag_name," +
                "v." + ExtensionFlagServerValueTable.VALUE + " as flag_value" +
                FROM + ExtensionFlagTable.TABLE_NAME + " f" +
                INNER_JOIN + ExtensionProviderTable.TABLE_NAME + " p on p." + ExtensionProviderTable.ID + "=f." + ExtensionFlagTable.PROVIDER_ID +
                INNER_JOIN + ExtensionPluginTable.TABLE_NAME + " pl on pl." + ExtensionPluginTable.ID + "=p." + ExtensionProviderTable.PLUGIN_ID +
                LEFT_JOIN + ServerTable.TABLE_NAME + " s on s." + ServerTable.SERVER_UUID + "=pl." + ExtensionPluginTable.SERVER_UUID +
                LEFT_JOIN + ExtensionFlagServerValueTable.TABLE_NAME + " v on v." + ExtensionFlagServerValueTable.FLAG_ID + "=f." + ExtensionFlagTable.ID +
                " AND v." + ExtensionFlagServerValueTable.SERVER_ID + "=s." + ServerTable.ID +
                LEFT_JOIN + ExtensionTabTable.TABLE_NAME + " t on t." + ExtensionTabTable.ID + "=p." + ExtensionProviderTable.TAB_ID +
                LEFT_JOIN + ExtensionIconTable.TABLE_NAME + " i1 on i1." + ExtensionIconTable.ID + "=p." + ExtensionProviderTable.ICON_ID +
                LEFT_JOIN + ExtensionIconTable.TABLE_NAME + " i2 on i2." + ExtensionIconTable.ID + "=t." + ExtensionTabTable.ICON_ID +
                WHERE + "pl." + ExtensionPluginTable.SERVER_UUID + "=?" +
                 AND + "f." + ExtensionFlagTable.PARAMETER_TYPE + "=?" +
                ORDER_BY + "plugin_id ASC, provider_id ASC, flag_name ASC";

        return db.query(new QueryStatement<>(sql, 1000) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
                statement.setInt(2, TableType.SERVER.getType());
            }

            @Override
            public Map<Integer, ExtensionData.Builder> processResults(ResultSet set) throws SQLException {
                return extractFlags(set).toQueriedTabs().toExtensionDataByPluginID();
            }
        });
    }

    private QueriedFlags extractFlags(ResultSet set) throws SQLException {
        QueriedFlags flags = new QueriedFlags();
        while (set.next()) {
            int pluginID = set.getInt("plugin_id");
            int providerID = set.getInt("provider_id");
            if (!flags.contains(pluginID, providerID)) {
                flags.put(pluginID, providerID, new QueriedFlags.QueriedFlag(
                        set.getString("provider_name"), set.getString("provider_text"), extractProviderIcon(set), extractTabInformation(set)));
            }
            boolean flagValue = set.getBoolean("flag_value");
            boolean missing = set.wasNull();
            flags.addFlag(pluginID, providerID, set.getString("flag_name"), missing ? null : flagValue);
        }
        return flags;
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
        if (set.wasNull()) tabPriority = Optional.empty();
        Optional<ElementOrder[]> elementOrder = Optional.ofNullable(set.getString(ExtensionTabTable.ELEMENT_ORDER)).map(ElementOrder::deserialize);
        return new TabInformation(tabName, extractTabIcon(set), elementOrder.orElse(ElementOrder.values()), tabPriority.orElse(100));
    }

    private Icon extractTabIcon(ResultSet set) throws SQLException {
        Optional<String> iconName = Optional.ofNullable(set.getString("tab_icon_name"));
        if (iconName.isPresent()) {
            Family family = Family.getByName(set.getString("tab_icon_family")).orElse(Family.SOLID);
            Color color = Color.getByName(set.getString("tab_icon_color")).orElse(Color.NONE);
            return new Icon(family, iconName.get(), color);
        }
        return TabInformation.defaultIcon();
    }
}