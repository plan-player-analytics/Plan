package com.djrapitops.plan.system.database.databases.operation;

import com.djrapitops.plan.api.exceptions.database.DBException;
import com.djrapitops.plan.system.info.server.ServerInfo;

import java.util.UUID;

public interface CheckOperations {

    boolean isPlayerRegistered(UUID player) throws DBException;

    boolean isPlayerRegistered(UUID player, UUID server) throws DBException;

    boolean doesWebUserExists(String username) throws DBException;

    default boolean isPlayerRegisteredOnThisServer(UUID player) throws DBException {
        return isPlayerRegistered(player, ServerInfo.getServerUUID());
    }

    boolean isServerInDatabase(UUID serverUUID) throws DBException;
}
