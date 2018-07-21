/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.bukkit.info;

import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.api.exceptions.database.DBOpException;
import com.djrapitops.plan.bukkit.PlanBukkit;
import com.djrapitops.plan.bukkit.info.server.BukkitServerProperties;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.file.FileSystem;
import com.djrapitops.plan.system.info.server.Server;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.info.server.ServerInfoFile;
import com.djrapitops.plan.system.info.server.ServerProperties;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.webserver.WebServerSystem;

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
public class BukkitServerInfo extends ServerInfo {

    private ServerInfoFile serverInfoFile;
    private Database database;

    public BukkitServerInfo(PlanBukkit plugin) {
        this(new BukkitServerProperties(plugin.getServer()));
    }

    public BukkitServerInfo(ServerProperties serverProperties) {
        super(serverProperties);
    }

    @Override
    public void enable() throws EnableException {
        database = Database.getActive();

        try {
            serverInfoFile = new ServerInfoFile(FileSystem.getDataFolder());
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
        Optional<Integer> serverID = database.fetch().getServerID(serverUUID);
        if (!serverID.isPresent()) {
            return registerServer(serverUUID);
        }
        String name = Settings.SERVER_NAME.toString().replaceAll("[^a-zA-Z0-9_\\s]", "_");
        String webAddress = WebServerSystem.getInstance().getWebServer().getAccessAddress();
        if ("plan".equalsIgnoreCase(name)) {
            name = "Server " + serverID.get();
        }
        int maxPlayers = serverProperties.getMaxPlayers();

        Server server = new Server(serverID.get(), serverUUID, name, webAddress, maxPlayers);
        database.save().serverInfoForThisServer(server);
        return server;
    }

    private Server registerServer() throws IOException {
        return registerServer(generateNewUUID(serverProperties));
    }

    private Server registerServer(UUID serverUUID) throws IOException {
        String webAddress = WebServerSystem.getInstance().getWebServer().getAccessAddress();
        String name = Settings.SERVER_NAME.toString().replaceAll("[^a-zA-Z0-9_\\s]", "_");
        int maxPlayers = ServerInfo.getServerProperties().getMaxPlayers();

        Server server = new Server(-1, serverUUID, name, webAddress, maxPlayers);
        database.save().serverInfoForThisServer(server);

        Optional<Integer> serverID = database.fetch().getServerID(serverUUID);
        if (!serverID.isPresent()) {
            throw new IllegalStateException("Failed to Register Server (ID not found)");
        }

        int id = serverID.get();
        server.setId(id);

        serverInfoFile.saveServerUUID(serverUUID);
        return server;
    }

    private UUID generateNewUUID(ServerProperties serverProperties) {
        String seed = serverProperties.getServerId() + serverProperties.getName() + serverProperties.getIp() + serverProperties.getPort() + serverProperties.getVersion() + serverProperties.getImplVersion();
        return UUID.nameUUIDFromBytes(seed.getBytes());
    }
}