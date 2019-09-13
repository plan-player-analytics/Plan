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
package com.djrapitops.plan.delivery.rendering.pages;

import com.djrapitops.plan.delivery.formatting.Formatters;
import com.djrapitops.plan.delivery.formatting.PlaceholderReplacer;
import com.djrapitops.plan.exceptions.ParseException;
import com.djrapitops.plan.extension.implementation.results.ExtensionData;
import com.djrapitops.plan.extension.implementation.storage.queries.ExtensionServerDataQuery;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.ProxySettings;
import com.djrapitops.plan.settings.theme.Theme;
import com.djrapitops.plan.settings.theme.ThemeVal;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.file.PlanFiles;
import com.djrapitops.plan.version.VersionCheckSystem;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.UUID;

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

            UUID serverUUID = serverInfo.getServerUUID();
            placeholders.put("networkDisplayName", config.get(ProxySettings.NETWORK_NAME));
            placeholders.put("serverUUID", serverUUID.toString());

            placeholders.put("gmPieColors", theme.getValue(ThemeVal.GRAPH_GM_PIE));
            placeholders.put("playersGraphColor", theme.getValue(ThemeVal.GRAPH_PLAYERS_ONLINE));
            placeholders.put("worldMapColLow", theme.getValue(ThemeVal.WORLD_MAP_LOW));
            placeholders.put("worldMapColHigh", theme.getValue(ThemeVal.WORLD_MAP_HIGH));
            placeholders.put("maxPingColor", theme.getValue(ThemeVal.GRAPH_MAX_PING));
            placeholders.put("minPingColor", theme.getValue(ThemeVal.GRAPH_MIN_PING));
            placeholders.put("avgPingColor", theme.getValue(ThemeVal.GRAPH_AVG_PING));
            placeholders.put("timeZone", config.getTimeZoneOffsetHours());

            placeholders.put("version", versionCheckSystem.getUpdateButton().orElse(versionCheckSystem.getCurrentVersionButton()));
            placeholders.put("updateModal", versionCheckSystem.getUpdateModal());

            List<ExtensionData> extensionData = dbSystem.getDatabase()
                    .query(new ExtensionServerDataQuery(serverUUID));
            ServerPluginTabs pluginTabs = new ServerPluginTabs(extensionData, formatters);

            String nav = pluginTabs.getNav();
            String tabs = pluginTabs.getTabs();

            placeholders.put("navPluginsTabs", nav);
            placeholders.put("tabsPlugins", StringUtils.remove(tabs, "${backButton}"));

            return placeholders.apply(files.getCustomizableResourceOrDefault("web/network.html").asString());
        } catch (Exception e) {
            throw new ParseException(e);
        }
    }
}