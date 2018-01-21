/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.info;

import com.djrapitops.plan.system.info.connection.BukkitConnectionSystem;
import com.djrapitops.plan.system.info.request.InfoRequest;

/**
 * InfoSystem for Bukkit servers.
 *
 * @author Rsl1122
 */
public class BukkitInfoSystem extends InfoSystem {

    public BukkitInfoSystem() {
        super(new BukkitConnectionSystem());
    }

    @Override
    protected void runLocally(InfoRequest infoRequest) {

    }

    @Override
    public void updateNetworkPage() {
        runLocally(new GenerateNetworkPageContentRequest());
    }
}