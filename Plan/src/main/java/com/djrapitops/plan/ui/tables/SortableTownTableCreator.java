package main.java.com.djrapitops.plan.ui.tables;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import main.java.com.djrapitops.plan.data.additional.TownyHook;
import main.java.com.djrapitops.plan.ui.Html;
import main.java.com.djrapitops.plan.utilities.HtmlUtils;

/**
 *
 * @author Rsl1122
 */
public class SortableTownTableCreator {

    public static String createSortableTownsTable(Collection<String> townNames, TownyHook tHook) {
        String html = "<table class=\"sortable table\"><thead><tr>"
                + "<th>Town</th>"
                + "<th>Residents</th>"
                + "<th>Land</th>"
                + "<th>Mayor</th>"
                + "</tr></thead>"
                + "<tbody>";
        if (townNames.isEmpty()) {
            html += "<tr><td>No Towns</td><td></td><td></td><td></td></tr>";
        } else {
            for (String town : townNames) {
                HashMap<String, Serializable> info = tHook.getTownInfo(town);
                html += "<tr>"
                        + "<td>" + town + "</td>"
                        + "<td>" + info.get("RESIDENTS") + "</td>"
                        + "<td>" + info.get("LAND") + "</td>"
                        + "<td>" + Html.LINK.parse(HtmlUtils.getInspectUrl((String) info.get("MAYOR")), (String) info.get("MAYOR")) + "</td>"
                        + "</tr>";
            }
        }
        html += "</tbody></table>";
        return html;
    }
}
