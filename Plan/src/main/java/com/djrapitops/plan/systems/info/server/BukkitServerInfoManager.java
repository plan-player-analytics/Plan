/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.systems.info.server;


import com.djrapitops.plan.Plan;
import com.djrapitops.plan.ServerVariableHolder;
import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.database.databases.sql.tables.ServerTable;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plugin.api.utility.log.Log;

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
@Deprecated
public class BukkitServerInfoManager {

    private final Plan plugin;
    private final ServerTable serverTable;
    private ServerInfo serverInfo;
    private ServerInfoFile serverInfoFile;

    public BukkitServerInfoManager(Plan plugin) throws EnableException {
        this.plugin = plugin;
        Database db = plugin.getDB();
        serverTable = db.getServerTable();

        try {
            serverInfoFile = new ServerInfoFile(plugin);
        } catch (IOException e) {
            throw new EnableException("Failed to read ServerInfoFile.yml", e);
        }
    }

    public void updateServerInfo() throws SQLException, IOException {
        Optional<UUID> serverUUID = serverInfoFile.getUUID();
        if (serverUUID.isPresent()) {
            updateDbInfo(serverUUID.get());
        } else {
            registerServer();
        }
    }

    private void updateDbInfo(UUID serverUUID) throws SQLException, IOException {
        Optional<Integer> serverID = serverTable.getServerID(serverUUID);
        if (!serverID.isPresent()) {
            registerServer(serverUUID);
            return;
        }
        String name = Settings.SERVER_NAME.toString().replaceAll("[^a-zA-Z0-9_\\s]", "_");
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
        String name = Settings.SERVER_NAME.toString().replaceAll("[^a-zA-Z0-9_\\s]", "_");
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
        String seed = plugin.getServer().getServerId() + variableHolder.getName() + variableHolder.getIp() + variableHolder.getPort() + variableHolder.getVersion() + variableHolder.getImplVersion();
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
        try {
            Optional<ServerInfo> bungeeInfo = plugin.getDB().getServerTable().getBungeeInfo();
            if (bungeeInfo.isPresent()) {
                return Optional.of(bungeeInfo.get().getWebAddress());
            }
        } catch (SQLException e) {
            Log.toLog(this.getClass().getName(), e);
        }
        return Optional.empty();
    }

    /**
     * Saves Bungee connection information to local file on Bukkit servers.
     *
     * @param address Address to save
     * @throws IOException If ServerInfo file can not be written to.
     */
    public void saveBungeeConnectionAddress(String address) throws IOException {
        serverInfoFile.saveInfo(serverInfo, new ServerInfo(-1, null, "Bungee", address, -1));
    }

    public void markConnectionFail() {
        serverInfoFile.markConnectionFail();
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