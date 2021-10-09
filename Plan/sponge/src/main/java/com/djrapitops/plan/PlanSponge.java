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

import com.djrapitops.plan.commands.use.ColorScheme;
import com.djrapitops.plan.commands.use.SpongeCommand;
import com.djrapitops.plan.commands.use.Subcommand;
import com.djrapitops.plan.exceptions.EnableException;
import com.djrapitops.plan.gathering.ServerShutdownSave;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.PluginLang;
import com.djrapitops.plan.settings.theme.PlanColorScheme;
import io.github.slimjar.app.builder.ApplicationBuilder;
import io.github.slimjar.resolver.data.Repository;
import net.playeranalytics.plugin.PlatformAbstractionLayer;
import net.playeranalytics.plugin.SpongePlatformLayer;
import net.playeranalytics.plugin.scheduling.RunnableFactory;
import net.playeranalytics.plugin.server.PluginLogger;
import org.bstats.sponge.Metrics;
import org.spongepowered.api.Game;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.manager.CommandMapping;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.*;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

@Plugin("plan")
public class PlanSponge implements PlanPlugin {

    private final Metrics metrics;
    private final PluginContainer plugin;
    private final File dataFolder;

    private PlanSpongeComponent component;
    private PlanSystem system;
    private Locale locale;
    private ServerShutdownSave serverShutdownSave;
    private PluginLogger logger;
    private RunnableFactory runnableFactory;
    private PlatformAbstractionLayer abstractionLayer;

    @com.google.inject.Inject
    public PlanSponge(
            @ConfigDir(sharedRoot = false) Path dataFolder,
            PluginContainer plugin/*,
            Metrics.Factory metrics*/
    ) {
        this.dataFolder = dataFolder.toFile();
        this.plugin = plugin;

        // Metrics commented out due to not being compatible with Sponge API 8 (yet!)
        int pluginId = 3086;
        this.metrics = null;//metrics.make(pluginId);
    }

    @Listener
    public void onServerLoad(ConstructPluginEvent event) {
        onLoad();
    }

    @Listener
    public void onServerStart(StartingEngineEvent<Server> event) {
        onEnable();
    }

    @Listener
    public void onServerStop(StoppingEngineEvent<Server> event) {
        onDisable();
    }

    private void onLoad() {
        abstractionLayer = new SpongePlatformLayer(plugin, dataFolder);
        logger = abstractionLayer.getPluginLogger();
        runnableFactory = abstractionLayer.getRunnableFactory();

        // TODO: incompatible with Sponge's environment
//        logger.info("Loading dependencies, this might take a while...");
//        try {
//            ApplicationBuilder.appending("Plan")
//                    .logger((message, args) -> java.util.logging.Logger.getGlobal().log(Level.INFO, message, args))
//                    // Use paper repository for downloading slimjar dependencies
//                    .internalRepositories(Collections.singletonList(new Repository(new URL("https://papermc.io/repo/repository/maven-public/"))))
//                    .downloadDirectoryPath(Paths.get(getDataFolder().getAbsolutePath()).resolve("libraries"))
//                    .build();
//        } catch (IOException | ReflectiveOperationException | URISyntaxException | NoSuchAlgorithmException e) {
//            String version = abstractionLayer.getPluginInformation().getVersion();
//            java.util.logging.Logger.getGlobal().log(Level.SEVERE, e, () -> this.getClass().getSimpleName() + "-v" + version);
//            logger.error("Plan failed to load its dependencies correctly!");
//            logger.error("This error should be reported at https://github.com/plan-player-analytics/Plan/issues");
//            onDisable();
//        }
    }

    public void onEnable() {
        PlanSpongeComponent component = DaggerPlanSpongeComponent.builder()
                .plan(this)
                .abstractionLayer(abstractionLayer)
                .game(Sponge.game())
                .build();
        try {
            system = component.system();
            serverShutdownSave = component.serverShutdownSave();
            locale = system.getLocaleSystem().getLocale();
            system.enable();

            new BStatsSponge(
                    metrics, system.getDatabaseSystem().getDatabase()
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
            String version = abstractionLayer.getPluginInformation().getVersion();
            java.util.logging.Logger.getGlobal().log(Level.SEVERE, e, () -> this.getClass().getSimpleName() + "-v" + version);
            logger.error("Plugin Failed to Initialize Correctly. If this issue is caused by config settings you can use /plan reload");
            logger.error("This error should be reported at https://github.com/plan-player-analytics/Plan/issues");
            onDisable();
        }
        registerCommand(component.planCommand().build());
        if (system != null) {
            system.getProcessing().submitNonCritical(() -> system.getListenerSystem().callEnableEvent(this));
        }
    }

    public void onDisable() {
        storeSessionsOnShutdown();
        cancelAllTasks();
        if (system != null) system.disable();

        logger.info(Locale.getStringNullSafe(locale, PluginLang.DISABLED));
    }

    private void storeSessionsOnShutdown() {
        if (serverShutdownSave != null) {
            Optional<Future<?>> complete = serverShutdownSave.performSave();
            if (complete.isPresent()) {
                try {
                    complete.get().get(4, TimeUnit.SECONDS); // wait for completion for 4s
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    logger.error("Failed to save sessions to database on shutdown: " + e.getCause().getMessage());
                } catch (TimeoutException e) {
                    logger.info(Locale.getStringNullSafe(locale, PluginLang.DISABLED_UNSAVED_SESSIONS_TIMEOUT));
                }
            }
        }
    }

    public void cancelAllTasks() {
        runnableFactory.cancelAllKnownTasks();
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
    public void registerCommand(Subcommand command) {
        // NOOP: Sponge commands are registered via the event below
    }

    @Listener
    public void onRegisterCommand(RegisterCommandEvent<Command.Raw> event) {
        // TODO(vankka): component not available here, yet
//        Subcommand command = component.planCommand().build();
//        List<String> aliases = new ArrayList<>(command.getAliases());
//        event.register(plugin, new SpongeCommand(runnableFactory, system.getErrorLogger(), command), aliases.remove(0), aliases.toArray(new String[0]));
    }

    @Override
    public PlanSystem getSystem() {
        return system;
    }

    public Game getGame() {
        return Sponge.game();
    }

    public PluginContainer getPlugin() {
        return plugin;
    }

    @Override
    public File getDataFolder() {
        return dataFolder;
    }
}
