package main.java.com.djrapitops.plan.database;

import java.sql.SQLException;
import java.util.*;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.cache.DBCallableProcessor;
import org.bukkit.configuration.ConfigurationSection;

/**
 *
 * @author Rsl1122
 */
public abstract class Database {

    private final Plan plugin;

    /**
     *
     * @param plugin
     */
    public Database(Plan plugin) {
        this.plugin = plugin;
    }

    /**
     *
     * @return
     */
    public boolean init(){
        return false;
    }

    /**
     *
     * @param uuid
     * @param processors
     * @throws SQLException
     */
    public void giveUserDataToProcessors(UUID uuid, DBCallableProcessor... processors) throws SQLException {
        List<DBCallableProcessor> coll = new ArrayList<>();
        coll.addAll(Arrays.asList(processors));
        giveUserDataToProcessors(uuid, coll);
    }

    /**
     *
     * @param uuid
     * @param processors
     * @throws SQLException
     */
    public abstract void giveUserDataToProcessors(UUID uuid, Collection<DBCallableProcessor> processors) throws SQLException;

    /**
     *
     * @param uuid
     * @param data
     * @throws SQLException
     */
    public abstract void saveUserData(UUID uuid, UserData data) throws SQLException;

    /**
     *
     * @param data
     * @throws SQLException
     */
    public abstract void saveMultipleUserData(List<UserData> data) throws SQLException;

    /**
     *
     * @param uuid
     * @return
     */
    public abstract boolean wasSeenBefore(UUID uuid);

    /**
     *
     */
    public abstract void clean();

    /**
     *
     * @return
     */
    public abstract String getName();

    /**
     *
     * @return
     */
    public String getConfigName() {
        return getName().toLowerCase().replace(" ", "");
    }

    /**
     *
     * @return
     */
    public ConfigurationSection getConfigSection() {
        return plugin.getConfig().getConfigurationSection(getConfigName());
    }

    /**
     *
     * @return
     * @throws SQLException
     */
    public abstract int getVersion() throws SQLException;

    /**
     *
     * @param version
     * @throws SQLException
     */
    public abstract void setVersion(int version) throws SQLException;

    /**
     *
     * @throws SQLException
     */
    public abstract void close() throws SQLException;

    /**
     *
     * @param uuid
     * @return
     * @throws SQLException
     */
    public abstract boolean removeAccount(String uuid) throws SQLException;

    /**
     *
     * @return
     * @throws SQLException
     */
    public abstract boolean removeAllData() throws SQLException;

    /**
     *
     * @param data
     * @throws SQLException
     * @throws NullPointerException
     */
    public abstract void saveCommandUse(HashMap<String, Integer> data) throws SQLException, NullPointerException;

    /**
     *
     * @return
     * @throws SQLException
     */
    public abstract Set<UUID> getSavedUUIDs() throws SQLException;

    /**
     *
     * @return
     * @throws SQLException
     */
    public abstract HashMap<String, Integer> getCommandUse() throws SQLException;

    /**
     *
     * @param uuid
     * @return
     * @throws SQLException
     */
    public abstract int getUserId(String uuid) throws SQLException;
}
