package com.djrapitops.plan.database;

import com.djrapitops.plan.Plan;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

public abstract class Database {

    private final Plan plugin;
    protected boolean cacheAccounts;

    public Database(Plan plugin) {
        this.plugin = plugin;
    }

    public boolean init() {
        return false;
    }

    public abstract UserData getUserData(UUID uuid);
    
    public abstract void saveUserData(UUID uuid, UserData data);

    public abstract boolean wasSeenBefore(UUID uuid);

    public abstract void getConfigDefaults(ConfigurationSection section);

    public abstract void clean();

    public abstract String getName();

    public String getConfigName() {
        return getName().toLowerCase().replace(" ", "");
    }

    public ConfigurationSection getConfigSection() {
        return plugin.getConfig().getConfigurationSection(getConfigName());
    }

    public abstract int getVersion();

    public abstract void setVersion(int version);

    public abstract void saveServerData(ServerData serverData);

    public abstract ServerData getNewestServerData();

    public abstract void close();
}
