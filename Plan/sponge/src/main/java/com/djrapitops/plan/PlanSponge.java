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

import com.djrapitops.plan.commands.use.SpongeCommand;
import com.djrapitops.plan.commands.use.Subcommand;
import com.djrapitops.plan.exceptions.EnableException;
import com.djrapitops.plan.gathering.ServerShutdownSave;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.PluginLang;
import com.djrapitops.plan.settings.theme.PlanColorScheme;
import com.djrapitops.plugin.SpongePlugin;
import com.djrapitops.plugin.command.ColorScheme;
import com.djrapitops.plugin.logging.L;
import org.bstats.sponge.Metrics2;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.command.CommandMapping;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.Task;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Plugin(
        id = "plan",
        name = "Plan",
        version = "@version@",
        description = "Player Analytics Plugin by Rsl1122",
        authors = {"Rsl1122"},
        dependencies = {
                @Dependency(id = "griefprevention", optional = true),
                @Dependency(id = "luckperms", optional = true),
                @Dependency(id = "nucleus", optional = true),
                @Dependency(id = "redprotect", optional = true),
                @Dependency(id = "nuvotifier", optional = true)
        }
)
public class PlanSponge extends SpongePlugin implements PlanPlugin {

    @com.google.inject.Inject
    private Metrics2.Factory metrics;

    @com.google.inject.Inject
    private Logger slf4jLogger;

    @com.google.inject.Inject
    @ConfigDir(sharedRoot = false)
    private File dataFolder;
    private PlanSystem system;
    private Locale locale;
    private ServerShutdownSave serverShutdownSave;

    private final Map<String, CommandMapping> commands = new HashMap<>();

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        onEnable();
    }

    @Listener
    public void onServerStop(GameStoppingServerEvent event) {
        onDisable();
    }

    @Override
    public void onEnable() {
        PlanSpongeComponent component = DaggerPlanSpongeComponent.builder().plan(this).build();
        try {
            system = component.system();
            serverShutdownSave = component.serverShutdownSave();
            locale = system.getLocaleSystem().getLocale();
            system.enable();

            int pluginId = 3086;
            new BStatsSponge(
                    metrics.make(pluginId),
                    system.getDatabaseSystem().getDatabase()
            ).registerMetrics();

            logger.info(locale.getString(PluginLang.ENABLED));
        } catch (AbstractMethodError e) {
            logger.error("Plugin ran into AbstractMethodError - Server restart is required. Likely cause is updating the jar without a restart.");
        } catch (EnableException e) {
            logger.error("----------------------------------------");
            logger.error("Error: " + e.getMessage());
            logger.error("----------------------------------------");
            logger.error("Plugin Failed to Initialize Correctly. If this issue is caused by config settings you can use /plan reload");
            onDisable();
        } catch (Exception e) {
            errorHandler.log(L.CRITICAL, this.getClass(), e);
            logger.error("Plugin Failed to Initialize Correctly. If this issue is caused by config settings you can use /plan reload");
            logger.error("This error should be reported at https://github.com/Rsl1122/Plan-PlayerAnalytics/issues");
            onDisable();
        }
        registerCommand(component.planCommand().build());
        if (system != null) {
            system.getProcessing().submitNonCritical(() -> system.getListenerSystem().callEnableEvent(this));
        }
    }

    @Override
    public void onDisable() {
        if (serverShutdownSave != null) serverShutdownSave.performSave();
        cancelAllTasks();
        if (system != null) system.disable();

        logger.info(locale.getString(PluginLang.DISABLED));
    }

    @Override
    public void cancelAllTasks() {
        runnableFactory.cancelAllKnownTasks();
        for (Task task : Sponge.getScheduler().getScheduledTasks(this)) {
            task.cancel();
        }
    }

    @Override
    public InputStream getResource(String resource) {
        return getClass().getResourceAsStream("/" + resource);
    }

    @Override
    public ColorScheme getColorScheme() {
        return PlanColorScheme.create(system.getConfigSystem().getConfig(), logger);
    }

    @Override
    public void onReload() {
        // Nothing to be done, systems are disabled
    }

    @Override
    public boolean isReloading() {
        return false;
    }

    @Override
    public void registerCommand(Subcommand command) {
        if (command == null) {
            logger.warn("Attempted to register a null command!");
            return;
        }
        for (String name : command.getAliases()) {
            CommandManager commandManager = Sponge.getCommandManager();

            CommandMapping registered = commands.get(name);
            if (registered != null) {
                commandManager.removeMapping(registered);
            }

            Optional<CommandMapping> register = commandManager.register(this, new SpongeCommand(runnableFactory, system.getErrorLogger(), command), name);
            register.ifPresent(commandMapping -> commands.put(name, commandMapping));
        }
    }

    @Override
    public Logger getLogger() {
        return slf4jLogger;
    }

    @Override
    public File getDataFolder() {
        return dataFolder;
    }

    @Override
    public String getVersion() {
        return getClass().getAnnotation(Plugin.class).version();
    }

    @Override
    public PlanSystem getSystem() {
        return system;
    }

    public Game getGame() {
        return Sponge.getGame();
    }
}
