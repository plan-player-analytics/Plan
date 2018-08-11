package com.djrapitops.plan.system.info.server.properties;

import org.spongepowered.api.Game;

import java.net.InetSocketAddress;
import java.util.function.Supplier;

/**
 * Class responsible for holding server variable values that do not change
 * without a reload.
 *
 * @author Rsl1122
 * @since 3.4.1
 */
public abstract class ServerProperties {

    private final String id;
    private final String name;
    private final int port;
    private final String version;
    private final String implVersion;
    private final Supplier<String> ip;
    private final int maxPlayers;

    private final Supplier<Integer> onlinePlayers;

    protected ServerProperties(
            String id, String name, int port,
            String version, String implVersion,
            Supplier<String> ip, int maxPlayers, Supplier<Integer> onlinePlayers) {
        this.id = id;
        this.name = name;
        this.port = port;
        this.version = version;
        this.implVersion = implVersion;
        this.ip = ip;
        this.maxPlayers = maxPlayers;
        this.onlinePlayers = onlinePlayers;
    }

    public ServerProperties(Game game) {
        if (game == null) {
            throw new IllegalStateException("Game did not inject.");
        }
        version = game.getPlatform().getMinecraftVersion().getName();
        ip = () -> game.getServer().getBoundAddress()
                .orElseGet(() -> new InetSocketAddress(25565))
                .getAddress().getHostAddress();
        name = "Sponge";
        port = game.getServer().getBoundAddress().orElseGet(() -> new InetSocketAddress(25565)).getPort();
        implVersion = version;

        id = game.getServer().getMotd().toPlain();

        maxPlayers = game.getServer().getMaxPlayers();
        onlinePlayers = () -> game.getServer().getOnlinePlayers().size();
    }

    /**
     * Ip string in server.properties.
     *
     * @return the ip.
     */
    public String getIp() {
        return ip.get();
    }

    public String getName() {
        return name;
    }

    public int getPort() {
        return port;
    }

    public String getVersion() {
        return version;
    }

    public String getImplVersion() {
        return implVersion;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public String getServerId() {
        return id;
    }

    public int getOnlinePlayers() {
        return onlinePlayers.get();
    }
}
