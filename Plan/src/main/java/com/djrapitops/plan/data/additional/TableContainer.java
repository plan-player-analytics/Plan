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

    /**
     * Constructor, call with super(...).
     *
     * @param header Required: example {@code new TableContainer("1st", "2nd"} parses into {@code <thead><tr><th>1st</th><th>2nd</th></tr></thead}.
     */
    public TableContainer(String... header) {
        this.header = header;
        values = new ArrayList<>();
    }

    protected void addRow(Serializable... values) {
        this.values.add(values);
    }

    public String parseHtml() {
        StringBuilder table = new StringBuilder(Html.TABLE.parse());

        table.append(parseHeader());
        table.append(parseBody());

        return table.append("</table>").toString();
    }

    private String parseBody() {
        StringBuilder body = new StringBuilder();
        if (values.isEmpty()) {
            addRow("No Data");
        }

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

        return Html.TABLE_BODY.parse(body.toString());
    }

    public String parseHeader() {
        StringBuilder header = new StringBuilder("<tr>");
        for (String title : this.header) {
            header.append("<th>").append(title).append("</th>");
        }
        header.append("</tr>");
        return Html.TABLE_HEAD.parse(header.toString());
    }
}