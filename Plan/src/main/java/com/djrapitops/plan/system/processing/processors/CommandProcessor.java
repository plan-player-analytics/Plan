/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.processing.processors;

import com.djrapitops.plan.api.exceptions.database.DBException;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.processing.CriticalRunnable;
import com.djrapitops.plugin.api.utility.log.Log;

/**
 * Updates Command usage amount in the database.
 *
 * @author Rsl1122
 */
public class CommandProcessor implements CriticalRunnable {

    private final String command;

    public CommandProcessor(String command) {
        this.command = command;
    }

    @Override
    public void run() {
        try {
            Database.getActive().save().commandUsed(command);
        } catch (DBException e) {
            Log.toLog(this.getClass(), e);
        }
    }
}