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

import com.djrapitops.plan.exceptions.ExportException;
import com.djrapitops.plan.exceptions.ParseException;
import com.djrapitops.plan.exceptions.connection.NotFoundException;
import com.djrapitops.plan.identification.Server;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.ExportSettings;
import com.djrapitops.plan.storage.file.PlanFiles;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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
    private final NetworkPageExporter networkPageExporter;

    private final Set<UUID> failedServers;

    @Inject
    public Exporter(
            PlanFiles files,
            PlanConfig config,
            ServerPageExporter serverPageExporter,
            NetworkPageExporter networkPageExporter
    ) {
        this.files = files;
        this.config = config;
        this.serverPageExporter = serverPageExporter;
        this.networkPageExporter = networkPageExporter;

        failedServers = new HashSet<>();
    }

    private Path getPageExportDirectory() {
        Path exportDirectory = Paths.get(config.get(ExportSettings.HTML_EXPORT_PATH));
        return exportDirectory.isAbsolute()
                ? exportDirectory
                : files.getDataDirectory().resolve(exportDirectory);
    }

    /**
     * Export a page of a server.
     *
     * @param server Server which page is going to be exported
     * @return false if the page was not exported due to previous failure.
     * @throws ExportException If the export failed
     */
    public boolean exportServerPage(Server server) throws ExportException {
        UUID serverUUID = server.getUuid();
        if (failedServers.contains(serverUUID)) return false;

        try {
            Path toDirectory = getPageExportDirectory();
            if (server.isProxy()) {
                networkPageExporter.export(toDirectory, server);
            } else {
                serverPageExporter.export(toDirectory, server);
            }
            return true;
        } catch (IOException | NotFoundException | ParseException e) {
            failedServers.add(serverUUID);
            throw new ExportException("Failed to export server: " + server.getIdentifiableName() + " (Attempts disabled until next reload), " + e.getMessage(), e);
        }
    }
}