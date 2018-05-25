package com.djrapitops.plan.system.database.databases.operation;

import com.djrapitops.plan.api.exceptions.database.DBException;
import com.djrapitops.plan.system.database.databases.Database;

import java.sql.SQLException;

public interface BackupOperations {

    void backup(Database toDatabase) throws SQLException;

    void restore(Database fromDatabase) throws DBException, SQLException;

}
