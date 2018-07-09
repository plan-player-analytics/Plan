/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.data.element;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

/**
 * Container used to parse data for Inspect page.
 * <p>
 * Can contain values: addValue("Total Examples", 1) parses into ("Total Examples: 1")
 * Html: addHtml(key, "{@code <html>}") parses into ("{@code <html>}")
 * Tables: addTable(key, TableContainer) parses into ("{@code <table>...</table}")
 * <p>
 * Has methods for adding icons to Strings:
 * getWithIcon("text", "cube") parses into {@code "<i class=\"fa fa-cube\"></i> text"}
 * getWithColoredIcon("text", "cube", "light-green") parses into {@code "<i class=\"col-light-green fa fa-cube\"></i> text"}
 *
 * @author Rsl1122
 * @see TableContainer
 * @since 4.1.0
 */
public class InspectContainer {

    protected TreeMap<String, String> values;
    protected TreeMap<String, String> html;
    protected TreeMap<String, TableContainer> tables;

    public InspectContainer() {
        values = new TreeMap<>();
        html = new TreeMap<>();
        tables = new TreeMap<>();
    }

    public final void addValue(String label, Serializable value) {
        values.put(label, value.toString());
    }

    public final void addHtml(String key, String html) {
        this.html.put(key, html);
    }

    public final void addTable(String key, TableContainer table) {
        tables.put(key, table);
    }

    public final String parseHtml() {
        StringBuilder html = new StringBuilder();

        if (!values.isEmpty()) {
            html.append("<div class=\"body\">");
            for (Map.Entry<String, String> entry : values.entrySet()) {
                html.append("<p>").append(entry.getKey()).append(": ").append(entry.getValue()).append("</p>");
            }
            html.append("</div>");
        }

        for (Map.Entry<String, String> entry : this.html.entrySet()) {
            html.append(entry.getValue());
        }

        for (Map.Entry<String, TableContainer> entry : tables.entrySet()) {
            html.append(entry.getValue().parseHtml());
        }

        return html.toString();
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
        return values.isEmpty() && html.isEmpty() && tables.isEmpty();
    }

    public final boolean hasValues() {
        return !values.isEmpty();
    }
}
