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
package com.djrapitops.plan.storage.database.transactions.patches;

import com.djrapitops.plan.exceptions.database.DBOpException;
import com.djrapitops.plan.storage.database.DBType;
import com.djrapitops.plan.storage.database.queries.QueryStatement;
import com.djrapitops.plan.storage.database.queries.schema.MySQLSchemaQueries;
import com.djrapitops.plan.storage.database.queries.schema.SQLiteSchemaQueries;
import com.djrapitops.plan.storage.database.transactions.init.OperationCriticalTransaction;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

public abstract class Patch extends OperationCriticalTransaction {

    private static final String ALTER_TABLE = "ALTER TABLE ";
    private boolean appliedPreviously = false;
    private boolean appliedNow = false;

    public abstract boolean hasBeenApplied();

    protected abstract void applyPatch();

    public boolean isApplied() {
        if (!success) throw new IllegalStateException("Asked a Patch if it is applied before it was executed!");
        return appliedPreviously || appliedNow;
    }

    public boolean wasApplied() {
        return appliedNow;
    }

    @Override
    protected boolean shouldBeExecuted() {
        boolean hasBeenApplied = hasBeenApplied();
        if (hasBeenApplied) appliedPreviously = true;
        return !hasBeenApplied;
    }

    @Override
    protected void performOperations() {
        if (dbType == DBType.MYSQL) disableForeignKeyChecks();
        applyPatch();
        appliedNow = true;
        if (dbType == DBType.MYSQL) enableForeignKeyChecks();
    }

    private void enableForeignKeyChecks() {
        execute("SET FOREIGN_KEY_CHECKS=1");
    }

    private void disableForeignKeyChecks() {
        execute("SET FOREIGN_KEY_CHECKS=0");
    }

    protected boolean hasColumn(String tableName, String columnName) {
        switch (dbType) {
            case MYSQL:
                return query(MySQLSchemaQueries.doesColumnExist(tableName, columnName));
            case SQLITE:
                return query(SQLiteSchemaQueries.doesColumnExist(tableName, columnName));
            default:
                throw new IllegalStateException("Unsupported Database Type: " + dbType.getName());
        }
    }

    protected void addColumn(String tableName, String columnInfo) {
        execute(ALTER_TABLE + tableName + " ADD " + (dbType.supportsMySQLQueries() ? "" : "COLUMN ") + columnInfo);
    }

    protected void dropColumn(String tableName, String columnName) {
        execute(ALTER_TABLE + tableName + " DROP COLUMN " + columnName);
    }

    protected void dropTable(String name) {
        execute("DROP TABLE IF EXISTS " + name);
    }

    protected void renameTable(String from, String to) {
        execute(getRenameTableSQL(from, to));
    }

    private String getRenameTableSQL(String from, String to) {
        switch (dbType) {
            case SQLITE:
                return ALTER_TABLE + from + " RENAME TO " + to;
            case MYSQL:
                return "RENAME TABLE " + from + " TO " + to;
            default:
                throw new IllegalArgumentException("DBType: " + dbType.getName() + " does not have rename table sql");
        }
    }

    protected void dropForeignKeys(String referencedTable) {
        if (dbType != DBType.MYSQL) {
            return;
        }

        List<MySQLSchemaQueries.ForeignKeyConstraint> constraints = query(MySQLSchemaQueries.foreignKeyConstraintsOf(referencedTable));

        for (MySQLSchemaQueries.ForeignKeyConstraint constraint : constraints) {
            // Uses information from https://stackoverflow.com/a/34574758
            execute(ALTER_TABLE + constraint.getTable() +
                    " DROP FOREIGN KEY " + constraint.getConstraintName());
        }
    }

    protected void ensureNoForeignKeyConstraints(String table) {
        if (dbType != DBType.MYSQL) {
            return;
        }

        List<MySQLSchemaQueries.ForeignKeyConstraint> constraints = query(MySQLSchemaQueries.foreignKeyConstraintsOf(table));

        if (constraints != null && !constraints.isEmpty()) {
            throw new DBOpException("Table '" + table + "' has constraints '" + constraints + "'");
        }
    }

    protected boolean allValuesHaveValueZero(String tableName, String column) {
        String sql = SELECT + '*' + FROM + tableName + WHERE + column + "=? LIMIT 1";
        return query(new QueryStatement<>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setInt(1, 0);
            }

            @Override
            public Boolean processResults(ResultSet set) throws SQLException {
                return !set.next();
            }
        });
    }

    protected int columnVarcharLength(String table, String column) {
        return dbType == DBType.SQLITE ? Integer.MAX_VALUE : query(MySQLSchemaQueries.columnVarcharLength(table, column));
    }
}
