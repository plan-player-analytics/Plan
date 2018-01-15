/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.processing.processors.player;

import com.djrapitops.plan.api.exceptions.database.DBException;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plugin.api.utility.log.Log;

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
            Database.getActive().save().playerWasKicked(uuid);
        } catch (DBException e) {
            Log.toLog(this.getClass().getName(), e);
        }
    }
}