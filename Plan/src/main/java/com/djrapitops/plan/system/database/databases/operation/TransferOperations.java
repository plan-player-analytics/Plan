/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.database.databases.operation;

import com.djrapitops.plan.api.exceptions.database.DBException;

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

    void storeConfigSettings(String encodedSettingString) throws DBException;

    // Get

    @Deprecated
    Optional<UUID> getServerPlayerIsOnlineOn(UUID playerUUID) throws DBException;

    Optional<String> getEncodedConfigSettings() throws DBException;
}
