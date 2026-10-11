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
package com.djrapitops.plan.extension.implementation.results;

import com.djrapitops.plan.extension.icon.Color;
import com.djrapitops.plan.extension.icon.Icon;
import com.djrapitops.plan.extension.statistic.Statistics;

import java.util.Objects;

/**
 * Represents statistics data from a single {@link com.djrapitops.plan.extension.annotation.StatisticsProvider}.
 *
 * @author AuroraLS3
 */
public class ExtensionStatisticsData implements Comparable<ExtensionStatisticsData> {

    private final String providerName;
    private final Statistics statistics;
    private final Icon icon;

    public ExtensionStatisticsData(String providerName, Statistics statistics, Icon icon) {
        this.providerName = providerName;
        this.statistics = statistics;
        this.icon = icon;
    }

    public String getProviderName() {
        return providerName;
    }

    public Statistics getStatistics() {
        return statistics;
    }

    public Icon getIcon() {
        return icon;
    }

    public Color getTableColor() {
        return icon.getColor();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExtensionStatisticsData that)) return false;
        return Objects.equals(providerName, that.providerName) &&
                Objects.equals(icon, that.icon);
    }

    @Override
    public int hashCode() {
        return Objects.hash(providerName, icon);
    }

    @Override
    public String toString() {
        return "ExtensionStatisticsData{" +
                "providerName='" + providerName + '\'' +
                ", icon=" + icon +
                '}';
    }

    @Override
    public int compareTo(ExtensionStatisticsData other) {
        return String.CASE_INSENSITIVE_ORDER.compare(providerName, other.providerName);
    }

    public boolean isWideTable() {
        return false;
    }
}