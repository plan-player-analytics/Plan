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
package com.djrapitops.plan.storage.database.transactions;

import com.djrapitops.plan.exceptions.database.DBOpException;
import com.djrapitops.plan.storage.database.DBType;
import com.djrapitops.plan.storage.database.SQLDB;
import net.playeranalytics.plugin.scheduling.RunnableFactory;
import net.playeranalytics.plugin.scheduling.UnscheduledTask;
import net.playeranalytics.plugin.server.PluginLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionRetryTest {

    @Mock
    SQLDB database;
    @Mock
    Connection connection;
    @Mock
    Savepoint savepoint;
    @Mock
    RunnableFactory runnableFactory;
    @Mock
    UnscheduledTask scheduledTask;
    @Mock
    PluginLogger logger;

    private final AtomicInteger heavyLoadDelay = new AtomicInteger();

    @BeforeEach
    void setUp() throws SQLException {
        when(database.getType()).thenReturn(DBType.MYSQL);
        when(database.getConnection()).thenReturn(connection);
        when(connection.setSavepoint()).thenReturn(savepoint);
        when(database.isUnderHeavyLoad()).thenAnswer(invocation -> heavyLoadDelay.get() > 0);
        when(database.getHeavyLoadDelayMs()).thenAnswer(invocation -> heavyLoadDelay.get());
        doAnswer(invocation -> {
            heavyLoadDelay.incrementAndGet();
            return null;
        }).when(database).increaseHeavyLoadDelay();
        when(database.getLogger()).thenReturn(logger);
        when(database.getRunnableFactory()).thenReturn(runnableFactory);
        when(runnableFactory.create(any(Runnable.class))).thenReturn(scheduledTask);
    }

    @Test
    void lockWaitRetriesAreBoundedAndEachConnectionIsReturned() throws SQLException {
        LockWaitTransaction transaction = new LockWaitTransaction(Integer.MAX_VALUE);

        assertThrows(DBOpException.class, () -> transaction.executeTransaction(database));

        assertEquals(5, transaction.getOperationCount());
        assertFalse(transaction.wasSuccessful());
        assertFalse(transaction.wasExecuted());
        verify(database, times(5)).getConnection();
        verify(database, times(5)).returnToPool(connection);
        verify(connection, times(5)).rollback(savepoint);
        verify(logger).warn(any(String.class));
        verify(scheduledTask).runTaskLaterAsynchronously(anyLong());
    }

    @Test
    void statementFailureCanRecoverOnANewAttempt() throws SQLException {
        LockWaitTransaction transaction = new LockWaitTransaction(1);

        transaction.executeTransaction(database);

        assertEquals(2, transaction.getOperationCount());
        assertTrue(transaction.wasSuccessful());
        assertTrue(transaction.wasExecuted());
        verify(database, times(2)).getConnection();
        verify(database, times(2)).returnToPool(connection);
    }

    @Test
    void nonCriticalTransactionIsDroppedAfterLockWaitTimeout() throws SQLException {
        LockWaitThrowawayTransaction transaction = new LockWaitThrowawayTransaction();

        transaction.executeTransaction(database);

        assertEquals(1, transaction.getOperationCount());
        assertTrue(transaction.wasSuccessful());
        assertFalse(transaction.wasExecuted());
        verify(database, times(2)).getConnection();
        verify(database, times(2)).returnToPool(connection);
    }

    private static final class LockWaitTransaction extends Transaction {
        private final int failuresBeforeSuccess;
        private int operationCount;

        private LockWaitTransaction(int failuresBeforeSuccess) {
            this.failuresBeforeSuccess = failuresBeforeSuccess;
        }

        @Override
        protected void performOperations() {
            operationCount++;
            if (operationCount <= failuresBeforeSuccess) {
                SQLException lockWait = new SQLException(
                        "Lock wait timeout exceeded; try restarting transaction",
                        "40001",
                        1205
                );
                throw DBOpException.forCause("UPDATE plan_test SET value=1", lockWait);
            }
        }

        private int getOperationCount() {
            return operationCount;
        }
    }

    private static final class LockWaitThrowawayTransaction extends ThrowawayTransaction {
        private int operationCount;

        @Override
        protected void performOperations() {
            operationCount++;
            SQLException lockWait = new SQLException(
                    "Lock wait timeout exceeded; try restarting transaction",
                    "40001",
                    1205
            );
            throw DBOpException.forCause("UPDATE plan_test SET value=1", lockWait);
        }

        private int getOperationCount() {
            return operationCount;
        }
    }
}
