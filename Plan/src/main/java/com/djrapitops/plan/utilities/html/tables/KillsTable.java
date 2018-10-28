/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.utilities.html.tables;

import com.djrapitops.plan.api.PlanAPI;
import com.djrapitops.plan.data.container.PlayerKill;
import com.djrapitops.plan.data.element.TableContainer;
import com.djrapitops.plan.data.store.objects.DateHolder;
import com.djrapitops.plan.utilities.comparators.DateHolderRecentComparator;
import com.djrapitops.plan.utilities.formatting.Formatter;
import com.djrapitops.plan.utilities.html.Html;
import com.djrapitops.plan.utilities.html.icon.Family;
import com.djrapitops.plan.utilities.html.icon.Icon;

import java.util.List;

/**
 * Html table that displays kills Player has performed.
 *
 * @author Rsl1122
 */
class KillsTable extends TableContainer {

    private final Formatter<DateHolder> yearFormatter;

    KillsTable(List<PlayerKill> playerKills, String color, Formatter<DateHolder> yearFormatter) {
        super(Icon.called("clock").of(Family.REGULAR) + " Time", "Killed", "With");
        setColor(color);

        this.yearFormatter = yearFormatter;

        if (playerKills.isEmpty()) {
            addRow("No Kills");
        } else {
            addValues(playerKills);
        }
    }

    private void addValues(List<PlayerKill> playerKills) {
        playerKills.sort(new DateHolderRecentComparator());

        int i = 0;
        for (PlayerKill kill : playerKills) {
            if (i >= 40) {
                break;
            }

            String victimName = kill.getVictimName().orElse("Unknown");
            addRow(
                    yearFormatter.apply(kill),
                    Html.LINK.parse(PlanAPI.getInstance().getPlayerInspectPageLink(victimName), victimName),
                    kill.getWeapon()
            );

            i++;
        }
    }
}
