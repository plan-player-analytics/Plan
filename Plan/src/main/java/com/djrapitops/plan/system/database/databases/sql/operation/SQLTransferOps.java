/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.database.databases.sql.operation;

import com.djrapitops.plan.api.exceptions.database.DBException;
import com.djrapitops.plan.system.database.databases.operation.TransferOperations;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;

import java.sql.SQLException;
import java.util.Map;
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

    @Override
    public void storePlayerPluginsTab(UUID player, String encodedHtml) throws DBException {
        try {
            transferTable.storePlayerPluginsTab(player, encodedHtml);
        } catch (SQLException e) {
            throw SQLErrorUtil.getExceptionFor(e);
        }
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
    public Map<UUID, String> getEncodedPlayerPluginsTabs(UUID playerUUID) throws DBException {
        try {
            return transferTable.getPlayerPluginsTabs(playerUUID);
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