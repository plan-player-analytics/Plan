/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.info.server;

import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.api.exceptions.database.DBOpException;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.info.server.properties.ServerProperties;
import com.djrapitops.plan.system.webserver.WebServer;
import com.djrapitops.plugin.logging.console.PluginLogger;

import javax.inject.Inject;
import java.util.Optional;
import java.util.UUID;

/**
 * Manages Server information on the Bungee instance.
 *
 * @author Rsl1122
 */
public class BungeeServerInfo extends ServerInfo {

    private final Database database;
    private final WebServer webServer;
    private final PluginLogger logger;

    @Inject
    public BungeeServerInfo(
            ServerProperties serverProperties,
            Database database,
            WebServer webServer,
            PluginLogger logger
    ) {
        super(serverProperties);
        this.database = database;
        this.webServer = webServer;
        this.logger = logger;
    }

    @Override
    public Server loadServerInfo() throws EnableException {
        checkIfDefaultIP();

        try {
            Optional<Server> bungeeInfo = database.fetch().getBungeeInformation();
            if (bungeeInfo.isPresent()) {
                server = bungeeInfo.get();
                updateServerInfo(database);
            } else {
                server = registerBungeeInfo(database);
            }
        } catch (DBOpException e) {
            throw new EnableException("Failed to read Server information from Database.");
        }
        return server;
    }

    private void updateServerInfo(Database db) {
        String accessAddress = webServer.getAccessAddress();
        if (!accessAddress.equals(server.getWebAddress())) {
            server.setWebAddress(accessAddress);
            db.save().serverInfoForThisServer(server);
        }
    }

    private void checkIfDefaultIP() throws EnableException {
        String ip = serverProperties.getIp();
        if ("0.0.0.0".equals(ip)) {
            logger.error("IP setting still 0.0.0.0 - Configure AlternativeIP/IP that connects to the Proxy server.");
            logger.info("Player Analytics partially enabled (Use /planbungee to reload config)");
            throw new EnableException("IP setting still 0.0.0.0 - Configure AlternativeIP/IP that connects to the Proxy server.");
        }
    }

    private Server registerBungeeInfo(Database db) throws EnableException {
        UUID serverUUID = generateNewUUID();
        String accessAddress = webServer.getAccessAddress();

        Server bungeeCord = new Server(-1, serverUUID, "BungeeCord", accessAddress, serverProperties.getMaxPlayers());
        db.save().serverInfoForThisServer(bungeeCord);

        Optional<Server> bungeeInfo = db.fetch().getBungeeInformation();
        if (bungeeInfo.isPresent()) {
            return bungeeInfo.get();
        }
        throw new EnableException("BungeeCord registration failed (DB)");
    }

    private UUID generateNewUUID() {
        String seed = serverProperties.getName() +
                serverProperties.getIp() +
                serverProperties.getPort() +
                serverProperties.getVersion() +
                serverProperties.getImplVersion();
        return UUID.nameUUIDFromBytes(seed.getBytes());
    }
}
