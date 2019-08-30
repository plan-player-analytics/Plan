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
package com.djrapitops.plan.delivery.export;

import com.djrapitops.plan.delivery.rendering.json.JSONFactory;
import com.djrapitops.plan.delivery.webserver.response.ResponseFactory;
import com.djrapitops.plan.system.identification.ServerInfo;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.config.paths.ExportSettings;
import com.djrapitops.plan.system.storage.database.DBSystem;
import com.djrapitops.plan.system.storage.database.queries.objects.UserIdentifierQueries;
import com.djrapitops.plan.system.storage.file.PlanFiles;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.error.ErrorHandler;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.UUID;

/**
 * Class in charge of exporting json files.
 *
 * @author Rsl1122
 */
@Singleton
public class JSONExport extends SpecificExport {

    private final PlanConfig config;
    private final DBSystem dbSystem;
    private final ResponseFactory responseFactory;
    private final ErrorHandler errorHandler;

    @Inject
    public JSONExport(
            PlanFiles files,
            PlanConfig config,
            DBSystem dbSystem,
            ServerInfo serverInfo,
            JSONFactory jsonFactory,
            ResponseFactory responseFactory,
            ErrorHandler errorHandler
    ) {
        super(files, jsonFactory, serverInfo);
        this.config = config;
        this.dbSystem = dbSystem;
        this.responseFactory = responseFactory;
        this.errorHandler = errorHandler;
    }

    @Override
    protected String getPath() {
        return config.get(ExportSettings.JSON_EXPORT_PATH);
    }

    public void exportPlayerJSON(UUID playerUUID) {
        String json = responseFactory.rawPlayerPageResponse(playerUUID).getContent();

        dbSystem.getDatabase().query(UserIdentifierQueries.fetchPlayerNameOf(playerUUID))
                .ifPresent(playerName -> {
                    try {
                        File htmlLocation = getPlayerFolder();
                        htmlLocation.mkdirs();
                        File exportFile = new File(htmlLocation, URLEncoder.encode(playerName, "UTF-8") + ".json");

                        export(exportFile, Collections.singletonList(json));
                    } catch (IOException e) {
                        errorHandler.log(L.WARN, this.getClass(), e);
                    }
                });
    }

    public void exportServerJSON(UUID serverUUID) {
        // TODO Export JSON Parser results
    }
}
