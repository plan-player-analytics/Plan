/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.api.exceptions.connection;

import com.djrapitops.plan.api.exceptions.database.DBException;

/**
 * Thrown when DBException occurs during InfoRequest#placeIntoDatabase.
 *
 * @author Rsl1122
 */
public class TransferDatabaseException extends WebException {

    public TransferDatabaseException(DBException cause) {
        super(cause);
    }
}
