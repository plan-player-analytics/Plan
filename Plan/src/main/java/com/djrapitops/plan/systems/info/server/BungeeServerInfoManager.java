/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.info.server;

import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.task.RunnableFactory;
import main.java.com.djrapitops.plan.PlanBungee;
import main.java.com.djrapitops.plan.ServerVariableHolder;
import main.java.com.djrapitops.plan.api.exceptions.PlanEnableException;
import main.java.com.djrapitops.plan.api.exceptions.WebAPIException;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.database.tables.ServerTable;
import main.java.com.djrapitops.plan.systems.webserver.webapi.bukkit.ConfigurationWebAPI;
import main.java.com.djrapitops.plan.systems.webserver.webapi.universal.PingWebAPI;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages Server information on the Bungee instance.
 *
 * @author Rsl1122
 */
public class BungeeServerInfoManager {

    private final PlanBungee plugin;
    private final Database db;
    private final Map<UUID, ServerInfo> bukkitServers;
    private final Set<UUID> onlineServers;
    private ServerInfo serverInfo;
    private ServerTable serverTable;

    public BungeeServerInfoManager(PlanBungee plugin) {
        this.plugin = plugin;
        this.db = plugin.getDB();
        serverTable = db.getServerTable();

        bukkitServers = new HashMap<>();
        onlineServers = new HashSet<>();
    }

    public void loadServerInfo() throws PlanEnableException {
        try {
            Optional<ServerInfo> bungeeInfo = db.getServerTable().getBungeeInfo();
            if (bungeeInfo.isPresent()) {
                serverInfo = bungeeInfo.get();
                String accessAddress = plugin.getWebServer().getAccessAddress();
                if (!accessAddress.equals(serverInfo.getWebAddress())) {
                    serverInfo.setWebAddress(accessAddress);
                    serverTable.saveCurrentServerInfo(serverInfo);
                }
            } else {
                serverInfo = registerBungeeInfo();
            }
        } catch (SQLException e) {
            throw new PlanEnableException("Failed to read Database for ServerInfo");
        }
    }

    private ServerInfo registerBungeeInfo() throws SQLException, PlanEnableException {
        ServerVariableHolder variable = plugin.getVariable();
        UUID serverUUID = generateNewUUID(variable);
        String accessAddress = plugin.getWebServer().getAccessAddress();

        serverTable.saveCurrentServerInfo(
                new ServerInfo(-1, serverUUID, "BungeeCord", accessAddress, variable.getMaxPlayers())
        );

        Optional<ServerInfo> bungeeInfo = db.getServerTable().getBungeeInfo();
        if (bungeeInfo.isPresent()) {
            return bungeeInfo.get();
        }
        throw new PlanEnableException("BungeeCord registration failed (DB)");
    }

    private UUID generateNewUUID(ServerVariableHolder variableHolder) {
        String seed = variableHolder.getName() + variableHolder.getIp() + variableHolder.getPort() + variableHolder.getVersion() + variableHolder.getImplVersion();
        return UUID.nameUUIDFromBytes(seed.getBytes());
    }

    public UUID getServerUUID() {
        return serverInfo.getUuid();
    }

    public boolean attemptConnection(ServerInfo server, String accessCode) {
        if (server == null) {
            Log.debug("Attempted a connection to a null ServerInfo");
            return false;
        }
        try {
            String webAddress = server.getWebAddress();
            Log.debug("Attempting to connect to Bukkit server.. (" + webAddress + ")");
            PingWebAPI pingApi = plugin.getWebServer().getWebAPI().getAPI(PingWebAPI.class);
            if (accessCode != null) {
                pingApi.sendRequest(webAddress, accessCode);
                plugin.getWebServer().getWebAPI().getAPI(ConfigurationWebAPI.class).sendRequest(webAddress, server.getUuid(), accessCode);
            } else {
                pingApi.sendRequest(webAddress);
            }
            connectedToServer(server);
            return true;
        } catch (WebAPIException e) {
            Log.debug(e.toString());
            serverHasGoneOffline(server.getUuid());
            return false;
        }
    }

    public boolean attemptConnection(ServerInfo server) {
        return attemptConnection(server, null);
    }

    public void sendConfigSettings(UUID serverUUID) {
        String webAddress = null;
        try {
            ServerInfo server = bukkitServers.get(serverUUID);
            if (server == null) {
                return;
            }
            webAddress = server.getWebAddress();
            Log.debug("Sending config settings to " + webAddress + "");
            plugin.getWebServer().getWebAPI().getAPI(ConfigurationWebAPI.class).sendRequest(webAddress, serverUUID);
        } catch (WebAPIException e) {
            Log.info("Connection to Bukkit (" + webAddress + ") did not succeed.");
            serverHasGoneOffline(serverUUID);
        }
    }

    public void connectedToServer(ServerInfo server) {
        Log.info("Connection to Bukkit (" + server.getWebAddress() + ") OK");
        bukkitServers.put(server.getUuid(), server);
        onlineServers.add(server.getUuid());
    }

    public boolean serverConnected(UUID serverUUID) {
        if (plugin.getServerUuid().equals(serverUUID)) {
            return false;
        }
        Log.info("Received a connection from a Bukkit server..");
        if (onlineServers.contains(serverUUID)) {
            sendConfigSettings(serverUUID);
            return true;
        }
        try {
            Optional<ServerInfo> serverInfo = db.getServerTable().getServerInfo(serverUUID);
            if (serverInfo.isPresent()) {
                ServerInfo server = serverInfo.get();
                Log.info("Server Info found from DB: " + server.getName());
                RunnableFactory.createNew("BukkitConnectionTask: " + server.getName(), new AbsRunnable() {
                    @Override
                    public void run() {
                        attemptConnection(server);
                        sendConfigSettings(serverUUID);
                        this.cancel();
                    }
                }).runTaskLaterAsynchronously(TimeAmount.SECOND.ticks() * 3L);
                return true;
            }
        } catch (SQLException e) {
            Log.toLog(this.getClass().getName(), e);
        }
        return false;
    }

    public Collection<ServerInfo> getOnlineBukkitServers() {
        return bukkitServers.entrySet().stream()
                .filter(entry -> onlineServers.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toSet());
    }

    public Collection<ServerInfo> getBukkitServers() {
        return bukkitServers.values();
    }

    public void serverHasGoneOffline(UUID serverUUID) {
        onlineServers.remove(serverUUID);
    }
}