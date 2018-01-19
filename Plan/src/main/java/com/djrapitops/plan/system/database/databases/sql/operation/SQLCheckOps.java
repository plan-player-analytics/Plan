package com.djrapitops.plan.system.database.databases.sql.operation;

import com.djrapitops.plan.api.exceptions.database.DBException;
import com.djrapitops.plan.system.database.databases.operation.CheckOperations;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;
import com.djrapitops.plan.system.database.databases.sql.SQLErrorUtil;
import com.djrapitops.plan.system.info.server.ServerInfo;

import java.sql.SQLException;
import java.util.UUID;

public class SQLCheckOps extends SQLOps implements CheckOperations {

    public SQLCheckOps(SQLDB db) {
        super(db);
    }

    @Override
    public boolean isPlayerRegistered(UUID player) throws DBException {
        try {
            return usersTable.isRegistered(player);
        } catch (SQLException e) {
            throw SQLErrorUtil.getExceptionFor(e);
        }
    }

    @Override
    public boolean isPlayerRegistered(UUID player, UUID server) throws DBException {
        try {
            return userInfoTable.isRegistered(player, server);
        } catch (SQLException e) {
            throw SQLErrorUtil.getExceptionFor(e);
        }
    }

    @Override
    public boolean doesWebUserExists(String username) throws DBException {
        try {
            return securityTable.userExists(username);
        } catch (SQLException e) {
            throw SQLErrorUtil.getExceptionFor(e);
        }
    }

    @Override
    public boolean isPlayerRegisteredOnThisServer(UUID player) throws DBException {
        return isPlayerRegistered(player, ServerInfo.getServerUUID());
    }

    @Override
    public boolean isServerInDatabase(UUID serverUUID) throws DBException {
        try {
            return serverTable.getServerID(serverUUID).isPresent();
        } catch (SQLException e) {
            throw SQLErrorUtil.getExceptionFor(e);
        }
    }
}
