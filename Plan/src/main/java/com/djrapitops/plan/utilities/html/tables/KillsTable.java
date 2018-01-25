package com.djrapitops.plan.utilities.html.tables;

import com.djrapitops.plan.api.PlanAPI;
import com.djrapitops.plan.data.container.PlayerKill;
import com.djrapitops.plan.data.element.TableContainer;
import com.djrapitops.plan.system.cache.DataCache;
import com.djrapitops.plan.utilities.FormatUtils;
import com.djrapitops.plan.utilities.comparators.PlayerKillComparator;
import com.djrapitops.plan.utilities.html.Html;

import java.util.Collections;
import java.util.List;

/**
 * @author Rsl1122
 */
public class KillsTable extends TableContainer {

    public KillsTable(List<PlayerKill> playerKills) {
        super(Html.FONT_AWESOME_ICON.parse("clock-o") + " Time", "Killed", "With");

        if (playerKills.isEmpty()) {
            addRow("No Kills");
        } else {
            addValues(playerKills);
        }
    }

    private void addValues(List<PlayerKill> playerKills) {
        playerKills.sort(new PlayerKillComparator());
        Collections.reverse(playerKills);

        int i = 0;
        DataCache dataCache = DataCache.getInstance();
        for (PlayerKill kill : playerKills) {
            if (i >= 20) {
                break;
            }

            long date = kill.getTime();

            String name = dataCache.getName(kill.getVictim());
            addRow(
                    FormatUtils.formatTimeStamp(date),
                    Html.LINK.parse(PlanAPI.getInstance().getPlayerInspectPageLink(name), name),
                    kill.getWeapon()
            );

            i++;
        }
    }
}
