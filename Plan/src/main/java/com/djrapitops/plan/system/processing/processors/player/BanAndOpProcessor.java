/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
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
public class BanAndOpProcessor implements Runnable {

    private final UUID uuid;
    private final boolean banned;
    private final boolean op;

    public BanAndOpProcessor(UUID uuid, boolean banned, boolean op) {
        this.uuid = uuid;
        this.banned = banned;
        this.op = op;
    }

    @Override
    public void run() {
        try {
            SaveOperations save = Database.getActive().save();
            save.banStatus(uuid, banned);
            save.opStatus(uuid, op);
        } catch (DBException e) {
            Log.toLog(this.getClass(), e);
        }
    }
}
