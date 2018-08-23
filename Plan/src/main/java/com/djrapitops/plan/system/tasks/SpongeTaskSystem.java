package com.djrapitops.plan.system.tasks;

import com.djrapitops.plan.PlanSponge;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.tasks.server.BootAnalysisTask;
import com.djrapitops.plan.system.tasks.server.NetworkPageRefreshTask;
import com.djrapitops.plan.system.tasks.server.PeriodicAnalysisTask;
import com.djrapitops.plan.system.tasks.server.SpongeTPSCountTimer;
import com.djrapitops.plugin.task.RunnableFactory;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;

import javax.inject.Inject;

public class SpongeTaskSystem extends ServerTaskSystem {

    private final PlanSponge plugin;

    @Inject
    public SpongeTaskSystem(
            PlanSponge plugin,
            PlanConfig config,
            RunnableFactory runnableFactory,
            SpongeTPSCountTimer spongeTPSCountTimer,
            NetworkPageRefreshTask networkPageRefreshTask,
            BootAnalysisTask bootAnalysisTask,
            PeriodicAnalysisTask periodicAnalysisTask
    ) {
        super(
                runnableFactory,
                spongeTPSCountTimer,
                config,
                networkPageRefreshTask,
                bootAnalysisTask,
                periodicAnalysisTask
        );
        this.plugin = plugin;
    }

    @Override
    public void disable() {
        super.disable();
        for (Task task : Sponge.getScheduler().getScheduledTasks(plugin)) {
            task.cancel();
        }
    }
}
