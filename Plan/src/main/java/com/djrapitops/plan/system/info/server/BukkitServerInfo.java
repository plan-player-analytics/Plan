/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the LGNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  LGNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.system.info.server;

import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.api.exceptions.database.DBOpException;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.info.server.properties.ServerProperties;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.webserver.WebServer;
import dagger.Lazy;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

/**
 * Manages the Server UUID for Bukkit servers.
 * <p>
 * Also manages Server ID required for MySQL database independence.
 *
 * @author Rsl1122
 */
@Singleton
public class BukkitServerInfo extends ServerInfo {

    private final Lazy<WebServer> webServer;
    private final PlanConfig config;
    private ServerInfoFile serverInfoFile;
    private DBSystem dbSystem;

    @Inject
    public BukkitServerInfo(
            ServerProperties serverProperties,
            ServerInfoFile serverInfoFile,
            DBSystem dbSystem,
            Lazy<WebServer> webServer,
            PlanConfig config
    ) {
        super(serverProperties);
        this.serverInfoFile = serverInfoFile;
        this.dbSystem = dbSystem;
        this.webServer = webServer;
        this.config = config;
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
    protected Server loadServerInfo() throws EnableException {
        Optional<UUID> serverUUID = serverInfoFile.getUUID();
        try {
            return serverUUID.isPresent() ? updateDbInfo(serverUUID.get()) : registerServer();
        } catch (DBOpException e) {
            String causeMsg = e.getCause().getMessage();
            throw new EnableException("Failed to read Server information from Database: " + causeMsg, e);
        } catch (IOException e) {
            throw new EnableException("Failed to read ServerInfoFile.yml", e);
        }
    }

    private Server updateDbInfo(UUID serverUUID) throws IOException {
        Database database = dbSystem.getDatabase();
        Optional<Integer> serverID = database.fetch().getServerID(serverUUID);
        if (!serverID.isPresent()) {
            return registerServer(serverUUID);
        }
        String name = config.getString(Settings.SERVER_NAME).replaceAll("[^a-zA-Z0-9_\\s]", "_");
        String webAddress = webServer.get().getAccessAddress();
        if ("plan".equalsIgnoreCase(name)) {
            name = "Server " + serverID.get();
        }
        int maxPlayers = serverProperties.getMaxPlayers();

        Server server = new Server(serverID.get(), serverUUID, name, webAddress, maxPlayers);
        database.save().serverInfoForThisServer(server);
        return server;
    }

    private Server registerServer() throws IOException {
        return registerServer(generateNewUUID());
    }

    private Server registerServer(UUID serverUUID) throws IOException {
        String webAddress = webServer.get().getAccessAddress();
        String name = config.getString(Settings.SERVER_NAME).replaceAll("[^a-zA-Z0-9_\\s]", "_");
        int maxPlayers = serverProperties.getMaxPlayers();

        Server server = new Server(-1, serverUUID, name, webAddress, maxPlayers);

        Database database = dbSystem.getDatabase();
        database.save().serverInfoForThisServer(server);

        Optional<Integer> serverID = database.fetch().getServerID(serverUUID);
        int id = serverID.orElseThrow(() -> new IllegalStateException("Failed to Register Server (ID not found)"));
        server.setId(id);

        serverInfoFile.saveServerUUID(serverUUID);
        return server;
    }

    private UUID generateNewUUID() {
        String seed = serverProperties.getServerId() +
                serverProperties.getName() +
                serverProperties.getIp() +
                serverProperties.getPort() +
                serverProperties.getVersion() +
                serverProperties.getImplVersion();
        return UUID.nameUUIDFromBytes(seed.getBytes());
    }
}