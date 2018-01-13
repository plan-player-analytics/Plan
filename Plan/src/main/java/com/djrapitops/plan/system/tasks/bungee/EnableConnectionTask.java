package com.djrapitops.plan.system.tasks.bungee;

import com.djrapitops.plan.PlanBungee;
import com.djrapitops.plan.systems.info.BungeeInformationManager;
import com.djrapitops.plugin.task.AbsRunnable;

public class EnableConnectionTask extends AbsRunnable {

    public EnableConnectionTask() {
        super(EnableConnectionTask.class.getSimpleName());
    }

    @Override
    public void run() {
        BungeeInformationManager infoManager = (BungeeInformationManager) PlanBungee.getInstance().getInfoManager();
        infoManager.attemptConnection();
        infoManager.sendConfigSettings();
        cancel();
    }
}
