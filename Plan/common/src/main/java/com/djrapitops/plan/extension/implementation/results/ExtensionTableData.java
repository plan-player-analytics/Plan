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

import com.djrapitops.plan.data.element.TableContainer;
import com.djrapitops.plan.extension.icon.Color;
import com.djrapitops.plan.extension.icon.Icon;
import com.djrapitops.plan.extension.table.Table;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Represents table data from a single TableProvider.
 *
 * @author Rsl1122
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

    public TableContainer getHtmlTable() {
        String[] columns = table.getColumns();
        Icon[] icons = table.getIcons();
        List<Object[]> rows = table.getRows();

        String[] header = buildHeader(columns, icons);

        TableContainer htmlTable = new TableContainer(header);
        if (rows.size() > 50) {
            htmlTable.useJqueryDataTables(); // Use a jQuery data table since there are a lot of rows.
        } else {
            String colorName = com.djrapitops.plan.delivery.rendering.html.icon.Color.getByName(tableColor.name()).orElse(com.djrapitops.plan.delivery.rendering.html.icon.Color.NONE).getHtmlClass()
                    .replace("col-", ""); // TODO after PluginData deprecation, change this thing
            htmlTable.setColor(colorName);
        }

        for (Object[] row : rows) {
            htmlTable.addRow(Arrays.stream(row).map(value -> value != null ? value.toString() : null).toArray(Serializable[]::new));
        }

        return htmlTable;
    }

    private String[] buildHeader(String[] columns, Icon[] icons) {
        ArrayList<String> header = new ArrayList<>();

        for (int i = 0; i < columns.length; i++) {
            String column = columns[i];
            if (column == null) {
                break;
            }
            header.add(com.djrapitops.plan.delivery.rendering.html.icon.Icon.fromExtensionIcon(icons[i]).toHtml() + ' ' + column);
        }

        return header.toArray(new String[0]);
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