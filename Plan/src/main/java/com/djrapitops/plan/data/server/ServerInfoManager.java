/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.data.server;


import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.database.tables.ServerTable;
import org.bukkit.Server;
import org.bukkit.configuration.InvalidConfigurationException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

/**
 * Manages the Server information required for Bungee-Bukkit WebAPI connection.
 * <p>
 * Also manages Server ID required for MySQL database independence.
 *
 * @author Rsl1122
 */
public class ServerInfoManager {

    private final Plan plugin;
    private ServerInfo serverInfo;
    private ServerInfoFile serverInfoFile;
    private final ServerTable serverTable;

    public ServerInfoManager(Plan plugin) {
        this.plugin = plugin;
        Database db = plugin.getDB();
        serverTable = db.getServerTable();

        try {
            serverInfoFile = new ServerInfoFile(plugin);
        } catch (IOException | InvalidConfigurationException e) {
            Log.toLog(this.getClass().getName(), e);
            Log.error("Failed to read server info from local file, disabling plugin.");
            plugin.disablePlugin();
        }

        Optional<UUID> serverUUID = serverInfoFile.getUUID();

        try {
            if (serverUUID.isPresent()) {
                updateDbInfo(serverUUID.get());
            } else {
                registerServer();
            }
        } catch (Exception e) {
            Log.toLog(this.getClass().getName(), e);
            Log.error("Failed to register server info to database, disabling plugin.");
            plugin.disablePlugin();
        }
    }

    private void updateDbInfo(UUID serverUUID) throws SQLException {
        Optional<Integer> serverID = serverTable.getServerID(serverUUID);
        if (!serverID.isPresent()) {
            registerServer(serverUUID);
            return;
        }
        String name = Settings.SERVER_NAME.toString();
        String webAddress = plugin.getUiServer().getAccessAddress();
        if ("plan".equalsIgnoreCase(name)) {
            name = "Server" + serverID.get();
        }

        serverInfo = new ServerInfo(serverID.get(), serverUUID, name, webAddress);
        serverTable.saveCurrentServerInfo(serverInfo);
    }

    private void registerServer() throws SQLException {
        registerServer(generateNewUUID(plugin.getServer()));
    }

    private void registerServer(UUID serverUUID) throws SQLException {
        String webAddress = plugin.getUiServer().getAccessAddress();
        String name = Settings.SERVER_NAME.toString();
        serverInfo = new ServerInfo(-1, serverUUID, name, webAddress);
        serverTable.saveCurrentServerInfo(serverInfo);
        Optional<Integer> serverID = serverTable.getServerID(serverUUID);
        if (serverID.isPresent()) {
            serverInfo.setId(serverID.get());
        } else {
            throw new IllegalStateException("Failed to Register Server (ID not found)");
        }
    }

    private UUID generateNewUUID(Server server) {
        String seed = server.getName() + server.getIp() + server.getPort() + server.getVersion() + server.getBukkitVersion();
        return UUID.nameUUIDFromBytes(seed.getBytes());
    }

    public Optional<String> getBungeeConnectionAddress() {
        try {
            String bungeeWebAddress = serverInfoFile.getBungeeWebAddress();
            if (!bungeeWebAddress.isEmpty()) {
                return Optional.of(bungeeWebAddress);
            }
        } catch (Exception ignored) {
            /* Ignored */
        }
        return Optional.empty();
    }

    public void saveBungeeConnectionAddress(String address) throws IOException {
        serverInfoFile.saveInfo(serverInfo, new ServerInfo(-1, null, "Bungee", address));
    }

    public int getServerID() {
        return serverInfo.getId();
    }

    public UUID getServerUUID() {
        return serverInfo.getUuid();
    }

    public String getServerName() {
        return serverInfo.getName();
    }
}