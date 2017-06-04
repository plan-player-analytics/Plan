package main.java.com.djrapitops.plan;

/**
 *
 * @author Rsl1122
 */
public class ServerVariableHolder {
    
    private int maxPlayers;

    public ServerVariableHolder(Plan plugin) {
        maxPlayers = plugin.getServer().getMaxPlayers();
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }
    
    
}
