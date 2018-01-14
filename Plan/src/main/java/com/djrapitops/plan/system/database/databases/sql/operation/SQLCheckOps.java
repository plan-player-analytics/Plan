package com.djrapitops.plan.system.database.databases.sql.operation;

import com.djrapitops.plan.api.exceptions.database.DBException;
import com.djrapitops.plan.system.database.databases.operation.CheckOperations;
import com.djrapitops.plan.system.database.databases.sql.ErrorUtil;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;

import java.sql.SQLException;
import java.util.UUID;

public class SQLCheckOps implements CheckOperations {

    private final SQLDB db;

    public SQLCheckOps(SQLDB db) {
        this.db = db;
    }

    @Override
    public boolean isPlayerRegistered(UUID player) throws DBException {
        try {
            return db.getUsersTable().isRegistered(player);
        } catch (SQLException e) {
            throw ErrorUtil.getExceptionFor(e);
        }
    }

    @Override
    public boolean isPlayerRegistered(UUID player, UUID server) throws DBException {
        try {
            return db.getUserInfoTable().isRegistered(player, server);
        } catch (SQLException e) {
            throw ErrorUtil.getExceptionFor(e);
        }
    }

    @Override
    public boolean doesWebUserExists(String username) throws DBException {
        try {
            return db.getSecurityTable().userExists(username);
        } catch (SQLException e) {
            throw ErrorUtil.getExceptionFor(e);
        }
    }
}
