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
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.config.paths.PluginSettings;
import com.djrapitops.plan.system.settings.config.paths.ProxySettings;
import com.djrapitops.plan.system.storage.file.PlanFiles;
import com.djrapitops.plan.system.update.VersionCheckSystem;
import com.djrapitops.plan.utilities.formatting.PlaceholderReplacer;

/**
 * Html String parser for /players page.
 *
 * @author Rsl1122
 */
public class PlayersPage implements Page {

    private final VersionCheckSystem versionCheckSystem;
    private final PlanFiles files;
    private final PlanConfig config;
    private final ServerInfo serverInfo;

    PlayersPage(
            VersionCheckSystem versionCheckSystem,
            PlanFiles files,
            PlanConfig config,
            ServerInfo serverInfo
    ) {
        this.versionCheckSystem = versionCheckSystem;
        this.files = files;
        this.config = config;
        this.serverInfo = serverInfo;
    }

    @Override
    public String toHtml() throws ParseException {
        try {
            PlaceholderReplacer placeholders = new PlaceholderReplacer();

            placeholders.put("version", versionCheckSystem.getUpdateButton().orElse(versionCheckSystem.getCurrentVersionButton()));
            placeholders.put("updateModal", versionCheckSystem.getUpdateModal());
            if (serverInfo.getServer().isProxy()) {
                placeholders.put("networkName", config.get(ProxySettings.NETWORK_NAME));
            } else {
                placeholders.put("networkName", config.get(PluginSettings.SERVER_NAME));
            }

            return placeholders.apply(files.getCustomizableResourceOrDefault("web/players.html").asString());
        } catch (Exception e) {
            throw new ParseException(e);
        }
    }
}