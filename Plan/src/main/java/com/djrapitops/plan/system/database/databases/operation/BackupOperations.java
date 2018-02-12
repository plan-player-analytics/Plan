package com.djrapitops.plan.system.database.databases.operation;

import com.djrapitops.plan.api.exceptions.database.DBException;
import com.djrapitops.plan.system.database.databases.Database;

public interface BackupOperations {

    void backup(Database toDatabase) throws DBException;

    void restore(Database fromDatabase) throws DBException;

}
