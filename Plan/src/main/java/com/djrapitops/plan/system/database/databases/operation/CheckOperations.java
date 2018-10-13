package com.djrapitops.plan.system.database.databases.operation;

import java.util.UUID;

public interface CheckOperations {

    boolean isPlayerRegistered(UUID player);

    boolean isPlayerRegistered(UUID player, UUID server);

    boolean doesWebUserExists(String username);

    boolean isPlayerRegisteredOnThisServer(UUID player);

    boolean isServerInDatabase(UUID serverUUID);
}
