/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.database.databases.operation;

import java.util.Optional;

/**
 * Operations for transferring data via Database to another server.
 * <p>
 * Receiving server has to be using the same database.
 *
 * @author Rsl1122
 */
public interface TransferOperations {

    // Save

    void storeConfigSettings(String encodedSettingString);

    // Get

    Optional<String> getEncodedConfigSettings();
}