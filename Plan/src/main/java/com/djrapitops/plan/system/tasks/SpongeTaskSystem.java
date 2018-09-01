package com.djrapitops.plan.system.tasks;

import com.djrapitops.plan.PlanSponge;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.tasks.server.PingCountTimerSponge;
import com.djrapitops.plan.system.tasks.server.SpongeTPSCountTimer;
import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.task.RunnableFactory;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;

public class SpongeTaskSystem extends ServerTaskSystem {

    public SpongeTaskSystem(PlanSponge plugin) {
        super(plugin, new SpongeTPSCountTimer(plugin));
    }
    
    @Override
    public void enable() {
        super.enable();
        PingCountTimerSponge pingCountTimer = new PingCountTimerSponge();
        ((PlanSponge) plugin).registerListener(pingCountTimer);
        long startDelay = TimeAmount.SECOND.ticks() * (long) Settings.PING_SERVER_ENABLE_DELAY.getNumber();
        RunnableFactory.createNew("PingCountTimer", pingCountTimer)
                .runTaskTimer(startDelay, PingCountTimerSponge.PING_INTERVAL);
    }

    @Override
    public void disable() {
        super.disable();
        for (Task task : Sponge.getScheduler().getScheduledTasks(plugin)) {
            task.cancel();
        }
    }
}
