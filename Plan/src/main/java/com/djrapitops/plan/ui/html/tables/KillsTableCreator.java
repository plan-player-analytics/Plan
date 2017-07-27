package main.java.com.djrapitops.plan.ui.html.tables;

import com.djrapitops.plugin.utilities.player.Fetch;
import com.djrapitops.plugin.utilities.player.IOfflinePlayer;
import main.java.com.djrapitops.plan.data.KillData;
import main.java.com.djrapitops.plan.ui.html.Html;
import main.java.com.djrapitops.plan.utilities.FormatUtils;
import main.java.com.djrapitops.plan.utilities.HtmlUtils;

import java.util.List;

/**
 * @author Rsl1122
 */
public class KillsTableCreator {

    /**
     * @param killData
     * @return
     */
    public static String createKillsTable(List<KillData> killData) {
        StringBuilder html = new StringBuilder(Html.TABLE_KILLS_START.parse());

        if (killData.isEmpty()) {
            html.append(Html.TABLELINE_3.parse(Html.KILLDATA_NONE.parse(), "", ""));
        } else {
            int i = 0;
            for (KillData kill : killData) {
                if (i >= 20) {
                    break;
                }
                long date = kill.getDate();
                IOfflinePlayer victim = Fetch.getIOfflinePlayer(kill.getVictim());
                String name = victim.getName();
                html.append(Html.TABLELINE_3_CUSTOMKEY_1.parse(
                        date + "", FormatUtils.formatTimeStamp(date),
                        Html.LINK.parse(HtmlUtils.getInspectUrl(name), name),
                        kill.getWeapon()
                ));
                i++;
            }
        }
        html.append(Html.TABLE_END.parse());

        return html.toString();
    }
}
