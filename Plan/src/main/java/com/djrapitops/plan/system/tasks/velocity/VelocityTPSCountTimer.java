package com.djrapitops.plan.system.tasks.velocity;

import com.djrapitops.plan.PlanVelocity;
import com.djrapitops.plan.data.container.TPS;
import com.djrapitops.plan.data.container.builders.TPSBuilder;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.tasks.TPSCountTimer;

public class VelocityTPSCountTimer extends TPSCountTimer<PlanVelocity> {

    public VelocityTPSCountTimer(PlanVelocity plugin) {
        super(plugin);
    }

    @Override
    public void addNewTPSEntry(long nanoTime, long now) {
        int onlineCount = ServerInfo.getServerProperties().getOnlinePlayers();
        TPS tps = TPSBuilder.get()
                .date(now)
                .skipTPS()
                .playersOnline(onlineCount)
                .usedCPU(getCPUUsage())
                .usedMemory(getUsedMemory())
                .entities(-1)
                .chunksLoaded(-1)
                .toTPS();

        history.add(tps);
        latestPlayersOnline = onlineCount;
    }
}
