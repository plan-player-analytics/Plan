package com.djrapitops.plan.sponge.info.server;

import com.djrapitops.plan.system.info.server.ServerProperties;
import org.spongepowered.api.Game;

import java.net.InetSocketAddress;
import java.util.function.Supplier;

public class SpongeServerProperties implements ServerProperties {
    private final String id;
    private final String name;
    private final int port;
    private final String version;
    private final String implVersion;
    private final Supplier<String> ip;
    private final int maxPlayers;

    private final Supplier<Integer> onlinePlayers;

    public SpongeServerProperties(Game game) {
        if (game == null) {
            throw new IllegalStateException("Game did not inject.");
        }

        this.version = game.getPlatform().getMinecraftVersion().getName();
        this.ip = () -> game.getServer().getBoundAddress()
                .orElseGet(() -> new InetSocketAddress(25565))
                .getAddress().getHostAddress();
        this.name = "Sponge";
        this.port = game.getServer().getBoundAddress().orElseGet(() -> new InetSocketAddress(25565)).getPort();
        this.implVersion = version;

        this.id = game.getServer().getMotd().toPlain();

        this.maxPlayers = game.getServer().getMaxPlayers();
        this.onlinePlayers = () -> game.getServer().getOnlinePlayers().size();
    }

    @Override
    public String getIp() {
        return ip.get();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public String getImplVersion() {
        return implVersion;
    }

    @Override
    public int getMaxPlayers() {
        return maxPlayers;
    }

    @Override
    public String getServerId() {
        return id;
    }

    @Override
    public int getOnlinePlayers() {
        return onlinePlayers.get();
    }
}
