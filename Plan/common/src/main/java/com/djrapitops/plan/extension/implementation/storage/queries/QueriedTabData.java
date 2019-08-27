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
import com.djrapitops.plan.extension.implementation.results.ExtensionTabData;
import com.djrapitops.plan.extension.implementation.results.player.ExtensionPlayerData;
import com.djrapitops.plan.extension.implementation.results.server.ExtensionServerData;
import com.djrapitops.plan.utilities.java.ThrowingSupplier;

import java.util.HashMap;
import java.util.Map;

/**
 * Query utility for extracting Tabs.
 *
 * @author Rsl1122
 */
public class QueriedTabData {

    private final Map<Integer, Map<String, ExtensionTabData.Factory>> byPluginID;

    public QueriedTabData() {
        byPluginID = new HashMap<>();
    }

    public <K extends Throwable> ExtensionTabData.Factory getTab(int pluginID, String tabName, ThrowingSupplier<TabInformation, K> newDefault) throws K {
        Map<String, ExtensionTabData.Factory> byTabName = byPluginID.getOrDefault(pluginID, new HashMap<>());

        ExtensionTabData.Factory tab = byTabName.get(tabName);
        if (tab == null) {
            tab = new ExtensionTabData.Factory(newDefault.get());
        }

        byTabName.put(tabName, tab);
        byPluginID.put(pluginID, byTabName);
        return tab;
    }

    public Map<Integer, ExtensionServerData.Factory> toServerDataByPluginID() {
        Map<Integer, ExtensionServerData.Factory> dataByPluginID = new HashMap<>();
        for (Map.Entry<Integer, Map<String, ExtensionTabData.Factory>> entry : byPluginID.entrySet()) {
            Integer pluginID = entry.getKey();

            ExtensionServerData.Factory data = dataByPluginID.get(pluginID);
            if (data == null) {
                data = new ExtensionServerData.Factory(pluginID);
            }

            for (ExtensionTabData.Factory tabData : entry.getValue().values()) {
                data.addTab(tabData.build());
            }
            dataByPluginID.put(pluginID, data);
        }
        return dataByPluginID;
    }

    public Map<Integer, ExtensionPlayerData.Factory> toPlayerDataByPluginID() {
        Map<Integer, ExtensionPlayerData.Factory> dataByPluginID = new HashMap<>();
        for (Map.Entry<Integer, Map<String, ExtensionTabData.Factory>> entry : byPluginID.entrySet()) {
            Integer pluginID = entry.getKey();

            ExtensionPlayerData.Factory data = dataByPluginID.get(pluginID);
            if (data == null) {
                data = new ExtensionPlayerData.Factory(pluginID);
            }

            for (ExtensionTabData.Factory tabData : entry.getValue().values()) {
                data.addTab(tabData.build());
            }
            dataByPluginID.put(pluginID, data);
        }
        return dataByPluginID;
    }
}