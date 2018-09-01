/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.tasks;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.tasks.server.BukkitTPSCountTimer;
import com.djrapitops.plan.system.tasks.server.PaperTPSCountTimer;
import com.djrapitops.plan.system.tasks.server.PingCountTimerBukkit;
import com.djrapitops.plugin.api.Check;
import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.task.RunnableFactory;
import org.bukkit.Bukkit;

/**
 * TaskSystem responsible for registering tasks for Bukkit.
 *
 * @author Rsl1122
 */
public class BukkitTaskSystem extends ServerTaskSystem {

    public BukkitTaskSystem(Plan plugin) {
        super(plugin,
                Check.isPaperAvailable()
                        ? new PaperTPSCountTimer(plugin)
                        : new BukkitTPSCountTimer(plugin)
        );
    }

    @Override
    public void enable() {
        super.enable();
        try {
            PingCountTimerBukkit pingCountTimer = new PingCountTimerBukkit();
            ((Plan) plugin).registerListener(pingCountTimer);
            long startDelay = TimeAmount.SECOND.ticks() * (long) Settings.PING_SERVER_ENABLE_DELAY.getNumber();
            RunnableFactory.createNew("PingCountTimer", pingCountTimer)
                    .runTaskTimer(startDelay, PingCountTimerBukkit.PING_INTERVAL);
        } catch (ExceptionInInitializerError | NoClassDefFoundError ignore) {
            // Running CraftBukkit
        }
    }

    @Override
    public void disable() {
        super.disable();
        Bukkit.getScheduler().cancelTasks((Plan) plugin);
    }
}
