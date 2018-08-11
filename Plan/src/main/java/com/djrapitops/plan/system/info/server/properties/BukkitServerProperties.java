package com.djrapitops.plan.system.info.server.properties;

import org.bukkit.Server;

/**
 * ServerProperties for Bukkit.
 *
 * @author Rsl1122
 */
public class BukkitServerProperties extends ServerProperties {

    public BukkitServerProperties(Server server) {
        super(
                server.getServerId(),
                server.getName(),
                server.getPort(),
                server.getVersion(),
                server.getBukkitVersion(),
                server::getIp,
                server.getMaxPlayers(),
                () -> server.getOnlinePlayers().size()
        );
    }

}