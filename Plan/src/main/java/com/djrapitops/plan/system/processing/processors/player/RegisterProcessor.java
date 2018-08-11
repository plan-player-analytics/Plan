/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.processing.processors.player;

import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.utilities.Verify;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Registers the user to the database and marks first session if the user has no actions.
 *
 * @author Rsl1122
 */
public class RegisterProcessor extends AbsRunnable {

    private final UUID uuid;
    private final Supplier<Long> registered;
    private final String name;
    private final Runnable[] afterProcess;

    public RegisterProcessor(UUID uuid, Supplier<Long> registered, String name, Runnable... afterProcess) {
        super(RegisterProcessor.class.getSimpleName());
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
                db.save().registerNewUser(uuid, registered.get(), name);
            }
            if (!db.check().isPlayerRegisteredOnThisServer(uuid)) {
                db.save().registerNewUserOnThisServer(uuid, registered.get());
            }
        } finally {
            for (Runnable runnable : afterProcess) {
                Processing.submit(runnable);
            }
            cancel();
        }
    }
}
