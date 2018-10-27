package com.djrapitops.plan.system.database.databases;

import com.djrapitops.plan.api.exceptions.database.DBException;
import com.djrapitops.plan.api.exceptions.database.DBInitException;
import com.djrapitops.plan.system.database.databases.operation.*;

/**
 * Abstract class representing a Database.
 * <p>
 * All Operations methods should be only called from an asynchronous thread.
 *
 * @author Rsl1122
 */
public abstract class Database {

    protected boolean open = false;

    public abstract void init() throws DBInitException;

    public abstract BackupOperations backup();

    public abstract CheckOperations check();

    public abstract FetchOperations fetch();

    public abstract RemoveOperations remove();

    public abstract SearchOperations search();

    public abstract CountOperations count();

    public abstract SaveOperations save();

    public abstract String getName();

    /**
     * Used to get the config name of the database type.
     * <p>
     * Thread safe.
     *
     * @return sqlite/mysql
     */
    public String getConfigName() {
        return getName().toLowerCase().trim();
    }

    public abstract void close() throws DBException;

    public boolean isOpen() {
        return open;
    }

    public abstract void scheduleClean(long delay);

    public abstract TransferOperations transfer();
}
