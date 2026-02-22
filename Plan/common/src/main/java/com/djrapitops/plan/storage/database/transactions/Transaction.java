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
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.settings.locale.lang.PluginLang;
import com.djrapitops.plan.storage.database.DBType;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.SQLDB;
import com.djrapitops.plan.storage.database.queries.Query;
import com.djrapitops.plan.storage.database.queries.QueryAPIQuery;
import com.djrapitops.plan.storage.database.queries.QueryStatement;
import com.djrapitops.plan.storage.database.queries.schema.MySQLSchemaQueries;
import com.djrapitops.plan.storage.database.queries.schema.SQLiteSchemaQueries;
import com.djrapitops.plan.storage.database.transactions.patches.Patch;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import net.playeranalytics.plugin.scheduling.TimeAmount;

import java.sql.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Represents a database transaction.
 *
 * @author AuroraLS3
 */
public abstract class Transaction {

    // SQLite version on 1.8.8 does not support save points, see createSavePoint() method
    private static final AtomicBoolean SUPPORTS_SAVE_POINTS = new AtomicBoolean(true);
    // Limit for Deadlock attempts.
    private static final int ATTEMPT_LIMIT = 5;
    protected DBType dbType;
    protected boolean success;
    protected int attempts;
    private SQLDB db;
    private Connection connection;
    private Savepoint savepoint;

    protected Transaction() {
        success = false;
        attempts = 0;
    }

    public void executeTransaction(SQLDB db) {
        if (db == null) throw new IllegalArgumentException("Given database was null");
        if (success) throw new IllegalStateException("Transaction has already been executed");

        this.db = db;
        this.dbType = db.getType();

        attempts++; // Keeps track how many attempts have been made to avoid infinite recursion.

        if (db.isUnderHeavyLoad()) {
            try {
                Thread.yield();
                Thread.sleep(db.getHeavyLoadDelayMs());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        try {
            initializeConnection(db);
            if (shouldBeExecuted()) {
                initializeTransaction();
                if (this instanceof Patch) {
                    db.getLogger().info(db.getLocale().getString(PluginLang.DB_APPLY_PATCH, getName()));
                }
                performOperations();
                if (connection != null) connection.commit();
            }
            success = true;
        } catch (SQLException statementFail) {
            manageFailure(statementFail); // Throws a DBOpException.
        } finally {
            db.returnToPool(connection);
        }
    }

    private void manageFailure(SQLException statementFail) {
        String failMsg = getClass().getSimpleName() + " failed: " + statementFail.getMessage();
        String rollbackStatusMsg = rollbackTransaction();

        // Retry if deadlock occurs.
        int errorCode = statementFail.getErrorCode();
        boolean mysqlOutdatedRead = dbType == DBType.MYSQL && errorCode == 1020;
        boolean mySQLDeadlock = dbType == DBType.MYSQL && errorCode == 1213;
        boolean deadlocked = mySQLDeadlock || statementFail instanceof SQLTransactionRollbackException;
        boolean lockWaitTimeout = errorCode == 1205;
        boolean duplicateEntry = errorCode == 1062;
        if (mysqlOutdatedRead || deadlocked || duplicateEntry || lockWaitTimeout && attempts < ATTEMPT_LIMIT) {
            executeTransaction(db); // Recurse to attempt again.
            return;
        }

        if (dbType == DBType.MYSQL && lockWaitTimeout) {
            if (!db.isUnderHeavyLoad()) {
                db.getLogger().warn("Database appears to be under heavy load. Dropping some unimportant transactions and adding short pauses for next 10 minutes.");
                db.getRunnableFactory().create(db::assumeNoMoreHeavyLoad)
                        .runTaskLaterAsynchronously(TimeAmount.toTicks(2, TimeUnit.MINUTES));
            }
            db.increaseHeavyLoadDelay();
            executeTransaction(db); // Recurse to attempt again.
            return;
        }

        if (attempts >= ATTEMPT_LIMIT) {
            failMsg += " (Attempted " + attempts + " times)";
        }

        throw new DBOpException(failMsg + rollbackStatusMsg, statementFail, ErrorContext.builder()
                .related("Attempts: " + attempts)
                .build());
    }

    private String rollbackTransaction() {
        String rollbackStatusMsg = ", Transaction was rolled back.";
        boolean hasNoSavePoints = !SUPPORTS_SAVE_POINTS.get();
        if (hasNoSavePoints) {
            rollbackStatusMsg = ", additionally rollbacks are not supported on this server version.";
        } else {
            // Rollbacks are supported.
            try {
                if (connection != null && savepoint != null) {
                    connection.rollback(savepoint);
                }
            } catch (SQLException rollbackFail) {
                rollbackStatusMsg = ", additionally Transaction rollback failed: " + rollbackFail.getMessage();
            }
        }
        return rollbackStatusMsg;
    }

    protected void commitMidTransaction() {
        try {
            connection.commit();
            initializeTransaction();
        } catch (SQLException e) {
            manageFailure(e);
        }
    }

    /**
     * Override this method for conditional execution.
     * <p>
     * Please note that the transaction has not been initialized and class variables are not available for
     * queries. The condition should depend on other variables (Like the data that is to be stored) given to the transaction.
     *
     * @return false if the transaction should not execute.
     */
    protected boolean shouldBeExecuted() {
        return true;
    }

    /**
     * Implement this method for transaction execution.
     */
    protected abstract void performOperations();

    private void initializeConnection(SQLDB db) {
        try {
            this.connection = db.getConnection();
        } catch (SQLException e) {
            throw new DBOpException(getClass().getSimpleName() + " initialization failed: " + e.getMessage(), e);
        }
    }

    private void initializeTransaction() {
        setIsolationLevel();
        try {
            createSavePoint();
        } catch (SQLException e) {
            throw new DBOpException(getClass().getSimpleName() + " save point initialization failed: " + e.getMessage(), e);
        }
    }

    private void setIsolationLevel() {
        if (dbType == DBType.MYSQL && attempts == 1) {
            IsolationLevel desiredIsolationLevel = getDesiredIsolationLevel();
            if (desiredIsolationLevel != IsolationLevel.UNCHANGED) {
                execute("SET TRANSACTION ISOLATION LEVEL " + desiredIsolationLevel.name().replace('_', ' '));
            }
        }
    }

    private void createSavePoint() throws SQLException {
        try {
            this.savepoint = connection.setSavepoint();
        } catch (SQLFeatureNotSupportedException noSavePoints) {
            SUPPORTS_SAVE_POINTS.set(false);
        } catch (SQLException sqlException) {
            handleUnsupportedSQLiteSavePoints(sqlException);
        }
    }

    private void handleUnsupportedSQLiteSavePoints(SQLException sqlException) throws SQLException {
        String errorMsg = sqlException.getMessage();
        if (errorMsg.contains("unsupported") && errorMsg.contains("savepoints")) {
            SUPPORTS_SAVE_POINTS.set(false);
        } else {
            throw sqlException;
        }
    }

    protected <T> T query(Query<T> query) {
        if (query instanceof QueryStatement) {
            return ((QueryStatement<T>) query).executeWithConnection(connection);
        } else if (query instanceof QueryAPIQuery) {
            return ((QueryAPIQuery<T>) query).executeWithConnection(connection);
        } else {
            return db.queryWithinTransaction(query, this);
        }
    }

    protected boolean execute(Executable executable) {
        return executable.execute(connection);
    }

    protected int executeReturningId(ExecStatement executable) {
        return executable.executeReturningId(connection);
    }

    protected boolean execute(String sql) {
        return execute(new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) {
                // Statement is ready for execution.
            }
        });
    }

