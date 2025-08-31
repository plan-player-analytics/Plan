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

import java.util.Objects;

public class Insert extends SqlBuilder {

    public Insert(String table) {
        super("INSERT INTO " + table + ' ');
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

    public static Insert into(String table, String... columns) {
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
        return builder;
    }

    /**
     * Appends values directly into the sql statement.
     * <p>
     * Should be used with care to avoid SQL injection.
     *
     * @param values Values to insert
     * @return This builder.
     */
    public Insert appendRow(Object... values) {
        int size = values.length;
        append('(');
        for (int i = 0; i < size; i++) {
            if (size > 1 && i > 0) {
                append(',');
            }
            append(Objects.toString(values[i]));
        }
        append(')');
        return this;
    }

    public String build() {
        return append(")").toString();
    }
}
