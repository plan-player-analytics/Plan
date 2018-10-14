package com.djrapitops.plan;

import com.djrapitops.plan.command.PlanCommand;
import com.djrapitops.plan.modules.APFModule;
import com.djrapitops.plan.modules.FilesModule;
import com.djrapitops.plan.modules.SuperClassBindingModule;
import com.djrapitops.plan.modules.SystemObjectBindingModule;
import com.djrapitops.plan.modules.plugin.SpongePlanModule;
import com.djrapitops.plan.modules.server.ServerSuperClassBindingModule;
import com.djrapitops.plan.modules.server.sponge.SpongeServerPropertiesModule;
import com.djrapitops.plan.modules.server.sponge.SpongeSuperClassBindingModule;
import com.djrapitops.plan.system.PlanSystem;
import com.djrapitops.pluginbridge.plan.PluginBridgeModule;
import dagger.BindsInstance;
import dagger.Component;

import javax.inject.Singleton;

/**
 * Dagger Component that constructs the plugin systems running on Sponge.
 *
 * @author Rsl1122
 */
@Singleton
@Component(modules = {
        SpongePlanModule.class,
        SuperClassBindingModule.class,
        SystemObjectBindingModule.class,
        APFModule.class,
        FilesModule.class,
        ServerSuperClassBindingModule.class,
        SpongeSuperClassBindingModule.class,
        SpongeServerPropertiesModule.class,
        PluginBridgeModule.Sponge.class
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