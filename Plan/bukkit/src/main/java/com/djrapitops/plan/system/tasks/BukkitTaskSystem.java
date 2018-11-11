/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.system.tasks;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.ShutdownHook;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.tasks.bukkit.BukkitTPSCountTimer;
import com.djrapitops.plan.system.tasks.bukkit.PaperTPSCountTimer;
import com.djrapitops.plan.system.tasks.bukkit.PingCountTimerBukkit;
import com.djrapitops.plan.system.tasks.server.BootAnalysisTask;
import com.djrapitops.plan.system.tasks.server.PeriodicAnalysisTask;
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
    private final ShutdownHook shutdownHook;
    private final PingCountTimerBukkit pingCountTimer;

    @Inject
    public BukkitTaskSystem(
            Plan plugin,
            PlanConfig config,
            ShutdownHook shutdownHook,
            RunnableFactory runnableFactory,
            PaperTPSCountTimer paperTPSCountTimer,
            BukkitTPSCountTimer bukkitTPSCountTimer,
            BootAnalysisTask bootAnalysisTask,
            PeriodicAnalysisTask periodicAnalysisTask,
            PingCountTimerBukkit pingCountTimer,
            LogsFolderCleanTask logsFolderCleanTask,
            PlayersPageRefreshTask playersPageRefreshTask
    ) {
        super(
                runnableFactory,
                Check.isPaperAvailable() ? paperTPSCountTimer : bukkitTPSCountTimer,
                config,
                bootAnalysisTask,
                periodicAnalysisTask,
                logsFolderCleanTask,
                playersPageRefreshTask);
        this.plugin = plugin;
        this.shutdownHook = shutdownHook;
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
        shutdownHook.register();
    }

    @Override
    public void disable() {
        super.disable();
        Bukkit.getScheduler().cancelTasks(plugin);
    }
}
