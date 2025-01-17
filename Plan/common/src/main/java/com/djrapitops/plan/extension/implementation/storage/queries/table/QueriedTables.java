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
package com.djrapitops.plan.extension.implementation.storage.queries.table;

import com.djrapitops.plan.extension.ElementOrder;
import com.djrapitops.plan.extension.icon.Color;
import com.djrapitops.plan.extension.icon.Icon;
import com.djrapitops.plan.extension.implementation.TabInformation;
import com.djrapitops.plan.extension.implementation.results.ExtensionTabData;
import com.djrapitops.plan.extension.implementation.results.ExtensionTableData;
import com.djrapitops.plan.extension.implementation.storage.queries.QueriedTabData;
import com.djrapitops.plan.extension.table.Table;
import com.djrapitops.plan.extension.table.TableAccessor;
import com.djrapitops.plan.utilities.java.Maps;

import java.util.HashMap;
import java.util.Map;

/**
 * Query utility for extracting Tables.
 *
 * @author AuroraLS3
 */
public class QueriedTables {

    // Map: <Plugin ID - <Table ID - Table.Factory>>
    private final Map<Integer, Map<Integer, Table.Factory>> byPluginID;

    public QueriedTables() {
        byPluginID = new HashMap<>();
    }

    public boolean contains(int pluginID, int tableID) {
        Map<Integer, Table.Factory> byTableID = byPluginID.get(pluginID);
        return byTableID != null && byTableID.containsKey(tableID);
    }

    public void put(int pluginID, int tableID, Table.Factory table) {
        Map<Integer, Table.Factory> byTableID = byPluginID.computeIfAbsent(pluginID, Maps::create);
        byTableID.put(tableID, table);
    }

    public void addRow(int pluginID, int tableID, Object... row) {
        if (row.length <= 0) return;

        Map<Integer, Table.Factory> byTableID = byPluginID.get(pluginID);
        if (byTableID == null) return;

        Table.Factory table = byTableID.get(tableID);
        if (table == null) return;

        table.addRow(row);
    }

    public QueriedTabData toQueriedTabs() {
        QueriedTabData tabData = new QueriedTabData();

        for (Map.Entry<Integer, Map<Integer, Table.Factory>> entry : byPluginID.entrySet()) {
            Integer pluginID = entry.getKey();

            for (Table.Factory table : entry.getValue().values()) {
                // Extra Table information
                String tableName = TableAccessor.getTableName(table);
                Color tableColor = TableAccessor.getColor(table);

                // Extra tab information
                String tabName = TableAccessor.getTabName(table);

                ExtensionTabData.Builder tab = tabData.getTab(pluginID, tabName, () -> extractTabInformation(table));
                tab.putTableData(new ExtensionTableData(
                        tableName, table.build(), tableColor
                ));
            }
        }

        return tabData;
    }

    private TabInformation extractTabInformation(Table.Factory table) {
        String tabName = TableAccessor.getTabName(table);
        int tabPriority = TableAccessor.getTabPriority(table);
        ElementOrder[] tabOrder = TableAccessor.getTabOrder(table);
        Icon tabIcon = TableAccessor.getTabIcon(table);

        return new TabInformation(tabName, tabIcon, tabOrder, tabPriority);
    }

}