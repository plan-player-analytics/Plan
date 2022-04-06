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
package com.djrapitops.plan.delivery.domain.datatransfer.extension;

import com.djrapitops.plan.delivery.rendering.html.icon.Color;
import com.djrapitops.plan.extension.implementation.results.ExtensionTableData;

import java.util.Objects;

public class ExtensionTableDataDto {

    private final String tableName;
    private final TableDto table;
    private final String tableColor;
    private final String tableColorClass;
    private final boolean wide;

    public ExtensionTableDataDto(ExtensionTableData extensionTableData) {
        tableName = extensionTableData.getProviderName();
        tableColor = extensionTableData.getTableColor().name();
        tableColorClass = Color.getByName(extensionTableData.getTableColor().name()).orElse(Color.NONE)
                .getBackgroundColorClass();
        table = new TableDto(extensionTableData.getTable());

        wide = extensionTableData.isWideTable();
    }

    public String getTableName() {
        return tableName;
    }

    public TableDto getTable() {
        return table;
    }

    public String getTableColor() {
        return tableColor;
    }

    public String getTableColorClass() {
        return tableColorClass;
    }

    public boolean isWide() {
        return wide;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExtensionTableDataDto that = (ExtensionTableDataDto) o;
        return isWide() == that.isWide() && Objects.equals(getTableName(), that.getTableName()) && Objects.equals(getTable(), that.getTable()) && Objects.equals(getTableColor(), that.getTableColor()) && Objects.equals(getTableColorClass(), that.getTableColorClass());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTableName(), getTable(), getTableColor(), getTableColorClass(), isWide());
    }

    @Override
    public String toString() {
        return "ExtensionTableDataDto{" +
                "tableName='" + tableName + '\'' +
                ", table=" + table +
                ", tableColor='" + tableColor + '\'' +
                ", tableColorClass='" + tableColorClass + '\'' +
                ", wide=" + wide +
                '}';
    }
}
