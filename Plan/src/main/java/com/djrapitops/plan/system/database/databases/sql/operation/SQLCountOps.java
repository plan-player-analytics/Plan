package com.djrapitops.plan.system.database.databases.sql.operation;

import com.djrapitops.plan.api.exceptions.database.DBException;
import com.djrapitops.plan.system.database.databases.operation.CountOperations;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;

import java.sql.SQLException;
import java.util.UUID;

public class SQLCountOps extends SQLOps implements CountOperations {

    public SQLCountOps(SQLDB db) {
        super(db);
    }

    @Override
    public int getServerPlayerCount(UUID server) throws DBException {
        try {
            return userInfoTable.getServerUserCount(server);
        } catch (SQLException e) {
            throw SQLErrorUtil.getExceptionFor(e);
        }
    }

    @Override
    public int getNetworkPlayerCount() throws DBException {
        try {
            return usersTable.getPlayerCount();
        } catch (SQLException e) {
            throw SQLErrorUtil.getExceptionFor(e);
        }
    }
}
