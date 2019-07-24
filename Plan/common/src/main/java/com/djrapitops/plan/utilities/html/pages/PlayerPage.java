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
import com.djrapitops.plan.data.store.containers.PlayerContainer;
import com.djrapitops.plan.data.store.keys.PlayerKeys;
import com.djrapitops.plan.system.file.PlanFiles;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.theme.Theme;
import com.djrapitops.plan.system.settings.theme.ThemeVal;
import com.djrapitops.plan.system.update.VersionCheckSystem;
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
        try {
            if (!player.getValue(PlayerKeys.REGISTERED).isPresent()) {
                throw new IllegalStateException("Player is not registered");
            }

            return parse(player);
        } catch (Exception e) {
            throw new ParseException(e);
        }
    }

    public String parse(PlayerContainer player) throws IOException {
        long now = System.currentTimeMillis();
        UUID playerUUID = player.getUnsafe(PlayerKeys.UUID);

        PlaceholderReplacer replacer = new PlaceholderReplacer();

        replacer.put("refresh", clockLongFormatter.apply(now));
        replacer.put("refreshFull", secondLongFormatter.apply(now));
        replacer.put("version", versionCheckSystem.getCurrentVersion());
        replacer.put("update", versionCheckSystem.getUpdateHtml().orElse(""));
        replacer.put("timeZone", config.getTimeZoneOffsetHours());

        String playerName = player.getValue(PlayerKeys.NAME).orElse(playerUUID.toString());
        replacer.put("playerName", playerName);

        replacer.put("worldPieColors", theme.getValue(ThemeVal.GRAPH_WORLD_PIE));
        replacer.put("gmPieColors", theme.getValue(ThemeVal.GRAPH_GM_PIE));
        replacer.put("serverPieColors", theme.getValue(ThemeVal.GRAPH_SERVER_PREF_PIE));
        replacer.put("firstDay", 1);

        if (serverInfo.getServer().isProxy()) {
            replacer.put("backButton", "<li><a title=\"to Network page\" href=\"/network\"><i class=\"material-icons\">arrow_back</i><i class=\"material-icons\">cloud</i></a></li>");
        } else {
            replacer.put("backButton", "<li><a title=\"to Server page\" href=\"/server\"><i class=\"material-icons\">arrow_back</i><i class=\"material-icons\">storage</i></a></li>");
        }

        InspectPluginTab pluginTabs = pageFactory.inspectPluginTabs(playerUUID);

        replacer.put("navPluginsTabs", pluginTabs.getNav());
        replacer.put("pluginsTabs", pluginTabs.getTab());

        return replacer.apply(files.getCustomizableResourceOrDefault("web/player.html").asString());
    }
}
