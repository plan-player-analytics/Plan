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

import com.djrapitops.plan.delivery.domain.container.PlayerContainer;
import com.djrapitops.plan.delivery.domain.keys.PlayerKeys;
import com.djrapitops.plan.delivery.formatting.Formatter;
import com.djrapitops.plan.delivery.formatting.Formatters;
import com.djrapitops.plan.delivery.formatting.PlaceholderReplacer;
import com.djrapitops.plan.delivery.rendering.html.Contributors;
import com.djrapitops.plan.delivery.rendering.html.Html;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.theme.Theme;
import com.djrapitops.plan.settings.theme.ThemeVal;
import com.djrapitops.plan.version.VersionCheckSystem;

import java.util.UUID;

/**
 * Html String generator for /player page.
 *
 * @author Rsl1122
 */
public class PlayerPage implements Page {

    private final String templateHtml;
    private final PlayerContainer player;

    private final VersionCheckSystem versionCheckSystem;

    private final PlanConfig config;
    private final PageFactory pageFactory;
    private final Theme theme;
    private final ServerInfo serverInfo;

    private final Formatter<Long> clockLongFormatter;
    private final Formatter<Long> secondLongFormatter;

    PlayerPage(
            String templateHtml,
            PlayerContainer player,
            VersionCheckSystem versionCheckSystem,
            PlanConfig config,
            PageFactory pageFactory,
            Theme theme,
            Formatters formatters,
            ServerInfo serverInfo
    ) {
        this.templateHtml = templateHtml;
        this.player = player;
        this.versionCheckSystem = versionCheckSystem;
        this.config = config;
        this.pageFactory = pageFactory;
        this.theme = theme;
        this.serverInfo = serverInfo;

        clockLongFormatter = formatters.clockLong();
        secondLongFormatter = formatters.secondLong();
    }

    @Override
    public String toHtml() {
        if (!player.getValue(PlayerKeys.REGISTERED).isPresent()) {
            throw new IllegalStateException("Player is not registered");
        }
        return createFor(player);
    }

    public String createFor(PlayerContainer player) {
        long now = System.currentTimeMillis();
        UUID playerUUID = player.getUnsafe(PlayerKeys.UUID);

        PlaceholderReplacer placeholders = new PlaceholderReplacer();

        placeholders.put("refresh", clockLongFormatter.apply(now));
        placeholders.put("refreshFull", secondLongFormatter.apply(now));
        placeholders.put("version", versionCheckSystem.getUpdateButton().orElse(versionCheckSystem.getCurrentVersionButton()));
        placeholders.put("updateModal", versionCheckSystem.getUpdateModal());
        placeholders.put("timeZone", config.getTimeZoneOffsetHours());

        String playerName = player.getValue(PlayerKeys.NAME).orElse(playerUUID.toString());
        placeholders.put("playerName", playerName);

        placeholders.put("worldPieColors", theme.getValue(ThemeVal.GRAPH_WORLD_PIE));
        placeholders.put("gmPieColors", theme.getValue(ThemeVal.GRAPH_GM_PIE));
        placeholders.put("serverPieColors", theme.getValue(ThemeVal.GRAPH_SERVER_PREF_PIE));
        placeholders.put("firstDay", 1);

        placeholders.put("backButton", (serverInfo.getServer().isProxy() ? Html.BACK_BUTTON_NETWORK : Html.BACK_BUTTON_SERVER).create());
        placeholders.put("contributors", Contributors.generateContributorHtml());

        PlayerPluginTab pluginTabs = pageFactory.inspectPluginTabs(playerUUID);

        placeholders.put("navPluginsTabs", pluginTabs.getNav());
        placeholders.put("pluginsTabs", pluginTabs.getTab());

        return placeholders.apply(templateHtml);
    }
}
