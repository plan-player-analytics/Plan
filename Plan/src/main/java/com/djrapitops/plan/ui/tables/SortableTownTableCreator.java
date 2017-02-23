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
        String html = Html.TABLE_TOWNS_START.parse();
        if (townNames.isEmpty()) {
            html += Html.TABLELINE_4.parse(Html.TOWN_NO_TOWNS.parse(), "", "", "");
        } else {
            for (String town : townNames) {
                HashMap<String, Serializable> info = tHook.getTownInfo(town);
                html += Html.TABLELINE_4.parse(
                        town,
                        info.get("RESIDENTS") + "",
                        info.get("LAND") + "",
                        Html.LINK.parse(HtmlUtils.getInspectUrl((String) info.get("MAYOR")), (String) info.get("MAYOR"))
                );
            }
        }
        html += Html.TABLE_END.parse();
        return html;
    }
}
