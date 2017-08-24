package main.java.com.djrapitops.plan.utilities.html.tables;

import com.djrapitops.plugin.utilities.player.Fetch;
import com.djrapitops.plugin.utilities.player.IOfflinePlayer;
import main.java.com.djrapitops.plan.data.KillData;
import main.java.com.djrapitops.plan.locale.Locale;
import main.java.com.djrapitops.plan.locale.Msg;
import main.java.com.djrapitops.plan.utilities.FormatUtils;
import main.java.com.djrapitops.plan.utilities.comparators.KillDataComparator;
import main.java.com.djrapitops.plan.utilities.html.Html;
import main.java.com.djrapitops.plan.utilities.html.HtmlUtils;

import java.util.Collections;
import java.util.List;

/**
 * @author Rsl1122
 */
public class KillsTableCreator {

    /**
     * Constructor used to hide the public constructor
     */
    private KillsTableCreator() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * @param killData The list of the {@link KillData} Objects from which the kill table should be created
     * @return The created kills table
     */
    public static String createKillsTable(List<KillData> killData) {
        StringBuilder html = new StringBuilder(Html.TABLE_KILLS_START.parse());

        if (killData.isEmpty()) {
            html.append(Html.TABLELINE_3.parse(Locale.get(Msg.HTML_TABLE_NO_KILLS).parse(), "", ""));
        } else {
            killData.sort(new KillDataComparator());
            Collections.reverse(killData);

            int i = 0;
            for (KillData kill : killData) {
                if (i >= 20) {
                    break;
                }

                long date = kill.getTime();

                IOfflinePlayer victim = Fetch.getIOfflinePlayer(kill.getVictim());
                String name = victim.getName();
                html.append(Html.TABLELINE_3_CUSTOMKEY_1.parse(
                        String.valueOf(date), FormatUtils.formatTimeStamp(date),
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
