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
package com.djrapitops.plan.utilities.html.structure;

import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.paths.DisplaySettings;
import com.djrapitops.plan.system.settings.theme.Theme;
import com.djrapitops.plan.utilities.formatting.Formatters;
import com.djrapitops.plan.utilities.html.graphs.Graphs;
import com.djrapitops.plan.utilities.html.tables.HtmlTables;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Factory class for different {@link Accordion} objects.
 *
 * @author Rsl1122
 */
@Singleton
public class Accordions {

    private final PlanConfig config;
    private final Theme theme;
    private final Graphs graphs;
    private final HtmlTables tables;
    private final Formatters formatters;

    @Inject
    public Accordions(
            PlanConfig config,
            Theme theme,
            Graphs graphs,
            HtmlTables tables,
            Formatters formatters
    ) {
        this.config = config;
        this.theme = theme;
        this.graphs = graphs;
        this.tables = tables;
        this.formatters = formatters;
    }

    /**
     * Create a new Session accordion for a server.
     *
     * @param sessions            Sessions that have occurred on a server.
     * @param serverNamesSupplier Supplier for server names.
     * @param playerNamesSupplier Supplier for names of players.
     * @return a new {@link SessionAccordion}
     */
    public SessionAccordion serverSessionAccordion(
            List<Session> sessions,
            Supplier<Map<UUID, String>> serverNamesSupplier,
            Supplier<Map<UUID, String>> playerNamesSupplier
    ) {
        boolean appendWorldPercentage = config.isTrue(DisplaySettings.SESSION_MOST_PLAYED_WORLD_IN_TITLE);
        int maxSessions = config.get(DisplaySettings.SESSIONS_PER_PAGE);
        return new SessionAccordion(
                false, sessions,
                serverNamesSupplier, playerNamesSupplier,
                appendWorldPercentage, maxSessions,
                config.getWorldAliasSettings(), theme, graphs, tables,
                formatters.year(), formatters.timeAmount()
        );
    }
}
