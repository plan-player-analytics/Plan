/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.systems.processing.player;

import com.djrapitops.plan.Plan;
import com.djrapitops.plugin.api.utility.log.Log;

import java.sql.SQLException;
import java.util.UUID;

/**
 * Updates the Kick count of a user.
 *
 * @author Rsl1122
 */
public class KickProcessor extends PlayerProcessor {
    public KickProcessor(UUID uuid) {
        super(uuid);
    }

    @Override
    public void process() {
        UUID uuid = getUUID();
        try {
            Plan.getInstance().getDB().getUsersTable().kicked(uuid);
        } catch (SQLException e) {
            Log.toLog(this.getClass().getName(), e);
        }
    }
}