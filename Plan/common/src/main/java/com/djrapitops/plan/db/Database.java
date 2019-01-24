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
package com.djrapitops.plan.db;

import com.djrapitops.plan.api.exceptions.database.DBException;
import com.djrapitops.plan.api.exceptions.database.DBInitException;
import com.djrapitops.plan.db.access.Query;
import com.djrapitops.plan.db.access.transactions.Transaction;
import com.djrapitops.plan.system.database.databases.operation.*;

/**
 * Interface for interacting with a Plan SQL database.
 *
 * @author Rsl1122
 */
public interface Database {

    void init() throws DBInitException;

    void close() throws DBException;

    boolean isOpen();

    /**
     * Execute an SQL Query statement to get a result.
     * <p>
     * This method should only be called from an asynchronous thread.
     *
     * @param query QueryStatement to execute.
     * @param <T>   Type of the object to be returned.
     * @return Result of the query.
     */
    <T> T query(Query<T> query);

    /**
     * Execute an SQL Transaction.
     *
     * @param transaction Transaction to execute.
     */
    void executeTransaction(Transaction transaction);

    @Deprecated
    CheckOperations check();

    @Deprecated
    FetchOperations fetch();

    @Deprecated
    RemoveOperations remove();

    @Deprecated
    SearchOperations search();

    @Deprecated
    CountOperations count();

    @Deprecated
    SaveOperations save();

    /**
     * Used to get the {@code DBType} of the Database
     *
     * @return the {@code DBType}
     * @see DBType
     */
    @Deprecated
    DBType getType();

    @Deprecated
    void scheduleClean(long delay);
}
