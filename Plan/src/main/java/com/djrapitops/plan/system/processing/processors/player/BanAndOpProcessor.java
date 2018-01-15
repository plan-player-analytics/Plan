/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.processing.processors.player;

import com.djrapitops.plan.api.exceptions.database.DBException;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.database.databases.operation.SaveOperations;
import com.djrapitops.plugin.api.utility.log.Log;

import java.util.UUID;

/**
 * Updates ban and OP status of the player to the database.
 *
 * @author Rsl1122
 */
public class BanAndOpProcessor extends PlayerProcessor {

    private final boolean banned;
    private final boolean opped;

    public BanAndOpProcessor(UUID uuid, boolean banned, boolean op) {
        super(uuid);
        this.banned = banned;
        opped = op;
    }

    @Override
    public void process() {
        UUID uuid = getUUID();
        try {
            SaveOperations save = Database.getActive().save();
            save.banStatus(uuid, banned);
            save.opStatus(uuid, opped);
        } catch (DBException e) {
            Log.toLog(this.getClass().getName(), e);
        }
    }
}