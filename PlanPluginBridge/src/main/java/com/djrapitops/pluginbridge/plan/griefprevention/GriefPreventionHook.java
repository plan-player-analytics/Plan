package com.djrapitops.pluginbridge.plan.griefprevention;

import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.pluginbridge.plan.Hook;
import me.ryanhamshire.GriefPrevention.DataStore;
import me.ryanhamshire.GriefPrevention.GriefPrevention;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.bukkit.plugin.java.JavaPlugin.getPlugin;

/**
 * A Class responsible for hooking to GriefPrevention and registering data
 * sources.
 *
 * @author Rsl1122
 * @since 3.5.0
 */
@Singleton
public class GriefPreventionHook extends Hook {

    @Inject
    public GriefPreventionHook() {
        super("me.ryanhamshire.GriefPrevention.GriefPrevention");
    }

    public void hook(HookHandler handler) throws NoClassDefFoundError {
        if (enabled) {
            DataStore dataStore = getPlugin(GriefPrevention.class).dataStore;
            handler.addPluginDataSource(new GriefPreventionData(dataStore));
        }
    }
}
