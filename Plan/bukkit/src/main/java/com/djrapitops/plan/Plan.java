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

import com.djrapitops.plan.addons.placeholderapi.BukkitPlaceholderRegistrar;
import com.djrapitops.plan.commands.use.BukkitCommand;
import com.djrapitops.plan.commands.use.Subcommand;
import com.djrapitops.plan.exceptions.EnableException;
import com.djrapitops.plan.gathering.ServerShutdownSave;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.PluginLang;
import com.djrapitops.plan.settings.theme.PlanColorScheme;
import com.djrapitops.plugin.BukkitPlugin;
import com.djrapitops.plugin.benchmarking.Benchmark;
import com.djrapitops.plugin.command.ColorScheme;
import com.djrapitops.plugin.task.AbsRunnable;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main class for Bukkit that manages the plugin.
 *
 * @author Rsl1122
 */
public class Plan extends BukkitPlugin implements PlanPlugin {

    private PlanSystem system;
    private Locale locale;
    private ServerShutdownSave serverShutdownSave;

    @Override
    public void onEnable() {
        PlanBukkitComponent component = DaggerPlanBukkitComponent.builder().plan(this).build();
        try {
            timings.start("Enable");
            system = component.system();
            serverShutdownSave = component.serverShutdownSave();
            locale = system.getLocaleSystem().getLocale();
            system.enable();

            registerMetrics();
            registerPlaceholderAPIExtension(component.placeholders());

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
        registerCommand(component.planCommand().build());
        if (system != null) {
            system.getProcessing().submitNonCritical(() -> system.getListenerSystem().callEnableEvent(this));
        }
    }

    private void registerPlaceholderAPIExtension(BukkitPlaceholderRegistrar placeholders) {
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
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

    private void registerMetrics() {
        Plan plugin = this;
        // Spigot 1.14 requires Sync events to be fired from a server thread.
        // Registering a service fires a sync event, and bStats registers a service,
        // so this has to be run on the server thread.
        runnableFactory.create("Register Metrics task", new AbsRunnable() {
            @Override
            public void run() {
                new BStatsBukkit(plugin).registerMetrics();
            }
        }).runTask();
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
    public void registerCommand(Subcommand command) {
        if (command == null) {
            logger.warn("Attempted to register a null command!");
            return;
        }
        for (String name : command.getAliases()) {
            PluginCommand registering = getCommand(name);
            if (registering == null) {
                logger.warn("Attempted to register '" + name + "'-command, but it is not in plugin.yml!");
                continue;
            }
            registering.setExecutor(new BukkitCommand(command));
        }
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

    @Override
    public PlanSystem getSystem() {
        return system;
    }
}