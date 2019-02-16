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
package com.djrapitops.plan.db.access.transactions;

import com.djrapitops.plan.api.exceptions.database.DBOpException;
import com.djrapitops.plan.db.DBType;
import com.djrapitops.plan.db.SQLDB;
import com.djrapitops.plan.db.access.ExecStatement;
import com.djrapitops.plan.db.access.Executable;
import com.djrapitops.plan.db.access.Query;
import com.djrapitops.plugin.utilities.Verify;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.UUID;

/**
 * Represents a database transaction.
 *
 * @author Rsl1122
 */
public abstract class Transaction {

    protected SQLDB db; // TODO Make private, this is a quick hack to access some tables while they are in use.
    protected DBType dbType;

    private Connection connection;
    private Savepoint savepoint;

    protected boolean success;

    protected Transaction() {
        success = false;
    }

    public void executeTransaction(SQLDB db) {
        Verify.nullCheck(db, () -> new IllegalArgumentException("Given database was null"));
        Verify.isFalse(success, () -> new IllegalStateException("Transaction has already been executed"));

        this.db = db;
        this.dbType = db.getType();

        if (!shouldBeExecuted()) {
            success = true;
            return;
        }

        try {
            initializeTransaction(db);
            performOperations();
            success = true;
        } finally {
            finalizeTransaction();
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

    private void initializeTransaction(SQLDB db) {
        try {
            this.connection = db.getConnection();
            // Temporary fix for MySQL Patch task test failing, TODO remove after Auto commit is off for MySQL
            if (connection.getAutoCommit()) connection.setAutoCommit(false);
            this.savepoint = connection.setSavepoint();
        } catch (SQLException e) {
            throw new DBOpException(getClass().getSimpleName() + " initialization failed: " + e.getMessage(), e);
        }
    }

    private void finalizeTransaction() {
        try {
            handleSavepoint();
            // Temporary fix for MySQL Patch task test failing, TODO remove after Auto commit is off for MySQL
            if (db.getType() == DBType.MYSQL) connection.setAutoCommit(true);
        } catch (SQLException e) {
            throw new DBOpException(getClass().getSimpleName() + " finalization failed: " + e.getMessage(), e);
        }
        if (db != null) db.returnToPool(connection);
    }

    private void handleSavepoint() throws SQLException {
        if (connection == null) {
            return;
        }
        // Commit or rollback the transaction
        if (success) {
            connection.commit();
        } else {
            connection.rollback(savepoint);
        }
    }

    protected <T> T query(Query<T> query) {
        return query.executeQuery(db);
    }

    protected boolean execute(Executable executable) {
        return executable.execute(connection);
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
        Verify.nullCheck(statements);
        for (String statement : statements) {
            try {
                execute(statement);
            } catch (DBOpException ignore) {
                /* Exceptions swallowed */
            }
        }
    }

    protected void executeOther(Transaction transaction) {
        transaction.connection = this.connection;
        transaction.performOperations();
        transaction.connection = null;
    }

    @Deprecated
    protected void setDb(SQLDB db) {
        this.db = db;
    }

    protected UUID getServerUUID() {
        return db.getServerUUIDSupplier().get();
    }
}