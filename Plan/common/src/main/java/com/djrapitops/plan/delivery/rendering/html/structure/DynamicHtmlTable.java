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

import com.djrapitops.plan.extension.table.Table;
import org.apache.commons.lang3.math.NumberUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

@Deprecated
public class DynamicHtmlTable implements HtmlTable {
    private final Header[] headers;
    private final List<Object[]> rows;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d yyyy, HH:mm");

    public DynamicHtmlTable(Header[] headers, List<Object[]> rows) {
        this.headers = headers;
        this.rows = rows;
    }

    public DynamicHtmlTable(Table table) {
        this(HtmlTable.mapToHeaders(table), table.getRows());
    }

    @Override
    public String toHtml() {
        return "<table class=\"table table-bordered table-striped table-hover player-plugin-table\" style=\"width: 100%\">" +
                buildTableHeader() +
                buildTableBody() +
                "</table>";
    }

    private String buildTableHeader() {
        StringBuilder builtHeader = new StringBuilder("<thead><tr>");
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
            appendRow(builtBody, "No Data");
        }
        for (Object[] row : rows) {
            appendRow(builtBody, row);
        }
        return builtBody.append("</tbody>").toString();
    }

    private void appendRow(StringBuilder builtBody, Object... row) {
        int headerLength = row.length - 1;
        builtBody.append("<tr>");
        for (int i = 0; i < headers.length; i++) {
            try {
                if (i > headerLength) {
                    builtBody.append("<td>-");
                } else {
                    appendValue(builtBody, row[i]);
                }
                builtBody.append("</td>");
            } catch (ClassCastException | ArrayIndexOutOfBoundsException e) {
                throw new IllegalStateException("Invalid formatter given at index " + i + ": " + e.getMessage(), e);
            }
        }
        builtBody.append("</tr>");
    }

    private void appendValue(StringBuilder builtBody, Object value) {
        String valueString = value != null ? value.toString() : "-";
        try {
            long time = dateFormat.parse(valueString).getTime();
            builtBody.append("<td data-order=\"").append(time).append("\">").append(valueString);
        } catch (ParseException e) {
            if (NumberUtils.isParsable(valueString)) {
                builtBody.append("<td data-order=\"").append(valueString).append("\">").append(valueString);
            } else {
                // Removes non numbers from the value
                String numbersInValue = valueString.replaceAll("\\D", "");
                if (!numbersInValue.isEmpty()) {
                    builtBody.append("<td data-order=\"").append(numbersInValue).append("\">").append(valueString);
                } else {
                    builtBody.append("<td>").append(valueString);
                }
            }
        }
    }
}
