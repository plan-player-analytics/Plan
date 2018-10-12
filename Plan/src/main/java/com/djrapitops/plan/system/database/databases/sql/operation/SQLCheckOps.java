package com.djrapitops.plan.system.database.databases.sql.operation;

import com.djrapitops.plan.system.database.databases.operation.CheckOperations;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;

import java.util.UUID;

public class SQLCheckOps extends SQLOps implements CheckOperations {

    public SQLCheckOps(SQLDB db) {
        super(db);
    }

    @Override
    public boolean isPlayerRegistered(UUID player) {
        return usersTable.isRegistered(player);
    }

    @Override
    public boolean isPlayerRegistered(UUID player, UUID server) {
        return userInfoTable.isRegistered(player, server);
    }

    @Override
    public boolean isPlayerRegisteredOnThisServer(UUID player) {
        return userInfoTable.isRegisteredOnThisServer(player);
    }

    @Override
    public boolean doesWebUserExists(String username) {
        return securityTable.userExists(username);
    }

    @Override
    public boolean isServerInDatabase(UUID serverUUID) {
        return serverTable.getServerID(serverUUID).isPresent();
    }
}
