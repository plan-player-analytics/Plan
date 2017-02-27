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

    public Database(Plan plugin) {
        this.plugin = plugin;
    }
    public boolean init(){
        return false;
    }
    public void giveUserDataToProcessors(UUID uuid, DBCallableProcessor... processors) throws SQLException {
        List<DBCallableProcessor> coll = new ArrayList<>();
        coll.addAll(Arrays.asList(processors));
        giveUserDataToProcessors(uuid, coll);
    }
    public abstract void giveUserDataToProcessors(UUID uuid, Collection<DBCallableProcessor> processors) throws SQLException;
    public abstract void saveUserData(UUID uuid, UserData data) throws SQLException;
    public abstract void saveMultipleUserData(List<UserData> data) throws SQLException;
    public abstract boolean wasSeenBefore(UUID uuid);
    public abstract void clean();
    public abstract String getName();
    public String getConfigName() {
        return getName().toLowerCase().replace(" ", "");
    }
    public ConfigurationSection getConfigSection() {
        return plugin.getConfig().getConfigurationSection(getConfigName());
    }
    public abstract int getVersion() throws SQLException;
    public abstract void setVersion(int version) throws SQLException;
    public abstract void close() throws SQLException;
    public abstract boolean removeAccount(String uuid) throws SQLException;
    public abstract boolean removeAllData() throws SQLException;
    public abstract void saveCommandUse(HashMap<String, Integer> data) throws SQLException, NullPointerException;
    public abstract Set<UUID> getSavedUUIDs() throws SQLException;
    public abstract HashMap<String, Integer> getCommandUse() throws SQLException;
    public abstract int getUserId(String uuid) throws SQLException;
}
