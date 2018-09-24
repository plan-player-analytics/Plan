package com.djrapitops.plan;

import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.command.PlanCommand;
import com.djrapitops.plan.modules.APFModule;
import com.djrapitops.plan.modules.FilesModule;
import com.djrapitops.plan.modules.SuperClassBindingModule;
import com.djrapitops.plan.modules.SystemObjectBindingModule;
import com.djrapitops.plan.modules.server.ServerSuperClassBindingModule;
import com.djrapitops.plan.modules.server.sponge.SpongeInfoModule;
import com.djrapitops.plan.modules.server.sponge.SpongeSuperClassBindingModule;
import com.djrapitops.plan.system.PlanSystem;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.PluginLang;
import com.djrapitops.plan.system.settings.theme.PlanColorScheme;
import com.djrapitops.plan.utilities.metrics.BStatsSponge;
import com.djrapitops.plugin.SpongePlugin;
import com.djrapitops.plugin.StaticHolder;
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
        SuperClassBindingModule.class,
        SystemObjectBindingModule.class,
        APFModule.class,
        FilesModule.class,
        ServerSuperClassBindingModule.class,
        SpongeSuperClassBindingModule.class,
        SpongeInfoModule.class
})
interface PlanSpongeComponent {

    PlanCommand planCommand();

    PlanSystem system();

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
    @Singleton
    PlanPlugin providePlanPlugin(PlanSponge plugin) {
        return plugin;
    }

    @Provides
    @Singleton
    @Named("mainCommand")
    CommandNode provideMainCommand(PlanCommand command) {
        command.registerCommands();
        return command;
    }
}

@Plugin(id = "plan", name = "Plan", version = "4.4.3", description = "Player Analytics Plugin by Rsl1122", authors = {"Rsl1122"})
public class PlanSponge extends SpongePlugin implements PlanPlugin {

    @Inject
    private Metrics metrics;

    @Inject
    private Logger slf4jLogger;

    @Inject
    @ConfigDir(sharedRoot = false)
    private File dataFolder;
    private PlanSystem system;
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

            new BStatsSponge(
                    metrics,
                    system.getDatabaseSystem().getActiveDatabase()
            ).registerMetrics();

            slf4jLogger.info(locale.getString(PluginLang.ENABLED));
        } catch (AbstractMethodError e) {
            slf4jLogger.error("Plugin ran into AbstractMethodError - Server restart is required. Likely cause is updating the jar without a restart.");
        } catch (EnableException e) {
            slf4jLogger.error("----------------------------------------");
            slf4jLogger.error("Error: " + e.getMessage());
            slf4jLogger.error("----------------------------------------");
            slf4jLogger.error("Plugin Failed to Initialize Correctly. If this issue is caused by config settings you can use /plan reload");
            onDisable();
        } catch (Exception e) {
            errorHandler.log(L.CRITICAL, this.getClass(), e);
            slf4jLogger.error("Plugin Failed to Initialize Correctly. If this issue is caused by config settings you can use /plan reload");
            slf4jLogger.error("This error should be reported at https://github.com/Rsl1122/Plan-PlayerAnalytics/issues");
            onDisable();
        }
        registerCommand("plan", component.planCommand());
    }

    @Override
    public void onDisable() {
        if (system != null) {
            system.disable();
        }

        logger.info(locale.getString(PluginLang.DISABLED));
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
}
