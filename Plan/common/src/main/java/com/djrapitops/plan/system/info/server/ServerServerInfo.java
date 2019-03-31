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
package com.djrapitops.plan.system.info.server;

import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.api.exceptions.database.DBOpException;
import com.djrapitops.plan.db.Database;
import com.djrapitops.plan.db.access.queries.objects.ServerQueries;
import com.djrapitops.plan.db.access.transactions.StoreServerInformationTransaction;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.info.server.properties.ServerProperties;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.paths.PluginSettings;
import com.djrapitops.plan.system.webserver.WebServer;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import dagger.Lazy;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * Manages the Server UUID for Bukkit servers.
 * <p>
 * Also manages Server ID required for MySQL database independence.
 *
 * @author Rsl1122
 */
@Singleton
public class ServerServerInfo extends ServerInfo {

    private final ServerInfoFile serverInfoFile;

    private final PlanConfig config;
    private final Processing processing;
    private final DBSystem dbSystem;
    private final Lazy<WebServer> webServer;
    private final ErrorHandler errorHandler;

    @Inject
    public ServerServerInfo(
            ServerProperties serverProperties,
            ServerInfoFile serverInfoFile,
            Processing processing,
            PlanConfig config,
            DBSystem dbSystem,
            Lazy<WebServer> webServer,
            ErrorHandler errorHandler
    ) {
        super(serverProperties);
        this.serverInfoFile = serverInfoFile;
        this.processing = processing;
        this.dbSystem = dbSystem;
        this.webServer = webServer;
        this.config = config;
        this.errorHandler = errorHandler;
    }

    @Override
    public void enable() throws EnableException {
        try {
            serverInfoFile.prepare();
        } catch (IOException e) {
            throw new EnableException("Failed to read ServerInfoFile.yml", e);
        }
        super.enable();
    }

    @Override
    protected void loadServerInfo() throws EnableException {
        Optional<UUID> serverUUID = serverInfoFile.getUUID();
        try {
            if (serverUUID.isPresent()) {
                server = createServerObject(serverUUID.get());
                processing.submitNonCritical(() -> updateDbInfo(serverUUID.get()));
            } else {
                server = registerServer();
            }
        } catch (DBOpException e) {
            String causeMsg = e.getMessage();
            throw new EnableException("Failed to read Server information from Database: " + causeMsg, e);
        } catch (IOException e) {
            throw new EnableException("Failed to read ServerInfoFile.yml", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            throw new EnableException("Failed to perform a database transaction to store the server information", e);
        }
    }

    private Server updateDbInfo(UUID serverUUID) throws InterruptedException, ExecutionException, IOException {
        Database db = dbSystem.getDatabase();

        Optional<Server> foundServer = db.query(ServerQueries.fetchServerMatchingIdentifier(serverUUID));
        if (!foundServer.isPresent()) {
            server = registerServer(serverUUID);
            return;
        }

        server = foundServer.get();

        // Update information
        String name = config.get(PluginSettings.SERVER_NAME).replaceAll("[^a-zA-Z0-9_\\s]", "_");
        server.setName("plan".equalsIgnoreCase(name) ? "Server " + server.getId() : name);

        String webAddress = webServer.get().getAccessAddress();
        server.setWebAddress(webAddress);

        int maxPlayers = serverProperties.getMaxPlayers();
        server.setMaxPlayers(maxPlayers);

        // Save
        db.executeTransaction(new StoreServerInformationTransaction(server));
    }

    private Server registerServer() throws Exception {
        return registerServer(generateNewUUID());
    }

    private Server registerServer(UUID serverUUID) throws ExecutionException, InterruptedException, IOException {
        Database db = dbSystem.getDatabase();

        Server server = createServerObject(serverUUID);

        // Save
        db.executeTransaction(new StoreServerInformationTransaction(server))
                .get(); // Wait until transaction has completed

        // Load from database
        server = db.query(ServerQueries.fetchServerMatchingIdentifier(serverUUID))
                .orElseThrow(() -> new IllegalStateException("Failed to Register Server (ID not found)"));

        // Store the UUID in ServerInfoFile
        serverInfoFile.saveServerUUID(serverUUID);
        return server;
    }

    private Server createServerObject(UUID serverUUID) {
        String webAddress = webServer.get().getAccessAddress();
        String name = config.get(PluginSettings.SERVER_NAME).replaceAll("[^a-zA-Z0-9_\\s]", "_");
        int maxPlayers = serverProperties.getMaxPlayers();
        return new Server(-1, serverUUID, name, webAddress, maxPlayers);
    }
}