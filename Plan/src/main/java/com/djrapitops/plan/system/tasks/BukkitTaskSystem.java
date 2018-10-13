/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.tasks;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.tasks.server.*;
import com.djrapitops.plugin.api.Check;
import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.task.RunnableFactory;
import org.bukkit.Bukkit;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

/**
 * TaskSystem responsible for registering tasks for Bukkit.
 *
 * @author Rsl1122
 */
public class BukkitTaskSystem extends ServerTaskSystem {

    private final Plan plugin;
    private final PingCountTimerBukkit pingCountTimer;

    @Inject
    public BukkitTaskSystem(
            Plan plugin,
            PlanConfig config,
            RunnableFactory runnableFactory,
            PaperTPSCountTimer paperTPSCountTimer,
            BukkitTPSCountTimer bukkitTPSCountTimer,
            BootAnalysisTask bootAnalysisTask,
            PeriodicAnalysisTask periodicAnalysisTask,
            PingCountTimerBukkit pingCountTimer,
            LogsFolderCleanTask logsFolderCleanTask
    ) {
        super(
                runnableFactory,
                Check.isPaperAvailable() ? paperTPSCountTimer : bukkitTPSCountTimer,
                config,
                bootAnalysisTask,
                periodicAnalysisTask,
                logsFolderCleanTask
        );
        this.plugin = plugin;
        this.pingCountTimer = pingCountTimer;
    }

    @Override
    public void enable() {
        super.enable();
        try {
            plugin.registerListener(pingCountTimer);
            long startDelay = TimeAmount.toTicks(config.getNumber(Settings.PING_SERVER_ENABLE_DELAY), TimeUnit.SECONDS);
            registerTask("PingCountTimer", pingCountTimer)
                    .runTaskTimer(startDelay, 40L);
        } catch (ExceptionInInitializerError | NoClassDefFoundError ignore) {
            // Running CraftBukkit
        }
    }

    @Override
    public void disable() {
        super.disable();
        Bukkit.getScheduler().cancelTasks(plugin);
    }
}
