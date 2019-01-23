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
package com.djrapitops.plan.db.patches;

import com.djrapitops.plan.api.exceptions.database.DBOpException;
import com.djrapitops.plan.db.DBType;
import com.djrapitops.plan.db.SQLDB;
import com.djrapitops.plan.db.access.transactions.OperationCriticalTransaction;
import com.djrapitops.plan.db.sql.parsing.TableSqlParser;
import com.djrapitops.plan.db.sql.queries.schema.H2SchemaQueries;
import com.djrapitops.plan.db.sql.queries.schema.MySQLSchemaQueries;
import com.djrapitops.plan.db.sql.queries.schema.SQLiteSchemaQueries;
import com.djrapitops.plugin.utilities.Verify;

import java.util.List;
import java.util.UUID;

public abstract class Patch extends OperationCriticalTransaction {

    protected final SQLDB db;
    protected final DBType dbType;

    public Patch(SQLDB db) {
        setDb(db);
        this.db = db;
        this.dbType = db.getType();
    }

    public abstract boolean hasBeenApplied();

    protected abstract void applyPatch();

    @Override
    protected void execute() {
//        if (!hasBeenApplied()) { TODO Uncomment after moving patches to the execution service
        if (dbType == DBType.MYSQL) disableForeignKeyChecks();
        applyPatch();
        if (dbType == DBType.MYSQL) enableForeignKeyChecks();
//        }
    }

    @Deprecated
    public void apply() {
        db.executeTransaction(this);
    }

    private void enableForeignKeyChecks() {
        execute("SET FOREIGN_KEY_CHECKS=1");
    }

    private void disableForeignKeyChecks() {
        execute("SET FOREIGN_KEY_CHECKS=0");
    }

    protected boolean hasTable(String tableName) {
        switch (dbType) {
            case H2:
                return query(H2SchemaQueries.doesTableExist(tableName));
            case SQLITE:
                return query(SQLiteSchemaQueries.doesTableExist(tableName));
            case MYSQL:
                return query(MySQLSchemaQueries.doesTableExist(tableName));
            default:
                throw new IllegalStateException("Unsupported Database Type: " + dbType.getName());
        }
    }

    protected boolean hasColumn(String tableName, String columnName) {
        switch (dbType) {
            case H2:
                return query(H2SchemaQueries.doesColumnExist(tableName, columnName));
            case MYSQL:
                return query(MySQLSchemaQueries.doesColumnExist(tableName, columnName));
            case SQLITE:
                return query(SQLiteSchemaQueries.doesColumnExist(tableName, columnName));
            default:
                throw new IllegalStateException("Unsupported Database Type: " + dbType.getName());
        }
    }

    protected void addColumn(String tableName, String columnInfo) {
        execute("ALTER TABLE " + tableName + " ADD " + (dbType.supportsMySQLQueries() ? "" : "COLUMN ") + columnInfo);
    }

    protected void dropTable(String name) {
        execute(TableSqlParser.dropTable(name));
    }

    protected void renameTable(String from, String to) {
        execute(getRenameTableSQL(from, to));
    }

    private String getRenameTableSQL(String from, String to) {
        switch (dbType) {
            case SQLITE:
                return "ALTER TABLE " + from + " RENAME TO " + to;
            case MYSQL:
                return "RENAME TABLE " + from + " TO " + to;
            case H2:
                return "ALTER TABLE " + from + " RENAME TO " + to;
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
            execute("ALTER TABLE " + constraint.getTable() +
                    " DROP FOREIGN KEY " + constraint.getConstraintName());
        }
    }

    protected void ensureNoForeignKeyConstraints(String table) {
        if (dbType != DBType.MYSQL) {
            return;
        }

        List<MySQLSchemaQueries.ForeignKeyConstraint> constraints = query(MySQLSchemaQueries.foreignKeyConstraintsOf(table));

        Verify.isTrue(constraints.isEmpty(), () -> new DBOpException("Table '" + table + "' has constraints '" + constraints + "'"));
    }

    protected UUID getServerUUID() {
        return db.getServerUUIDSupplier().get();
    }
}
