/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the LGNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  LGNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.system.database.databases.sql.operation;

import com.djrapitops.plan.api.exceptions.database.DBException;
import com.djrapitops.plan.api.exceptions.database.FatalDBException;

import java.sql.SQLException;

public class SQLErrorUtil {

    private SQLErrorUtil() {
    }

    public static DBException getExceptionFor(SQLException e) {
        String message = e.getMessage();
        if (message.contains("Communications link failure")) {
            return new FatalDBException("MySQL-connection failed", e);
        } else if (message.contains("constraint failed")) {
            return new FatalDBException("There is an error in saving an item.", e);
        } else if (message.contains("syntax")
                || message.contains("SQL Error or missing database")
                || message.contains("SQLITE_MISUSE")
                || message.contains("no such column")) {
            return new FatalDBException("There is an error in SQL syntax", e);
        } else if (message.contains("duplicate key")) {
            return new FatalDBException("An SQL save method attempts to save duplicates.", e);
        }
        return new DBException(e);
    }

    public static FatalDBException getFatalExceptionFor(SQLException e) {
        DBException normalException = getExceptionFor(e);
        if (normalException instanceof FatalDBException) {
            return (FatalDBException) normalException;
        } else {
            return new FatalDBException(normalException.getCause());
        }
    }
}
