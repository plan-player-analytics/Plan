package main.java.com.djrapitops.plan.ui.html.tables;

import com.djrapitops.plugin.utilities.player.Fetch;
import com.djrapitops.plugin.utilities.player.IOfflinePlayer;
import java.util.List;
import main.java.com.djrapitops.plan.data.KillData;
import main.java.com.djrapitops.plan.ui.html.Html;
import main.java.com.djrapitops.plan.utilities.FormatUtils;
import main.java.com.djrapitops.plan.utilities.HtmlUtils;

/**
 *
 * @author Rsl1122
 */
public class KillsTableCreator {

    /**
     *
     * @param killData
     * @return
     */
    public static String createSortedSessionDataTable10(List<KillData> killData) {
        String html = Html.TABLE_KILLS_START.parse();
        if (killData.isEmpty()) {
            html += Html.TABLELINE_3.parse(Html.KILLDATA_NONE.parse(), "", "");
        } else {
            int i = 0;
            for (KillData kill : killData) {
                if (i >= 10) {
                    break;
                }
                long date = kill.getDate();
                IOfflinePlayer victim = Fetch.getIOfflinePlayer(kill.getVictim());
                String name = victim.getName();
                html += Html.TABLELINE_3_CUSTOMKEY_1.parse(
                        date + "", FormatUtils.formatTimeStamp(date),
                        Html.LINK.parse(HtmlUtils.getInspectUrl(name), name),
                        kill.getWeapon()
                );
                i++;
            }
        }
        html += Html.TABLE_END.parse();
        return html;
    }
}
