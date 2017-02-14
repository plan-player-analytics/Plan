package main.java.com.djrapitops.plan.database;

import java.util.*;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.UserData;
import org.bukkit.configuration.ConfigurationSection;

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
     * Closes the database.
     */
    public abstract void close();
    
    public abstract void removeAccount(String uuid);
    public abstract void removeAllData();
    public abstract void saveCommandUse(HashMap<String, Integer> data);
    public abstract Set<UUID> getSavedUUIDs();
    public abstract HashMap<String, Integer> getCommandUse();
    public abstract int getUserId(String uuid);
}
