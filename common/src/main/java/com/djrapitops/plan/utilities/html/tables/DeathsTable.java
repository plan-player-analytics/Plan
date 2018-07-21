package com.djrapitops.plan.utilities.html.tables;

import com.djrapitops.plan.api.PlanAPI;
import com.djrapitops.plan.data.container.PlayerDeath;
import com.djrapitops.plan.data.element.TableContainer;
import com.djrapitops.plan.data.store.mutators.formatting.Formatter;
import com.djrapitops.plan.data.store.mutators.formatting.Formatters;
import com.djrapitops.plan.data.store.objects.DateHolder;
import com.djrapitops.plan.system.cache.DataCache;
import com.djrapitops.plan.utilities.comparators.DateHolderRecentComparator;
import com.djrapitops.plan.utilities.html.Html;
import com.djrapitops.plan.utilities.html.icon.Family;
import com.djrapitops.plan.utilities.html.icon.Icon;

import java.util.List;

/**
 * @author Rsl1122
 */
public class DeathsTable extends TableContainer {

    public DeathsTable(List<PlayerDeath> playerPlayerDeaths) {
        super(Icon.called("clock").of(Family.REGULAR) + " Time", "Killed by", "With");
        setColor("red");

        if (playerPlayerDeaths.isEmpty()) {
            addRow("No Player caused Deaths");
        } else {
            addValues(playerPlayerDeaths);
        }
    }

    private void addValues(List<PlayerDeath> playerPlayerDeaths) {
        playerPlayerDeaths.sort(new DateHolderRecentComparator());
        Formatter<DateHolder> timestamp = Formatters.year();

        int i = 0;
        DataCache dataCache = DataCache.getInstance();
        for (PlayerDeath death : playerPlayerDeaths) {
            if (i >= 40) {
                break;
            }

            String name = dataCache.getName(death.getKiller());
            addRow(
                    timestamp.apply(death),
                    Html.LINK.parse(PlanAPI.getInstance().getPlayerInspectPageLink(name), name),
                    death.getWeapon()
            );

            i++;
        }
    }
}
