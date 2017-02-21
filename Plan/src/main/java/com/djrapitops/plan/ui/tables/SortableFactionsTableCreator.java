package main.java.com.djrapitops.plan.ui.tables;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import main.java.com.djrapitops.plan.data.additional.FactionsHook;
import main.java.com.djrapitops.plan.ui.Html;
import main.java.com.djrapitops.plan.utilities.HtmlUtils;

/**
 *
 * @author Rsl1122
 */
public class SortableFactionsTableCreator {

    public static String createSortableFactionsTable(Collection<String> factionList, FactionsHook fHook) {
        String html = "<table class=\"sortable table\"><thead><tr>"
                + "<th>Faction</th>"
                + "<th>Power</th>"
                + "<th>Land</th>"
                + "<th>Leader</th>"
                + "</tr></thead>"
                + "<tbody>";
        if (factionList.isEmpty()) {
            html += "<tr><td>No Factions</td><td></td><td></td><td></td></tr>";
        } else {
            for (String factionName : factionList) {
                HashMap<String, Serializable> info = fHook.getFactionInfo(factionName);
                html += "<tr>"
                        + "<td>" + factionName + "</td>"
                        + "<td>" + info.get("POWER") + "</td>"
                        + "<td>" + info.get("LAND") + "</td>"
                        + "<td>" + Html.LINK.parse(HtmlUtils.getInspectUrl((String) info.get("LEADER")), (String) info.get("LEADER")) + "</td>"
                        + "</tr>";
            }
        }
        html += "</tbody></table>";
        return html;
    }
}
