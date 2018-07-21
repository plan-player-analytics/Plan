/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.processing.processors.player;

import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.processing.CriticalRunnable;

import java.util.UUID;

/**
 * Updates the Kick count of a user.
 *
 * @author Rsl1122
 */
public class KickProcessor implements CriticalRunnable {

    private final UUID uuid;

    public KickProcessor(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public void run() {
        Database.getActive().save().playerWasKicked(uuid);
    }
}
