package com.djrapitops.plan.system.database.databases.operation;

import com.djrapitops.plan.system.database.databases.Database;

public interface BackupOperations {

    void backup(Database toDatabase);

    void restore(Database fromDatabase);

}
