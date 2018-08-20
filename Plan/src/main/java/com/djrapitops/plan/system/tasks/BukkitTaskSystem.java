/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.tasks;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.system.tasks.server.BukkitTPSCountTimer;
import com.djrapitops.plan.system.tasks.server.PaperTPSCountTimer;
import com.djrapitops.plan.system.tasks.server.PingCountTimer;
import com.djrapitops.plugin.api.Check;
import com.djrapitops.plugin.task.RunnableFactory;
import org.bukkit.Bukkit;

import javax.inject.Inject;

/**
 * TaskSystem responsible for registering tasks for Bukkit.
 *
 * @author Rsl1122
 */
public class BukkitTaskSystem extends ServerTaskSystem {

    private final Plan plugin;

    @Inject
    public BukkitTaskSystem(Plan plugin, RunnableFactory runnableFactory) {
        super(runnableFactory,
                Check.isPaperAvailable()
                        ? new PaperTPSCountTimer(plugin)
                        : new BukkitTPSCountTimer(plugin)
        );
        this.plugin = plugin;
    }

    @Override
    public void enable() {
        super.enable();
        try {
            PingCountTimer pingCountTimer = new PingCountTimer();
            plugin.registerListener(pingCountTimer);
            registerTask("PingCountTimer", pingCountTimer)
                    .runTaskTimer(20L, PingCountTimer.PING_INTERVAL);
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
