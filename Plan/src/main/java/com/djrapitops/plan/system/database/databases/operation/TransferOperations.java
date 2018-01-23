/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.database.databases.operation;

import com.djrapitops.plan.api.exceptions.database.DBException;

import java.util.Map;
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

    void storePlayerHtml(UUID player, String encodedHtml) throws DBException;

    void storeServerHtml(UUID serverUUID, String encodedHtml) throws DBException;

    void storeNetworkPageContent(UUID serverUUID, String encodedHtml) throws DBException;

    void storePlayerPluginsTab(UUID player, UUID serverUUID, String encodedHtml) throws DBException;

    // Get

    Map<UUID, String> getEncodedPlayerHtml() throws DBException;

    Map<UUID, String> getEncodedNetworkPageContent() throws DBException;

    Map<UUID, String> getEncodedServerHtml() throws DBException;

    UUID getServerPlayerIsOnline(UUID playerUUID) throws DBException;

    Map<UUID, String> getEncodedPlayerPluginsTabs(UUID playerUUID) throws DBException;
}