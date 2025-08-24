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
import com.djrapitops.plan.extension.table.Table;

import java.util.Objects;

/**
 * Represents table data from a single TableProvider.
 *
 * @author AuroraLS3
 */
public class ExtensionTableData implements Comparable<ExtensionTableData> {

    private final String providerName;
    private final Table table;
    private final Color tableColor;

    public ExtensionTableData(String providerName, Table table, Color tableColor) {
        this.providerName = providerName;
        this.table = table;
        this.tableColor = tableColor;
    }

    public String getProviderName() {
        return providerName;
    }

    public Table getTable() {
        return table;
    }

    public Color getTableColor() {
        return tableColor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExtensionTableData)) return false;
        ExtensionTableData that = (ExtensionTableData) o;
        return providerName.equals(that.providerName) &&
                tableColor == that.tableColor;
    }

    @Override
    public int hashCode() {
        return Objects.hash(providerName, tableColor);
    }

    @Override
    public String toString() {
        return "ExtensionTableData{" +
                "providerName='" + providerName + '\'' +
                '}';
    }

    @Override
    public int compareTo(ExtensionTableData other) {
        return String.CASE_INSENSITIVE_ORDER.compare(providerName, other.providerName);
    }

    public boolean isWideTable() {
        return table.getMaxColumnSize() >= 3;
    }
}