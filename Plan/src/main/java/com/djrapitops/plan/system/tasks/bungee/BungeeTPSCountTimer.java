package com.djrapitops.plan.system.tasks.bungee;

import com.djrapitops.plan.PlanBungee;
import com.djrapitops.plan.data.container.TPS;
import com.djrapitops.plan.system.tasks.TPSCountTimer;

public class BungeeTPSCountTimer extends TPSCountTimer<PlanBungee> {

    public BungeeTPSCountTimer(PlanBungee plugin) {
        super(plugin);
    }

    @Override
    public void addNewTPSEntry(long nanoTime, long now) {
        int onlineCount = plugin.getProxy().getOnlineCount();
        history.add(new TPS(now, -1, onlineCount, -1, -1, -1, -1));
        latestPlayersOnline = onlineCount;
    }
}
