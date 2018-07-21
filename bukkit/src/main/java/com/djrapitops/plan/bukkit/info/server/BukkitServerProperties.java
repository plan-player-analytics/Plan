package com.djrapitops.plan.bukkit.info.server;

import com.djrapitops.plan.system.info.server.ServerProperties;
import org.bukkit.Server;

import java.util.function.Supplier;

public class BukkitServerProperties implements ServerProperties {
    private final String id;
    private final String name;
    private final int port;
    private final String version;
    private final String implVersion;
    private final Supplier<String> ip;
    private final int maxPlayers;

    private final Supplier<Integer> onlinePlayers;

    public BukkitServerProperties(Server server) {
        this.id = server.getServerId();
        this.ip = server::getIp;
        this.name = server.getName();
        this.port = server.getPort();
        this.version = server.getVersion();
        this.implVersion = server.getBukkitVersion();

        this.maxPlayers = server.getMaxPlayers();

        this.onlinePlayers = () -> server.getOnlinePlayers().size();
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
