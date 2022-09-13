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
package com.djrapitops.plan.delivery.rendering.html.structure;

import com.djrapitops.plan.delivery.domain.datatransfer.extension.TableCellDto;
import com.djrapitops.plan.delivery.rendering.html.icon.Color;
import com.djrapitops.plan.extension.table.Table;

import java.util.List;

/**
 * @deprecated Table html generation is to be done in frontend in the future.
 */
@Deprecated(since = "5.5")
public class HtmlTableWithColoredHeader implements HtmlTable {
    private final Header[] headers;
    private final Color headerColor;
    private final List<TableCellDto[]> rows;

    public HtmlTableWithColoredHeader(Header[] headers, Color headerColor, List<TableCellDto[]> rows) {
        this.headers = headers;
        this.headerColor = headerColor;
        this.rows = rows;
    }

    public HtmlTableWithColoredHeader(Table table, Color headerColor) {
        this(HtmlTable.mapToHeaders(table), headerColor, HtmlTable.mapToRows(table.getRows(), table.getTableColumnFormats()));
    }

    @Override
    public String toHtml() {
        return "<div class=\"scrollbar\">" +
                "<table class=\"table table-striped\">" +
                buildTableHeader() +
                buildTableBody() +
                "</table>" +
                "</div>";
    }

    private String buildTableHeader() {
        StringBuilder builtHeader = new StringBuilder("<thead class=\"" + headerColor.getBackgroundColorClass() + "\"><tr>");
        for (Header header : headers) {
            builtHeader.append("<th>")
                    .append(header.getIcon().toHtml())
                    .append(' ')
                    .append(header.getText())
                    .append("</th>");
        }
        builtHeader.append("</tr></thead>");
        return builtHeader.toString();
    }

    private String buildTableBody() {
        StringBuilder builtBody = new StringBuilder();
        builtBody.append("<tbody>");
        if (rows.isEmpty()) {
            appendRow(builtBody, new TableCellDto("No Data"));
        }
        for (TableCellDto[] row : rows) {
            appendRow(builtBody, row);
        }
        return builtBody.append("</tbody>").toString();
    }

    private void appendRow(StringBuilder builtBody, TableCellDto... row) {
        int headerLength = row.length - 1;
        builtBody.append("<tr>");
        for (int i = 0; i < headers.length; i++) {
            try {
                if (i > headerLength) {
                    builtBody.append("<td>-");
                } else {
                    builtBody.append("<td>");
                    TableCellDto cell = row[i];
                    builtBody.append(cell != null ? cell.getValue() : '-');
                }
                builtBody.append("</td>");
            } catch (ClassCastException | ArrayIndexOutOfBoundsException e) {
                throw new IllegalStateException("Invalid formatter given at index " + i + ": " + e.getMessage(), e);
            }
        }
        builtBody.append("</tr>");
    }

}
