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

    /**
     *
     * @param factionList
     * @param fHook
     * @return
     */
    public static String createSortableFactionsTable(Collection<String> factionList, FactionsHook fHook) {
        String html = Html.TABLE_FACTIONS_START.parse();
        if (factionList.isEmpty()) {
            html += Html.TABLELINE_4.parse(Html.FACTION_NO_FACTIONS.parse(), "", "", "");
        } else {
            for (String factionName : factionList) {
                HashMap<String, Serializable> info = fHook.getFactionInfo(factionName);
                String leader = (String) info.get("LEADER");
                html += Html.TABLELINE_4.parse(
                        factionName, 
                        info.get("POWER")+"", 
                        info.get("LAND")+"", 
                        Html.LINK.parse(HtmlUtils.getInspectUrl(leader), leader)
                );                    
            }
        }
        html += Html.TABLE_END.parse();
        return html;
    }
}
