/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan;

import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.command.PlanVelocityCommand;
import com.djrapitops.plan.modules.APFModule;
import com.djrapitops.plan.modules.FilesModule;
import com.djrapitops.plan.modules.SuperClassBindingModule;
import com.djrapitops.plan.modules.SystemObjectBindingModule;
import com.djrapitops.plan.modules.proxy.ProxySuperClassBindingModule;
import com.djrapitops.plan.modules.proxy.velocity.VelocityServerPropertiesModule;
import com.djrapitops.plan.modules.proxy.velocity.VelocitySuperClassBindingModule;
import com.djrapitops.plan.system.PlanSystem;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.PluginLang;
import com.djrapitops.plan.system.settings.theme.PlanColorScheme;
import com.djrapitops.plugin.VelocityPlugin;
import com.djrapitops.plugin.command.ColorScheme;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.logging.L;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import dagger.BindsInstance;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import org.slf4j.Logger;

import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;

@Singleton
@Component(modules = {
        VelocityPlanModule.class,
        SuperClassBindingModule.class,
        SystemObjectBindingModule.class,
        APFModule.class,
        FilesModule.class,
        ProxySuperClassBindingModule.class,
        VelocitySuperClassBindingModule.class,
        VelocityServerPropertiesModule.class
})
interface PlanVelocityComponent {

    PlanVelocityCommand planCommand();

    PlanSystem system();

    @Component.Builder
    interface Builder {

        @BindsInstance
        Builder plan(PlanVelocity plan);

        PlanVelocityComponent build();
    }
}

@Module
class VelocityPlanModule {

    @Provides
    @Singleton
    PlanPlugin providePlanPlugin(PlanVelocity plugin) {
        return plugin;
    }

    @Provides
    @Singleton
    @Named("mainCommand")
    CommandNode provideMainCommand(PlanVelocityCommand command) {
        return command;
    }
}

/**
 * Velocity Main class.
 * <p>
 * Based on the PlanBungee class
 *
 * @author MicleBrick
 */
@Plugin(id = "plan", name = "Plan", version = "4.4.6", description = "Player Analytics Plugin by Rsl1122", authors = {"Rsl1122"})
public class PlanVelocity extends VelocityPlugin implements PlanPlugin {

    private PlanSystem system;
    private Locale locale;

    @com.google.inject.Inject
    @DataDirectory
    private Path dataFolderPath;
    @com.google.inject.Inject
    private ProxyServer proxy;
    @com.google.inject.Inject
    private Logger slf4jLogger;

    @Override
    public File getDataFolder() {
        return dataFolderPath.toFile();
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
            logger.error("Plugin Failed to Initialize Correctly. If this issue is caused by config settings you can use /planbungee reload");
            onDisable();
        } catch (Exception e) {
            errorHandler.log(L.CRITICAL, this.getClass(), e);
            logger.error("Plugin Failed to Initialize Correctly. If this issue is caused by config settings you can use /planbungee reload");
            logger.error("This error should be reported at https://github.com/Rsl1122/Plan-PlayerAnalytics/issues");
            onDisable();
        }
        PlanVelocityCommand command = component.planCommand();
        command.registerCommands();
        registerCommand("planvelocity", command);
    }

    @Override
    public void onDisable() {
        system.disable();

        slf4jLogger.info(locale.getString(PluginLang.DISABLED));
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

    @Override
    public ProxyServer getProxy() {
        return proxy;
    }

    @Override
    protected Logger getLogger() {
        return slf4jLogger;
    }
}
