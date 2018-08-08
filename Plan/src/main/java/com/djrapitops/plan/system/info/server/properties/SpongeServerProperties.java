package com.djrapitops.plan.system.info.server.properties;

import org.spongepowered.api.Game;

import java.net.InetSocketAddress;

/**
 * ServerProperties for Sponge.
 *
 * @author Rsl1122
 */
public class SpongeServerProperties extends ServerProperties {

    public SpongeServerProperties(Game game) {
        super(
                game.getServer().getMotd().toPlain(),
                "Sponge",
                game.getServer().getBoundAddress().orElseGet(() -> new InetSocketAddress(25565)).getPort(),
                game.getPlatform().getMinecraftVersion().getName(),
                game.getPlatform().getMinecraftVersion().getName(),
                () -> game.getServer().getBoundAddress()
                        .orElseGet(() -> new InetSocketAddress(25565))
                        .getAddress().getHostAddress(),
                game.getServer().getMaxPlayers(),
                () -> game.getServer().getOnlinePlayers().size()
        );
    }
}