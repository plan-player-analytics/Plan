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

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import com.djrapitops.plan.addons.placeholderapi.NukkitPlaceholderRegistrar;
import com.djrapitops.plan.commands.use.*;
import com.djrapitops.plan.exceptions.EnableException;
import com.djrapitops.plan.gathering.ServerShutdownSave;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.PluginLang;
import com.djrapitops.plan.settings.theme.PlanColorScheme;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plugin.NukkitPlugin;
import com.djrapitops.plugin.benchmarking.Benchmark;
import com.djrapitops.plugin.command.ColorScheme;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.task.AbsRunnable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
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

    private final Map<String, Subcommand> commands = new HashMap<>();

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
            logger.error("This error should be reported at https://github.com/plan-player-analytics/Plan/issues");
            onDisable();
        }

        registerCommand(component.planCommand().build());
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
        if (serverShutdownSave != null) serverShutdownSave.performSave();
        cancelAllTasks();
        if (system != null) system.disable();

        logger.info(locale != null ? locale.getString(PluginLang.DISABLED) : PluginLang.DISABLED.getDefault());
    }

    @Override
    public void cancelAllTasks() {
        runnableFactory.cancelAllKnownTasks();
        Optional.ofNullable(Server.getInstance().getScheduler()).ifPresent(scheduler -> scheduler.cancelTask(this));
    }

    @Override
    public boolean onCommand(CommandSender actualSender, Command actualCommand, String label, String[] args) {
        String name = actualCommand.getName();
        Subcommand command = commands.get(name);
        if (command == null) return false;

        CMDSender sender;
        if (actualSender instanceof Player) {
            sender = new NukkitPlayerCMDSender((Player) actualSender);
        } else {
            sender = new NukkitCMDSender(actualSender);
        }

        runnableFactory.create("", new AbsRunnable() {
            @Override
            public void run() {
                try {
                    command.getExecutor().accept(sender, new Arguments(args));
                } catch (Exception e) {
                    system.getErrorLogger().log(L.ERROR, e, ErrorContext.builder()
                            .related(sender.getClass())
                            .related(label + " " + Arrays.toString(args))
                            .build());
                }
            }
        }).runTaskAsynchronously();
        return true;
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
            commands.put(name, command);
        }
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