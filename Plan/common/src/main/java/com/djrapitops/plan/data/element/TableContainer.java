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
package com.djrapitops.plan.data.element;

import com.djrapitops.plan.delivery.formatting.Formatter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Container used for creating Html tables.
 *
 * @author AuroraLS3
 * @deprecated PluginData API has been deprecated - see <a href="https://github.com/plan-player-analytics/Plan/wiki/APIv5---DataExtension-API">wiki</a> for new API.
 */
@Deprecated(since = "5.0")
public class TableContainer {

    protected final String[] header;
    protected final Formatter[] formatters;
    private final List<Serializable[]> values;

    private String jqueryDatatable;

    private String color;

    /**
     * Constructor, call with super(...).
     *
     * @param header Required: example {@code new TableContainer("1st", "2nd"} turns into {@code <thead><tr><th>1st</th><th>2nd</th></tr></thead}.
     */
    public TableContainer(String... header) {
        this.header = header;
        this.formatters = new Formatter[this.header.length];
        values = new ArrayList<>();
    }

    public TableContainer(boolean players, String... header) {
        this(header);
    }

    public final void addRow(Serializable... values) {
        this.values.add(values);
    }

    public String parsehtml() {
        return buildHtml();
    }

    public String buildHtml() {
        return getTableHeader() +
                parseHeader() +
                parseBody() +
                "</table></div>";
    }

    public final String parseBody() {
        if (values.isEmpty()) {
            addRow("No Data");
        }
        return "<tbody>" + buildBody() + "</tbody>";

    }

    private String buildBody() {
        StringBuilder body = new StringBuilder();
        for (Serializable[] row : values) {
            appendRow(body, row);
        }
        return body.toString();
    }

    private void appendRow(StringBuilder body, Serializable[] row) {
        int maxIndex = row.length - 1;
        body.append("<tr>");
        for (int i = 0; i < header.length; i++) {
            try {
                if (i > maxIndex) {
                    body.append("<td>-");
                } else {
                    appendValue(body, row[i], formatters[i]);
                }
                body.append("</td>");
            } catch (ClassCastException | ArrayIndexOutOfBoundsException e) {
                throw new IllegalStateException("Invalid formatter given at index " + i + ": " + e.getMessage(), e);
            }
        }
        body.append("</tr>");
    }

    private void appendValue(StringBuilder body, Serializable value, Formatter formatter) {
        body.append("<td").append(formatter != null ? " data-order=\"" + value + "\">" : ">");
        if (formatter != null) {
            body.append(formatter.apply(value));
        } else {
            body.append(value != null ? value : '-');
        }
    }

    public final void setColor(String color) {
        this.color = color;
    }

    public final String parseHeader() {
        StringBuilder parsedHeader = new StringBuilder("<thead" + (color != null ? " class=\"bg-" + color + "\"" : "") + "><tr>");
        for (String title : header) {
            parsedHeader.append("<th>").append(title).append("</th>");
        }
        parsedHeader.append("</tr></thead>");
        return parsedHeader.toString();
    }

    public final void setFormatter(int index, Formatter formatter) {
        if (index < formatters.length) {
            formatters[index] = formatter;
        }
    }

    /**
     * Make use of jQuery Data-tables plugin.
     * <p>
     * Use this with custom tables.
     */
    public void useJqueryDataTables() {
        this.jqueryDatatable = "player-plugin-table";
    }

    /**
     * Make use of jQuery Data-tables plugin.
     *
     * @param sortType "player-table" or "player-plugin-table"
     */
    public void useJqueryDataTables(String sortType) {
        jqueryDatatable = sortType;
    }

    private String getTableHeader() {
        if (jqueryDatatable != null) {
            return "<table class=\"table table-bordered table-striped table-hover " + jqueryDatatable + "\" style=\"width: 100%\">";
        } else {
            return "<div class=\"scrollbar\"><table class=\"table table-striped\">";
        }
    }
}
