/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.processing.processors.player;

import com.djrapitops.plan.api.exceptions.database.DBException;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.processing.CriticalRunnable;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plugin.api.utility.log.Log;

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
        } catch (DBException e) {
            Log.toLog(this.getClass(), e);
        } finally {
            for (Runnable process : afterProcess) {
                Processing.submit(process);
            }

        }
    }
}