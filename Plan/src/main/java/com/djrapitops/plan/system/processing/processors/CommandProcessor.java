/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.processing.processors;

import com.djrapitops.plan.api.exceptions.database.DBException;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plugin.api.utility.log.Log;

/**
 * Updates Command usage amount in the database.
 *
 * @author Rsl1122
 */
public class CommandProcessor extends ObjectProcessor<String> {

    public CommandProcessor(String object) {
        super(object);
    }

    @Override
    public void process() {
        try {
            Database.getActive().save().commandUsed(object);
        } catch (DBException e) {
            Log.toLog(this.getClass(), e);
        }
    }
}