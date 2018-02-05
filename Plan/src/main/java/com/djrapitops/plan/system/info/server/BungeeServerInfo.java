/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.info.server;

import com.djrapitops.plan.PlanBungee;
import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.api.exceptions.database.DBException;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.webserver.WebServerSystem;
import com.djrapitops.plugin.api.utility.log.Log;

import java.util.Optional;
import java.util.UUID;

/**
 * Manages Server information on the Bungee instance.
 *
 * @author Rsl1122
 */
public class BungeeServerInfo extends ServerInfo {

    public BungeeServerInfo(PlanBungee plugin) {
        serverProperties = new ServerProperties(plugin.getProxy());
    }

    @Override
    public Server loadServerInfo() throws EnableException {
        checkIfDefaultIP();

        try {
            Database db = Database.getActive();
            Optional<Server> bungeeInfo = db.fetch().getBungeeInformation();
            if (bungeeInfo.isPresent()) {
                server = bungeeInfo.get();
                updateServerInfo(db);
            } else {
                server = registerBungeeInfo(db);
            }
        } catch (DBException e) {
            throw new EnableException("Failed to read Database for Server");
        }
        return server;
    }

    private void updateServerInfo(Database db) throws DBException {
        String accessAddress = WebServerSystem.getInstance().getWebServer().getAccessAddress();
        if (!accessAddress.equals(server.getWebAddress())) {
            server.setWebAddress(accessAddress);
            db.save().serverInfoForThisServer(server);
        }
    }

    private void checkIfDefaultIP() throws EnableException {
        String ip = ServerInfo.getServerProperties().getIp();
        if ("0.0.0.0".equals(ip)) {
            Log.error("IP setting still 0.0.0.0 - Configure AlternativeIP/IP that connects to the Proxy server.");
            Log.info("Player Analytics partially enabled (Use /planbungee to reload config)");
            throw new EnableException("IP setting still 0.0.0.0 - Configure AlternativeIP/IP that connects to the Proxy server.");
        }
    }

    private Server registerBungeeInfo(Database db) throws DBException, EnableException {
        ServerProperties properties = ServerInfo.getServerProperties();
        UUID serverUUID = generateNewUUID(properties);
        String accessAddress = WebServerSystem.getInstance().getWebServer().getAccessAddress();

        Server bungeeCord = new Server(-1, serverUUID, "BungeeCord", accessAddress, properties.getMaxPlayers());
        db.save().serverInfoForThisServer(bungeeCord);

        Optional<Server> bungeeInfo = db.fetch().getBungeeInformation();
        if (bungeeInfo.isPresent()) {
            return bungeeInfo.get();
        }
        throw new EnableException("BungeeCord registration failed (DB)");
    }

    private UUID generateNewUUID(ServerProperties properties) {
        String seed = properties.getName() + properties.getIp() + properties.getPort() + properties.getVersion() + properties.getImplVersion();
        return UUID.nameUUIDFromBytes(seed.getBytes());
    }
}