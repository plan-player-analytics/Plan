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

import com.djrapitops.plan.extension.implementation.TabInformation;
import com.djrapitops.plan.extension.implementation.results.ExtensionData;
import com.djrapitops.plan.extension.implementation.results.ExtensionTabData;
import com.djrapitops.plan.utilities.java.Maps;
import com.djrapitops.plan.utilities.java.ThrowingSupplier;

import java.util.HashMap;
import java.util.Map;

/**
 * Query utility for extracting Tabs.
 *
 * @author AuroraLS3
 */
public class QueriedTabData {

    private final Map<Integer, Map<String, ExtensionTabData.Builder>> byPluginID;

    public QueriedTabData() {
        byPluginID = new HashMap<>();
    }

    public <K extends Throwable> ExtensionTabData.Builder getTab(int pluginID, String tabName, ThrowingSupplier<TabInformation, K> newDefault) throws K {
        Map<String, ExtensionTabData.Builder> byTabName = byPluginID.computeIfAbsent(pluginID, Maps::create);

        ExtensionTabData.Builder tab = byTabName.get(tabName);
        if (tab == null) {
            tab = new ExtensionTabData.Builder(newDefault.get());
        }

        byTabName.put(tabName, tab);
        return tab;
    }

    public Map<Integer, ExtensionData.Builder> toExtensionDataByPluginID() {
        Map<Integer, ExtensionData.Builder> dataByPluginID = new HashMap<>();
        for (Map.Entry<Integer, Map<String, ExtensionTabData.Builder>> entry : byPluginID.entrySet()) {
            Integer pluginID = entry.getKey();

            ExtensionData.Builder data = dataByPluginID.get(pluginID);
            if (data == null) {
                data = new ExtensionData.Builder(pluginID);
            }

            for (ExtensionTabData.Builder tabData : entry.getValue().values()) {
                data.addTab(tabData.build());
            }
            dataByPluginID.put(pluginID, data);
        }
        return dataByPluginID;
    }
}