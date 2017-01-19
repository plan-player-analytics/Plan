package com.djrapitops.plan.data.handlers;

import com.djrapitops.plan.data.ServerData;

/**
 *
 * @author Rsl1122
 */
public class ServerDataHandler {

    private ServerData serverData;

    /**
     * Class constructor.
     *
     * @param serverData ServerData in the DataCacheHandler.
     */
    public ServerDataHandler(ServerData serverData) {
        this.serverData = serverData;
    }

    /**
     * Updates playercount and adds new player if player is new.
     *
     * @param newPlayer true if player not in database.
     */
    public void handleLogin(boolean newPlayer) {
        serverData.playerJoined(newPlayer);
    }

    /**
     * Updates playercount.
     */
    public void handleLogout() {
        serverData.playerLeft();
    }

    /**
     * Updates playercount.
     */
    public void handleKick() {
        handleLogout();
    }

    /**
     * Adds command to the command usage.
     * @param command Used command, first part (eg. /plan)
     */
    public void handleCommand(String command) {
        serverData.commandRegistered(command);
    }
}
