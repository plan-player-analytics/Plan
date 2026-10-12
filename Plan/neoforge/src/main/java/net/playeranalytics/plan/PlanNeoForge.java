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
package net.playeranalytics.plan;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.PlanSystem;
import com.djrapitops.plan.commands.use.ColorScheme;
import com.djrapitops.plan.commands.use.Subcommand;
import com.djrapitops.plan.exceptions.EnableException;
import com.djrapitops.plan.gathering.ServerShutdownSave;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.PluginLang;
import com.djrapitops.plan.settings.theme.PlanColorScheme;
import com.djrapitops.plan.utilities.java.ThreadContextClassLoaderSwap;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.dedicated.DedicatedServer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.playeranalytics.plan.commands.FabricCommandManager;
import net.playeranalytics.plan.identification.properties.NeoForgeServerProperties;
import net.playeranalytics.plugin.NeoForgePlatformLayer;
import net.playeranalytics.plugin.PlatformAbstractionLayer;
import net.playeranalytics.plugin.scheduling.RunnableFactory;
import net.playeranalytics.plugin.server.FabricPluginLogger;

import java.io.File;
import java.io.InputStream;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Main class for Plan's NeoForge version, using native lifecycle and command events
 * and FFAPI for shared Fabric listeners.
 */
@Mod(value = "plan", dist = Dist.DEDICATED_SERVER)
public class PlanNeoForge implements PlanPlugin {

    private DedicatedServer server;
    private CommandDispatcher<CommandSourceStack> commandDispatcher;
    private Subcommand rootCommand;

    private PlanSystem system;
    private Locale locale;
    private ServerShutdownSave serverShutdownSave;

    private FabricPluginLogger pluginLogger;
    private RunnableFactory runnableFactory;
    private PlatformAbstractionLayer abstractionLayer;
    private ErrorLogger errorLogger;

    public PlanNeoForge(IEventBus modEventBus) {
        abstractionLayer = new NeoForgePlatformLayer(this);
        pluginLogger = (FabricPluginLogger) abstractionLayer.getPluginLogger();
        runnableFactory = abstractionLayer.getRunnableFactory();

        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
        NeoForge.EVENT_BUS.addListener(this::onServerStarting);
        NeoForge.EVENT_BUS.addListener(this::onServerStopping);
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        commandDispatcher = event.getDispatcher();
        if (system != null && system.isEnabled() && rootCommand != null) {
            registerCommand(rootCommand);
        }
    }

    private void onServerStarting(ServerStartingEvent event) {
        this.server = (DedicatedServer) event.getServer();
        onEnable();
    }

    private void onServerStopping(ServerStoppingEvent event) {
        onDisable();
    }

    @Override
    public InputStream getResource(String resource) {
        return this.getClass().getResourceAsStream("/" + resource);
    }

    @Override
    public ColorScheme getColorScheme() {
        return PlanColorScheme.create(system.getConfigSystem().getConfig(), pluginLogger);
    }

    @Override
    public PlanSystem getSystem() {
        return system;
    }

    @Override
    public void registerCommand(Subcommand command) {
        rootCommand = command;
        new FabricCommandManager(commandDispatcher, this, errorLogger).registerRoot(command, runnableFactory);
    }

    @Override
    public void onEnable() {
        PlanNeoForgeComponent component = DaggerPlanNeoForgeComponent.builder()
                .plan(this)
                .abstractionLayer(abstractionLayer)
                .server(server)
                .serverProperties(new NeoForgeServerProperties(getServer()))
                .build();

        try {
            system = ThreadContextClassLoaderSwap.performOperation(getClass().getClassLoader(), component::system);
            errorLogger = component.errorLogger();
            serverShutdownSave = component.serverShutdownSave();
            locale = system.getLocaleSystem().getLocale();
            registerCommand(component.planCommand().build());
            system.enable();

            pluginLogger.info(locale.getString(PluginLang.ENABLED));
        } catch (AbstractMethodError e) {
            pluginLogger.error("Plugin ran into AbstractMethodError, server restart is required! This error is likely caused by updating the JAR without a restart.");
        } catch (EnableException e) {
            pluginLogger.error("----------------------------------------");
            pluginLogger.error("Error: " + e.getMessage());
            pluginLogger.error("----------------------------------------");
            pluginLogger.error("Plugin failed to initialize correctly. If this issue is caused by config settings you can use /plan reload");
            onDisable();
        } catch (Exception e) {
            String version = abstractionLayer.getPluginInformation().getVersion();
            pluginLogger.error(this.getClass().getSimpleName() + "-v" + version, e);
            pluginLogger.error("Plugin Failed to Initialize Correctly. If this issue is caused by config settings you can use /plan reload");
            pluginLogger.error("This error should be reported at https://github.com/plan-player-analytics/Plan/issues");
            onDisable();
        }
        if (system != null) {
            system.getProcessing().submitNonCritical(() -> system.getListenerSystem().callEnableEvent(this));
        }
    }

    @Override
    public void onDisable() {
        storeSessionsOnShutdown();
        runnableFactory.cancelAllKnownTasks();

        if (system != null) system.disable();

        pluginLogger.info(Locale.getStringNullSafe(locale, PluginLang.DISABLED));
    }

    private void storeSessionsOnShutdown() {
        if (serverShutdownSave != null) {
            Optional<Future<?>> complete = serverShutdownSave.performSave();
            if (complete.isPresent()) {
                try {
                    complete.get().get(4, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    pluginLogger.error("Failed to save sessions to database on shutdown: " + e.getCause().getMessage());
                } catch (TimeoutException e) {
                    pluginLogger.info(Locale.getStringNullSafe(locale, PluginLang.DISABLED_UNSAVED_SESSIONS_TIMEOUT));
                }
            }
        }
    }

    public File getDataFolder() {
        return FMLPaths.CONFIGDIR.get().resolve("plan").toFile();
    }

    public DedicatedServer getServer() {
        return server;
    }
}
