package com.djrapitops.plan.system.database.databases.sql.operation;

import com.djrapitops.plan.system.database.databases.operation.CheckOperations;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;
import com.djrapitops.plan.system.info.server.ServerInfo;

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
    public boolean doesWebUserExists(String username) {
        return securityTable.userExists(username);
    }

    @Override
    public boolean isPlayerRegisteredOnThisServer(UUID player) {
        return isPlayerRegistered(player, ServerInfo.getServerUUID());
    }

    @Override
    public boolean isServerInDatabase(UUID serverUUID) {
        return serverTable.getServerID(serverUUID).isPresent();
    }
}
