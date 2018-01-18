/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.database.databases.sql.operation;

import com.djrapitops.plan.system.database.databases.operation.TransferOperations;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;

/**
 * TransferOperations for MySQL Database.
 *
 * @author Rsl1122
 */
public class SQLTransferOps extends SQLOps implements TransferOperations {

    public SQLTransferOps(SQLDB db) {
        super(db);
    }

    // TODO create Transfer table
}