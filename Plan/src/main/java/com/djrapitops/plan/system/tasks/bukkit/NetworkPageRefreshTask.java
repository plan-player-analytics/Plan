package com.djrapitops.plan.system.tasks.bukkit;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plugin.task.AbsRunnable;

public class NetworkPageRefreshTask extends AbsRunnable {

    public NetworkPageRefreshTask() {
        super(NetworkPageRefreshTask.class.getSimpleName());
    }

    @Override
    public void run() {
        PlanPlugin.getInstance().getInfoManager().updateNetworkPageContent();
    }
}
