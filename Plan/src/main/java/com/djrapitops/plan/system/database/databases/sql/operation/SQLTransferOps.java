/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.database.databases.sql.operation;

import com.djrapitops.plan.api.exceptions.database.DBException;
import com.djrapitops.plan.system.database.databases.operation.TransferOperations;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;
import com.djrapitops.plan.system.database.databases.sql.SQLErrorUtil;

import java.sql.SQLException;
import java.util.Map;
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
    public void storePlayerHtml(UUID player, String encodedHtml) throws DBException {
        try {
            transferTable.storePlayerHtml(player, encodedHtml);
        } catch (SQLException e) {
            throw SQLErrorUtil.getExceptionFor(e);
        }
    }

    @Override
    public void storeServerHtml(UUID serverUUID, String encodedHtml) throws DBException {
        try {
            transferTable.storeServerHtml(serverUUID, encodedHtml);
        } catch (SQLException e) {
            throw SQLErrorUtil.getExceptionFor(e);
        }
    }

    @Override
    public void storeNetworkPageContent(UUID serverUUID, String encodedHtml) throws DBException {
        try {
            transferTable.storeNetworkPageContent(serverUUID, encodedHtml);
        } catch (SQLException e) {
            throw SQLErrorUtil.getExceptionFor(e);
        }
    }

    @Override
    public Map<UUID, String> getEncodedPlayerHtml() throws DBException {
        try {
            return transferTable.getPlayerHtml();
        } catch (SQLException e) {
            throw SQLErrorUtil.getExceptionFor(e);
        }
    }

    @Override
    public Map<UUID, String> getEncodedNetworkPageContent() throws DBException {
        try {
            return transferTable.getNetworkPageContent();
        } catch (SQLException e) {
            throw SQLErrorUtil.getExceptionFor(e);
        }
    }

    @Override
    public Map<UUID, String> getEncodedServerHtml() throws DBException {
        try {
            return transferTable.getServerHtml();
        } catch (SQLException e) {
            throw SQLErrorUtil.getExceptionFor(e);
        }
    }
}