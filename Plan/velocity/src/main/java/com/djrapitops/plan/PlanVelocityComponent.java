/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the LGNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  LGNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan;

import com.djrapitops.plan.command.PlanVelocityCommand;
import com.djrapitops.plan.modules.*;
import com.djrapitops.plan.modules.velocity.VelocityPlanModule;
import com.djrapitops.plan.modules.velocity.VelocityServerPropertiesModule;
import com.djrapitops.plan.modules.velocity.VelocitySuperClassBindingModule;
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
        SystemObjectProvidingModule.class,
        APFModule.class,
        FilesModule.class,
        ProxySuperClassBindingModule.class,
        VelocitySuperClassBindingModule.class,
        VelocityServerPropertiesModule.class,
        PluginBridgeModule.Velocity.class
})
public interface PlanVelocityComponent {

    PlanVelocityCommand planCommand();

    PlanSystem system();

    @Component.Builder
    interface Builder {

        @BindsInstance
        Builder plan(PlanVelocity plan);

        PlanVelocityComponent build();
    }
}