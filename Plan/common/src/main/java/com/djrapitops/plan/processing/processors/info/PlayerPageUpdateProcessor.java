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
package com.djrapitops.plan.processing.processors.info;

import com.djrapitops.plan.delivery.export.HtmlExport;
import com.djrapitops.plan.delivery.export.JSONExport;
import com.djrapitops.plan.delivery.webserver.cache.PageId;
import com.djrapitops.plan.delivery.webserver.cache.ResponseCache;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.config.paths.ExportSettings;

import java.util.UUID;

public class PlayerPageUpdateProcessor implements Runnable {

    private final UUID playerUUID;

    private final PlanConfig config;
    private final HtmlExport htmlExport;
    private final JSONExport jsonExport;

    PlayerPageUpdateProcessor(
            UUID playerUUID,
            PlanConfig config,
            HtmlExport htmlExport,
            JSONExport jsonExport
    ) {
        this.playerUUID = playerUUID;
        this.config = config;
        this.htmlExport = htmlExport;
        this.jsonExport = jsonExport;
    }

    @Override
    public void run() {
        ResponseCache.clearResponse(PageId.PLAYER.of(playerUUID));

        if (config.get(ExportSettings.EXPORT_ON_ONLINE_STATUS_CHANGE)) {
            if (config.get(ExportSettings.PLAYER_JSON)) {
                jsonExport.exportPlayerJSON(playerUUID);
            }
            if (config.get(ExportSettings.PLAYER_PAGES)) {
                htmlExport.exportPlayerPage(playerUUID);
            }
        }
    }
}
