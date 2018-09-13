package com.djrapitops.plan.utilities.html.tables;

import com.djrapitops.plan.api.PlanAPI;
import com.djrapitops.plan.data.container.PlayerDeath;
import com.djrapitops.plan.data.element.TableContainer;
import com.djrapitops.plan.data.store.objects.DateHolder;
import com.djrapitops.plan.utilities.comparators.DateHolderRecentComparator;
import com.djrapitops.plan.utilities.formatting.Formatter;
import com.djrapitops.plan.utilities.formatting.Formatters;
import com.djrapitops.plan.utilities.html.Html;
import com.djrapitops.plan.utilities.html.icon.Family;
import com.djrapitops.plan.utilities.html.icon.Icon;

import java.util.List;

/**
 * @author Rsl1122
 */
public class DeathsTable extends TableContainer {

    public DeathsTable(List<PlayerDeath> playerPlayerDeaths) {
        super(Icon.called("clock_Old").of(Family.REGULAR) + " Time", "Killed by", "With");
        setColor("red");

        if (playerPlayerDeaths.isEmpty()) {
            addRow("No Player caused Deaths");
        } else {
            addValues(playerPlayerDeaths);
        }
    }

    private void addValues(List<PlayerDeath> playerPlayerDeaths) {
        playerPlayerDeaths.sort(new DateHolderRecentComparator());
        Formatter<DateHolder> timestamp = Formatters.year_Old();

        int i = 0;
        for (PlayerDeath death : playerPlayerDeaths) {
            if (i >= 40) {
                break;
            }

            String killerName = death.getKillerName();
            addRow(
                    timestamp.apply(death),
                    Html.LINK.parse(PlanAPI.getInstance().getPlayerInspectPageLink(killerName), killerName),
                    death.getWeapon()
            );

            i++;
        }
    }
}
