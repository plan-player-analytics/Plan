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
import com.djrapitops.plan.extension.icon.Color;
import com.djrapitops.plan.extension.icon.Family;
import com.djrapitops.plan.extension.icon.Icon;
import com.djrapitops.plan.extension.implementation.results.ExtensionData;
import com.djrapitops.plan.extension.table.Table;
import com.djrapitops.plan.extension.table.TableAccessor;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.SQLDB;
import com.djrapitops.plan.storage.database.queries.Query;
import com.djrapitops.plan.storage.database.queries.QueryStatement;
import com.djrapitops.plan.storage.database.sql.tables.extension.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Query for selecting Tables out of groups for players.
 * <p>
 * Returns Map: PluginID - {@link ExtensionData.Builder}.
 * <p>
 * How it's done:
 * 1. Query count for each group name
 * 2. Join with provider information
 * 3. Map to ExtensionData objects, one per Plugin ID
 *
 * @author AuroraLS3
 */
public class ExtensionAggregateGroupsQuery implements Query<Map<Integer, ExtensionData.Builder>> {

    private final ServerUUID serverUUID;

    public ExtensionAggregateGroupsQuery(ServerUUID serverUUID) {
        this.serverUUID = serverUUID;
    }

    @Override
    public Map<Integer, ExtensionData.Builder> executeQuery(SQLDB db) {
        String selectGroupCounts = SELECT +
                ExtensionGroupsTable.PROVIDER_ID + ',' +
                ExtensionGroupsTable.GROUP_NAME + ',' +
                "COUNT(1) as count" +
                FROM + ExtensionGroupsTable.TABLE_NAME +
                GROUP_BY + ExtensionGroupsTable.GROUP_NAME + ',' + ExtensionGroupsTable.PROVIDER_ID;

        String sql = SELECT +
                "b1." + ExtensionGroupsTable.GROUP_NAME + " as group_name," +
                "b1.count as count," +
                "p1." + ExtensionProviderTable.ID + " as table_id," +
                "p1." + ExtensionProviderTable.PLUGIN_ID + " as plugin_id," +
                "p1." + ExtensionProviderTable.PROVIDER_NAME + " as table_name," +
                "p1." + ExtensionProviderTable.TEXT + " as col_1_name," +
                "t1." + ExtensionTabTable.TAB_NAME + " as tab_name," +
                "t1." + ExtensionTabTable.TAB_PRIORITY + " as tab_priority," +
                "t1." + ExtensionTabTable.ELEMENT_ORDER + " as element_order," +
                "i1." + ExtensionIconTable.ICON_NAME + " as i1_name," +
                "i1." + ExtensionIconTable.FAMILY + " as i1_family," +
                "i1." + ExtensionIconTable.COLOR + " as table_color," +
                "i2." + ExtensionIconTable.ICON_NAME + " as tab_icon_name," +
                "i2." + ExtensionIconTable.FAMILY + " as tab_icon_family," +
                "i2." + ExtensionIconTable.COLOR + " as tab_icon_color" +
                FROM + '(' + selectGroupCounts + ") b1" +
                INNER_JOIN + ExtensionProviderTable.TABLE_NAME + " p1 on p1." + ExtensionProviderTable.ID + "=b1." + ExtensionGroupsTable.PROVIDER_ID +
                INNER_JOIN + ExtensionPluginTable.TABLE_NAME + " e1 on p1." + ExtensionProviderTable.PLUGIN_ID + "=e1." + ExtensionPluginTable.ID +
                LEFT_JOIN + ExtensionTabTable.TABLE_NAME + " t1 on t1." + ExtensionTabTable.ID + "=p1." + ExtensionProviderTable.TAB_ID +
                LEFT_JOIN + ExtensionIconTable.TABLE_NAME + " i1 on i1." + ExtensionIconTable.ID + "=p1." + ExtensionProviderTable.ICON_ID +
                LEFT_JOIN + ExtensionIconTable.TABLE_NAME + " i2 on i2." + ExtensionIconTable.ID + "=t1." + ExtensionTabTable.ICON_ID +
                WHERE + ExtensionPluginTable.SERVER_UUID + "=?" +
                AND + "p1." + ExtensionProviderTable.HIDDEN + "=?" +
                ORDER_BY + "table_id ASC, group_name ASC";

        return db.query(new QueryStatement<>(sql, 1000) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
                statement.setBoolean(2, false); // Don't select hidden values
            }

            @Override
            public Map<Integer, ExtensionData.Builder> processResults(ResultSet set) throws SQLException {
                return extractTables(set).toQueriedTabs().toExtensionDataByPluginID();
            }
        });
    }

    private QueriedTables extractTables(ResultSet set) throws SQLException {
        QueriedTables tables = new QueriedTables();

        while (set.next()) {
            int pluginID = set.getInt(ExtensionProviderTable.PLUGIN_ID);
            int tableID = set.getInt(ExtensionPlayerTableValueTable.TABLE_ID);

            if (!tables.contains(pluginID, tableID)) {
                tables.put(pluginID, tableID, extractTable(set));
            }
            tables.addRow(pluginID, tableID, set.getString("group_name"), set.getInt("count"));
        }
        return tables;
    }

    private Table.Factory extractTable(ResultSet set) throws SQLException {
        Table.Factory table = Table.builder();

        extractColumns(set, table);

        TableAccessor.setColor(table, Color.getByName(set.getString("table_color")).orElse(Color.NONE));
        TableAccessor.setTableName(table, set.getString("table_name"));
        TableAccessor.setTabName(table, Optional.ofNullable(set.getString("tab_name")).orElse(""));
        TableAccessor.setTabPriority(table, Optional.of(set.getInt("tab_priority")).orElse(100));
        TableAccessor.setTabOrder(table, Optional.ofNullable(set.getString(ExtensionTabTable.ELEMENT_ORDER)).map(ElementOrder::deserialize).orElse(ElementOrder.values()));
        TableAccessor.setTabIcon(table, extractIcon(set, "tab_icon"));
        return table;
    }

    private void extractColumns(ResultSet set, Table.Factory table) throws SQLException {
        String col1 = set.getString(ExtensionTableProviderTable.COL_1);
        if (col1 != null) {
            table.columnOne(col1, extractIcon(set, "i1"));
        }

        table.columnTwo("Players", Icon.called("user").build());
    }

    private Icon extractIcon(ResultSet set, String iconColumnName) throws SQLException {
        String iconName = set.getString(iconColumnName + "_name");
        if (iconName == null) {
            return null;
        }
        return new Icon(
                Family.getByName(set.getString(iconColumnName + "_family")).orElse(Family.SOLID),
                iconName,
                Color.NONE
        );
    }
}