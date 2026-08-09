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

import com.djrapitops.plan.storage.database.DBType;
import com.djrapitops.plan.storage.database.SQLDB;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionExecutionStateTest {

    @Mock
    SQLDB database;
    @Mock
    Connection connection;
    @Mock
    Savepoint savepoint;

    @BeforeEach
    void setUp() throws SQLException {
        when(database.getType()).thenReturn(DBType.SQLITE);
        when(database.getConnection()).thenReturn(connection);
    }

    @Test
    void executedTransactionReportsExecution() throws SQLException {
        when(connection.setSavepoint()).thenReturn(savepoint);
        Transaction transaction = new Transaction() {
            @Override
            protected void performOperations() {
                // No database operations are needed for execution-state tracking.
            }
        };

        transaction.executeTransaction(database);

        assertTrue(transaction.wasSuccessful());
        assertTrue(transaction.wasExecuted());
    }

    @Test
    void skippedTransactionDoesNotReportExecution() {
        Transaction transaction = new Transaction() {
            @Override
            protected boolean shouldBeExecuted() {
                return false;
            }

            @Override
            protected void performOperations() {
                throw new AssertionError("Skipped transaction was executed");
            }
        };

        transaction.executeTransaction(database);

        assertTrue(transaction.wasSuccessful());
        assertFalse(transaction.wasExecuted());
    }
}
