package com.djrapitops.plan;

import com.djrapitops.plan.system.settings.Settings;
import net.md_5.bungee.api.ProxyServer;
import org.bukkit.Server;

/**
 * Class responsible for holding server variable values that do not change
 * without a reload.
 *
 * @author Rsl1122
 * @since 3.4.1
 */
public class ServerVariableHolder {

    private final String name;
    private final int port;
    private final String version;
    private final String implVersion;
    private final String ip;
    private final int maxPlayers;
    private final boolean usingPaper;

    /**
     * Constructor, grabs the variables.
     *
     * @param server instance the plugin is running on.
     */
    public ServerVariableHolder(Server server) {
        ip = server.getIp();
        name = server.getName();
        port = server.getPort();
        version = server.getVersion();
        implVersion = server.getBukkitVersion();

        maxPlayers = server.getMaxPlayers();

        usingPaper = name.equals("Paper")
                || name.equals("TacoSpigot"); //Fork of Paper
    }

    /**
     * Constructor, grabs the variables.
     *
     * @param server instance the plugin is running on.
     */
    public ServerVariableHolder(ProxyServer server) {
        ip = Settings.BUNGEE_IP.toString();
        name = "BungeeCord";
        port = -1;
        version = server.getVersion();
        implVersion = server.getVersion();

        maxPlayers = server.getConfig().getPlayerLimit();

        usingPaper = false;
    }

    /**
     * Ip string in server.properties.
     *
     * @return the ip.
     */
    public String getIp() {
        return ip;
    }

    /**
     * Returns if the server is using PaperSpigot.
     *
     * @return if the server is using PaperSpigot.
     */
    public boolean isUsingPaper() {
        return usingPaper;
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
}
