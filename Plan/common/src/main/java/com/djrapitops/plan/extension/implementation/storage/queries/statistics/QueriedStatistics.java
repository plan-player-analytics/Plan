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

import com.djrapitops.plan.extension.icon.Color;
import com.djrapitops.plan.extension.icon.Icon;
import com.djrapitops.plan.extension.implementation.TabInformation;
import com.djrapitops.plan.extension.implementation.results.ExtensionStatisticsData;
import com.djrapitops.plan.extension.implementation.results.ExtensionTabData;
import com.djrapitops.plan.extension.implementation.storage.queries.QueriedTabData;
import com.djrapitops.plan.extension.statistic.Statistics;
import com.djrapitops.plan.utilities.java.Maps;

import java.util.HashMap;
import java.util.Map;

/**
 * Query utility for extracting Statistics.
 *
 * @author AuroraLS3
 */
public class QueriedStatistics {

    // Map: <Plugin ID - <Provider ID - QueriedStatistic>>
    private final Map<Integer, Map<Integer, QueriedStatistic>> byPluginID;

    public QueriedStatistics() {
        byPluginID = new HashMap<>();
    }

    public boolean contains(int pluginID, int providerID) {
        Map<Integer, QueriedStatistic> byProviderID = byPluginID.get(pluginID);
        return byProviderID != null && byProviderID.containsKey(providerID);
    }

    public void put(int pluginID, int providerID, QueriedStatistic statistic) {
        Map<Integer, QueriedStatistic> byProviderID = byPluginID.computeIfAbsent(pluginID, Maps::create);
        byProviderID.put(providerID, statistic);
    }

    public void addStatistic(int pluginID, int providerID, String statisticName, Long statisticValue) {
        Map<Integer, QueriedStatistic> byProviderID = byPluginID.get(pluginID);
        if (byProviderID == null) return;

        QueriedStatistic statistic = byProviderID.get(providerID);
        if (statistic == null) return;

        statistic.getStatistics().put(statisticName, statisticValue);
    }

    public QueriedTabData toQueriedTabs() {
        QueriedTabData tabData = new QueriedTabData();

        for (Map.Entry<Integer, Map<Integer, QueriedStatistic>> entry : byPluginID.entrySet()) {
            Integer pluginID = entry.getKey();

            for (QueriedStatistic stat : entry.getValue().values()) {
                if (stat.getStatistics().isEmpty()) continue;

                String tabName = stat.getTabInformation().getTabName();

                ExtensionTabData.Builder tab = tabData.getTab(pluginID, tabName, stat::getTabInformation);
                tab.putStatisticsData(new ExtensionStatisticsData(
                        stat.getProviderName(),
                        stat.getStatistics(),
                        stat.getIcon()
                ));
            }
        }

        return tabData;
    }

    public static class QueriedStatistic {
        private final String providerName;
        private final Icon icon;
        private final TabInformation tabInformation;
        private final Statistics statistics;

        public QueriedStatistic(String providerName, Icon icon, TabInformation tabInformation) {
            this.providerName = providerName;
            this.icon = icon;
            this.tabInformation = tabInformation;
            this.statistics = new Statistics();
        }

        public String getProviderName() {
            return providerName;
        }

        public Icon getIcon() {
            return icon;
        }

        public Color getTableColor() {
            return icon.getColor();
        }

        public TabInformation getTabInformation() {
            return tabInformation;
        }

        public Statistics getStatistics() {
            return statistics;
        }
    }
}
