/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.data.additional;

import main.java.com.djrapitops.plan.utilities.html.Html;

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

    public String getWithIcon(String text, String icon) {
        return getWithColoredIcon(text, icon, "black");
    }

    public String getWithColoredIcon(String text, String icon, String color) {
        return Html.FA_COLORED_ICON.parse(color, icon) + " " + text;
    }

    public void addValue(String label, Serializable value) {
        values.put(label, value.toString());
    }

    public void addHtml(String key, String html) {
        this.html.put(key, html);
    }

    public void addTable(String key, TableContainer table) {
        tables.put(key, table);
    }

    public String parseHtml() {
        StringBuilder html = new StringBuilder();

        for (Map.Entry<String, String> entry : values.entrySet()) {
            html.append("<p>").append(entry.getKey()).append(": ").append(entry.getValue()).append("</p>");
        }

        for (Map.Entry<String, String> entry : this.html.entrySet()) {
            html.append(entry.getValue());
        }

        for (Map.Entry<String, TableContainer> entry : tables.entrySet()) {
            html.append(entry.getValue().parseHtml());
        }

        return html.toString();
    }
}