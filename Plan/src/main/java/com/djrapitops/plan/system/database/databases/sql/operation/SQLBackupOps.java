package com.djrapitops.plan.system.database.databases.sql.operation;

import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.database.databases.operation.BackupOperations;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;
import com.djrapitops.plan.system.database.databases.sql.tables.move.BatchOperationTable;

public class SQLBackupOps extends SQLOps implements BackupOperations {

    public SQLBackupOps(SQLDB db) {
        super(db);
    }

    @Override
    public void backup(Database toDatabase) {
        BatchOperationTable toDB = new BatchOperationTable((SQLDB) toDatabase);
        BatchOperationTable fromDB = new BatchOperationTable(db);

        toDB.removeAllData();
        fromDB.copyEverything(toDB);
    }

    @Override
    public void restore(Database fromDatabase) {
        fromDatabase.backup().backup(db);
    }
}
