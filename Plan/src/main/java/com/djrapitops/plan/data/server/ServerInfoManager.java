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
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public class ServerInfoManager {

    private Plan plugin;
    private ServerInfo serverInfo;
    private ServerInfoFile serverInfoFile;
    private ServerTable serverTable;

    public ServerInfoManager(Plan plugin) {
        this.plugin = plugin;
        Database db = plugin.getDB();
        if ("sqlite".equals(db.getConfigName())) {
            return;
        }
        try {
            serverInfoFile = new ServerInfoFile(plugin);
        } catch (IOException | InvalidConfigurationException e) {
            Log.toLog(this.getClass().getName(), e);
            Log.error("Failed to read server info from local file, disabling plugin.");
            plugin.disablePlugin();
        }

        serverTable = db.getServerTable();

        int serverID = serverInfoFile.getID();
        try {
            if (serverID == -1) {
                registerServer();
            } else {
                updateDbInfo(serverID);
            }
        } catch (SQLException e) {
            Log.toLog(this.getClass().getName(), e);
        }

    }

    private void updateDbInfo(int serverID) throws SQLException {
        UUID uuid = serverInfoFile.getUUID();
        String name = Settings.SERVER_NAME.toString();
        String webAddress = plugin.getUiServer().getAccessAddress();
        if ("plan".equalsIgnoreCase(name)) {
            name = "Server" + Integer.toString(serverID);
        }

        serverInfo = new ServerInfo(serverID, uuid, name, webAddress);
        serverTable.saveCurrentServerInfo(serverInfo);
    }

    private void registerServer() throws SQLException {
        UUID serverUUID = generateNewUUID(plugin.getServer());
        String webAddress = plugin.getUiServer().getAccessAddress();
        String name = Settings.SERVER_NAME.toString();
        serverInfo = new ServerInfo(-1, serverUUID, name, webAddress);
        serverTable.saveCurrentServerInfo(serverInfo);
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

    public int getServerID() {
        return serverInfo.getId();
    }

    public String getServerName() {
        return serverInfo.getName();
    }
}