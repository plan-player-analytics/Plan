
package com.djrapitops.plan.datahandlers;

import com.djrapitops.plan.database.ServerData;

public class ServerDataHandler {
    private ServerData serverData;

    public ServerDataHandler(ServerData serverData) {
        this.serverData = serverData;
    }
    
    public void handleLogin(boolean newPlayer) {
        serverData.playerJoined(newPlayer);
    }
    
    public void handleLogout() {
        serverData.playerLeft();
    }
    
    public void handleKick() {
        handleLogout();
    }
    
    public void handleCommand(String command) {
        serverData.commandRegistered(command);
    }
}
