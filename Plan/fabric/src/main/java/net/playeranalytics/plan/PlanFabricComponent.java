package net.playeranalytics.plan;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.PlanSystem;
import com.djrapitops.plan.commands.PlanCommand;
import com.djrapitops.plan.gathering.ServerShutdownSave;
import dagger.BindsInstance;
import dagger.Component;
import net.playeranalytics.plugin.PlatformAbstractionLayer;

import javax.inject.Singleton;

@Singleton
public interface PlanFabricComponent {

    PlanCommand planCommand();

    PlanSystem system();

    ServerShutdownSave serverShutdownSave();

    @Component.Builder
    interface Builder {

        @BindsInstance
        Builder plan(PlanPlugin plan);

        @BindsInstance
        Builder abstractionLayer(PlatformAbstractionLayer abstractionLayer);

        PlanFabricComponent build();
    }
}
