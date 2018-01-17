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
 *
 * Receiving server has to be using the same database.
 *
 * @author Rsl1122
 */
public interface TransferOperations {

    // Save

    void playerHtml(UUID player, String html) throws DBException;

    // Get

    Map<UUID, String> getPlayerHtml() throws DBException;
}