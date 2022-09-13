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
