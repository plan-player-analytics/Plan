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

import com.djrapitops.plan.db.DBType;
import com.djrapitops.plugin.utilities.Verify;

/**
 * SqlParser Class for parsing table creation, removal and modification statements.
 *
 * @author Rsl1122
 */
public class CreateTableParser {

    private final DBType dbType;

    private final StringBuilder columns;
    private final StringBuilder keyConstraints;

    private StringBuilder columnBuilder;
    private int columnCount = 0;
    private int constraintCount = 0;

    private CreateTableParser(DBType dbType, String tableName) {
        this.dbType = dbType;
        columns = new StringBuilder("CREATE TABLE IF NOT EXISTS " + tableName + " (");
        keyConstraints = new StringBuilder();
    }

    public static CreateTableParser create(String tableName, DBType type) {
        return new CreateTableParser(type, tableName);
    }

    private void finalizeColumn() {
        if (columnBuilder != null) {
            if (columnCount > 0) {
                columns.append(',');
            }
            columns.append(columnBuilder.toString());
            columnCount++;
            columnBuilder = null;
        }
    }

    public CreateTableParser column(String column, String type) {
        finalizeColumn();
        columnBuilder = new StringBuilder();
        columnBuilder.append(column).append(" ").append(type);
        return this;
    }

    public CreateTableParser primaryKey() {
        String currentColumn = columnBuilder.substring(0, columnBuilder.indexOf(" "));
        if (dbType.supportsMySQLQueries()) {
            notNull();
            columnBuilder.append(" AUTO_INCREMENT");
            primaryKey(currentColumn);
        } else {
            columnBuilder.append(" PRIMARY KEY");
        }
        return this;
    }

    public CreateTableParser notNull() {
        columnBuilder.append(" NOT NULL");
        return this;
    }

    public CreateTableParser unique() {
        columnBuilder.append(" UNIQUE");
        return this;
    }

    public CreateTableParser defaultValue(boolean value) {
        return defaultValue(value ? "1" : "0");
    }

    public CreateTableParser defaultValue(String value) {
        columnBuilder.append(" DEFAULT ").append(value);
        return this;
    }

    public CreateTableParser foreignKey(String column, String referencedTable, String referencedColumn) {
        finalizeColumn();
        if (constraintCount > 0) {
            keyConstraints.append(',');
        }
        keyConstraints.append("FOREIGN KEY(")
                .append(column)
                .append(") REFERENCES ")
                .append(referencedTable)
                .append('(')
                .append(referencedColumn)
                .append(')');
        constraintCount++;
        return this;
    }

    private void primaryKey(String column) {
        finalizeColumn();
        if (constraintCount > 0) {
            keyConstraints.append(',');
        }
        keyConstraints.append("PRIMARY KEY (").append(column).append(')');
        constraintCount++;
    }

    public String build() {
        return toString();
    }

    @Override
    public String toString() {
        finalizeColumn();

        Verify.isTrue(columnCount > 0, () -> new IllegalStateException("No columns specified for statement '" + columns.toString() + "..'"));
        if (constraintCount > 0) {
            return columns.toString() + ',' + keyConstraints.toString() + ')';
        } else {
            return columns.toString() + ')';
        }
    }
}
