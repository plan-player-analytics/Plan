/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.api;

import com.djrapitops.plan.api.exceptions.database.DBOpException;
import com.djrapitops.plan.utilities.uuid.UUIDUtility;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.error.ErrorHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * PlanAPI extension for all implementations.
 *
 * @author Rsl1122
 */
public abstract class CommonAPI implements PlanAPI {

    private final UUIDUtility uuidUtility;
    private final ErrorHandler errorHandler;

    CommonAPI(UUIDUtility uuidUtility, ErrorHandler errorHandler) {
        this.uuidUtility = uuidUtility;
        this.errorHandler = errorHandler;
        PlanAPIHolder.set(this);
    }

    @Override
    public String getPlayerInspectPageLink(UUID uuid) {
        return getPlayerInspectPageLink(getPlayerName(uuid));
    }

    @Override
    public String getPlayerInspectPageLink(String playerName) {
        return "../player/" + playerName;
    }

    @Override
    public UUID playerNameToUUID(String playerName) {
        return uuidUtility.getUUIDOf(playerName);
    }

    @Override
    public Map<UUID, String> getKnownPlayerNames() {
        try {
            return fetchFromPlanDB().getPlayerNames();
        } catch (DBOpException e) {
            errorHandler.log(L.ERROR, this.getClass(), e);
            return new HashMap<>();
        }
    }

}
