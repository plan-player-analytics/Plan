/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.processing.processors.player;

import com.djrapitops.plan.data.Actions;
import com.djrapitops.plan.data.container.Action;
import com.djrapitops.plan.system.cache.SessionCache;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.processing.CriticalRunnable;

import java.util.UUID;

/**
 * Processor for inserting a FIRST_LOGOUT Action.
 *
 * @author Rsl1122
 * @since 4.0.0
 */
public class FirstLeaveProcessor implements CriticalRunnable {

    private final UUID uuid;
    private final Action leaveAction;

    public FirstLeaveProcessor(UUID uuid, long time, int messagesSent) {
        this.uuid = uuid;
        leaveAction = new Action(time, Actions.FIRST_LOGOUT, "Messages sent: " + messagesSent);
    }

    @Override
    public void run() {
        try {
            Database.getActive().save().action(uuid, leaveAction);
        } finally {
            SessionCache.getInstance().endFirstSessionActionTracking(uuid);
        }
    }
}