package main.java.com.djrapitops.plan.database.tables;

import com.djrapitops.plugin.utilities.Verify;
import com.google.common.base.Objects;
import main.java.com.djrapitops.plan.api.exceptions.DBCreateTableException;
import main.java.com.djrapitops.plan.database.Container;
import main.java.com.djrapitops.plan.database.DBUtils;
import main.java.com.djrapitops.plan.database.databases.SQLDB;
import main.java.com.djrapitops.plan.utilities.MiscUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

/**
 * @author Rsl1122
 */
public abstract class Table {

    /**
     *
     */
    protected final String tableName;

    /**
     *
     */
    protected final SQLDB db;

    /**
     *
     */
    protected final boolean usingMySQL;

    /**
     * @param name
     * @param db
     * @param usingMySQL
     */
    public Table(String name, SQLDB db, boolean usingMySQL) {
        this.tableName = name;
        this.db = db;
        this.usingMySQL = usingMySQL;
    }

    /**
     * @return
     */
    public abstract void createTable() throws DBCreateTableException;

    protected void createTable(String sql) throws DBCreateTableException {
        try {
            execute(sql);
        } catch (SQLException e) {
            throw new DBCreateTableException(tableName, "Failed to create table", e);
        }
    }

    /**
     * @return
     * @throws SQLException
     */
    protected Connection getConnection() throws SQLException {
        return db.getConnection();
    }

    /**
     * @return
     * @throws SQLException
     */
    public int getVersion() throws SQLException {
        return db.getVersion();
    }

    /**
     * @param statementString
     * @return
     * @throws SQLException
     */
    protected boolean execute(String statementString) throws SQLException {
        Statement statement = null;
        try (Connection connection = getConnection()){
            statement = connection.createStatement();
            boolean b = statement.execute(statementString);
            connection.commit();
            return b;
        } finally {
            close(statement);
        }
    }

    /**
     * Used to execute queries while possible SQLExceptions are suppressed.
     *
     * @param statements SQL statements to execute
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
     * @param toClose
     */
    protected void close(AutoCloseable... toClose) {
        MiscUtils.close(toClose);
    }

    /**
     * @return
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * @return
     */
    public void removeAllData() throws SQLException {
        execute("DELETE FROM " + tableName);
    }

    /**
     * @param <T>
     * @param objects
     * @return
     */
    protected <T> List<List<Container<T>>> splitIntoBatches(Map<Integer, List<T>> objects) {
        return DBUtils.splitIntoBatchesId(objects);
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

    @Override
    public int hashCode() {
        return Objects.hashCode(tableName, db, usingMySQL);
    }

    public SQLDB getDb() {
        return db;
    }
}