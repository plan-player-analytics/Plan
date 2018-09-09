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

    private final PlanSponge planSponge;

    public SpongeTaskSystem(PlanSponge plugin) {
        super(plugin, new SpongeTPSCountTimer(plugin));
        this.planSponge = plugin;
    }
    
    @Override
    public void enable() {
        super.enable();
        PingCountTimerSponge pingCountTimer = new PingCountTimerSponge();
        planSponge.registerListener(pingCountTimer);
        long startDelay = TimeAmount.SECOND.ticks() * (long) Settings.PING_SERVER_ENABLE_DELAY.getNumber();
        RunnableFactory.createNew("PingCountTimer", pingCountTimer)
                .runTaskTimer(startDelay, PingCountTimerSponge.PING_INTERVAL);
    }

    @Override
    public void disable() {
        super.disable();
        for (Task task : Sponge.getScheduler().getScheduledTasks(planSponge)) {
            task.cancel();
        }
    }
}
