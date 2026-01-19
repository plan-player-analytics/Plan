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
package com.djrapitops.plan.storage.database.sql.building;

import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.DBType;

import java.util.Objects;
import java.util.UUID;

import static com.djrapitops.plan.storage.database.sql.building.Sql.INSERT_INTO;

public class Insert extends SqlBuilder {

    private String[] columns;
    private int rowCount = 0;

    public Insert(String table) {
        super(INSERT_INTO + table + ' ');
    }

    public static String values(String table, String... columns) {
        Insert builder = new Insert(table);
        builder.append('(');
        int size = columns.length;
        for (int i = 0; i < size; i++) {
            if (size > 1 && i > 0) {
                builder.append(',');
            }
            builder.append(columns[i]);
        }
        builder.append(") VALUES (");
        for (int i = 0; i < size; i++) {
            if (size > 1 && i > 0) {
                builder.append(',');
            }
            builder.append("?");
        }
        builder.append(')');
        return builder.toString();
    }

    @Deprecated
    public static Insert into(String table, String... columns) {
        Insert builder = new Insert(table);
        builder.append('(');
        builder.columns = columns;
        int size = columns.length;
        for (int i = 0; i < size; i++) {
            if (size > 1 && i > 0) {
                builder.append(',');
            }
            builder.append(columns[i]);
        }
        builder.append(") ");
        return builder;
    }

    private static String valueAsString(Object value) {
        String asString = Objects.toString(value);
        return value instanceof String || value instanceof UUID || value instanceof ServerUUID ? "'" + asString + "'" : asString;
    }

    /**
     * Appends values directly into the sql statement.
     * <p>
     * Should be used with care to avoid SQL injection.
     *
     * @param values Values to insert
     * @return This builder.
     */
    @Deprecated
    public Insert appendRow(DBType dbType, Object... values) {
        int size = values.length;
        if (dbType == DBType.MYSQL) {
            if (rowCount > 0) {
                append(',');
            } else {
                append("VALUES ");
            }
            append('(');
            for (int i = 0; i < size; i++) {
                if (size > 1 && i > 0) {
                    append(',');
                }
                append(valueAsString(values[i]));
            }
            append(')');
        } else {
            if (rowCount == 0) {
                append("SELECT ");
                for (int i = 0; i < size; i++) {
                    if (size > 1 && i > 0) {
                        append(',');
                    }
                    append(valueAsString(values[i]));
                    append(" AS " + columns[i]);
                }
            } else {
                append(" UNION ALL SELECT ");
                for (int i = 0; i < size; i++) {
                    if (size > 1 && i > 0) {
                        append(',');
                    }
                    append(valueAsString(values[i]));
                }
            }
        }
        rowCount++;
        return this;
    }

    public String build() {
        return toString();
    }
}
