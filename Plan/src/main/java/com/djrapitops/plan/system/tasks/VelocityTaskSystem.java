/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.tasks;

import com.djrapitops.plan.PlanVelocity;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.tasks.velocity.VelocityTPSCountTimer;
import com.djrapitops.plan.system.tasks.velocity.EnableConnectionTask;
import com.djrapitops.plan.system.tasks.server.NetworkPageRefreshTask;
import com.djrapitops.plan.system.tasks.server.PingCountTimerVelocity;
import com.djrapitops.plan.utilities.file.export.HtmlExport;
import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.task.RunnableFactory;

/**
 * TaskSystem responsible for registering tasks for Velocity.
 *
 * @author Rsl1122
 */
public class VelocityTaskSystem extends TaskSystem {

    private final PlanVelocity plugin;

    public VelocityTaskSystem(PlanVelocity plugin) {
        super(new VelocityTPSCountTimer(plugin));
        this.plugin = plugin;
    }

    @Override
    public void enable() {
        registerTasks();
    }

    private void registerTasks() {
        registerTask(new EnableConnectionTask()).runTaskAsynchronously();
        registerTask(tpsCountTimer).runTaskTimerAsynchronously(1000, TimeAmount.SECOND.ticks());
        registerTask(new NetworkPageRefreshTask()).runTaskTimerAsynchronously(1500, TimeAmount.MINUTE.ticks());
        if (Settings.ANALYSIS_EXPORT.isTrue()) {
            registerTask(new HtmlExport(plugin)).runTaskAsynchronously();
        }
        PingCountTimerVelocity pingCountTimer = new PingCountTimerVelocity();
        plugin.registerListener(pingCountTimer);
        long startDelay = TimeAmount.SECOND.ticks() * (long) Settings.PING_SERVER_ENABLE_DELAY.getNumber();
        RunnableFactory.createNew("PingCountTimer", pingCountTimer)
                .runTaskTimer(startDelay, PingCountTimerVelocity.PING_INTERVAL);
    }
}
