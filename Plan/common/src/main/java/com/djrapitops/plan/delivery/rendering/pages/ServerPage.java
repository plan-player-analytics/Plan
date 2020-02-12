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

import com.djrapitops.plan.delivery.domain.container.CachingSupplier;
import com.djrapitops.plan.delivery.domain.container.DataContainer;
import com.djrapitops.plan.delivery.domain.container.RawDataContainer;
import com.djrapitops.plan.delivery.domain.keys.AnalysisKeys;
import com.djrapitops.plan.delivery.formatting.Formatters;
import com.djrapitops.plan.delivery.formatting.PlaceholderReplacer;
import com.djrapitops.plan.delivery.rendering.html.Contributors;
import com.djrapitops.plan.delivery.rendering.html.Html;
import com.djrapitops.plan.delivery.webserver.cache.DataID;
import com.djrapitops.plan.delivery.webserver.cache.JSONCache;
import com.djrapitops.plan.extension.implementation.results.ExtensionData;
import com.djrapitops.plan.extension.implementation.storage.queries.ExtensionServerDataQuery;
import com.djrapitops.plan.identification.Server;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.DisplaySettings;
import com.djrapitops.plan.settings.theme.Theme;
import com.djrapitops.plan.settings.theme.ThemeVal;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.version.VersionCheckSystem;

import java.util.List;
import java.util.UUID;

import static com.djrapitops.plan.delivery.domain.keys.AnalysisKeys.*;

/**
 * Html String generator for /server page.
 *
 * @author Rsl1122
 */
public class ServerPage implements Page {

    private final String templateHtml;
    private final Server server;
    private final PlanConfig config;
    private final Theme theme;
    private final VersionCheckSystem versionCheckSystem;
    private final DBSystem dbSystem;
    private final ServerInfo serverInfo;
    private final Formatters formatters;

    ServerPage(
            String templateHtml, Server server,
            PlanConfig config,
            Theme theme,
            VersionCheckSystem versionCheckSystem,
            DBSystem dbSystem,
            ServerInfo serverInfo,
            Formatters formatters
    ) {
        this.templateHtml = templateHtml;
        this.server = server;
        this.config = config;
        this.theme = theme;
        this.versionCheckSystem = versionCheckSystem;
        this.dbSystem = dbSystem;
        this.serverInfo = serverInfo;
        this.formatters = formatters;
    }

    @Override
    public String toHtml() {
        PlaceholderReplacer placeholders = new PlaceholderReplacer();

        UUID serverUUID = server.getUuid();
        placeholders.put("serverUUID", serverUUID.toString());
        placeholders.put("serverName", server.getIdentifiableName());
        placeholders.put("serverDisplayName", server.getName());

        placeholders.put("timeZone", config.getTimeZoneOffsetHours());

        DataContainer constants = new RawDataContainer();
        // TODO Move these graph settings to the graph requests instead of placeholders
        constants.putRawData(AnalysisKeys.FIRST_DAY, 1);
        constants.putRawData(AnalysisKeys.TPS_MEDIUM, config.get(DisplaySettings.GRAPH_TPS_THRESHOLD_MED));
        constants.putRawData(AnalysisKeys.TPS_HIGH, config.get(DisplaySettings.GRAPH_TPS_THRESHOLD_HIGH));
        constants.putRawData(AnalysisKeys.DISK_MEDIUM, config.get(DisplaySettings.GRAPH_DISK_THRESHOLD_MED));
        constants.putRawData(AnalysisKeys.DISK_HIGH, config.get(DisplaySettings.GRAPH_DISK_THRESHOLD_HIGH));
        constants.putRawData(AnalysisKeys.ACTIVITY_PIE_COLORS, theme.getValue(ThemeVal.GRAPH_ACTIVITY_PIE));
        constants.putRawData(AnalysisKeys.GM_PIE_COLORS, theme.getValue(ThemeVal.GRAPH_GM_PIE));
        constants.putRawData(AnalysisKeys.PLAYERS_GRAPH_COLOR, theme.getValue(ThemeVal.GRAPH_PLAYERS_ONLINE));
        constants.putRawData(AnalysisKeys.TPS_LOW_COLOR, theme.getValue(ThemeVal.GRAPH_TPS_LOW));
        constants.putRawData(AnalysisKeys.TPS_MEDIUM_COLOR, theme.getValue(ThemeVal.GRAPH_TPS_MED));
        constants.putRawData(AnalysisKeys.TPS_HIGH_COLOR, theme.getValue(ThemeVal.GRAPH_TPS_HIGH));
        constants.putRawData(AnalysisKeys.WORLD_MAP_LOW_COLOR, theme.getValue(ThemeVal.WORLD_MAP_LOW));
        constants.putRawData(AnalysisKeys.WORLD_MAP_HIGH_COLOR, theme.getValue(ThemeVal.WORLD_MAP_HIGH));
        constants.putRawData(AnalysisKeys.WORLD_PIE_COLORS, theme.getValue(ThemeVal.GRAPH_WORLD_PIE));
        constants.putRawData(AnalysisKeys.AVG_PING_COLOR, theme.getValue(ThemeVal.GRAPH_AVG_PING));
        constants.putRawData(AnalysisKeys.MAX_PING_COLOR, theme.getValue(ThemeVal.GRAPH_MAX_PING));
        constants.putRawData(AnalysisKeys.MIN_PING_COLOR, theme.getValue(ThemeVal.GRAPH_MIN_PING));

        placeholders.addAllPlaceholdersFrom(constants,
                FIRST_DAY, TPS_MEDIUM, TPS_HIGH,
                DISK_MEDIUM, DISK_HIGH,
                PLAYERS_MAX, PLAYERS_ONLINE, PLAYERS_TOTAL,

                WORLD_PIE_COLORS, GM_PIE_COLORS, ACTIVITY_PIE_COLORS,
                PLAYERS_GRAPH_COLOR, TPS_HIGH_COLOR, TPS_MEDIUM_COLOR,
                TPS_LOW_COLOR, WORLD_MAP_HIGH_COLOR, WORLD_MAP_LOW_COLOR,
                AVG_PING_COLOR, MAX_PING_COLOR, MIN_PING_COLOR
        );

        placeholders.put("backButton", serverInfo.getServer().isProxy() ? Html.BACK_BUTTON_NETWORK.create() : "");
        placeholders.put("contributors", Contributors.generateContributorHtml());
        placeholders.put("version", versionCheckSystem.getUpdateButton().orElse(versionCheckSystem.getCurrentVersionButton()));
        placeholders.put("updateModal", versionCheckSystem.getUpdateModal());

        CachingSupplier<ServerPluginTabs> pluginTabs = new CachingSupplier<>(() -> {
            List<ExtensionData> extensionData = dbSystem.getDatabase().query(new ExtensionServerDataQuery(serverUUID));
            return new ServerPluginTabs(extensionData, formatters);
        });

        String nav = JSONCache.getOrCacheString(DataID.EXTENSION_NAV, serverUUID, () -> pluginTabs.get().getNav());
        String tabs = JSONCache.getOrCacheString(DataID.EXTENSION_TABS, serverUUID, () -> pluginTabs.get().getTabs());

        placeholders.put("navPluginsTabs", nav);
        placeholders.put("tabsPlugins", tabs);

        return placeholders.apply(templateHtml);
    }
}