package com.djrapitops.plan;

import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.command.PlanCommand;
import com.djrapitops.plan.modules.APFModule;
import com.djrapitops.plan.modules.common.*;
import com.djrapitops.plan.modules.server.ServerAPIModule;
import com.djrapitops.plan.modules.server.ServerDataCacheModule;
import com.djrapitops.plan.modules.server.ServerDatabaseModule;
import com.djrapitops.plan.modules.server.ServerInfoSystemModule;
import com.djrapitops.plan.modules.server.bukkit.BukkitConfigModule;
import com.djrapitops.plan.modules.server.sponge.SpongeInfoModule;
import com.djrapitops.plan.system.SpongeSystem;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.PluginLang;
import com.djrapitops.plan.system.settings.theme.PlanColorScheme;
import com.djrapitops.plan.utilities.metrics.BStatsSponge;
import com.djrapitops.plugin.SpongePlugin;
import com.djrapitops.plugin.StaticHolder;
import com.djrapitops.plugin.api.Benchmark;
import com.djrapitops.plugin.api.utility.log.DebugLog;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.command.ColorScheme;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.logging.L;
import com.google.inject.Inject;
import dagger.BindsInstance;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import org.bstats.sponge.Metrics;
import org.slf4j.Logger;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.plugin.Plugin;

import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.io.InputStream;

@Singleton
@Component(modules = {
        SpongePlanModule.class,
        APFModule.class,
        ExportModule.class,
        VersionCheckModule.class,
        BukkitConfigModule.class,
        LocaleModule.class,
        ServerDatabaseModule.class,
        ServerDataCacheModule.class,
        WebServerSystemModule.class,
        ServerInfoSystemModule.class,
        SpongeInfoModule.class,
        PluginHookModule.class,
        ServerAPIModule.class,
})
interface PlanSpongeComponent {

    PlanCommand planCommand();

    SpongeSystem system();

    @Component.Builder
    interface Builder {

        @BindsInstance
        Builder plan(PlanSponge plan);

        PlanSpongeComponent build();
    }
}

@Module
class SpongePlanModule {

    @Provides
    PlanPlugin providePlanPlugin(PlanSponge plan) {
        return plan;
    }

    @Provides
    @Named("mainCommand")
    CommandNode provideMainCommand(PlanCommand command) {
        return command;
    }
}

@Plugin(id = "plan", name = "Plan", version = "4.4.3", description = "Player Analytics Plugin by Rsl1122", authors = {"Rsl1122"})
public class PlanSponge extends SpongePlugin implements PlanPlugin {

    @Inject
    private Metrics metrics;

    @Inject
    private Logger logger;

    @Inject
    @ConfigDir(sharedRoot = false)
    private File dataFolder;
    private SpongeSystem system;
    private Locale locale;

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        onEnable();
    }

    @Listener
    public void onServerStop(GameStoppingServerEvent event) {
        onDisable();
    }

    public static PlanSponge getInstance() {
        return (PlanSponge) StaticHolder.getInstance(PlanSponge.class);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        PlanSpongeComponent component = DaggerPlanSpongeComponent.builder().plan(this).build();
        try {
            system = component.system();
            locale = system.getLocaleSystem().getLocale();
            system.enable();

            new BStatsSponge(metrics).registerMetrics();

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
        registerCommand("plan", component.planCommand());
    }

    @Override
    public void onDisable() {
        if (system != null) {
            system.disable();
        }

        Log.info(locale.getString(PluginLang.DISABLED));
        Benchmark.pluginDisabled(PlanSponge.class);
        DebugLog.pluginDisabled(PlanSponge.class);
    }

    @Override
    public InputStream getResource(String resource) {
        return getClass().getResourceAsStream("/" + resource);
    }

    @Override
    public ColorScheme getColorScheme() {
        return PlanColorScheme.create();
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
    public Logger getLogger() {
        return logger;
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
    public SpongeSystem getSystem() {
        return system;
    }
}
