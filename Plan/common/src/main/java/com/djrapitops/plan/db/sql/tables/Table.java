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
package com.djrapitops.plan.db.sql.tables;

import com.djrapitops.plan.db.SQLDB;
import com.djrapitops.plan.db.access.ExecStatement;
import com.djrapitops.plan.db.access.QueryStatement;
import com.djrapitops.plan.utilities.MiscUtils;
import com.google.common.base.Objects;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Abstract representation of a SQL database table.
 *
 * @author Rsl1122
 */
public abstract class Table {

    protected final String tableName;
    protected final SQLDB db;
    protected final boolean supportsMySQLQueries;

    /**
     * Constructor.
     *
     * @param name Name of the table in the db.
     * @param db   Database to use.
     */
    public Table(String name, SQLDB db) {
        this.tableName = name;
        this.db = db;
        this.supportsMySQLQueries = db != null && db.getType().supportsMySQLQueries();
    }

    protected UUID getServerUUID() {
        return db.getServerUUIDSupplier().get();
    }

    /**
     * Used to get a new Connection to the Database.
     *
     * @return SQL Connection
     * @throws SQLException DB Error
     * @deprecated Use db.getConnection - db is protected variable.
     */
    @Deprecated
    protected Connection getConnection() throws SQLException {
        return db.getConnection();
    }

    /**
     * Executes an SQL Statement
     *
     * @param statementString Statement to execute in the database.
     * @return true if rows were updated.
     * @deprecated Use {@code db.execute(statements)}
     */
    @Deprecated
    protected boolean execute(String statementString) {
        return db.execute(statementString);
    }

    /**
     * Used to execute statements while possible exceptions are suppressed.
     *
     * @param statements SQL statements to setUp
     * @deprecated Use {@code db.executeUnsafe(statements)}
     */
    @Deprecated
    protected void executeUnsafe(String... statements) {
        db.executeUnsafe(statements);
    }

    /**
     * Closes DB elements.
     *
     * @param toClose All elements to close.
     */
    protected void close(AutoCloseable... toClose) {
        MiscUtils.close(toClose);
    }

    public String getTableName() {
        return tableName;
    }

    @Override
    public String toString() {
        return tableName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Table table = (Table) o;
        return supportsMySQLQueries == table.supportsMySQLQueries &&
                Objects.equal(tableName, table.tableName) &&
                Objects.equal(db, table.db);
    }

    /**
     * @deprecated Use db.commit - db is a protected variable.
     */
    @Deprecated
    protected void commit(Connection connection) {
        db.commit(connection);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(tableName, db, supportsMySQLQueries);
    }

    protected boolean execute(ExecStatement statement) {
        return db.execute(statement);
    }

    protected void executeBatch(ExecStatement statement) {
        db.executeBatch(statement);
    }

    protected <T> T query(QueryStatement<T> statement) {
        return db.query(statement);
    }
}