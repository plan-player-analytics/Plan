package com.djrapitops.plan.bungee.tasks.bungee;

import com.djrapitops.plan.bungee.PlanBungee;
import com.djrapitops.plan.data.container.TPS;
import com.djrapitops.plan.data.container.builders.TPSBuilder;
import com.djrapitops.plan.system.tasks.TPSCountTimer;

public class BungeeTPSCountTimer extends TPSCountTimer<PlanBungee> {

    public BungeeTPSCountTimer(PlanBungee plugin) {
        super(plugin);
    }

    @Override
    public void addNewTPSEntry(long nanoTime, long now) {
        int onlineCount = plugin.getProxy().getOnlineCount();
        TPS tps = TPSBuilder.get()
                .date(now)
                .skipTPS()
                .playersOnline(onlineCount)
                .toTPS();

        history.add(tps);
        latestPlayersOnline = onlineCount;
    }
}
