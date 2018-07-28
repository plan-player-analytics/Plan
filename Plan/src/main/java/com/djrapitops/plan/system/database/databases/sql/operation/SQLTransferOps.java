/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.database.databases.sql.operation;

import com.djrapitops.plan.system.database.databases.operation.TransferOperations;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;

import java.util.Optional;

/**
 * TransferOperations for MySQL Database.
 *
 * @author Rsl1122
 */
public class SQLTransferOps extends SQLOps implements TransferOperations {

    public SQLTransferOps(SQLDB db) {
        super(db);
    }

    @Override
    public void storeConfigSettings(String encodedSettingString) {
        transferTable.storeConfigSettings(encodedSettingString);
    }

    @Override
    public Optional<String> getEncodedConfigSettings() {
        return transferTable.getConfigSettings();
    }

}
