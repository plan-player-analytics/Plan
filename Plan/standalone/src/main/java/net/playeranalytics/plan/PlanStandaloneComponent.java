package net.playeranalytics.plan;

import com.djrapitops.plan.PlanSystem;
import com.djrapitops.plan.commands.PlanCommand;
import dagger.BindsInstance;
import dagger.Component;

public interface PlanStandaloneComponent {

    PlanCommand planCommand();

    PlanSystem system();

    @Component.Builder
    interface Builder {

        @BindsInstance
        Builder plan(PlanStandalone plan);

        PlanStandaloneComponent build();
    }

}
