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
package com.djrapitops.plan.system.delivery.rendering.pages;

import com.djrapitops.plan.exceptions.ParseException;
import com.djrapitops.plan.system.delivery.domain.container.PlayerContainer;
import com.djrapitops.plan.system.delivery.domain.keys.PlayerKeys;
import com.djrapitops.plan.system.delivery.rendering.html.Html;
import com.djrapitops.plan.system.identification.ServerInfo;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.theme.Theme;
import com.djrapitops.plan.system.settings.theme.ThemeVal;
import com.djrapitops.plan.system.storage.file.PlanFiles;
import com.djrapitops.plan.system.version.VersionCheckSystem;
import com.djrapitops.plan.utilities.formatting.Formatter;
import com.djrapitops.plan.utilities.formatting.Formatters;
import com.djrapitops.plan.utilities.formatting.PlaceholderReplacer;

import java.io.IOException;
import java.util.UUID;

/**
 * Used for parsing Inspect page out of database data and the html.
 *
 * @author Rsl1122
 */
public class PlayerPage implements Page {

    private final PlayerContainer player;

    private final VersionCheckSystem versionCheckSystem;

    private final PlanFiles files;
    private final PlanConfig config;
    private final PageFactory pageFactory;
    private final Theme theme;
    private final ServerInfo serverInfo;

    private final Formatter<Long> clockLongFormatter;
    private final Formatter<Long> secondLongFormatter;

    PlayerPage(
            PlayerContainer player,
            VersionCheckSystem versionCheckSystem,
            PlanFiles files,
            PlanConfig config,
            PageFactory pageFactory,
            Theme theme,
            Formatters formatters,
            ServerInfo serverInfo
    ) {
        this.player = player;
        this.versionCheckSystem = versionCheckSystem;
        this.files = files;
        this.config = config;
        this.pageFactory = pageFactory;
        this.theme = theme;
        this.serverInfo = serverInfo;

        clockLongFormatter = formatters.clockLong();
        secondLongFormatter = formatters.secondLong();
    }

    @Override
    public String toHtml() throws ParseException {
        if (!player.getValue(PlayerKeys.REGISTERED).isPresent()) {
            throw new IllegalStateException("Player is not registered");
        }
        try {
            return parse(player);
        } catch (Exception e) {
            throw new ParseException(e);
        }
    }

    public String parse(PlayerContainer player) throws IOException {
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

        placeholders.put("backButton", (serverInfo.getServer().isProxy() ? Html.BACK_BUTTON_NETWORK : Html.BACK_BUTTON_SERVER).parse());

        PlayerPluginTab pluginTabs = pageFactory.inspectPluginTabs(playerUUID);

        placeholders.put("navPluginsTabs", pluginTabs.getNav());
        placeholders.put("pluginsTabs", pluginTabs.getTab());

        return placeholders.apply(files.getCustomizableResourceOrDefault("web/player.html").asString());
    }
}
