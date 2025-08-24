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

import com.djrapitops.plan.delivery.web.resolver.exception.NotFoundException;
import com.djrapitops.plan.exceptions.ExportException;
import com.djrapitops.plan.identification.Server;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.ExportSettings;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Handles export for different pages.
 *
 * @author AuroraLS3
 */
@Singleton
public class Exporter extends FileExporter {

    private final PlanConfig config;
    private final PlayerJSONExporter playerJSONExporter;
    private final PlayerPageExporter playerPageExporter;
    private final PlayersPageExporter playersPageExporter;
    private final ServerPageExporter serverPageExporter;
    private final NetworkPageExporter networkPageExporter;

    private final Set<ServerUUID> failedServers;
    private final ReactExporter reactExporter;

    @Inject
    public Exporter(
            PlanConfig config,
            PlayerJSONExporter playerJSONExporter,
            PlayerPageExporter playerPageExporter,
            PlayersPageExporter playersPageExporter,
            ServerPageExporter serverPageExporter,
            NetworkPageExporter networkPageExporter,
            ReactExporter reactExporter
    ) {
        this.config = config;
        this.playerJSONExporter = playerJSONExporter;
        this.playerPageExporter = playerPageExporter;
        this.playersPageExporter = playersPageExporter;
        this.serverPageExporter = serverPageExporter;
        this.networkPageExporter = networkPageExporter;
        this.reactExporter = reactExporter;

        failedServers = new HashSet<>();
    }

    /**
     * Export a page of a server.
     *
     * @param server Server which page is going to be exported
     * @return false if the page was not exported due to previous failure or is disabled in config.
     * @throws ExportException If the export failed due to IO, NotFound or GenerationException.
     */
    public boolean exportServerPage(Server server) throws ExportException {
        ServerUUID serverUUID = server.getUuid();
        if (failedServers.contains(serverUUID) || config.isFalse(ExportSettings.SERVER_PAGE)) return false;

        try {
            Path toDirectory = config.getPageExportPath();
            if (server.isProxy()) {
                networkPageExporter.export(toDirectory, server);
            } else {
                serverPageExporter.export(toDirectory, server);
            }
            return true;
        } catch (IOException | NotFoundException e) {
            failedServers.add(serverUUID);
            throw new ExportException("Failed to export server: " + server.getIdentifiableName() + " (Attempts disabled until next reload), " + e.toString(), e);
        }
    }

    public boolean exportServerJSON(Server server) throws ExportException {
        ServerUUID serverUUID = server.getUuid();
        if (failedServers.contains(serverUUID) || config.isFalse(ExportSettings.SERVER_JSON)) return false;

        try {
            Path toDirectory = config.getJSONExportPath().resolve(toFileName(server.getName()));
            if (server.isProxy()) {
                networkPageExporter.exportJSON(new ExportPaths(), toDirectory, server);
            } else {
                serverPageExporter.exportJSON(toDirectory, server);
            }
            return true;
        } catch (IOException | NotFoundException e) {
            failedServers.add(serverUUID);
            throw new ExportException("Failed to export server: " + server.getIdentifiableName() + " (Attempts disabled until next reload), " + e.toString(), e);
        }
    }

    /**
     * Export page of a player.
     *
     * @param playerUUID UUID of the player.
     * @param playerName Name of the player.
     * @return false if the page was not exported due to config settings.
     * @throws ExportException If the export failed due to IO, NotFound or GenerationException.
     */
    public boolean exportPlayerPage(UUID playerUUID, String playerName) throws ExportException {
        Path toDirectory = config.getPageExportPath();
        if (config.isFalse(ExportSettings.PLAYER_PAGES)) return false;

        try {
            playerPageExporter.export(toDirectory, playerUUID);
            return true;
        } catch (IOException | NotFoundException e) {
            throw new ExportException("Failed to export player: " + playerName + ", " + e.toString(), e);
        }
    }

    public boolean exportPlayersPage() throws ExportException {
        Path toDirectory = config.getPageExportPath();
        if (config.isFalse(ExportSettings.PLAYERS_PAGE)) return false;

        try {
            playersPageExporter.export(toDirectory);
            return true;
        } catch (IOException | NotFoundException e) {
            throw new ExportException("Failed to export players page, " + e.toString(), e);
        }
    }

    /**
     * Export Raw Data JSON of a player.
     *
     * @param playerUUID UUID of the player.
     * @param playerName Name of the player.
     * @return false if the json was not exported due to config settings.
     * @throws ExportException If the export failed due to IOException.
     */
    public boolean exportPlayerJSON(UUID playerUUID, String playerName) throws ExportException {
        Path toDirectory = config.getJSONExportPath();
        if (config.isFalse(ExportSettings.PLAYER_JSON)) return false;

        try {
            playerJSONExporter.export(toDirectory, playerUUID, playerName);
            return true;
        } catch (IOException e) {
            throw new ExportException("Failed to export player: " + playerName + ", " + e.toString(), e);
        }
    }

    public void exportReact() throws ExportException {
        if (config.isFalse(ExportSettings.PLAYER_PAGES)
                && config.isFalse(ExportSettings.SERVER_PAGE)
                && config.isFalse(ExportSettings.PLAYERS_PAGE)) {
            return;
        }

        Path toDirectory = config.getPageExportPath();

        try {
            reactExporter.exportReactFiles(toDirectory);
        } catch (IOException e) {
            throw new ExportException("Failed to export react: " + e.toString(), e);
        }
    }
}