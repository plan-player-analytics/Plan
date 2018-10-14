package com.djrapitops.plan;

import com.djrapitops.plan.command.PlanBungeeCommand;
import com.djrapitops.plan.modules.*;
import com.djrapitops.plan.modules.proxy.ProxySuperClassBindingModule;
import com.djrapitops.plan.modules.proxy.bungee.BungeeServerPropertiesModule;
import com.djrapitops.plan.modules.proxy.bungee.BungeeSuperClassBindingModule;
import com.djrapitops.plan.system.PlanSystem;
import com.djrapitops.pluginbridge.plan.PluginBridgeModule;
import dagger.BindsInstance;
import dagger.Component;

import javax.inject.Singleton;

/**
 * Dagger Component that constructs the plugin systems running on Bungee.
 *
 * @author Rsl1122
 */
@Singleton
@Component(modules = {
        BungeePlanModule.class,
        SuperClassBindingModule.class,
        SystemObjectBindingModule.class,
        APFModule.class,
        FilesModule.class,
        ProxySuperClassBindingModule.class,
        BungeeSuperClassBindingModule.class,
        BungeeServerPropertiesModule.class,
        PluginBridgeModule.Bungee.class
})
public interface PlanBungeeComponent {

    PlanBungeeCommand planCommand();

    PlanSystem system();

    @Component.Builder
    interface Builder {

        @BindsInstance
        Builder plan(PlanBungee plan);

        PlanBungeeComponent build();
    }
}