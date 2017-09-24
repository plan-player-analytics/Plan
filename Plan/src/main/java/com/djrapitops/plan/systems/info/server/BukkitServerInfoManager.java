/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.info.server;


import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.ServerVariableHolder;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.api.exceptions.PlanEnableException;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.database.tables.ServerTable;

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
public class BukkitServerInfoManager {

    private final Plan plugin;
    private ServerInfo serverInfo;
    private ServerInfoFile serverInfoFile;
    private final ServerTable serverTable;

    public BukkitServerInfoManager(Plan plugin) throws PlanEnableException {
        this.plugin = plugin;
        Database db = plugin.getDB();
        serverTable = db.getServerTable();

        try {
            serverInfoFile = new ServerInfoFile(plugin);
        } catch (IOException e) {
            throw new PlanEnableException("Failed to read ServerInfoFile.yml", e);
        }

        Optional<UUID> serverUUID = serverInfoFile.getUUID();

        try {
            if (serverUUID.isPresent()) {
                updateDbInfo(serverUUID.get());
            } else {
                registerServer();
            }
        } catch (SQLException e) {
            throw new PlanEnableException("Failed to update Database server info", e);
        } catch (IOException e) {
            throw new PlanEnableException("Failed to write to ServerInfoFile.yml", e);
        }
    }

    private void updateDbInfo(UUID serverUUID) throws SQLException, IOException {
        Optional<Integer> serverID = serverTable.getServerID(serverUUID);
        if (!serverID.isPresent()) {
            registerServer(serverUUID);
            return;
        }
        String name = Settings.SERVER_NAME.toString();
        String webAddress = plugin.getWebServer().getAccessAddress();
        if ("plan".equalsIgnoreCase(name)) {
            name = "Server " + serverID.get();
        }
        int maxPlayers = plugin.getVariable().getMaxPlayers();

        serverInfo = new ServerInfo(serverID.get(), serverUUID, name, webAddress, maxPlayers);
        serverTable.saveCurrentServerInfo(serverInfo);
    }

    private void registerServer() throws SQLException, IOException {
        registerServer(generateNewUUID(plugin.getVariable()));
    }

    private void registerServer(UUID serverUUID) throws SQLException, IOException {
        String webAddress = plugin.getWebServer().getAccessAddress();
        String name = Settings.SERVER_NAME.toString();
        int maxPlayers = plugin.getVariable().getMaxPlayers();
        serverInfo = new ServerInfo(-1, serverUUID, name, webAddress, maxPlayers);
        serverTable.saveCurrentServerInfo(serverInfo);
        Optional<Integer> serverID = serverTable.getServerID(serverUUID);
        if (!serverID.isPresent()) {
            throw new IllegalStateException("Failed to Register Server (ID not found)");
        }

        int id = serverID.get();
        serverInfo.setId(id);

        serverInfoFile.saveInfo(serverInfo, new ServerInfo(-1, null, name, "", 0));
    }

    private UUID generateNewUUID(ServerVariableHolder variableHolder) {
        String seed = variableHolder.getName() + variableHolder.getIp() + variableHolder.getPort() + variableHolder.getVersion() + variableHolder.getImplVersion();
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

    /**
     * Saves Bungee connection information to local file on Bukkit servers.
     *
     * @param address
     * @throws IOException
     */
    public void saveBungeeConnectionAddress(String address) throws IOException {
        serverInfoFile.saveInfo(serverInfo, new ServerInfo(-1, null, "Bungee", address, -1));
    }

    public void markConnectionFail() {
        int timesFailed = serverInfoFile.markConnectionFail();
        if (timesFailed == -1) {
            return;
        }
        if (timesFailed >= 10) {
            try {
                serverInfoFile.saveInfo(serverInfo, new ServerInfo(-1, null, "Bungee", "", -1));
                Log.info("----------------------------------");
                Log.info("Bungee connection has failed 10 times in a row, assuming Bungee uninstalled - Restarting Plan..");
                Log.info("----------------------------------");
                plugin.restart();
            } catch (IOException e) {
                Log.toLog(this.getClass().getName(), e);
            }
        }
    }

    public void resetConnectionFails() {
        serverInfoFile.resetConnectionFails();
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