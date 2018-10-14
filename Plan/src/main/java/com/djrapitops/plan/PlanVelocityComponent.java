package com.djrapitops.plan;

import com.djrapitops.plan.command.PlanVelocityCommand;
import com.djrapitops.plan.modules.*;
import com.djrapitops.plan.modules.proxy.ProxySuperClassBindingModule;
import com.djrapitops.plan.modules.proxy.velocity.VelocityServerPropertiesModule;
import com.djrapitops.plan.modules.proxy.velocity.VelocitySuperClassBindingModule;
import com.djrapitops.plan.system.PlanSystem;
import com.djrapitops.pluginbridge.plan.PluginBridgeModule;
import dagger.BindsInstance;
import dagger.Component;

import javax.inject.Singleton;

/**
 * Dagger Component that constructs the plugin systems running on Velocity.
 *
 * @author Rsl1122
 */
@Singleton
@Component(modules = {
        VelocityPlanModule.class,
        SuperClassBindingModule.class,
        SystemObjectBindingModule.class,
        APFModule.class,
        FilesModule.class,
        ProxySuperClassBindingModule.class,
        VelocitySuperClassBindingModule.class,
        VelocityServerPropertiesModule.class,
        PluginBridgeModule.Velocity.class
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