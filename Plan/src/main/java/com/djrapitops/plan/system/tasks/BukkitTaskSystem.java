/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.tasks;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.system.tasks.server.BukkitTPSCountTimer;
import com.djrapitops.plan.system.tasks.server.PaperTPSCountTimer;
import com.djrapitops.plugin.api.Check;
import org.bukkit.Bukkit;

/**
 * TaskSystem responsible for registering tasks for Bukkit.
 *
 * @author Rsl1122
 */
public class BukkitTaskSystem extends ServerTaskSystem {

    public BukkitTaskSystem(Plan plugin) {
        super(plugin);
        tpsCountTimer = Check.isPaperAvailable()
                ? new PaperTPSCountTimer(plugin)
                : new BukkitTPSCountTimer(plugin);
    }

    @Override
    public void disable() {
        super.disable();
        Bukkit.getScheduler().cancelTasks((Plan) plugin);
    }
}