/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
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
        if (toDatabase instanceof SQLDB) {
            BatchOperationTable toDB = new BatchOperationTable((SQLDB) toDatabase);
            BatchOperationTable fromDB = new BatchOperationTable(db);

            toDB.removeAllData();
            fromDB.copyEverything(toDB);
        } else {
            throw new IllegalArgumentException("Database was not a SQL database - backup not implemented.");
        }
    }

    @Override
    public void restore(Database fromDatabase) {
        fromDatabase.backup().backup(db);
    }
}
