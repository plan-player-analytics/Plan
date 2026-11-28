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

import com.djrapitops.plan.extension.icon.Icon;
import com.djrapitops.plan.extension.implementation.TabInformation;
import com.djrapitops.plan.extension.implementation.results.ExtensionFlagsData;
import com.djrapitops.plan.extension.implementation.results.ExtensionTabData;
import com.djrapitops.plan.extension.implementation.storage.queries.QueriedTabData;
import com.djrapitops.plan.extension.statistic.Flags;
import com.djrapitops.plan.utilities.java.Maps;

import java.util.HashMap;
import java.util.Map;

public class QueriedFlags {

    private final Map<Integer, Map<Integer, QueriedFlag>> byPluginID = new HashMap<>();

    public boolean contains(int pluginID, int providerID) {
        Map<Integer, QueriedFlag> byProviderID = byPluginID.get(pluginID);
        return byProviderID != null && byProviderID.containsKey(providerID);
    }

    public void put(int pluginID, int providerID, QueriedFlag flag) {
        byPluginID.computeIfAbsent(pluginID, Maps::create).put(providerID, flag);
    }

    public void addFlag(int pluginID, int providerID, String flagName, Boolean flagValue) {
        Map<Integer, QueriedFlag> byProviderID = byPluginID.get(pluginID);
        if (byProviderID == null) return;

        QueriedFlag flag = byProviderID.get(providerID);
        if (flag == null) return;

        flag.getFlags().put(flagName, flagValue);
    }

    public QueriedTabData toQueriedTabs() {
        QueriedTabData tabData = new QueriedTabData();
        for (Map.Entry<Integer, Map<Integer, QueriedFlag>> entry : byPluginID.entrySet()) {
            int pluginID = entry.getKey();
            for (QueriedFlag flag : entry.getValue().values()) {
                if (flag.getFlags().isEmpty()) continue;
                String tabName = flag.getTabInformation().getTabName();
                ExtensionTabData.Builder tab = tabData.getTab(pluginID, tabName, flag::getTabInformation);
                tab.putFlagsData(new ExtensionFlagsData(flag.getProviderName(), flag.getProviderText(), flag.getFlags(), flag.getIcon()));
            }
        }
        return tabData;
    }

    public static class QueriedFlag {
        private final String providerName;
        private final String providerText;
        private final Icon icon;
        private final TabInformation tabInformation;
        private final Flags flags = Flags.create();

        public QueriedFlag(String providerName, String providerText, Icon icon, TabInformation tabInformation) {
            this.providerName = providerName;
            this.providerText = providerText;
            this.icon = icon;
            this.tabInformation = tabInformation;
        }

        public String getProviderName() {
            return providerName;
        }

        public String getProviderText() {
            return providerText;
        }

        public Icon getIcon() {
            return icon;
        }

        public TabInformation getTabInformation() {
            return tabInformation;
        }

        public Flags getFlags() {
            return flags;
        }
    }
}