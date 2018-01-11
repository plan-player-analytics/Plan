/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.systems.processing.player;

import com.djrapitops.plan.Plan;

import java.util.UUID;

/**
 * Ends a session and saves it to the database.
 *
 * @author Rsl1122
 */
public class EndSessionProcessor extends PlayerProcessor {

    private final long time;

    public EndSessionProcessor(UUID uuid, long time) {
        super(uuid);
        this.time = time;
    }

    @Override
    public void process() {
        UUID uuid = getUUID();
        Plan.getInstance().getDataCache().endSession(uuid, time);
    }
}