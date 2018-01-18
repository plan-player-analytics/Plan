/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.info.server;

import com.djrapitops.plan.PlanBungee;
import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.api.exceptions.connection.WebException;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.database.databases.sql.tables.ServerTable;
import com.djrapitops.plan.system.webserver.webapi.bukkit.ConfigurationWebAPI;
import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.task.RunnableFactory;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages Server information on the Bungee instance.
 *
 * @author Rsl1122
 */
public class BungeeServerInfo extends ServerInfo {

    private final PlanBungee plugin;
    private final Database db;
    private final Map<UUID, Server> bukkitServers;
    private final Set<UUID> onlineServers;
    private Server server;
    private ServerTable serverTable;

    public BungeeServerInfo(PlanBungee plugin) {
        this.plugin = plugin;
        this.db = plugin.getDB();
        serverTable = db.getServerTable();

        bukkitServers = new HashMap<>();
        onlineServers = new HashSet<>();
    }

    public void loadServerInfo() throws EnableException {
        try {
            Optional<Server> bungeeInfo = db.getServerTable().getBungeeInfo();
            if (bungeeInfo.isPresent()) {
                server = bungeeInfo.get();
                String accessAddress = plugin.getWebServer().getAccessAddress();
                if (!accessAddress.equals(server.getWebAddress())) {
                    server.setWebAddress(accessAddress);
                    serverTable.saveCurrentServerInfo(server);
                }
            } else {
                server = registerBungeeInfo();
            }
        } catch (SQLException e) {
            throw new EnableException("Failed to read Database for Server");
        }
    }

    private Server registerBungeeInfo() throws SQLException, EnableException {
        ServerProperties variable = plugin.getVariable();
        UUID serverUUID = generateNewUUID(variable);
        String accessAddress = plugin.getWebServer().getAccessAddress();

        serverTable.saveCurrentServerInfo(
                new Server(-1, serverUUID, "BungeeCord", accessAddress, variable.getMaxPlayers())
        );

        Optional<Server> bungeeInfo = db.getServerTable().getBungeeInfo();
        if (bungeeInfo.isPresent()) {
            return bungeeInfo.get();
        }
        throw new EnableException("BungeeCord registration failed (DB)");
    }

    private UUID generateNewUUID(ServerProperties variableHolder) {
        String seed = variableHolder.getName() + variableHolder.getIp() + variableHolder.getPort() + variableHolder.getVersion() + variableHolder.getImplVersion();
        return UUID.nameUUIDFromBytes(seed.getBytes());
    }

    public UUID getServerUUID() {
        return server.getUuid();
    }

    public boolean attemptConnection(Server server, String accessCode) {
        if (server == null) {
            Log.debug("Attempted a connection to a null Server");
            return false;
        }
        try {
            String webAddress = server.getWebAddress();
            Log.debug("Attempting to connect to Bukkit server.. (" + webAddress + ")");
                plugin.getWebServer().getWebAPI().getAPI(ConfigurationWebAPI.class).sendRequest(webAddress, server.getUuid(), accessCode);
            connectedToServer(server);
            return true;
        } catch (WebException e) {
            Log.debug(e.toString());
            serverHasGoneOffline(server.getUuid());
            return false;
        }
    }

    public boolean attemptConnection(Server server) {
        return attemptConnection(server, null);
    }

    public void sendConfigSettings(UUID serverUUID) {
        String webAddress = null;
        try {
            Server server = bukkitServers.get(serverUUID);
            if (server == null) {
                return;
            }
            webAddress = server.getWebAddress();
            Log.debug("Sending config settings to " + webAddress + "");
            plugin.getWebServer().getWebAPI().getAPI(ConfigurationWebAPI.class).sendRequest(webAddress, serverUUID);
        } catch (WebException e) {
            Log.info("Connection to Bukkit (" + webAddress + ") did not succeed.");
            serverHasGoneOffline(serverUUID);
        }
    }

    public void connectedToServer(Server server) {
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
            RunnableFactory.createNew("BukkitRebootedConnectionTask: " + serverUUID, new AbsRunnable() {
                @Override
                public void run() {
                    sendConfigSettings(serverUUID);
                    connectedToServer(bukkitServers.get(serverUUID));
                    this.cancel();
                }
            }).runTaskLaterAsynchronously(TimeAmount.SECOND.ticks() * 3L);
            return true;
        }
        try {
            Optional<Server> serverInfo = db.getServerTable().getServerInfo(serverUUID);
            if (serverInfo.isPresent()) {
                Server server = serverInfo.get();
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

    public Collection<Server> getOnlineBukkitServers() {
        return bukkitServers.entrySet().stream()
                .filter(entry -> onlineServers.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toSet());
    }

    public Collection<Server> getBukkitServers() {
        return bukkitServers.values();
    }

    public void serverHasGoneOffline(UUID serverUUID) {
        onlineServers.remove(serverUUID);
    }
}