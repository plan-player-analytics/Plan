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
package utilities.dagger;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.PlanSystem;
import com.djrapitops.plan.commands.PlanCommand;
import com.djrapitops.plan.gathering.events.PlayerJoinEventConsumer;
import com.djrapitops.plan.gathering.events.PlayerLeaveEventConsumer;
import com.djrapitops.plan.gathering.events.PlayerSwitchServerEventConsumer;
import com.djrapitops.plan.modules.FiltersModule;
import com.djrapitops.plan.modules.PlaceholderModule;
import com.djrapitops.plan.modules.PlatformAbstractionLayerModule;
import com.djrapitops.plan.placeholder.PlanPlaceholders;
import com.djrapitops.plan.utilities.logging.PluginErrorLogger;
import dagger.BindsInstance;
import dagger.Component;
import net.playeranalytics.plugin.PlatformAbstractionLayer;

import javax.inject.Named;
import javax.inject.Singleton;
import java.nio.file.Path;

/**
 * Dagger component for {@link com.djrapitops.plan.PlanPlugin} based Plan system.
 *
 * @author AuroraLS3
 */
@Singleton
@Component(modules = {
        PlanPluginModule.class,
        PlanServerPluginModule.class,
        TestSystemObjectProvidingModule.class,
        PlatformAbstractionLayerModule.class,
        FiltersModule.class,
        PlaceholderModule.class,

        PluginServerPropertiesModule.class,
        PluginSuperClassBindingModule.class,
        PlanPluginTaskModule.class,
        DBSystemModule.class
})
public interface PlanPluginComponent {
    PlanCommand planCommand();

    PlanSystem system();

    PluginErrorLogger pluginErrorLogger();

    PlanPlaceholders placeholders();

    PlayerJoinEventConsumer joinConsumer();

    PlayerLeaveEventConsumer leaveConsumer();

    PlayerSwitchServerEventConsumer serverSwitchConsumer();

    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder bindTemporaryDirectory(@Named("tempDir") Path tempDir);

        @BindsInstance
        Builder plan(PlanPlugin plan);

        @BindsInstance
        Builder abstractionLayer(PlatformAbstractionLayer plan);

        PlanPluginComponent build();
    }
}
