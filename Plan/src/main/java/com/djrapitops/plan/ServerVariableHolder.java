package main.java.com.djrapitops.plan;

import org.bukkit.Server;

/**
 * Class responsible for holding server variable values that do not change
 * without a reload.
 *
 * @author Rsl1122
 * @since 3.4.1
 */
public class ServerVariableHolder {

    private int maxPlayers;
    private String ip;

    public ServerVariableHolder(Server server) {
        maxPlayers = server.getMaxPlayers();
        ip = server.getIp();
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public String getIp() {
        return ip;
    }
}
