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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Container used to hold data for Player page.
 *
 * @author AuroraLS3
 * @see TableContainer
 * @deprecated PluginData API has been deprecated - see <a href="https://github.com/plan-player-analytics/Plan/wiki/APIv5---DataExtension-API">wiki</a> for new API.
 */
@Deprecated(since = "5.0")
public class InspectContainer {

    protected final List<String> values;
    protected final TreeMap<String, String> html;
    protected final TreeMap<String, TableContainer> tables;

    public InspectContainer() {
        values = new ArrayList<>();
        html = new TreeMap<>();
        tables = new TreeMap<>();
    }

    public final void addValue(String label, Serializable value) {
        values.add(label + ": " + value.toString());
    }

    public final void addHtml(String key, String html) {
        this.html.put(key, html);
    }

    public final void addTable(String key, TableContainer table) {
        tables.put(key, table);
    }

    public final String parseHtml() {
        StringBuilder parsed = new StringBuilder();

        if (!values.isEmpty()) {
            parsed.append("<div class=\"body\">");
            for (String value : values) {
                parsed.append("<p>").append(value).append("</p>");
            }
            parsed.append("</div>");
        }

        for (Map.Entry<String, String> entry : this.html.entrySet()) {
            parsed.append(entry.getValue());
        }

        for (Map.Entry<String, TableContainer> entry : tables.entrySet()) {
            parsed.append(entry.getValue().buildHtml());
        }

        return parsed.toString();
    }

    /**
     * Check if InspectContainer has only values, and not HTML or Tables.
     *
     * @return true/false
     */
    public final boolean hasOnlyValues() {
        return html.isEmpty() && tables.isEmpty();
    }

    public boolean isEmpty() {
        return values.isEmpty() && hasOnlyValues();
    }

    public final boolean hasValues() {
        return !values.isEmpty();
    }
}
