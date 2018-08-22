/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan;

import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.command.PlanBungeeCommand;
import com.djrapitops.plan.modules.APFModule;
import com.djrapitops.plan.modules.bungee.BungeeAPIModule;
import com.djrapitops.plan.modules.bungee.BungeeConfigModule;
import com.djrapitops.plan.modules.bungee.BungeeDatabaseModule;
import com.djrapitops.plan.modules.bungee.BungeeInfoModule;
import com.djrapitops.plan.modules.common.ExportModule;
import com.djrapitops.plan.modules.common.LocaleModule;
import com.djrapitops.plan.modules.common.PluginHookModule;
import com.djrapitops.plan.modules.common.VersionCheckModule;
import com.djrapitops.plan.modules.server.ServerInfoSystemModule;
import com.djrapitops.plan.system.BungeeSystem;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.PluginLang;
import com.djrapitops.plan.system.settings.theme.PlanColorScheme;
import com.djrapitops.plan.utilities.metrics.BStatsBungee;
import com.djrapitops.plugin.BungeePlugin;
import com.djrapitops.plugin.StaticHolder;
import com.djrapitops.plugin.api.Benchmark;
import com.djrapitops.plugin.api.utility.log.DebugLog;
import com.djrapitops.plugin.command.ColorScheme;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.logging.L;
import dagger.BindsInstance;
import dagger.Component;
import dagger.Module;
import dagger.Provides;

import javax.inject.Named;
import javax.inject.Singleton;
import java.io.InputStream;

@Singleton
@Component(modules = {
        BungeePlanModule.class,
        APFModule.class,
        ExportModule.class,
        VersionCheckModule.class,
        BungeeConfigModule.class,
        LocaleModule.class,
        BungeeInfoModule.class,
        BungeeDatabaseModule.class,
        ServerInfoSystemModule.class,
        PluginHookModule.class,
        BungeeAPIModule.class
})
interface PlanBungeeComponent {

    PlanBungeeCommand planCommand();

    BungeeSystem system();

    @Component.Builder
    interface Builder {

        @Singleton
        @BindsInstance
        Builder plan(PlanBungee plan);

        PlanBungeeComponent build();
    }
}

@Module
class BungeePlanModule {

    @Provides
    PlanPlugin providePlanPlugin(PlanBungee plan) {
        return plan;
    }

    @Provides
    @Named("mainCommand")
    CommandNode provideMainCommand(PlanBungeeCommand command) {
        return command;
    }
}

/**
 * Bungee Main class.
 *
 * @author Rsl1122
 */
public class PlanBungee extends BungeePlugin implements PlanPlugin {

    private BungeeSystem system;
    private Locale locale;

    public static PlanBungee getInstance() {
        return (PlanBungee) StaticHolder.getInstance(PlanBungee.class);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        PlanBungeeComponent component = DaggerPlanBungeeComponent.builder().plan(this).build();
        try {
            system = component.system();
            locale = system.getLocaleSystem().getLocale();
            system.enable();

            new BStatsBungee(this).registerMetrics();

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
        registerCommand("planbungee", component.planCommand());
    }

    @Override
    public void onDisable() {
        system.disable();

        logger.info(locale.getString(PluginLang.DISABLED));
        Benchmark.pluginDisabled(PlanBungee.class);
        DebugLog.pluginDisabled(PlanBungee.class);
    }

    @Override
    public String getVersion() {
        return super.getDescription().getVersion();
    }

    @Override
    public void onReload() {
        // Nothing to be done, systems are disabled
    }

    @Override
    public InputStream getResource(String resource) {
        return getResourceAsStream(resource);
    }

    @Override
    public ColorScheme getColorScheme() {
        return PlanColorScheme.create();
    }

    @Override
    public BungeeSystem getSystem() {
        return system;
    }

    @Override
    public boolean isReloading() {
        return reloading;
    }
}
