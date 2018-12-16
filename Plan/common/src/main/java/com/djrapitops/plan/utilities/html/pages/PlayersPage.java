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
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.file.PlanFiles;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.paths.ProxySettings;
import com.djrapitops.plan.system.update.VersionCheckSystem;
import com.djrapitops.plan.utilities.formatting.PlaceholderReplacer;
import com.djrapitops.plan.utilities.html.tables.HtmlTables;
import com.djrapitops.plugin.api.Check;
import com.djrapitops.plugin.benchmarking.Timings;

import java.util.List;

/**
 * Html String parser for /players page.
 *
 * @author Rsl1122
 */
public class PlayersPage implements Page {

    private final VersionCheckSystem versionCheckSystem;
    private final PlanFiles files;
    private final PlanConfig config;
    private final Database database;
    private final ServerInfo serverInfo;

    private final HtmlTables tables;

    private final Timings timings;

    PlayersPage(
            VersionCheckSystem versionCheckSystem,
            PlanFiles files,
            PlanConfig config,
            Database database,
            ServerInfo serverInfo,
            HtmlTables tables,
            Timings timings
    ) {
        this.versionCheckSystem = versionCheckSystem;
        this.files = files;
        this.config = config;
        this.database = database;
        this.serverInfo = serverInfo;
        this.tables = tables;
        this.timings = timings;
    }

    @Override
    public String toHtml() throws ParseException {
        try {
            PlaceholderReplacer placeholderReplacer = new PlaceholderReplacer();

            placeholderReplacer.put("version", versionCheckSystem.getCurrentVersion());
            placeholderReplacer.put("update", versionCheckSystem.getUpdateHtml().orElse(""));
            if (Check.isBukkitAvailable()) {
                placeholderReplacer.put("networkName", serverInfo.getServer().getName());
            } else {
                placeholderReplacer.put("networkName", config.get(ProxySettings.NETWORK_NAME));
            }

            timings.start("Players page players table parsing");
            List<PlayerContainer> playerContainers = database.fetch().getAllPlayerContainers();
            placeholderReplacer.put("playersTable", tables.playerTableForPlayersPage(playerContainers).parseHtml());
            timings.end("Pages", "Players page players table parsing");

            return placeholderReplacer.apply(files.readCustomizableResourceFlat("web/players.html"));
        } catch (Exception e) {
            throw new ParseException(e);
        }
    }
}