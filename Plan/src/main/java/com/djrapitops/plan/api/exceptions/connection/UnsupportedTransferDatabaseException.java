/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.api.exceptions.connection;

import com.djrapitops.plan.system.database.databases.Database;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public class UnsupportedTransferDatabaseException extends WebException {

    public UnsupportedTransferDatabaseException(Database db) {
        super(db.getName() + " does not support Transfer operations!");
    }
}