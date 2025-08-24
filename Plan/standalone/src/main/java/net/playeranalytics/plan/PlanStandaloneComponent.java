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
package net.playeranalytics.plan;

import com.djrapitops.plan.PlanSystem;
import com.djrapitops.plan.commands.PlanCommand;
import com.djrapitops.plan.modules.FiltersModule;
import com.djrapitops.plan.modules.PlatformAbstractionLayerModule;
import dagger.BindsInstance;
import dagger.Component;
import net.playeranalytics.plan.module.StandaloneBindingModule;
import net.playeranalytics.plan.module.StandaloneProvidingModule;
import net.playeranalytics.plan.module.StandaloneServerPropertiesModule;
import net.playeranalytics.plugin.PlatformAbstractionLayer;

import javax.inject.Singleton;

@Singleton
@Component(modules = {
        PlatformAbstractionLayerModule.class,
        FiltersModule.class,

        StandaloneBindingModule.class,
        StandaloneProvidingModule.class,
        StandaloneServerPropertiesModule.class
})
public interface PlanStandaloneComponent {

    PlanCommand planCommand();

    PlanSystem system();

    @Component.Builder
    interface Builder {

        @BindsInstance
        Builder plan(PlanStandalone plan);

        @BindsInstance
        Builder abstractionLayer(PlatformAbstractionLayer abstractionLayer);

        PlanStandaloneComponent build();
    }

}
