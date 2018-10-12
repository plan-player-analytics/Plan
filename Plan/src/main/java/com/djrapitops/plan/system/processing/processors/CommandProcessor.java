/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.processing.processors;

import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.processing.CriticalRunnable;

/**
 * Updates Command usage amount in the database.
 *
 * @author Rsl1122
 */
public class CommandProcessor implements CriticalRunnable {

    private final String command;

    private final Database database;

    CommandProcessor(String command, Database database) {
        this.command = command;
        this.database = database;
    }

    @Override
    public void run() {
        database.save().commandUsed(command);
    }
}
