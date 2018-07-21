/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.bungee;

import com.djrapitops.plan.PlanHelper;
import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.bungee.command.PlanBungeeCommand;
import com.djrapitops.plan.system.settings.locale.Locale;
import com.djrapitops.plan.system.settings.locale.Msg;
import com.djrapitops.plan.system.settings.theme.PlanColorScheme;
import com.djrapitops.plugin.BungeePlugin;
import com.djrapitops.plugin.api.Benchmark;
import com.djrapitops.plugin.api.utility.log.DebugLog;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.settings.ColorScheme;

import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Bungee Main class.
 *
 * @author Rsl1122
 */
public class PlanBungee extends BungeePlugin implements PlanPlugin {

    private BungeeSystem system;

    @Override
    public void onEnable() {
        super.onEnable();

        PlanHelper.setInstance(this); //ToDo: Really dirty temporary hack

        try {
            system = new BungeeSystem(this);
            system.enable();

            Log.info(Locale.get(Msg.ENABLED).toString());
        } catch (AbstractMethodError e) {
            Log.error("Plugin ran into AbstractMethodError - Server restart is required. Likely cause is updating the jar without a restart.");
        } catch (EnableException e) {
            Log.error("----------------------------------------");
            Log.error("Error: " + e.getMessage());
            Log.error("----------------------------------------");
            Log.error("Plugin Failed to Initialize Correctly. If this issue is caused by config settings you can use /planbungee reload");
            onDisable();
        } catch (Exception e) {
            Logger.getGlobal().log(Level.SEVERE, this.getClass().getSimpleName() + "-v" + getVersion(), e);
            Log.error("Plugin Failed to Initialize Correctly. If this issue is caused by config settings you can use /planbungee reload");
            Log.error("This error should be reported at https://github.com/Rsl1122/Plan-PlayerAnalytics/issues");
            onDisable();
        }
        registerCommand("planbungee", new PlanBungeeCommand(this));
    }

    @Override
    public void onDisable() {
        system.disable();

        Log.info(Locale.get(Msg.DISABLED).toString());
        Benchmark.pluginDisabled(PlanBungee.class);
        DebugLog.pluginDisabled(PlanBungee.class);
    }

    @Override
    public String getVersion() {
        return super.getDescription().getVersion();
    }

    @Override
    public void onReload() {
    }

    @Override
    public InputStream getResource(String resource) {
        return getResourceAsStream(resource);
    }

    @Override
    public ColorScheme getColorScheme() {
        return PlanColorScheme.create();
    }

    public BungeeSystem getSystem() {
        return system;
    }

    @Override
    public boolean isReloading() {
        return reloading;
    }
}
