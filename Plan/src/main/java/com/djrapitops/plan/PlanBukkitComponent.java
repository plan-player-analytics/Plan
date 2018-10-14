package com.djrapitops.plan;

import com.djrapitops.plan.command.PlanCommand;
import com.djrapitops.plan.modules.*;
import com.djrapitops.plan.modules.server.ServerSuperClassBindingModule;
import com.djrapitops.plan.modules.server.bukkit.BukkitServerPropertiesModule;
import com.djrapitops.plan.modules.server.bukkit.BukkitSuperClassBindingModule;
import com.djrapitops.plan.system.PlanSystem;
import com.djrapitops.pluginbridge.plan.PluginBridgeModule;
import dagger.BindsInstance;
import dagger.Component;

import javax.inject.Singleton;

/**
 * Dagger Component that constructs the plugin systems running on Bukkit.
 *
 * @author Rsl1122
 */
@Singleton
@Component(modules = {
        BukkitPlanModule.class,
        SuperClassBindingModule.class,
        SystemObjectBindingModule.class,
        APFModule.class,
        FilesModule.class,
        BukkitServerPropertiesModule.class,
        ServerSuperClassBindingModule.class,
        BukkitSuperClassBindingModule.class,
        PluginBridgeModule.Bukkit.class
})
public interface PlanBukkitComponent {

    PlanCommand planCommand();

    PlanSystem system();

    @Component.Builder
    interface Builder {

        @BindsInstance
        Builder plan(Plan plan);

        PlanBukkitComponent build();
    }
}