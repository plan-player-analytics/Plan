package com.djrapitops.plan.system.info.server.properties;

import com.djrapitops.plan.system.settings.Settings;
import com.velocitypowered.api.proxy.ProxyServer;

/**
 * ServerProperties for Velocity.
 * <p>
 * Supports RedisVelocity for Players online getting.
 *
 * @author Rsl1122
 */
public class VelocityServerProperties extends ServerProperties {

    public VelocityServerProperties(ProxyServer server) {
        super(
                server.getAllServers().toString(),
                "Velocity",
                server.getBoundAddress().getPort(),
                // not sure how to get these
                server.getClass().getPackage().getImplementationVersion(),
                server.getClass().getPackage().getImplementationVersion(),
                Settings.BUNGEE_IP::toString,
                -1, // not sure how to get this
                RedisCheck.isClassAvailable() ? new RedisPlayersOnlineSupplier() : server::getPlayerCount
        );
    }
}