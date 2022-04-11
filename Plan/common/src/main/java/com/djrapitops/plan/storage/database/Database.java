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
package com.djrapitops.plan.storage.database;

import com.djrapitops.plan.exceptions.database.DBInitException;
import com.djrapitops.plan.storage.database.queries.Query;
import com.djrapitops.plan.storage.database.sql.building.Sql;
import com.djrapitops.plan.storage.database.transactions.Transaction;

import java.util.concurrent.CompletableFuture;

/**
 * Interface for interacting with a Plan SQL database.
 *
 * @author AuroraLS3
 */
public interface Database {

    /**
     * Initializes the Database.
     * <p>
     * Queries can be performed after this request has completed all required transactions for the database operations.
     *
     * @throws DBInitException if Database fails to initiate.
     */
    void init();

    void close();

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
     * @return Future that is finished when the transaction has been executed.
     */
    CompletableFuture<?> executeTransaction(Transaction transaction);

    /**
     * Used to get the {@code DBType} of the Database
     *
     * @return the {@code DBType}
     * @see DBType
     */
    DBType getType();

    default Sql getSql() {
        return getType().getSql();
    }

    State getState();

    /**
     * Possible State changes:
     * CLOSED to PATCHING (Database init),
     * PATCHING to OPEN (Database initialized),
     * OPEN to CLOSING (Database closing),
     * CLOSING to CLOSED (Database closed),
     * PATCHING to CLOSED (Database closed prematurely)
     */
    enum State {
        CLOSED,
        PATCHING,
        OPEN,
        CLOSING
    }
}
