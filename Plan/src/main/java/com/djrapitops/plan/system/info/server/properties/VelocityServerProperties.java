package com.djrapitops.plan.system.info.server.properties;

import com.velocitypowered.api.proxy.ProxyServer;

/**
 * ServerProperties for Velocity.
 * <p>
 * Supports RedisBungee for Players online getting.
 *
 * @author Rsl1122
 */
public class VelocityServerProperties extends ServerProperties {

    public VelocityServerProperties(ProxyServer server, String serverIP) {
        super(
                server.getAllServers().toString(),
                "Velocity",
                server.getBoundAddress().getPort(),
                // not sure how to get these
                server.getClass().getPackage().getImplementationVersion(),
                server.getClass().getPackage().getImplementationVersion(),
                () -> serverIP,
                -1, // not sure how to get this
                RedisCheck.isClassAvailable() ? new RedisPlayersOnlineSupplier() : server::getPlayerCount
        );
    }
}