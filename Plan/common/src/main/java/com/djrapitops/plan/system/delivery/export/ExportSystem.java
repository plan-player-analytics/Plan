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
package com.djrapitops.plan.system.delivery.export;

import com.djrapitops.plan.system.SubSystem;
import com.djrapitops.plan.system.identification.ServerInfo;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.config.paths.ExportSettings;
import com.djrapitops.plan.system.storage.database.DBSystem;
import com.djrapitops.plan.system.storage.database.Database;
import com.djrapitops.plan.system.storage.database.queries.objects.ServerQueries;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * System in charge of exporting html.
 *
 * @author Rsl1122
 */
@Singleton
public class ExportSystem implements SubSystem {

    private final PlanConfig config;
    private final DBSystem dbSystem;
    private final ServerInfo serverInfo;
    private final Processing processing;
    private final HtmlExport htmlExport;

    @Inject
    public ExportSystem(
            PlanConfig config,
            DBSystem dbSystem,
            ServerInfo serverInfo,
            Processing processing,
            HtmlExport htmlExport
    ) {
        this.config = config;
        this.dbSystem = dbSystem;
        this.serverInfo = serverInfo;
        this.processing = processing;
        this.htmlExport = htmlExport;
    }

    @Override
    public void enable() {
        Database database = dbSystem.getDatabase();
        boolean hasProxy = database.query(ServerQueries.fetchProxyServerInformation()).isPresent();
        if (serverInfo.getServer().isNotProxy() && hasProxy) {
            return;
        }

        if (config.isTrue(ExportSettings.JS_AND_CSS)) {
            processing.submitNonCritical(htmlExport::exportJs);
            processing.submitNonCritical(htmlExport::exportCss);
            processing.submitNonCritical(htmlExport::exportPlugins);
        }
        if (config.isTrue(ExportSettings.PLAYERS_PAGE)) {
            processing.submitNonCritical(htmlExport::exportPlayersPage);
        }
        if (config.isTrue(ExportSettings.PLAYER_PAGES)) {
            processing.submitNonCritical(htmlExport::exportAvailablePlayers);
        }
        if (config.isTrue(ExportSettings.SERVER_PAGE)) {
            processing.submitNonCritical(() -> {
                htmlExport.cacheNetworkPage();
                htmlExport.exportAvailableServerPages();
            });
        }
    }

    @Override
    public void disable() {
        // Nothing to disable
    }
}