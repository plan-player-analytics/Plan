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
import com.djrapitops.plan.db.access.QueryAllStatement;
import com.djrapitops.plan.db.access.QueryStatement;
import com.djrapitops.plan.db.access.transactions.Transaction;
import com.djrapitops.plan.db.sql.parsing.TableSqlParser;
import com.djrapitops.plan.db.sql.queries.H2SchemaQueries;
import com.djrapitops.plan.db.sql.queries.MySQLSchemaQueries;
import com.djrapitops.plan.db.sql.queries.SQLiteSchemaQueries;
import com.djrapitops.plan.system.settings.paths.DatabaseSettings;
import com.djrapitops.plugin.utilities.Verify;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public abstract class Patch extends Transaction {

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
                return query(MySQLSchemaQueries.doesTableExist(db.getConfig().get(DatabaseSettings.MYSQL_DATABASE), tableName));
            default:
                throw new IllegalStateException("Unsupported Database Type: " + dbType.getName());
        }
    }

    protected boolean hasColumn(String tableName, String columnName) {
        if (dbType.supportsMySQLQueries()) {
            String query;

            if (dbType == DBType.H2) {
                query = "SELECT * FROM INFORMATION_SCHEMA.COLUMNS" +
                        " WHERE TABLE_NAME=? AND COLUMN_NAME=?";
            } else {
                query = "SELECT * FROM information_schema.COLUMNS" +
                        " WHERE TABLE_NAME=? AND COLUMN_NAME=? AND TABLE_SCHEMA=?";
            }

            return query(new QueryStatement<Boolean>(query) {
                @Override
                public void prepare(PreparedStatement statement) throws SQLException {
                    statement.setString(1, tableName);
                    statement.setString(2, columnName);
                    if (dbType != DBType.H2) {
                        statement.setString(3, db.getConfig().get(DatabaseSettings.MYSQL_DATABASE));
                    }
                }

                @Override
                public Boolean processResults(ResultSet set) throws SQLException {
                    return set.next();
                }
            });
        } else {
            return query(new QueryAllStatement<Boolean>("PRAGMA table_info(" + tableName + ")") {
                @Override
                public Boolean processResults(ResultSet set) throws SQLException {
                    while (set.next()) {
                        if (columnName.equals(set.getString("name"))) {
                            return true;
                        }
                    }
                    return false;
                }
            });
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

        String schema = db.getConfig().get(DatabaseSettings.MYSQL_DATABASE);
        List<MySQLSchemaQueries.ForeignKeyConstraint> constraints = query(MySQLSchemaQueries.foreignKeyConstraintsOf(schema, referencedTable));

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

        String schema = db.getConfig().get(DatabaseSettings.MYSQL_DATABASE);
        List<MySQLSchemaQueries.ForeignKeyConstraint> constraints = query(MySQLSchemaQueries.foreignKeyConstraintsOf(schema, table));

        Verify.isTrue(constraints.isEmpty(), () -> new DBOpException("Table '" + table + "' has constraints '" + constraints + "'"));
    }

    protected UUID getServerUUID() {
        return db.getServerUUIDSupplier().get();
    }
}
