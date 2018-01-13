package com.djrapitops.plan.system.database.databases.operation;

import java.util.UUID;

public interface CheckOperations {

    boolean wasSeenBefore(UUID player);

    boolean wasSeenBefore(UUID player, UUID server);
    

}
