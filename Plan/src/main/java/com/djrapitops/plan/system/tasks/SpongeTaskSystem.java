package com.djrapitops.plan.system.tasks;

import com.djrapitops.plan.PlanSponge;
import com.djrapitops.plan.system.tasks.server.SpongeTPSCountTimer;
import com.djrapitops.plugin.task.RunnableFactory;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;

import javax.inject.Inject;

public class SpongeTaskSystem extends ServerTaskSystem {

    private final PlanSponge plugin;

    @Inject
    public SpongeTaskSystem(PlanSponge plugin, RunnableFactory runnableFactory) {
        super(runnableFactory, new SpongeTPSCountTimer(plugin));
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
