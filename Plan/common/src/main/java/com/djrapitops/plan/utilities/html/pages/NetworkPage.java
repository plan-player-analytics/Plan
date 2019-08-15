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
import com.djrapitops.plan.extension.implementation.results.server.ExtensionServerData;
import com.djrapitops.plan.extension.implementation.storage.queries.ExtensionServerDataQuery;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.file.PlanFiles;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.paths.ProxySettings;
import com.djrapitops.plan.system.settings.theme.Theme;
import com.djrapitops.plan.system.settings.theme.ThemeVal;
import com.djrapitops.plan.system.update.VersionCheckSystem;
import com.djrapitops.plan.utilities.formatting.Formatters;
import com.djrapitops.plan.utilities.formatting.PlaceholderReplacer;

import java.util.List;

/**
 * Html String parser for /network page.
 *
 * @author Rsl1122
 */
public class NetworkPage implements Page {

    private final DBSystem dbSystem;

    private final VersionCheckSystem versionCheckSystem;
    private final PlanFiles files;
    private final PlanConfig config;
    private final Theme theme;
    private final ServerInfo serverInfo;
    private final Formatters formatters;

    NetworkPage(
            DBSystem dbSystem,
            VersionCheckSystem versionCheckSystem,
            PlanFiles files,
            PlanConfig config,
            Theme theme,
            ServerInfo serverInfo,
            Formatters formatters
    ) {
        this.dbSystem = dbSystem;
        this.versionCheckSystem = versionCheckSystem;
        this.files = files;
        this.config = config;
        this.theme = theme;
        this.serverInfo = serverInfo;
        this.formatters = formatters;
    }

    @Override
    public String toHtml() throws ParseException {
        try {
            PlaceholderReplacer placeholders = new PlaceholderReplacer();

            placeholders.put("networkDisplayName", config.get(ProxySettings.NETWORK_NAME));

            placeholders.put("gmPieColors", theme.getValue(ThemeVal.GRAPH_GM_PIE));
            placeholders.put("playersGraphColor", theme.getValue(ThemeVal.GRAPH_PLAYERS_ONLINE));
            placeholders.put("timeZone", config.getTimeZoneOffsetHours());

            placeholders.put("update", versionCheckSystem.getUpdateHtml().orElse(""));

            List<ExtensionServerData> extensionData = dbSystem.getDatabase()
                    .query(new ExtensionServerDataQuery(serverInfo.getServerUUID()));
            ServerPluginTabs pluginTabs = new ServerPluginTabs(extensionData, formatters);

            String nav = pluginTabs.getNav();
            String tabs = pluginTabs.getTabs();

            placeholders.put("navPluginsTabs", nav);
            placeholders.put("tabsPlugins", tabs);

            return placeholders.apply(files.getCustomizableResourceOrDefault("web/network.html").asString());
        } catch (Exception e) {
            throw new ParseException(e);
        }
    }
}