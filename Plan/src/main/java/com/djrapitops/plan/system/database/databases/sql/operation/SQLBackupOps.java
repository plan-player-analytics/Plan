package com.djrapitops.plan.system.database.databases.sql.operation;

import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.database.databases.operation.BackupOperations;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;

public class SQLBackupOps extends SQLOps implements BackupOperations {

    public SQLBackupOps(SQLDB db) {
        super(db);
    }

    @Override
    public void backup(Database toDatabase) {
        // TODO
    }

    @Override
    public void restore(Database fromDatabase) {
        // TODO
    }
}
