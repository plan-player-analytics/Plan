/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.database.databases.sql.operation;

import com.djrapitops.plan.api.exceptions.database.DBException;
import com.djrapitops.plan.system.database.databases.operation.TransferOperations;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;

import java.sql.SQLException;
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
    public Optional<UUID> getServerPlayerIsOnlineOn(UUID playerUUID) throws DBException {
        try {
            return transferTable.getServerPlayerIsOnline(playerUUID);
        } catch (SQLException e) {
            throw SQLErrorUtil.getExceptionFor(e);
        }
    }

    @Override
    public void storeConfigSettings(String encodedSettingString) throws DBException {
        try {
            transferTable.storeConfigSettings(encodedSettingString);
        } catch (SQLException e) {
            throw SQLErrorUtil.getExceptionFor(e);
        }
    }

    @Override
    public Optional<String> getEncodedConfigSettings() throws DBException {
        try {
            return transferTable.getConfigSettings();
        } catch (SQLException e) {
            throw SQLErrorUtil.getExceptionFor(e);
        }
    }

}
