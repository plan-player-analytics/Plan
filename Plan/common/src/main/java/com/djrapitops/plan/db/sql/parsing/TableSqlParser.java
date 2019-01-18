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
package com.djrapitops.plan.db.sql.parsing;

/**
 * SqlParser Class for parsing table creation, removal and modification statements.
 *
 * @author Rsl1122
 */
public class TableSqlParser extends SqlParser {

    private int columns = 0;

    public TableSqlParser(String start) {
        super(start);
    }

    public static TableSqlParser createTable(String tableName) {
        return new TableSqlParser("CREATE TABLE IF NOT EXISTS " + tableName + " (");
    }

    public static String dropTable(String tableName) {
        return "DROP TABLE IF EXISTS " + tableName;
    }

    /**
     * Used for ALTER TABLE sql statements.
     *
     * @param column column to modify
     * @return TableSqlParser object
     */
    public static TableSqlParser newColumn(String column, String type) {
        return new TableSqlParser("").column(column, type);
    }

    public TableSqlParser column(Column column, String type) {
        return column(column.get(), type);
    }

    public TableSqlParser column(String column, String type) {
        if (columns > 0) {
            append(", ");
        }
        append(column).addSpace();
        append(type);

        columns++;
        return this;
    }

    public TableSqlParser foreignKey(Column column, String refrencedTable, Column referencedColumn) {
        return foreignKey(column.get(), refrencedTable, referencedColumn.get());
    }

    public TableSqlParser foreignKey(String column, String refrencedTable, String referencedColumn) {
        if (columns > 0) {
            append(", ");
        }
        append("FOREIGN KEY(")
                .append(column)
                .append(") REFERENCES ")
                .append(refrencedTable)
                .append("(")
                .append(referencedColumn)
                .append(")");
        columns++;
        return this;
    }

    public TableSqlParser notNull() {
        addSpace();
        append("NOT NULL");
        return this;
    }

    public TableSqlParser unique() {
        addSpace();
        append("UNIQUE");
        return this;
    }

    public TableSqlParser defaultValue(boolean value) {
        return defaultValue(value ? "1" : "0");
    }

    public TableSqlParser defaultValue(String value) {
        addSpace();
        append("DEFAULT ").append(value);
        return this;
    }

    public TableSqlParser primaryKeyIDColumn(boolean supportsMySQLQueries, Column column) {
        return primaryKeyIDColumn(supportsMySQLQueries, column.get());
    }

    public TableSqlParser primaryKeyIDColumn(boolean supportsMySQLQueries, String column) {
        if (columns > 0) {
            append(", ");
        }
        append(column).addSpace();
        append(Sql.INT).addSpace();
        append((supportsMySQLQueries) ? "NOT NULL AUTO_INCREMENT" : "PRIMARY KEY");
        columns++;
        return this;
    }

    public TableSqlParser primaryKey(boolean supportsMySQLQueries, Column column) {
        return primaryKey(supportsMySQLQueries, column.get());
    }

    public TableSqlParser primaryKey(boolean supportsMySQLQueries, String column) {
        if (supportsMySQLQueries) {
            if (columns > 0) {
                append(", ");
            }
            append("PRIMARY KEY (").append(column).append(")");
            columns++;
        }
        return this;
    }

    public TableSqlParser charSetUTF8(boolean mySQL) {
        if (mySQL) {
            addSpace();
            append("CHARACTER SET utf8 COLLATE utf8mb4_general_ci");
        }
        return this;
    }

    @Override
    public String toString() {
        append(")");
        return super.toString();
    }
}
