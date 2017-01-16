package com.djrapitops.plan.data;

import com.djrapitops.plan.Plan;
import java.util.HashMap;
import org.bukkit.Bukkit;
import static org.bukkit.plugin.java.JavaPlugin.getPlugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Rsl1122
 */
public class ServerData {

    private final HashMap<String, Integer> commandUsage;
    private int playersOnline;
    private int newPlayers;

    /**
     * Class Constructor.
     *
     * Creates a new DataPoint of ServerData with the currently known player
     * count.
     *
     * @param commandUsage HashMap of Commands used and times they have been
     * used.
     * @param newPlayers Amount of new players that have joined that day.
     */
    public ServerData(HashMap<String, Integer> commandUsage, int newPlayers) {
        this.commandUsage = commandUsage;
        this.playersOnline = Bukkit.getServer().getOnlinePlayers().size();
        this.newPlayers = newPlayers;
    }

    /**
     * Class Constructor with playersOnline.
     *
     * Creates a new DataPoint of ServerData with defined player count.
     *
     * @param commandUsage HashMap of Commands used and times they have been
     * used.
     * @param newPlayers Amount of new players that have joined that day.
     * @param playersOnline Amount of players at the time of save.
     */
    public ServerData(HashMap<String, Integer> commandUsage, int newPlayers, int playersOnline) {
        this.commandUsage = commandUsage;
        this.playersOnline = playersOnline;
        this.newPlayers = newPlayers;
    }

    /**
     * Updates PlayerCount if the player is new.
     *
     * @param newPlayer If data for player is not found in the database this is
     * true.
     */
    public void playerJoined(boolean newPlayer) {
        updatePlayerCount();
        if (newPlayer) {
            newPlayers++;
        }
    }

    /**
     * Updates playersOnline to current playercount.
     */
    public void updatePlayerCount() {
        playersOnline = Bukkit.getServer().getOnlinePlayers().size();
    }

    /**
     * Updates playersOnline after 5 seconds to wait for Player to leave.
     */
    public void playerLeft() {
        (new BukkitRunnable() {
            @Override
            public void run() {
                updatePlayerCount();
                this.cancel();
            }
        }).runTaskLater(getPlugin(Plan.class), 5 * 20);
    }

    /**
     * Adds command to the commandUse Map and adds 1 to the amount it has been used.
     * @param command Used command (eg. /plan)
     */
    public void commandRegistered(String command) {
        if (!commandUsage.containsKey(command)) {
            commandUsage.put(command, 0);
        }
        commandUsage.put(command, commandUsage.get(command) + 1);
    }

    /**
     * @return HashMap of used Commands and how many times they've been used.
     */
    public HashMap<String, Integer> getCommandUsage() {
        return commandUsage;
    }

    /**
     * @return How many players were online at the time of save.
     */
    public int getPlayersOnline() {
        return playersOnline;
    }

    /**
     * @return How many players were new at the time of save.
     */
    public int getNewPlayers() {
        return newPlayers;
    }
}
