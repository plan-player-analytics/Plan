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

import com.djrapitops.plan.exceptions.EnableException;
import com.djrapitops.plan.storage.database.transactions.Transaction;
import com.djrapitops.plan.storage.database.transactions.init.OperationCriticalTransaction;

/**
 * Database Lock that prevents queries and transactions from taking place before database schema is ready.
 * <p>
 * - OperationCriticalTransactions pass through the Access lock without blocking to allow the initial transactions.
 * - Queries inside Transactions skip access log to allow OperationCriticalTransactions perform queries.
 *
 * @author Rsl1122
 */
public class DBAccessLock {

    private final Database database;

    private final Object lockObject;

    public DBAccessLock(Database database) {
        this.database = database;
        this.lockObject = new Object();
    }

    public void checkAccess() {
        checkAccess(false);
    }

    public void checkAccess(Transaction transaction) {
        checkAccess(transaction instanceof OperationCriticalTransaction);
    }

    private void checkAccess(boolean isOperationCriticalTransaction) {
        if (isOperationCriticalTransaction) {
            return;
        }
        try {
            // Wait for the database to be in OPEN or CLOSING state before allowing execution.
            // CLOSING is not an allowed state if database was not OPEN at the time of close.
            while (database.getState() != Database.State.OPEN && database.getState() != Database.State.CLOSING) {
                synchronized (lockObject) {
                    lockObject.wait();
                    if (database.getState() == Database.State.CLOSED) {
                        throw new EnableException("Database failed to open, Query has failed. (This exception is necessary to not keep query threads waiting)");
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void operabilityChanged() {
        synchronized (lockObject) {
            lockObject.notifyAll();
        }
    }
}