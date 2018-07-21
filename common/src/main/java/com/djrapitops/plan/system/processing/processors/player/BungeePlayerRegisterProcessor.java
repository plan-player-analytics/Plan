/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.processing.processors.player;

import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.processing.CriticalRunnable;
import com.djrapitops.plan.system.processing.Processing;

import java.util.UUID;

/**
 * Processor that registers a new User for all servers to use as UUID - ID reference.
 *
 * @author Rsl1122
 */
public class BungeePlayerRegisterProcessor implements CriticalRunnable {

    private final UUID uuid;
    private final String name;
    private final long registered;
    private final Runnable[] afterProcess;

    public BungeePlayerRegisterProcessor(UUID uuid, String name, long registered, Runnable... afterProcess) {
        this.uuid = uuid;
        this.name = name;
        this.registered = registered;
        this.afterProcess = afterProcess;
    }

    @Override
    public void run() {
        Database database = Database.getActive();
        try {
            if (database.check().isPlayerRegistered(uuid)) {
                return;
            }
            database.save().registerNewUser(uuid, registered, name);
        } finally {
            for (Runnable process : afterProcess) {
                Processing.submit(process);
            }

        }
    }
}
