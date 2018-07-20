/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.common.api.exceptions.connection;

import com.djrapitops.plan.common.system.database.databases.Database;

/**
 * Exception thrown when calling Database#transfer and Database implementation doesn't support it.
 *
 * @author Rsl1122
 */
public class UnsupportedTransferDatabaseException extends WebException {

    public UnsupportedTransferDatabaseException(Database db) {
        super(db.getName() + " does not support Transfer operations!");
    }
}
