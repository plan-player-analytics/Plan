/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.systems.tasks;

import com.djrapitops.plan.PlanBungee;
import com.djrapitops.plan.systems.info.BungeeInformationManager;
import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.task.AbsRunnable;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public class PlanBungeeTaskSystem extends TaskSystem {

    @Override
    public void enable() {
        registerTasks();
    }

    private void registerTasks() {
        BungeeInformationManager infoManager = ((BungeeInformationManager) PlanBungee.getInstance().getInfoManager());

        registerTask("Enable Bukkit Connection Task", new AbsRunnable() {
            @Override
            public void run() {
                infoManager.attemptConnection();
                infoManager.sendConfigSettings();
            }
        }).runTaskAsynchronously();
        registerTask("Player Count task", new TPSCountTimer(PlanBungee.getInstance()))
                .runTaskTimerAsynchronously(1000, TimeAmount.SECOND.ticks());
        registerTask("NetworkPageContentUpdateTask", new AbsRunnable("NetworkPageContentUpdateTask") {
            @Override
            public void run() {
                infoManager.updateNetworkPageContent();
            }
        }).runTaskTimerAsynchronously(1500, TimeAmount.MINUTE.ticks());
    }
}