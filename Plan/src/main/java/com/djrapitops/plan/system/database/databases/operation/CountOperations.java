package com.djrapitops.plan.system.database.databases.operation;

import com.djrapitops.plan.api.exceptions.database.DBException;

import java.util.UUID;

public interface CountOperations {

    int getServerPlayerCount(UUID server) throws DBException;

    int getNetworkPlayerCount() throws DBException;
}
