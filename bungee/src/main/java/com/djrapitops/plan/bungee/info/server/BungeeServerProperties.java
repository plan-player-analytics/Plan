package com.djrapitops.plan.bungee.info.server;

import com.djrapitops.plan.system.info.server.ServerProperties;
import com.djrapitops.plan.system.settings.Settings;
import net.md_5.bungee.api.ProxyServer;

import java.util.function.Supplier;

public class BungeeServerProperties implements ServerProperties {
    private final String id;
    private final String name;
    private final int port;
    private final String version;
    private final String implVersion;
    private final Supplier<String> ip;
    private final int maxPlayers;

    private final Supplier<Integer> onlinePlayers;

    public BungeeServerProperties(ProxyServer server) {
        this.id = server.getServers().toString();
        this.ip = Settings.BUNGEE_IP::toString;
        this.name = "BungeeCord";
        this.port = -1;
        this.version = server.getVersion();
        this.implVersion = server.getVersion();

        this.maxPlayers = server.getConfig().getPlayerLimit();

        this.onlinePlayers = server::getOnlineCount;
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
