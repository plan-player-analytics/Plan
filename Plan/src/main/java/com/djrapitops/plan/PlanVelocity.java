/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan;

import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.command.PlanVelocityCommand;
import com.djrapitops.plan.system.VelocitySystem;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.PluginLang;
import com.djrapitops.plan.system.settings.theme.PlanColorScheme;
import com.djrapitops.plan.utilities.metrics.BStatsVelocity;
import com.djrapitops.plugin.StaticHolder;
import com.djrapitops.plugin.VelocityPlugin;
import com.djrapitops.plugin.api.Benchmark;
import com.djrapitops.plugin.api.utility.log.DebugLog;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.settings.ColorScheme;
import com.google.inject.Inject;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * Velocity Main class.
 *
 * Based on the PlanBungee class
 *
 * @author MicleBrick
 */
@Plugin(id = "plan", name = "Plan", version = "4.4.6", description = "Player Analytics Plugin by Rsl1122", authors = {"Rsl1122"})
public class PlanVelocity extends VelocityPlugin implements PlanPlugin {

    private VelocitySystem system;
    private Locale locale;

    public static PlanVelocity getInstance() {
        return (PlanVelocity) StaticHolder.getInstance(PlanVelocity.class);
    }

    @Inject
    @DataDirectory
    private Path dataFolderPath;

    @Override
    public File getDataFolder() {
        return dataFolderPath.toFile();
    }

    @Override
    public void onEnable() {
        super.onEnable();
        try {
            system = new VelocitySystem(this);
            locale = system.getLocaleSystem().getLocale();
            system.enable();

            new BStatsVelocity(this).registerMetrics();

            Log.info(locale.getString(PluginLang.ENABLED));
        } catch (AbstractMethodError e) {
            Log.error("Plugin ran into AbstractMethodError - Server restart is required. Likely cause is updating the jar without a restart.");
        } catch (EnableException e) {
            Log.error("----------------------------------------");
            Log.error("Error: " + e.getMessage());
            Log.error("----------------------------------------");
            Log.error("Plugin Failed to Initialize Correctly. If this issue is caused by config settings you can use /planvelocity reload");
            onDisable();
        } catch (Exception e) {
            getLogger().error(this.getClass().getSimpleName() + "-v" + getVersion(), e);
            Log.error("Plugin Failed to Initialize Correctly. If this issue is caused by config settings you can use /planvelocity reload");
            Log.error("This error should be reported at https://github.com/Rsl1122/Plan-PlayerAnalytics/issues");
            onDisable();
        }
        registerCommand("planvelocity", new PlanVelocityCommand(this));
    }

    @Override
    public void onDisable() {
        system.disable();

        Log.info(locale.getString(PluginLang.DISABLED));
        Benchmark.pluginDisabled(PlanVelocity.class);
        DebugLog.pluginDisabled(PlanVelocity.class);
    }

    @Override
    public String getVersion() {
        return getClass().getAnnotation(Plugin.class).version();
    }


    @Override
    public void onReload() {
        // Nothing to be done, systems are disabled
    }

    @Override
    public InputStream getResource(String resource) {
        return getClass().getResourceAsStream(resource);
    }

    @Override
    public ColorScheme getColorScheme() {
        return PlanColorScheme.create();
    }

    @Override
    public VelocitySystem getSystem() {
        return system;
    }

    @Override
    public boolean isReloading() {
        return reloading;
    }

    @Inject
    private ProxyServer proxy;

    @Override
    public ProxyServer getProxy() {
        return proxy;
    }

    @Inject
    private Logger logger;

    @Override
    protected Logger getLogger() {
        return logger;
    }
}
