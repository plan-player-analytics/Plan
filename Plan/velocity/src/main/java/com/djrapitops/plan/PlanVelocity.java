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
import com.djrapitops.plan.commands.use.Subcommand;
import com.djrapitops.plan.commands.use.VelocityCommand;
import com.djrapitops.plan.exceptions.EnableException;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.PluginLang;
import com.djrapitops.plan.settings.theme.PlanColorScheme;
import com.djrapitops.plan.unrelocate.org.slf4j.Logger;
import com.djrapitops.plan.utilities.java.ThreadContextClassLoaderSwap;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import net.playeranalytics.plan.BuildParameters;
import net.playeranalytics.plugin.PlatformAbstractionLayer;
import net.playeranalytics.plugin.VelocityPlatformLayer;
import net.playeranalytics.plugin.scheduling.RunnableFactory;
import net.playeranalytics.plugin.server.PluginLogger;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.logging.Level;

/**
 * Velocity Main class.
 * <p>
 * Based on the PlanBungee class
 *
 * @author MicleBrick
 */
@Plugin(
        id = "plan",
        name = "Plan",
        version = BuildParameters.VERSION,
        description = "Player Analytics Plugin by AuroraLS3",
        dependencies = {
                @Dependency(id = "viaversion", optional = true),
                @Dependency(id = "redisbungee", optional = true)
        },
        authors = {"AuroraLS3"}
)
public class PlanVelocity implements PlanPlugin {

    private final ProxyServer proxy;
    private final Logger slf4jLogger;
    private final Path dataFolderPath;
    private PlanSystem system;
    private Locale locale;
    private PluginLogger logger;
    private RunnableFactory runnableFactory;
    private PlatformAbstractionLayer abstractionLayer;
    private ErrorLogger errorLogger;

    @com.google.inject.Inject
    public PlanVelocity(
            ProxyServer proxy,
            Logger slf4jLogger,
            @DataDirectory Path dataFolderPath
    ) {
        this.proxy = proxy;
        this.slf4jLogger = slf4jLogger;
        this.dataFolderPath = dataFolderPath;
    }

    @Subscribe
    public void onProxyStart(ProxyInitializeEvent event) {
        abstractionLayer = new VelocityPlatformLayer(this, proxy, new Slf4jLoggerWrapper(slf4jLogger), dataFolderPath);
        logger = abstractionLayer.getPluginLogger();
        runnableFactory = abstractionLayer.getRunnableFactory();

        onEnable();
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        onDisable();
    }

    @Override
    public void onEnable() {
        PlanVelocityComponent component = DaggerPlanVelocityComponent.builder()
                .plan(this)
                .abstractionLayer(abstractionLayer)
                .build();
        try {
            system = ThreadContextClassLoaderSwap.performOperation(getClass().getClassLoader(), component::system);
            errorLogger = component.errorLogger();
            locale = system.getLocaleSystem().getLocale();
            system.enable();

            logger.info(locale.getString(PluginLang.ENABLED));
        } catch (AbstractMethodError e) {
            logger.error("Plugin ran into AbstractMethodError - Server restart is required. Likely cause is updating the jar without a restart.");
        } catch (EnableException e) {
            logger.error("----------------------------------------");
            logger.error("Error: " + e.getMessage());
            logger.error("----------------------------------------");
            logger.error("Plugin Failed to Initialize Correctly. If this issue is caused by config settings you can use /planvelocity reload");
            onDisable();
        } catch (Exception e) {
            String version = abstractionLayer.getPluginInformation().getVersion();
            java.util.logging.Logger.getGlobal().log(Level.SEVERE, e, () -> this.getClass().getSimpleName() + "-v" + version);
            logger.error("Plugin Failed to Initialize Correctly. If this issue is caused by config settings you can use /planvelocity reload");
            logger.error("This error should be reported at https://github.com/plan-player-analytics/Plan/issues");
            onDisable();
        }

        registerCommand(component.planCommand().build());
        if (system != null) {
            system.getProcessing().submitNonCritical(() -> system.getListenerSystem().callEnableEvent(this));
        }
    }

    @Override
    public void onDisable() {
        runnableFactory.cancelAllKnownTasks();
        if (system != null) system.disable();

        logger.info(locale.getString(PluginLang.DISABLED));
    }


    @Override
    public void registerCommand(Subcommand command) {
        if (command == null) {
            logger.warn("Attempted to register a null command!");
            return;
        }
        CommandManager commandManager = proxy.getCommandManager();
        commandManager.register(
                commandManager.metaBuilder(command.getPrimaryAlias())
                        .aliases(command.getAliases().toArray(new String[0]))
                        .build(),
                new VelocityCommand(runnableFactory, errorLogger, command)
        );
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
    public PlanSystem getSystem() {
        return system;
    }

    public ProxyServer getProxy() {
        return proxy;
    }

    public Logger getSlf4jLogger() {
        return slf4jLogger;
    }

    @Override
    public File getDataFolder() {
        return dataFolderPath.toFile();
    }
}
