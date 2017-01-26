package com.djrapitops.plan.database;

import com.djrapitops.plan.data.UserData;
import com.djrapitops.plan.data.ServerData;
import com.djrapitops.plan.Plan;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

/**
 *
 * @author Rsl1122
 */
public abstract class Database {

    private final Plan plugin;

    /**
     * Abstract class constructor.
     *
     * @param plugin Current instance of Plan
     */
    public Database(Plan plugin) {
        this.plugin = plugin;
    }

    /**
     * Initiates the Database.
     *
     * @return false
     */
    public boolean init() {
        return false;
    }

    /**
     * Returns the UserData fetched from the Database.
     *
     * @param uuid UUID of Player
     * @return UserData of Player
     */
    public abstract UserData getUserData(UUID uuid);

    /**
     * Saves the UserData to the Database.
     *
     * @param uuid UUID of Player
     * @param data UserData of Player
     */
    public abstract void saveUserData(UUID uuid, UserData data);

    /**
     * Saves multiple UserData to the Database using batch processing.
     * @param data List of Data
     */
    public abstract void saveMultipleUserData(List<UserData> data);
    
    /**
     * Check if the player is found in the database.
     *
     * @param uuid UUID of Player
     * @return true if player is found in the database
     */
    public abstract boolean wasSeenBefore(UUID uuid);

    /**
     * Gets the settings to the config for each database type.
     *
     * @param section
     */
    public abstract void getConfigDefaults(ConfigurationSection section);

    /**
     * Cleans the database.
     */
    public abstract void clean();

    /**
     * Used by the Config section.
     *
     * @return
     */
    public abstract String getName();

    /**
     * Used by the Config section.
     *
     * @return
     */
    public String getConfigName() {
        return getName().toLowerCase().replace(" ", "");
    }

    /**
     * Used by the Config section.
     *
     * @return
     */
    public ConfigurationSection getConfigSection() {
        return plugin.getConfig().getConfigurationSection(getConfigName());
    }

    /**
     * Get the version of the database in case of updates.
     *
     * @return Current version of the database
     */
    public abstract int getVersion();

    /**
     * Set the version of the database.
     *
     * @param version Version number
     */
    public abstract void setVersion(int version);

    /**
     * Saves new server data to the table
     *
     * @param serverData Current serverdata
     */
    public abstract void saveServerData(ServerData serverData);

    /**
     * Gets the newest serverdata from the database.
     *
     * @return ServerData with the highest save date.
     */
    public abstract ServerData getNewestServerData();

    /**
     * Closes the database.
     */
    public abstract void close();

    /**
     * Returns raw data for analysis, contains all of player activity history.
     *
     * @return HashMap with save date (long in ms) and ServerData.
     */
    public abstract HashMap<Long, ServerData> getServerDataHashMap();
}
