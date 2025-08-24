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

import com.djrapitops.plan.exceptions.database.DBClosedException;
import com.djrapitops.plan.storage.database.transactions.Transaction;
import com.djrapitops.plan.storage.database.transactions.init.OperationCriticalTransaction;
import com.djrapitops.plan.utilities.java.ThrowingSupplier;
import com.djrapitops.plan.utilities.java.ThrowingVoidFunction;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Database Lock that prevents queries and transactions from taking place before database schema is ready.
 * <p>
 * - OperationCriticalTransactions pass through the Access lock without blocking to allow the initial transactions.
 * - Queries inside Transactions skip access log to allow OperationCriticalTransactions perform queries.
 *
 * @author AuroraLS3
 */
public class DBAccessLock {

    private final Database database;
    private final ReentrantLock reentrantLock;

    public DBAccessLock(Database database) {
        this.database = database;
        reentrantLock = new ReentrantLock();
    }

    public void operabilityChanged() {
        for (int i = 0; i < reentrantLock.getHoldCount(); i++) {
            reentrantLock.unlock();
        }
    }

    public <E extends Exception> void performDatabaseOperation(ThrowingVoidFunction<E> operation) throws E {
        performDatabaseOperation(operation, false);
    }

    public <E extends Exception> void performDatabaseOperation(ThrowingVoidFunction<E> operation, Transaction transaction) throws E {
        performDatabaseOperation(operation, transaction instanceof OperationCriticalTransaction);
    }

    private <E extends Exception> void performDatabaseOperation(ThrowingVoidFunction<E> operation, boolean isOperationCriticalTransaction) throws E {
        if (isOperationCriticalTransaction) {
            operation.apply();
            return;
        }
        if (isDatabasePatching()) {
            boolean interrupted = false;
            try {
                reentrantLock.lockInterruptibly();
                operation.apply();
            } catch (InterruptedException e) {
                interrupted = true;
                Thread.currentThread().interrupt();
            } finally {
                if (!interrupted) {
                    reentrantLock.unlock();
                }
            }
        } else {
            operation.apply();
        }
    }

    private boolean isDatabasePatching() {
        return database.getState() != Database.State.OPEN && database.getState() != Database.State.CLOSING;
    }

    public <T, E extends Exception> T performDatabaseOperation(ThrowingSupplier<T, E> operation) throws E {
        return performDatabaseOperation(operation, false);
    }

    public <T, E extends Exception> T performDatabaseOperation(ThrowingSupplier<T, E> operation, Transaction transaction) throws E {
        return performDatabaseOperation(operation, transaction instanceof OperationCriticalTransaction);
    }

    private <T, E extends Exception> T performDatabaseOperation(ThrowingSupplier<T, E> operation, boolean isOperationCriticalTransaction) throws E {
        if (isOperationCriticalTransaction) {
            return operation.get();
        }
        if (isDatabasePatching()) {
            boolean interrupted = false;
            try {
                reentrantLock.lockInterruptibly();
                return operation.get();
            } catch (InterruptedException e) {
                interrupted = true;
                Thread.currentThread().interrupt();
                throw new DBClosedException("Operation interrupted");
            } finally {
                if (!interrupted) {
                    reentrantLock.unlock();
                }
            }
        } else {
            return operation.get();
        }
    }
}