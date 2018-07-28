/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.processing.processors.player;

import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.processing.CriticalRunnable;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plugin.utilities.Verify;

import java.util.UUID;

/**
 * Registers the user to the database and marks first session if the user has no actions.
 *
 * @author Rsl1122
 */
public class RegisterProcessor implements CriticalRunnable {

    private final UUID uuid;
    private final long registered;
    private final String name;
    private final Runnable[] afterProcess;

    public RegisterProcessor(UUID uuid, long registered, String name, Runnable... afterProcess) {
        this.uuid = uuid;
        this.registered = registered;
        this.name = name;
        this.afterProcess = afterProcess;
    }

    @Override
    public void run() {
        Database db = Database.getActive();
        Verify.nullCheck(uuid, () -> new IllegalStateException("UUID was null"));
        try {
            if (!db.check().isPlayerRegistered(uuid)) {
                db.save().registerNewUser(uuid, registered, name);
            }
            if (!db.check().isPlayerRegisteredOnThisServer(uuid)) {
                db.save().registerNewUserOnThisServer(uuid, registered);
            }
        } finally {
            for (Runnable runnable : afterProcess) {
                Processing.submit(runnable);
            }
        }
    }
}
