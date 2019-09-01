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

import com.djrapitops.plan.exceptions.ParseException;
import com.djrapitops.plan.exceptions.connection.NotFoundException;
import com.djrapitops.plan.identification.Server;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.ExportSettings;
import com.djrapitops.plan.storage.file.PlanFiles;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.error.ErrorHandler;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Handles export for different pages.
 *
 * @author Rsl1122
 */
@Singleton
public class Exporter {

    private final PlanFiles files;
    private final PlanConfig config;
    private final ServerPageExporter serverPageExporter;
    private final ErrorHandler errorHandler;

    @Inject
    public Exporter(
            PlanFiles files,
            PlanConfig config,
            ServerPageExporter serverPageExporter,
            ErrorHandler errorHandler
    ) {
        this.files = files;
        this.config = config;
        this.serverPageExporter = serverPageExporter;
        this.errorHandler = errorHandler;
    }

    public Path getPageExportDirectory() {
        Path exportDirectory = Paths.get(config.get(ExportSettings.HTML_EXPORT_PATH));
        return exportDirectory.isAbsolute()
                ? exportDirectory
                : files.getDataDirectory().resolve(exportDirectory);
    }

    public void exportServerPage(Server server) {
        try {
            serverPageExporter.export(getPageExportDirectory(), server);
        } catch (IOException | NotFoundException | ParseException e) {
            errorHandler.log(L.WARN, this.getClass(), e);
        }
    }
}