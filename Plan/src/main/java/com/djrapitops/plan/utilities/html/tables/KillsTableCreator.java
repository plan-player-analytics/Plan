package main.java.com.djrapitops.plan.utilities.html.tables;

import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.container.PlayerKill;
import main.java.com.djrapitops.plan.settings.locale.Locale;
import main.java.com.djrapitops.plan.settings.locale.Msg;
import main.java.com.djrapitops.plan.utilities.FormatUtils;
import main.java.com.djrapitops.plan.utilities.comparators.KillDataComparator;
import main.java.com.djrapitops.plan.utilities.html.Html;

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
     * @param playerKills The list of the {@link PlayerKill} Objects from which the kill table should be created
     * @return The created kills table
     */
    public static String createTable(List<PlayerKill> playerKills) {
        StringBuilder html = new StringBuilder(Html.TABLE_KILLS_START.parse());

        if (playerKills.isEmpty()) {
            html.append(Html.TABLELINE_3.parse(Locale.get(Msg.HTML_TABLE_NO_KILLS).parse(), "", ""));
        } else {
            playerKills.sort(new KillDataComparator());
            Collections.reverse(playerKills);

            int i = 0;
            for (PlayerKill kill : playerKills) {
                if (i >= 20) {
                    break;
                }

                long date = kill.getTime();

                String name = Plan.getInstance().getDataCache().getName(kill.getVictim());
                html.append(Html.TABLELINE_3_CUSTOMKEY_1.parse(
                        String.valueOf(date), FormatUtils.formatTimeStamp(date),
                        Html.LINK.parse(Plan.getPlanAPI().getPlayerInspectPageLink(name), name),
                        kill.getWeapon()
                ));

                i++;
            }
        }

        html.append(Html.TABLE_END.parse());

        return html.toString();
    }
}
