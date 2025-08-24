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

import com.djrapitops.plan.extension.table.Table;
import com.djrapitops.plan.extension.table.TableColumnFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class TableDto {

    private final List<String> columns;
    private final List<IconDto> icons;

    private final List<List<TableCellDto>> rows;

    public TableDto(Table table) {
        columns = Arrays.stream(table.getColumns())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        icons = Arrays.stream(table.getIcons())
                .filter(Objects::nonNull)
                .map(IconDto::new)
                .collect(Collectors.toList());

        rows = mapToRows(table.getRows(), table.getTableColumnFormats()).stream()
                .map(row -> constructRow(columns, row))
                .collect(Collectors.toList());
    }

    public static List<TableCellDto[]> mapToRows(List<Object[]> rows, TableColumnFormat[] tableColumnFormats) {
        return rows.stream()
                .map(row -> {
                    List<TableCellDto> mapped = new ArrayList<>(row.length);
                    for (int i = 0; i < row.length; i++) {
                        Object value = row[i];
                        if (value == null) {
                            mapped.add(null);
                        } else {
                            TableColumnFormat format = tableColumnFormats[i];
                            mapped.add(new TableCellDto(value, format));
                        }
                    }
                    return mapped.toArray(new TableCellDto[0]);
                })
                .collect(Collectors.toList());
    }

    private List<TableCellDto> constructRow(List<String> columns, TableCellDto[] row) {
        List<TableCellDto> constructedRow = new ArrayList<>();

        int headerLength = row.length - 1;
        int columnCount = columns.size();
        for (int i = 0; i < columnCount; i++) {
            if (i > headerLength) {
                constructedRow.add(new TableCellDto("-", null));
            } else {
                TableCellDto cell = row[i];
                constructedRow.add(cell != null ? cell : new TableCellDto("-", null));
            }
        }
        return constructedRow;
    }

    public List<String> getColumns() {
        return columns;
    }

    public List<IconDto> getIcons() {
        return icons;
    }

    public List<List<TableCellDto>> getRows() {
        return rows;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TableDto tableDto = (TableDto) o;
        return Objects.equals(getColumns(), tableDto.getColumns()) && Objects.equals(getIcons(), tableDto.getIcons()) && Objects.equals(getRows(), tableDto.getRows());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getColumns(), getIcons(), getRows());
    }

    @Override
    public String toString() {
        return "TableDto{" +
                "columns=" + columns +
                ", icons=" + icons +
                ", rows=" + rows +
                '}';
    }
}
