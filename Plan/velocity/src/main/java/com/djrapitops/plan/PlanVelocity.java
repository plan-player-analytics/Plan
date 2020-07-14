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

import com.djrapitops.plan.commands.use.Subcommand;
import com.djrapitops.plan.commands.use.VelocityCommand;
import com.djrapitops.plan.exceptions.EnableException;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.PluginLang;
import com.djrapitops.plan.settings.theme.PlanColorScheme;
import com.djrapitops.plugin.VelocityPlugin;
import com.djrapitops.plugin.command.ColorScheme;
import com.djrapitops.plugin.logging.L;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

import java.io.InputStream;
import java.nio.file.Path;

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
        version = "@version@",
        description = "Player Analytics Plugin by Rsl1122",
        authors = {"Rsl1122"}
)
public class PlanVelocity extends VelocityPlugin implements PlanPlugin {

    private PlanSystem system;
    private Locale locale;

    @com.google.inject.Inject
    public PlanVelocity(ProxyServer proxy, Logger slf4jLogger, @DataDirectory Path dataFolderPath) {
        super(proxy, slf4jLogger, dataFolderPath);
    }

    @Subscribe
    public void onProxyStart(ProxyInitializeEvent event) {
        onEnable();
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        onDisable();
    }

    @Override
    public void onEnable() {
        PlanVelocityComponent component = DaggerPlanVelocityComponent.builder().plan(this).build();
        try {
            system = component.system();
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
            errorHandler.log(L.CRITICAL, this.getClass(), e);
            logger.error("Plugin Failed to Initialize Correctly. If this issue is caused by config settings you can use /planvelocity reload");
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
        system.disable();

        logger.info(locale.getString(PluginLang.DISABLED));
    }

    @Override
    public void onReload() {
        // Nothing to be done, systems are disabled
    }

    @Override
    public void registerCommand(Subcommand command) {
        if (command == null) {
            logger.warn("Attempted to register a null command!");
            return;
        }
        getProxy().getCommandManager().register(
                new VelocityCommand(runnableFactory, command),
                command.getAliases().toArray(new String[0])
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

    @Override
    public boolean isReloading() {
        return reloading;
    }
}
