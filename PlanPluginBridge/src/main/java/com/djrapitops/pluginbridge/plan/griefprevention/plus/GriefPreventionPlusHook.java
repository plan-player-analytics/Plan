package com.djrapitops.pluginbridge.plan.griefprevention.plus;

import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.pluginbridge.plan.Hook;
import net.kaikk.mc.gpp.DataStore;
import net.kaikk.mc.gpp.GriefPreventionPlus;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.bukkit.plugin.java.JavaPlugin.getPlugin;

/**
 * A Class responsible for hooking to GriefPreventionPlus and registering data
 * sources.
 *
 * @author Rsl1122
 * @since 3.5.0
 */
@Singleton
public class GriefPreventionPlusHook extends Hook {

    @Inject
    public GriefPreventionPlusHook() {
        super("net.kaikk.mc.gpp.GriefPreventionPlus");
    }

    public void hook(HookHandler handler) throws NoClassDefFoundError {
        if (enabled) {
            DataStore dataStore = getPlugin(GriefPreventionPlus.class).getDataStore();
            handler.addPluginDataSource(new GriefPreventionPlusData(dataStore));
        }
    }
}
