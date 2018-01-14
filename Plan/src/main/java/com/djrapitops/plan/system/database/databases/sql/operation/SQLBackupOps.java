package com.djrapitops.plan.system.database.databases.sql.operation;

import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.database.databases.operation.BackupOperations;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;

public class SQLBackupOps implements BackupOperations {

    private final SQLDB db;

    public SQLBackupOps(SQLDB db) {
        this.db = db;
    }

    @Override
    public void backup(Database toDatabase) {
    }

    @Override
    public void restore(Database fromDatabase) {
    }
}
