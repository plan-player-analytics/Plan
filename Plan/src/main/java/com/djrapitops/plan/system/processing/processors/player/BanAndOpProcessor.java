/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.processing.processors.player;

import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.database.databases.operation.SaveOperations;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Updates ban and OP status of the player to the database.
 *
 * @author Rsl1122
 */
public class BanAndOpProcessor implements Runnable {

    private final UUID uuid;
    private final Supplier<Boolean> banned;
    private final boolean op;

    public BanAndOpProcessor(UUID uuid, Supplier<Boolean> banned, boolean op) {
        this.uuid = uuid;
        this.banned = banned;
        this.op = op;
    }

    @Override
    public void run() {
        SaveOperations save = Database.getActive().save();
        save.banStatus(uuid, banned.get());
        save.opStatus(uuid, op);
    }
}
