package com.djrapitops.plan.system.tasks.bungee;

import com.djrapitops.plugin.task.AbsRunnable;

public class EnableConnectionTask extends AbsRunnable {

    public EnableConnectionTask() {
        super(EnableConnectionTask.class.getSimpleName());
    }

    @Override
    public void run() {
// TODO Config InfoRequests.
        //        infoManager.attemptConnection();
//        infoManager.sendConfigSettings();
        cancel();
    }
}
