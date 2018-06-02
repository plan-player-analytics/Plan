/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.database.databases.sql.operation;

import com.djrapitops.plan.system.database.databases.operation.TransferOperations;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;

import java.util.Optional;
import java.util.UUID;

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
    public Optional<UUID> getServerPlayerIsOnlineOn(UUID playerUUID) {
        return transferTable.getServerPlayerIsOnline(playerUUID);
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