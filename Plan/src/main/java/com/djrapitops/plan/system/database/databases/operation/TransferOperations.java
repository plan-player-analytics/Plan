/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.database.databases.operation;

import com.djrapitops.plan.api.exceptions.database.DBException;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Operations for transferring data via Database to another server.
 * <p>
 * Receiving server has to be using the same database.
 *
 * @author Rsl1122
 */
public interface TransferOperations {

    // Save

    @Deprecated
    void storePlayerHtml(UUID player, String encodedHtml) throws DBException;

    @Deprecated
    void storeServerHtml(UUID serverUUID, String encodedHtml) throws DBException;

    @Deprecated
    void storeNetworkPageContent(UUID serverUUID, String encodedHtml) throws DBException;

    void storeConfigSettings(String encodedSettingString) throws DBException;

    // Get

    @Deprecated
    Map<UUID, String> getEncodedPlayerHtml() throws DBException;

    @Deprecated
    Map<UUID, String> getEncodedNetworkPageContent() throws DBException;

    @Deprecated
    Map<UUID, String> getEncodedServerHtml() throws DBException;

    @Deprecated
    Optional<UUID> getServerPlayerIsOnlineOn(UUID playerUUID) throws DBException;

    Optional<String> getEncodedConfigSettings() throws DBException;
}