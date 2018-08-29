/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.processing.processors.player;

import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.database.databases.operation.CheckOperations;
import com.djrapitops.plan.system.database.databases.operation.SaveOperations;
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

    private Processing processing;
    private Database database;

    public RegisterProcessor(UUID uuid, Supplier<Long> registered, String name, Runnable... afterProcess) {
        this.uuid = uuid;
        this.registered = registered;
        this.name = name;
        this.afterProcess = afterProcess;
    }

    @Override
    public void run() {
        Verify.nullCheck(uuid, () -> new IllegalStateException("UUID was null"));

        CheckOperations check = database.check();
        SaveOperations save = database.save();
        try {
            if (!check.isPlayerRegistered(uuid)) {
                save.registerNewUser(uuid, registered.get(), name);
            }
            if (!check.isPlayerRegisteredOnThisServer(uuid)) {
                save.registerNewUserOnThisServer(uuid, registered.get());
            }
        } finally {
            for (Runnable runnable : afterProcess) {
                processing.submit(runnable);
            }
            cancel();
        }
    }
}
