/*
 *    Player Analytics Bukkit plugin for monitoring server activity.
 *    Copyright (C) 2017  Risto Lahtela / Rsl1122
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the Plan License. (LICENSE)
 *    Modified software can only be redistributed if allowed in the licence.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    License for more details.
 *
 *    You should have received a copy of the License
 *    along with this program.
 *    If not it should be visible on the distribution page.
 *    Or here
 *    https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan;

import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.command.PlanCommand;
import com.djrapitops.plan.system.BukkitSystem;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.PluginLang;
import com.djrapitops.plan.system.processing.importing.ImporterManager;
import com.djrapitops.plan.system.processing.importing.importers.OfflinePlayerImporter;
import com.djrapitops.plan.system.settings.theme.PlanColorScheme;
import com.djrapitops.plan.utilities.metrics.BStatsBukkit;
import com.djrapitops.plugin.BukkitPlugin;
import com.djrapitops.plugin.StaticHolder;
import com.djrapitops.plugin.api.Benchmark;
import com.djrapitops.plugin.api.utility.log.DebugLog;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.settings.ColorScheme;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main class for Bukkit that manages the plugin.
 *
 * @author Rsl1122
 * @since 1.0.0
 */
public class Plan extends BukkitPlugin implements PlanPlugin {

    private BukkitSystem system;
    private Locale locale;

    /**
     * Used to get the plugin-instance singleton.
     *
     * @return this object.
     */
    public static Plan getInstance() {
        return (Plan) StaticHolder.getInstance(Plan.class);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        try {
            Benchmark.start("Enable");
            system = new BukkitSystem(this);
            system.enable();
            locale = system.getLocaleSystem().getLocale();

            ImporterManager.registerImporter(new OfflinePlayerImporter());

            new BStatsBukkit(this).registerMetrics();

            Log.debug("Verbose debug messages are enabled.");
            Benchmark.stop("Enable", "Enable");
            Log.logDebug("Enable");
            Log.info(locale.getString(PluginLang.ENABLED));
        } catch (AbstractMethodError e) {
            Log.error("Plugin ran into AbstractMethodError - Server restart is required. Likely cause is updating the jar without a restart.");
        } catch (EnableException e) {
            Log.error("----------------------------------------");
            Log.error("Error: " + e.getMessage());
            Log.error("----------------------------------------");
            Log.error("Plugin Failed to Initialize Correctly. If this issue is caused by config settings you can use /plan reload");
            onDisable();
        } catch (Exception e) {
            Logger.getGlobal().log(Level.SEVERE, this.getClass().getSimpleName() + "-v" + getVersion(), e);
            Log.error("Plugin Failed to Initialize Correctly. If this issue is caused by config settings you can use /plan reload");
            Log.error("This error should be reported at https://github.com/Rsl1122/Plan-PlayerAnalytics/issues");
            onDisable();
        }
        registerCommand("plan", new PlanCommand(this));
    }

    @Override
    public ColorScheme getColorScheme() {
        return PlanColorScheme.create();
    }

    /**
     * Disables the plugin.
     */
    @Override
    public void onDisable() {
        system.disable();

        Log.info(locale.getString(PluginLang.DISABLED));
        Benchmark.pluginDisabled(Plan.class);
        DebugLog.pluginDisabled(Plan.class);
    }

    @Override
    public String getVersion() {
        return getDescription().getVersion();
    }

    @Override
    public void onReload() {

    }

    @Override
    public boolean isReloading() {
        return reloading;
    }

    /**
     * @deprecated Deprecated due to use of APF Config
     */
    @Override
    @Deprecated
    public void reloadConfig() {
        throw new IllegalStateException("This method should be used on this plugin. Use onReload() instead");
    }

    /**
     * @deprecated Deprecated due to use of APF Config
     */
    @Override
    @Deprecated
    public FileConfiguration getConfig() {
        throw new IllegalStateException("This method should be used on this plugin. Use getMainConfig() instead");
    }

    /**
     * @deprecated Deprecated due to use of APF Config
     */
    @Override
    @Deprecated
    public void saveConfig() {
        throw new IllegalStateException("This method should be used on this plugin. Use getMainConfig().save() instead");
    }

    /**
     * @deprecated Deprecated due to use of APF Config
     */
    @Override
    @Deprecated
    public void saveDefaultConfig() {
        throw new IllegalStateException("This method should be used on this plugin.");
    }

    public BukkitSystem getSystem() {
        return system;
    }
}
