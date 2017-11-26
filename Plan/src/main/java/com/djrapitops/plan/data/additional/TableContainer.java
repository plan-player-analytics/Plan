/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.data.additional;

import main.java.com.djrapitops.plan.utilities.html.Html;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Container used for parsing Html tables.
 *
 * @author Rsl1122
 */
public final class TableContainer {

    private final String[] header;
    private List<Serializable[]> values;

    private String color;

    /**
     * Constructor, call with super(...).
     *
     * @param header Required: example {@code new TableContainer("1st", "2nd"} parses into {@code <thead><tr><th>1st</th><th>2nd</th></tr></thead}.
     */
    public TableContainer(String... header) {
        this.header = header;
        values = new ArrayList<>();
    }

    public void addRow(Serializable... values) {
        this.values.add(values);
    }

    public String parseHtml() {
        return Html.TABLE.parse() +
                parseHeader() +
                parseBody() +
                "</table>";
    }

    private String parseBody() {
        StringBuilder body = new StringBuilder();
        if (values.isEmpty()) {
            addRow("No Data");
        }

        body.append("<tbody>");
        for (Serializable[] row : values) {
            int maxIndex = row.length - 1;
            body.append("<tr>");
            for (int i = 0; i < header.length; i++) {
                body.append("<td>");
                if (i > maxIndex) {
                    body.append("-");
                } else {
                    body.append(row[i]);
                }
                body.append("</td>");
            }
            body.append("</tr>");
        }
        body.append("</tbody>");

        return Html.TABLE_BODY.parse(body.toString());
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String parseHeader() {
        StringBuilder header = new StringBuilder("<thead" + (color != null ? " bg-" + color : "") + "><tr>");
        for (String title : this.header) {
            header.append("<th>").append(title).append("</th>");
        }
        header.append("</tr></thead>");
        return Html.TABLE_HEAD.parse(header.toString());
    }
}