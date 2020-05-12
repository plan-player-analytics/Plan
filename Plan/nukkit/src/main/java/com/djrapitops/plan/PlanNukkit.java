/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan;

import com.djrapitops.plan.addons.placeholderapi.NukkitPlaceholderRegistrar;
import com.djrapitops.plan.commands.OldPlanCommand;
import com.djrapitops.plan.commands.use.Subcommand;
import com.djrapitops.plan.exceptions.EnableException;
import com.djrapitops.plan.gathering.ServerShutdownSave;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.PluginLang;
import com.djrapitops.plan.settings.theme.PlanColorScheme;
import com.djrapitops.plugin.NukkitPlugin;
import com.djrapitops.plugin.benchmarking.Benchmark;
import com.djrapitops.plugin.command.ColorScheme;
import com.djrapitops.plugin.task.AbsRunnable;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main class for Nukkit that manages the plugin.
 *
 * @author Rsl1122
 */
public class PlanNukkit extends NukkitPlugin implements PlanPlugin {

    private PlanSystem system;
    private Locale locale;
    private ServerShutdownSave serverShutdownSave;

    @Override
    public void onEnable() {
        PlanNukkitComponent component = DaggerPlanNukkitComponent.builder().plan(this).build();
        try {
            timings.start("Enable");
            system = component.system();
            serverShutdownSave = component.serverShutdownSave();
            locale = system.getLocaleSystem().getLocale();
            system.enable();

            registerPlaceholderAPI(component.placeholders());

            logger.debug("Verbose debug messages are enabled.");
            String benchTime = " (" + timings.end("Enable").map(Benchmark::toDurationString).orElse("-") + ")";
            logger.info(locale.getString(PluginLang.ENABLED) + benchTime);
        } catch (AbstractMethodError e) {
            logger.error("Plugin ran into AbstractMethodError - Server restart is required. Likely cause is updating the jar without a restart.");
        } catch (EnableException e) {
            logger.error("----------------------------------------");
            logger.error("Error: " + e.getMessage());
            logger.error("----------------------------------------");
            logger.error("Plugin Failed to Initialize Correctly. If this issue is caused by config settings you can use /plan reload");
            onDisable();
        } catch (Exception e) {
            Logger.getGlobal().log(Level.SEVERE, this.getClass().getSimpleName() + "-v" + getVersion(), e);
            logger.error("Plugin Failed to Initialize Correctly. If this issue is caused by config settings you can use /plan reload");
            logger.error("This error should be reported at https://github.com/Rsl1122/Plan-PlayerAnalytics/issues");
            onDisable();
        }
        OldPlanCommand command = component.planCommand();
        command.registerCommands();
        registerCommand("plan", command);
        if (system != null) {
            system.getProcessing().submitNonCritical(() -> system.getListenerSystem().callEnableEvent(this));
        }
    }

    @Override
    public ColorScheme getColorScheme() {
        return PlanColorScheme.create(system.getConfigSystem().getConfig(), logger);
    }

    /**
     * Disables the plugin.
     */
    @Override
    public void onDisable() {
        if (serverShutdownSave != null) {
            serverShutdownSave.performSave();
        }
        if (system != null) {
            system.disable();
        }

        logger.info(locale != null ? locale.getString(PluginLang.DISABLED) : PluginLang.DISABLED.getDefault());
    }

    @Override
    public String getVersion() {
        return getDescription().getVersion();
    }

    @Override
    public void onReload() {
        // Nothing to be done, systems are disabled
    }

    @Override
    public boolean isReloading() {
        return reloading;
    }

    @Override
    public void registerCommand(Subcommand subcommand) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PlanSystem getSystem() {
        return system;
    }

    private void registerPlaceholderAPI(NukkitPlaceholderRegistrar placeholders) {
        if (this.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            runnableFactory.create("Placeholders Registrar", new AbsRunnable() {
                @Override
                public void run() {
                    try {
                        placeholders.register();
                    } catch (Exception | NoClassDefFoundError | NoSuchMethodError failed) {
                        logger.warn("Failed to register PlaceholderAPI placeholders: " + failed.toString());
                    }
                }
            }).runTask();
        }
    }
}