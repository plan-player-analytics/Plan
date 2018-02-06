package com.djrapitops.plan.system.database.databases.sql.tables;

import com.djrapitops.plan.api.exceptions.database.DBInitException;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;
import com.djrapitops.plan.system.database.databases.sql.processing.ExecStatement;
import com.djrapitops.plan.system.database.databases.sql.processing.QueryStatement;
import com.djrapitops.plan.utilities.MiscUtils;
import com.djrapitops.plugin.utilities.Verify;
import com.google.common.base.Objects;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author Rsl1122
 */
public abstract class Table {

    protected final String tableName;
    protected final SQLDB db;
    protected final boolean usingMySQL;

    /**
     * Constructor.
     *
     * @param name       Name of the table in the db.
     * @param db         Database to use.
     */
    public Table(String name, SQLDB db) {
        this.tableName = name;
        this.db = db;
        this.usingMySQL = db != null && db.isUsingMySQL();
    }

    public abstract void createTable() throws DBInitException;

    protected void createTable(String sql) throws DBInitException {
        try {
            execute(sql);
        } catch (SQLException e) {
            throw new DBInitException("Failed to create table: " + tableName, e);
        }
    }

    /**
     * Used to get a new Connection to the Database.
     *
     * @return SQL Connection
     * @throws SQLException DB Error
     */
    protected Connection getConnection() throws SQLException {
        return db.getConnection();
    }

    /**
     * Get the Database Schema version from VersionTable.
     *
     * @return Database Schema version.
     * @throws SQLException DB Error
     */
    public int getVersion() throws SQLException {
        return db.getVersion();
    }

    /**
     * Executes an SQL Statement
     *
     * @param statementString Statement to setUp
     * @return What setUp returns.
     * @throws SQLException DB error
     */
    protected boolean execute(String statementString) throws SQLException {
        Statement statement = null;
        Connection connection = null;
        try {
            connection = getConnection();
            statement = connection.createStatement();
            boolean b = statement.execute(statementString);
//            Log.debug("Execute: " + statementString);
            commit(connection);
            return b;
        } finally {
            close(statement);
            db.returnToPool(connection);
        }
    }

    /**
     * Used to setUp queries while possible SQLExceptions are suppressed.
     *
     * @param statements SQL statements to setUp
     */
    protected void executeUnsafe(String... statements) {
        Verify.nullCheck(statements);
        for (String statement : statements) {
            try {
                execute(statement);
            } catch (SQLException ignored) {
                /* Ignored */
            }
        }
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

    /**
     * Removes all data from the table.
     */
    public void removeAllData() throws SQLException {
        execute("DELETE FROM " + tableName);
    }

    protected void addColumns(String... columnInfo) {
        for (int i = 0; i < columnInfo.length; i++) {
            columnInfo[i] = "ALTER TABLE " + tableName + " ADD " + (usingMySQL ? "" : "COLUMN ") + columnInfo[i];
        }
        executeUnsafe(columnInfo);
    }

    protected void removeColumns(String... columnNames) {
        if (usingMySQL) {
            StringBuilder sqlBuild = new StringBuilder();
            sqlBuild.append("ALTER TABLE ").append(tableName);
            for (int i = 0; i < columnNames.length; i++) {
                sqlBuild.append(" DROP COLUMN ").append(columnNames[i]);
                if (i < columnNames.length - 1) {
                    sqlBuild.append(",");
                }
            }
            executeUnsafe(sqlBuild.toString());
        }
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
        return usingMySQL == table.usingMySQL &&
                Objects.equal(tableName, table.tableName) &&
                Objects.equal(db, table.db);
    }

    protected void commit(Connection connection) throws SQLException {
        db.commit(connection);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(tableName, db, usingMySQL);
    }

    public SQLDB getDb() {
        return db;
    }

    protected boolean execute(ExecStatement statement) throws SQLException {
        boolean updatedSomething;
        Connection connection = null;
        try {
            connection = getConnection();
            updatedSomething = statement.execute(connection.prepareStatement(statement.getSql()));
            commit(connection);
        } finally {
            db.returnToPool(connection);
        }
        return updatedSomething;
    }

    protected void executeBatch(ExecStatement statement) throws SQLException {
        Connection connection = null;
        try {
            connection = getConnection();
            statement.executeBatch(connection.prepareStatement(statement.getSql()));
            commit(connection);
        } finally {
            db.returnToPool(connection);
        }
    }

    protected <T> T query(QueryStatement<T> statement) throws SQLException {
        Connection connection = null;
        try {
            connection = getConnection();
            return statement.executeQuery(connection.prepareStatement(statement.getSql()));
        } finally {
            db.returnToPool(connection);
        }
    }
}