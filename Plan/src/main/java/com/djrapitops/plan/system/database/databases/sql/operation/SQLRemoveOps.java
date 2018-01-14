package com.djrapitops.plan.system.database.databases.sql.operation;

import com.djrapitops.plan.api.exceptions.database.DBException;
import com.djrapitops.plan.api.exceptions.database.FatalDBException;
import com.djrapitops.plan.system.database.databases.operation.RemoveOperations;
import com.djrapitops.plan.system.database.databases.sql.ErrorUtil;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;
import com.djrapitops.plan.system.database.databases.sql.tables.Table;
import com.djrapitops.plan.system.database.databases.sql.tables.UserIDTable;
import com.djrapitops.plugin.api.Benchmark;
import com.djrapitops.plugin.api.utility.log.Log;

import java.sql.SQLException;
import java.util.UUID;

public class SQLRemoveOps extends SQLOps implements RemoveOperations {

    public SQLRemoveOps(SQLDB db) {
        super(db);
    }

    @Override
    public void player(UUID uuid) throws DBException {
        if (uuid == null) {
            return;
        }

        try {
            Log.logDebug("Database", "Removing Account: " + uuid);
            Benchmark.start("Database", "Remove Account");
            String webUser = usersTable.getPlayerName(uuid);

            for (Table t : db.getAllTablesInRemoveOrder()) {
                if (!(t instanceof UserIDTable)) {
                    continue;
                }

                UserIDTable table = (UserIDTable) t;
                table.removeUser(uuid);
            }

            securityTable.removeUser(webUser);
        } catch (SQLException e) {
            throw ErrorUtil.getFatalExceptionFor(e);
        } finally {
            Benchmark.stop("Database", "Remove Account");
        }
    }

    @Override
    public void player(UUID player, UUID server) throws DBException {
        throw new FatalDBException("Not Implemented");
    }

    @Override
    public void server(UUID serverUUID) throws DBException {
        throw new FatalDBException("Not Implemented");
    }

    @Override
    public void everything() throws DBException {
        try {
            for (Table table : db.getAllTablesInRemoveOrder()) {
                table.removeAllData();
            }
        } catch (SQLException e) {
            throw ErrorUtil.getFatalExceptionFor(e);
        }
    }

    @Override
    public void webUser(String userName) throws DBException {
        try {
            securityTable.removeUser(userName);
        } catch (SQLException e) {
            throw ErrorUtil.getFatalExceptionFor(e);
        }
    }
}
