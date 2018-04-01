/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.processing.processors.player;

import com.djrapitops.plan.api.exceptions.database.DBException;
import com.djrapitops.plan.data.Actions;
import com.djrapitops.plan.data.container.Action;
import com.djrapitops.plan.system.cache.SessionCache;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.processing.CriticalRunnable;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plugin.api.utility.log.Log;
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
    private final long time;
    private final int playersOnline;
    private final String name;
    private final Runnable[] afterProcess;

    public RegisterProcessor(UUID uuid, long registered, long time, String name, int playersOnline, Runnable... afterProcess) {
        this.uuid = uuid;
        this.registered = registered;
        this.time = time;
        this.playersOnline = playersOnline;
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
            if (db.fetch().getActions(uuid).size() > 0) {
                return;
            }
            SessionCache.getInstance().markFirstSession(uuid);
            db.save().action(uuid, new Action(time, Actions.FIRST_SESSION, "Online: " + playersOnline + " Players"));
        } catch (DBException e) {
            Log.toLog(this.getClass(), e);
        } finally {
            for (Runnable runnable : afterProcess) {
                Processing.submit(runnable);
            }
        }
    }
}