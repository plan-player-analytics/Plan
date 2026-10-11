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

public record TableDto(
        List<String> columns,
        List<IconDto> icons,
        List<List<TableCellDto>> rows
) {

    public TableDto(Table table) {
        this(
                Arrays.stream(table.getColumns())
                        .filter(Objects::nonNull)
                        .toList(),
                Arrays.stream(table.getIcons())
                        .filter(Objects::nonNull)
                        .map(IconDto::new)
                        .toList(),
                mapToRows(table.getRows(), table.getTableColumnFormats(), Arrays.stream(table.getColumns())
                        .filter(Objects::nonNull)
                        .toList())
        );
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

    private static List<List<TableCellDto>> mapToRows(List<Object[]> rows, TableColumnFormat[] tableColumnFormats, List<String> columns) {
        return mapToRows(rows, tableColumnFormats).stream()
                .map(row -> constructRow(columns, row))
                .collect(Collectors.toList());
    }

    private static List<TableCellDto> constructRow(List<String> columns, TableCellDto[] row) {
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
}
