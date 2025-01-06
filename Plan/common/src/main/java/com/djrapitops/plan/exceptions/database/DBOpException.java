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
package com.djrapitops.plan.exceptions.database;

import com.djrapitops.plan.exceptions.ExceptionWithContext;
import com.djrapitops.plan.utilities.logging.ErrorContext;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Runtime exception for wrapping database errors.
 *
 * @author AuroraLS3
 */
public class DBOpException extends IllegalStateException implements ExceptionWithContext {

    public static final String CONSTRAINT_VIOLATION = "Constraint Violation";
    public static final String DUPLICATE_KEY = "Duplicate key";
    private final ErrorContext context;

    public DBOpException(String message) {
        super(message);
        this.context = null;
    }

    public DBOpException(String message, Throwable cause) {
        this(message, cause, null);
    }

    public DBOpException(String message, Throwable cause, ErrorContext context) {
        super(message, cause);
        this.context = context;
    }

    // Checkstyle.OFF: CyclomaticComplexity

    public static DBOpException forCause(String sql, SQLException e) {
        ErrorContext.Builder context = ErrorContext.builder();
        int errorCode = e.getErrorCode();
        context.related("Error code: " + errorCode)
                .related(sql);
        switch (errorCode) {
            // SQLite Corrupt
            case 10:
            case 523:
                context.related("SQL Corrupted")
                        .whatToDo("Your SQLite has corrupted. This can happen if .db-wal or .db-shm files get replaced mid operation. Restore database.db from backup.");
                break;
            // Syntax error codes
            case 1054: // MySQL
            case 1064:
            case 1146:
                context.related("SQL Grammar error")
                        .whatToDo("Report this, there is an SQL grammar error.");
                break;
            // Type mismatch
            case 20: // SQLite
                context.related("SQL Type mismatch")
                        .whatToDo("Report this, there is an SQL Type mismatch.");
                break;
            // Duplicate key
            case 1062:
            case 1022:
            case 23001:
            case 23505:
                context.related(DUPLICATE_KEY)
                        .whatToDo("Report this, duplicate key exists in SQL.");
                break;
            // Constraint violation
            case 19: // SQLite
            case 275:
            case 531:
            case 787:
            case 1043:
            case 1555:
            case 2579:
            case 1811:
            case 2067:
            case 2323:
            case 630: // MySQL
            case 839:
            case 840:
            case 893:
            case 1169:
            case 1215:
            case 1216:
            case 1217:
            case 1364:
            case 1451:
            case 1557:
                context.related(CONSTRAINT_VIOLATION)
                        .whatToDo("Report this, there is an SQL Constraint Violation.");
                break;
            // Custom rules based on reported errors
            case 11:
            case 14:
                context.related("SQLite file is corrupt.")
                        .whatToDo("SQLite database is corrupt, restore database.db, .db-shm & .db-wal files from a backup, or repair the database: See https://wordpress.semnaitik.com/repair-sqlite-database/.");
                break;
            case 13: // SQLite
            case 1021: // MariaDB
                context.related("Disk or temporary directory is full.")
                        .whatToDo("Disk or temporary directory is full, attempt to clear space in the temporary directory. See https://sqlite.org/rescode.html#full. If you use the Pterodactyl panel, increase the \"tmpfs_size\" config setting. See https://pterodactyl.io/wings/1.0/configuration.html#other-values");
                break;
            case 1104:
                context.whatToDo("MySQL has too small query limits for the query. SET SQL_BIG_SELECTS=1 or SET MAX_JOIN_SIZE=# (higher number)");
                break;
            case 1142:
                context.related("Missing privilege")
                        .whatToDo("Grant the required privileges to your MySQL user (often 'REFERENCES' privilege is missing).");
                break;
            case 1213:
                context.related("Deadlock");
                break;
            case 1267:
            case 1366:
            case 1115:
                context.related("Incorrect character encoding in MySQL")
                        .whatToDo("Convert your MySQL database and tables to use utf8mb4: https://www.a2hosting.com/kb/developer-corner/mysql/convert-mysql-database-utf-8");
                break;
            case 1299: // SQLite
            case 1048: // MySQL or MariaDB
            case 1452:
            case 1121:
            case 1171:
            case 1830:
            case 1263:
                context.related(CONSTRAINT_VIOLATION)
                        .whatToDo("Report this error. NOT NULL constraint violation occurred.");
                break;
            case 1071:
                context.related("column byte length exceeded")
                        .whatToDo("Update your MySQL, column key size was exceeded (max key length is 767 bytes in 5.6) - MySQL 5.7 increases the limit.");
                break;
            default:
                context.related("Unknown SQL Error code");
        }
        return new DBOpException("SQL Failure: " + e.getMessage(), e, context.build());
    }

    // Checkstyle.ON: CyclomaticComplexity

    @Override
    public Optional<ErrorContext> getContext() {
        return Optional.ofNullable(context);
    }

    public boolean isUserIdConstraintViolation() {
        return context != null
                && context.getRelated().contains(DBOpException.CONSTRAINT_VIOLATION)
                && getCause() != null
                && getCause().getMessage().contains("user_id");
    }

    public boolean isDuplicateKeyViolation() {
        return context != null
                && context.getRelated().contains(DBOpException.DUPLICATE_KEY);
    }
}