    protected void executeSwallowingExceptions(String... statements) {
        if (statements == null) return;
        for (String statement : statements) {
            if (statement == null) continue;
            try {
                execute(statement);
            } catch (DBOpException ignore) {
                /* Exceptions swallowed */
            }
        }
    }

    protected void executeOther(Transaction transaction) {
        transaction.db = db;
        transaction.dbType = dbType;
        transaction.connection = this.connection;
        if (transaction.shouldBeExecuted()) {
            transaction.performOperations();
        }
        transaction.connection = null;
        transaction.dbType = null;
        transaction.db = null;
    }

    protected Database.State getDBState() {
        return db.getState();
    }

    protected ServerUUID getServerUUID() {
        return db.getServerUUIDSupplier().get();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + (success ? " (finished)" : "");
    }

    public boolean wasSuccessful() {
        return success;
    }

    public boolean dbIsNotUnderHeavyLoad() {
        return !db.isUnderHeavyLoad() && !db.shouldDropUnimportantTransactions();
    }

    public String getName() {
        String simpleName = getClass().getSimpleName();
        return simpleName.isEmpty() ? getClass().getName() : simpleName;
    }

    protected boolean hasTable(String tableName) {
        switch (dbType) {
            case SQLITE:
                return query(SQLiteSchemaQueries.doesTableExist(tableName));
            case MYSQL:
                return query(MySQLSchemaQueries.doesTableExist(tableName));
            default:
                throw new IllegalStateException("Unsupported Database Type: " + dbType.getName());
        }
    }

    protected String lockForUpdate() {return db.getSql().lockForUpdate();}

    protected IsolationLevel getDesiredIsolationLevel() {
        return IsolationLevel.UNCHANGED;
    }

    public enum IsolationLevel {
        UNCHANGED,
        READ_COMMITTED
    }
}