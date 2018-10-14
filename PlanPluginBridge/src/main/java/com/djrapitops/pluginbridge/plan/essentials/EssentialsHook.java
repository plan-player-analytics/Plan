package com.djrapitops.pluginbridge.plan.essentials;

import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.pluginbridge.plan.Hook;
import com.earth2me.essentials.Essentials;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.bukkit.plugin.java.JavaPlugin.getPlugin;

/**
 * A Class responsible for hooking to Essentials.
 *
 * @author Rsl1122
 * @since 3.1.0
 */
@Singleton
public class EssentialsHook extends Hook {

    @Inject
    public EssentialsHook() {
        super("com.earth2me.essentials.Essentials");
    }

    public void hook(HookHandler handler) throws NoClassDefFoundError {
        if (enabled) {
            Essentials ess = getPlugin(Essentials.class);
            handler.addPluginDataSource(new EssentialsData(ess));
        }
    }
}
