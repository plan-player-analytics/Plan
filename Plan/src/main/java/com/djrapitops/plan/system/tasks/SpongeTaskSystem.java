package com.djrapitops.plan.system.tasks;

import com.djrapitops.plan.PlanSponge;
import com.djrapitops.plan.system.tasks.server.SpongeTPSCountTimer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;

public class SpongeTaskSystem extends ServerTaskSystem {

    private final PlanSponge plugin;

    public SpongeTaskSystem(PlanSponge plugin) {
        super(plugin.getRunnableFactory(), new SpongeTPSCountTimer(plugin));
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
