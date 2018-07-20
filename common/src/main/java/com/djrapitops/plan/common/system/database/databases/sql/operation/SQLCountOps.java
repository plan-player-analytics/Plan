package com.djrapitops.plan.common.system.database.databases.sql.operation;

import com.djrapitops.plan.common.system.database.databases.operation.CountOperations;
import com.djrapitops.plan.common.system.database.databases.sql.SQLDB;

import java.util.UUID;

public class SQLCountOps extends SQLOps implements CountOperations {

    public SQLCountOps(SQLDB db) {
        super(db);
    }

    @Override
    public int getServerPlayerCount(UUID server) {
        return userInfoTable.getServerUserCount(server);
    }

    @Override
    public int getNetworkPlayerCount() {
        return usersTable.getPlayerCount();
    }
}
