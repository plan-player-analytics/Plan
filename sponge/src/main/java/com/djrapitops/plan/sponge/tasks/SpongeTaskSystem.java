package com.djrapitops.plan.sponge.tasks;

import com.djrapitops.plan.sponge.PlanSponge;
import com.djrapitops.plan.sponge.tasks.server.SpongeTPSCountTimer;
import com.djrapitops.plan.system.tasks.ServerTaskSystem;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;

public class SpongeTaskSystem extends ServerTaskSystem {

    public SpongeTaskSystem(PlanSponge plugin) {
        super(plugin, new SpongeTPSCountTimer(plugin));
    }

    @Override
    public void disable() {
        super.disable();
        for (Task task : Sponge.getScheduler().getScheduledTasks(plugin)) {
            task.cancel();
        }
    }
}
