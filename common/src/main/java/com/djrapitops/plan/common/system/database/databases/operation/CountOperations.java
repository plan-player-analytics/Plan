package com.djrapitops.plan.common.system.database.databases.operation;

import java.util.UUID;

public interface CountOperations {

    int getServerPlayerCount(UUID server);

    int getNetworkPlayerCount();
}
