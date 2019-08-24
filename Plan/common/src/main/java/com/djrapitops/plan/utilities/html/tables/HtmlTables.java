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

import com.djrapitops.plan.data.element.TableContainer;
import com.djrapitops.plan.data.store.containers.PlayerContainer;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.paths.DisplaySettings;
import com.djrapitops.plan.system.settings.paths.TimeSettings;
import com.djrapitops.plan.utilities.formatting.Formatters;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 * Factory class for objects that represent HTML tables.
 *
 * @author Rsl1122
 */
@Singleton
public class HtmlTables {

    private final PlanConfig config;
    private final Formatters formatters;

    @Inject
    public HtmlTables(
            PlanConfig config,
            Formatters formatters
    ) {
        this.config = config;
        this.formatters = formatters;
    }

    /**
     * Create a Player table for a players page.
     *
     * @param players List of {@link PlayerContainer}s of players.
     * @return a new {@link PlayersTable}.
     */
    public TableContainer playerTableForPlayersPage(List<PlayerContainer> players) {
        return new PlayersTable(
                players, config.get(DisplaySettings.PLAYERS_PER_PLAYERS_PAGE),
                config.get(TimeSettings.ACTIVE_PLAY_THRESHOLD),
                config.get(DisplaySettings.OPEN_PLAYER_LINKS_IN_NEW_TAB),
                formatters.timeAmount(), formatters.yearLong(), formatters.decimals()
        );
    }

}