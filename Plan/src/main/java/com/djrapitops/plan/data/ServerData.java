
package com.djrapitops.plan.data;

import java.util.HashMap;
import org.bukkit.Bukkit;

public class ServerData {
    private final HashMap<String, Integer> commandUsage;
    private int playersOnline;
    private int newPlayers;

    public ServerData(HashMap<String, Integer> commandUsage, int newPlayers) {
        this.commandUsage = commandUsage;
        this.playersOnline = Bukkit.getServer().getOnlinePlayers().size();
        this.newPlayers = newPlayers;
    }
    
    public void playerJoined(boolean newPlayer) {
        updatePlayerCount();
        if (newPlayer) {
            newPlayers++;
        }
    }

    public void updatePlayerCount() {
        playersOnline = Bukkit.getServer().getOnlinePlayers().size();
    }
    
    public void playerLeft() {
        updatePlayerCount();
    }
    
    public void commandRegistered(String command) {
        if (!commandUsage.containsKey(command)) {
            commandUsage.put(command, 0);
        }
        commandUsage.put(command, commandUsage.get(command)+1);
    }

    public HashMap<String, Integer> getCommandUsage() {
        return commandUsage;
    }

    public int getPlayersOnline() {
        return playersOnline;
    }

    public int getNewPlayers() {
        return newPlayers;
    }    
}
