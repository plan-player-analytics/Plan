package com.djrapitops.plan.system.tasks.bungee;

import com.djrapitops.plan.data.container.TPS;
import com.djrapitops.plan.data.container.builders.TPSBuilder;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.tasks.TPSCountTimer;

public class BungeeTPSCountTimer extends TPSCountTimer {

    @Override
    public void addNewTPSEntry(long nanoTime, long now) {
        int onlineCount = ServerInfo.getServerProperties_Old().getOnlinePlayers();
        TPS tps = TPSBuilder.get()
                .date(now)
                .skipTPS()
                .playersOnline(onlineCount)
                .toTPS();

        history.add(tps);
        latestPlayersOnline = onlineCount;
    }
}
