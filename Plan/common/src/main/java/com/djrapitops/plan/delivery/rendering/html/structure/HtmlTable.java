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

import com.djrapitops.plan.delivery.formatting.Formatters;
import com.djrapitops.plan.delivery.rendering.html.Html;
import com.djrapitops.plan.delivery.rendering.html.icon.Color;
import com.djrapitops.plan.delivery.rendering.html.icon.Icon;
import com.djrapitops.plan.extension.table.Table;
import com.djrapitops.plan.extension.table.TableColumnFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public interface HtmlTable {

    static HtmlTable fromExtensionTable(Table table, com.djrapitops.plan.extension.icon.Color tableColor) {
        return fromExtensionTable(table, Color.getByName(tableColor.name()).orElse(Color.NONE));
    }

    static HtmlTable fromExtensionTable(Table table, Color tableColor) {
        if (table.getRows().size() > 10) {
            return new DynamicHtmlTable(table);
        } else {
            return new HtmlTableWithColoredHeader(table, tableColor);
        }
    }

    static Header[] mapToHeaders(Table table) {
        ArrayList<Header> headers = new ArrayList<>();

        com.djrapitops.plan.extension.icon.Icon[] icons = table.getIcons();
        String[] columns = table.getColumns();
        for (int i = 0; i < columns.length; i++) {
            String column = columns[i];
            if (column == null) {
                break;
            }
            headers.add(new Header(Icon.fromExtensionIcon(icons[i]), column));
        }

        return headers.toArray(new Header[0]);
    }

    static List<Object[]> mapToRows(List<Object[]> rows, TableColumnFormat[] tableColumnFormats) {
        return rows.stream()
                .map(row -> {
                    List<Object> mapped = new ArrayList<>(row.length);
                    for (int i = 0; i < row.length; i++) {
                        Object value = row[i];
                        if (value == null) {
                            mapped.add(null);
                        } else {
                            TableColumnFormat format = tableColumnFormats[i];
                            mapped.add(applyFormat(format, value));
                        }
                    }
                    return mapped.toArray();
                })
                .collect(Collectors.toList());
    }

    static Object applyFormat(TableColumnFormat format, Object value) {
        try {
            switch (format) {
                case TIME_MILLISECONDS:
                    return Formatters.getInstance().timeAmount().apply(Long.parseLong(value.toString()));
                case DATE_YEAR:
                    return Formatters.getInstance().yearLong().apply(Long.parseLong(value.toString()));
                case DATE_SECOND:
                    return Formatters.getInstance().secondLong().apply(Long.parseLong(value.toString()));
                case PLAYER_NAME:
                    return Html.LINK.create("../player/" + Html.encodeToURL(Html.swapColorCodesToSpan(value.toString())));
                default:
                    return Html.swapColorCodesToSpan(value.toString());
            }
        } catch (Exception e) {
            return value;
        }
    }

    String toHtml();

    class Header {
        private final Icon icon;
        private final String text;

        public Header(Icon icon, String text) {
            this.icon = icon;
            this.text = text;
        }

        public Icon getIcon() {
            return icon;
        }

        public String getText() {
            return text;
        }
    }
}
