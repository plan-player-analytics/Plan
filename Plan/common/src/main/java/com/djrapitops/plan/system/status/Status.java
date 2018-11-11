package com.djrapitops.plan.system.status;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class Status {

    private boolean countKicks;

    @Inject
    public Status() {
        countKicks = true;
    }

    public boolean areKicksCounted() {
        return countKicks;
    }

    public void setCountKicks(boolean countKicks) {
        this.countKicks = countKicks;
    }
}
