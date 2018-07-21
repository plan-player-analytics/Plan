package com.djrapitops.plan.system.database.databases.sql.tables;

import com.djrapitops.plan.api.exceptions.database.DBInitException;
import com.djrapitops.plan.api.exceptions.database.DBOpException;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;
import com.djrapitops.plan.system.database.databases.sql.processing.ExecStatement;
import com.djrapitops.plan.system.database.databases.sql.processing.QueryStatement;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.utilities.MiscUtils;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.utilities.Verify;
import com.google.common.base.Objects;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Abstract representation of a SQL database table.
 *
 * @author Rsl1122
 */
public abstract class Table {

    protected final String tableName;
    protected final SQLDB db;
    protected final boolean usingMySQL;

    /**
     * Constructor.
     *
     * @param name Name of the table in the db.
     * @param db   Database to use.
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
        } catch (DBOpException e) {
            throw new DBInitException("Failed to create table: " + tableName, e);
        }
    }

    protected void renameTable(String to) {
        String sql = usingMySQL ?
                "RENAME TABLE " + tableName + " TO " + to :
                "ALTER TABLE " + tableName + " RENAME TO " + to;
        execute(sql);
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
     * Get the Database Schema version from VersionTable.
     *
     * @return Database Schema version.
     * @deprecated Use db.getVersion - db is protected variable.
     */
    @Deprecated
    public int getVersion() {
        return db.getVersion();
    }

    /**
     * Executes an SQL Statement
     *
     * @param statementString Statement to execute in the database.
     * @return true if rows were updated.
     */
    protected boolean execute(String statementString) {
        return execute(new ExecStatement(statementString) {
            @Override
            public void prepare(PreparedStatement statement) {
                /* No preparations necessary */
            }
        });
    }

    /**
     * Used to execute statements while possible exceptions are suppressed.
     *
     * @param statements SQL statements to setUp
     */
    protected void executeUnsafe(String... statements) {
        Verify.nullCheck(statements);
        for (String statement : statements) {
            try {
                execute(statement);
            } catch (DBOpException e) {
                if (Settings.DEV_MODE.isTrue()) {
                    Log.toLog(this.getClass(), e);
                }
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
    public void removeAllData() {
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

    /**
     * @deprecated Use db.commit - db is a protected variable.
     */
    @Deprecated
    protected void commit(Connection connection) {
        db.commit(connection);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(tableName, db, usingMySQL);
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