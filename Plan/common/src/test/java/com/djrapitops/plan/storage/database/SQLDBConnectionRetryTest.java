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

import com.djrapitops.plan.exceptions.database.DBOpException;
import com.djrapitops.plan.exceptions.database.FatalDBException;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.sql.SQLNonTransientConnectionException;
import java.sql.SQLTransientConnectionException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SQLDBConnectionRetryTest {

    @Test
    void recognizesConnectionExceptionInCauseChain() {
        DBOpException failure = new DBOpException(
                "Connection unavailable",
                new IllegalStateException(new SQLTransientConnectionException("Connection unavailable"))
        );

        assertTrue(SQLDB.isConnectionFailure(failure));
    }

    @Test
    void recognizesConnectionSqlState() {
        DBOpException failure = new DBOpException(
                "Connection unavailable",
                new SQLException("Connection unavailable", "08006")
        );

        assertTrue(SQLDB.isConnectionFailure(failure));
    }

    @Test
    void doesNotRetryStatementFailure() {
        DBOpException failure = new DBOpException(
                "Statement failed",
                new SQLException("Constraint violation", "23000", 1062)
        );

        assertFalse(SQLDB.isConnectionFailure(failure));
    }

    @Test
    void doesNotRetryNonTransientConnectionFailure() {
        DBOpException failure = new DBOpException(
                "Connection rejected",
                new SQLNonTransientConnectionException("Connection rejected", "08004")
        );

        assertFalse(SQLDB.isConnectionFailure(failure));
    }

    @Test
    void doesNotRetryFatalDatabaseFailure() {
        FatalDBException failure = new FatalDBException(
                "Database initialization failed: ",
                new DBOpException(
                        "Connection unavailable",
                        new SQLTransientConnectionException("Connection unavailable", "08001")
                )
        );

        assertFalse(SQLDB.isConnectionFailure(failure));
    }
}
