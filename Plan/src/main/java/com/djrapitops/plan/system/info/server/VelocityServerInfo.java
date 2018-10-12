/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.info.server;

import com.djrapitops.plan.PlanVelocity;
import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.api.exceptions.database.DBOpException;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.info.server.properties.VelocityServerProperties;
import com.djrapitops.plan.system.info.server.properties.ServerProperties;
import com.djrapitops.plan.system.webserver.WebServerSystem;
import com.djrapitops.plugin.api.utility.log.Log;

import java.util.Optional;
import java.util.UUID;

/**
 * Manages Server information on the Velocity instance.
 * 
 * Based on BungeeServerInfo
 *
 * @author MicleBrick
 */
public class VelocityServerInfo extends ServerInfo {

    public VelocityServerInfo(PlanVelocity plugin) {
        super(new VelocityServerProperties(plugin.getProxy()));
    }

    @Override
    public Server loadServerInfo() throws EnableException {
        checkIfDefaultIP();

        try {
            Database db = Database.getActive();
            // doesn't seem like this would need to be different for velocity, perhaps rename to getProxyInformation()?
            Optional<Server> velocityInfo = db.fetch().getBungeeInformation();
            if (velocityInfo.isPresent()) {
                server = velocityInfo.get();
                updateServerInfo(db);
            } else {
                server = registerVelocityInfo(db);
            }
        } catch (DBOpException e) {
            throw new EnableException("Failed to read Server information from Database.");
        }
        return server;
    }

    private void updateServerInfo(Database db) {
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
            Log.info("Player Analytics partially enabled (Use /planvelocity to reload config)");
            throw new EnableException("IP setting still 0.0.0.0 - Configure AlternativeIP/IP that connects to the Proxy server.");
        }
    }

    private Server registerVelocityInfo(Database db) throws EnableException {
        ServerProperties properties = ServerInfo.getServerProperties();
        UUID serverUUID = generateNewUUID(properties);
        String accessAddress = WebServerSystem.getInstance().getWebServer().getAccessAddress();

        Server velocityCord = new Server(-1, serverUUID, "VelocityCord", accessAddress, properties.getMaxPlayers());
        db.save().serverInfoForThisServer(velocityCord);

        Optional<Server> velocityInfo = db.fetch().getBungeeInformation();
        if (velocityInfo.isPresent()) {
            return velocityInfo.get();
        }
        throw new EnableException("VelocityCord registration failed (DB)");
    }

    private UUID generateNewUUID(ServerProperties properties) {
        String seed = properties.getName() + properties.getIp() + properties.getPort() + properties.getVersion() + properties.getImplVersion();
        return UUID.nameUUIDFromBytes(seed.getBytes());
    }
}
