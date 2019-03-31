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
package com.djrapitops.plan.utilities.html.pages;

import com.djrapitops.plan.api.exceptions.ParseException;
import com.djrapitops.plan.data.store.containers.NetworkContainer;
import com.djrapitops.plan.data.store.keys.NetworkKeys;
import com.djrapitops.plan.data.store.keys.ServerKeys;
import com.djrapitops.plan.system.file.PlanFiles;
import com.djrapitops.plan.system.info.server.properties.ServerProperties;
import com.djrapitops.plan.system.update.VersionCheckSystem;
import com.djrapitops.plan.utilities.formatting.Formatters;
import com.djrapitops.plan.utilities.formatting.PlaceholderReplacer;
import com.djrapitops.plan.utilities.html.structure.AnalysisPluginsTabContentCreator;

import java.util.ArrayList;

import static com.djrapitops.plan.data.store.keys.NetworkKeys.*;

/**
 * Html String parser for /network page.
 *
 * @author Rsl1122
 */
public class NetworkPage implements Page {

    private final NetworkContainer networkContainer;
    private final AnalysisPluginsTabContentCreator analysisPluginsTabContentCreator;

    private final VersionCheckSystem versionCheckSystem;
    private final PlanFiles files;
    private final ServerProperties serverProperties;
    private final Formatters formatters;

    NetworkPage(
            NetworkContainer networkContainer,
            AnalysisPluginsTabContentCreator analysisPluginsTabContentCreator,
            VersionCheckSystem versionCheckSystem,
            PlanFiles files,
            ServerProperties serverProperties,
            Formatters formatters
    ) {
        this.networkContainer = networkContainer;
        this.analysisPluginsTabContentCreator = analysisPluginsTabContentCreator;
        this.versionCheckSystem = versionCheckSystem;
        this.files = files;
        this.serverProperties = serverProperties;
        this.formatters = formatters;
    }

    @Override
    public String toHtml() throws ParseException {
        try {
            networkContainer.putSupplier(NetworkKeys.PLAYERS_ONLINE, serverProperties::getOnlinePlayers);

            PlaceholderReplacer placeholderReplacer = new PlaceholderReplacer();
            placeholderReplacer.addAllPlaceholdersFrom(networkContainer,
                    VERSION, NETWORK_NAME, TIME_ZONE,
                    PLAYERS_ONLINE, PLAYERS_ONLINE_SERIES, PLAYERS_TOTAL, PLAYERS_GRAPH_COLOR,
                    REFRESH_TIME_F, RECENT_PEAK_TIME_F, ALL_TIME_PEAK_TIME_F,
                    PLAYERS_ALL_TIME_PEAK, PLAYERS_RECENT_PEAK,
                    PLAYERS_DAY, PLAYERS_WEEK, PLAYERS_MONTH,
                    PLAYERS_NEW_DAY, PLAYERS_NEW_WEEK, PLAYERS_NEW_MONTH,
                    WORLD_MAP_SERIES, WORLD_MAP_HIGH_COLOR, WORLD_MAP_LOW_COLOR,
                    COUNTRY_CATEGORIES, COUNTRY_SERIES,
                    HEALTH_INDEX, HEALTH_NOTES,
                    ACTIVITY_PIE_SERIES, ACTIVITY_STACK_SERIES, ACTIVITY_STACK_CATEGORIES,
                    SERVERS_TAB
            );
            placeholderReplacer.put("update", versionCheckSystem.getUpdateHtml().orElse(""));

            AnalysisPluginTabs analysisPluginTabs = new AnalysisPluginTabs(networkContainer.getBungeeContainer().getValue(ServerKeys.EXTENSION_DATA).orElse(new ArrayList<>()), formatters);

            String[] content = analysisPluginsTabContentCreator.createContent(null, networkContainer.getUnsafe(NetworkKeys.PLAYERS_MUTATOR));
            String nav = analysisPluginTabs.getNav() + content[0];
            String tabs = analysisPluginTabs.getTabs() + content[1];

            placeholderReplacer.put("navPluginsTabs", nav);
            placeholderReplacer.put("tabsPlugins", tabs);

            return placeholderReplacer.apply(files.readCustomizableResourceFlat("web/network.html"));
        } catch (Exception e) {
            throw new ParseException(e);
        }
    }
}