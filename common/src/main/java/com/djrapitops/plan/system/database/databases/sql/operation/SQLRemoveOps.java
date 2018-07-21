package com.djrapitops.plan.system.database.databases.sql.operation;

import com.djrapitops.plan.system.database.databases.operation.RemoveOperations;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;
import com.djrapitops.plan.system.database.databases.sql.tables.Table;
import com.djrapitops.plan.system.database.databases.sql.tables.UserIDTable;

import java.util.UUID;

public class SQLRemoveOps extends SQLOps implements RemoveOperations {

    public SQLRemoveOps(SQLDB db) {
        super(db);
    }

    @Override
    public void player(UUID uuid) {
        if (uuid == null) {
            return;
        }

        String webUser = usersTable.getPlayerName(uuid);

        for (Table t : db.getAllTablesInRemoveOrder()) {
            if (!(t instanceof UserIDTable)) {
                continue;
            }

            UserIDTable table = (UserIDTable) t;
            table.removeUser(uuid);
        }

        securityTable.removeUser(webUser);
    }

    @Override
    public void everything() {
        for (Table table : db.getAllTablesInRemoveOrder()) {
            table.removeAllData();
        }
    }

    @Override
    public void webUser(String userName) {
        securityTable.removeUser(userName);
    }
}
