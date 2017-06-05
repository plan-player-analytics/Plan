package main.java.com.djrapitops.plan;

import org.bukkit.Server;

/**
 *
 * @author Rsl1122
 */
public class ServerVariableHolder {

    private int maxPlayers;

    public ServerVariableHolder(Server server) {
        maxPlayers = server.getMaxPlayers();
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

}
