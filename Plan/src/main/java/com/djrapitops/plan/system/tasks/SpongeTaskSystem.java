package com.djrapitops.plan.system.tasks;

import com.djrapitops.plan.PlanSponge;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.tasks.server.BootAnalysisTask;
import com.djrapitops.plan.system.tasks.server.PeriodicAnalysisTask;
import com.djrapitops.plan.system.tasks.server.sponge.PingCountTimerSponge;
import com.djrapitops.plan.system.tasks.server.sponge.SpongeTPSCountTimer;
import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.task.RunnableFactory;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

public class SpongeTaskSystem extends ServerTaskSystem {

    private final PlanSponge plugin;
    private final PingCountTimerSponge pingCountTimer;

    @Inject
    public SpongeTaskSystem(
            PlanSponge plugin,
            PlanConfig config,
            RunnableFactory runnableFactory,
            SpongeTPSCountTimer spongeTPSCountTimer,
            BootAnalysisTask bootAnalysisTask,
            PeriodicAnalysisTask periodicAnalysisTask,
            PingCountTimerSponge pingCountTimer,
            LogsFolderCleanTask logsFolderCleanTask,
            PlayersPageRefreshTask playersPageRefreshTask
    ) {
        super(
                runnableFactory,
                spongeTPSCountTimer,
                config,
                bootAnalysisTask,
                periodicAnalysisTask,
                logsFolderCleanTask,
                playersPageRefreshTask);
        this.plugin = plugin;
        this.pingCountTimer = pingCountTimer;
    }

    @Override
    public void enable() {
        super.enable();

        plugin.registerListener(pingCountTimer);
        long startDelay = TimeAmount.toTicks(config.getNumber(Settings.PING_SERVER_ENABLE_DELAY), TimeUnit.SECONDS);
        runnableFactory.create("PingCountTimer", pingCountTimer)
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
