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

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

public class Select extends WhereBuilder {

    public static final String COLUMN_TABLE_NAME = "table_name";
    public static final String COLUMN_COUNT = "count";
    public static final String COLUMN_MIN_ID = "min_id";

    public Select(String start) {
        super(start);
    }

    public static Select from(String table, String... columns) {
        Select builder = new Select(SELECT);
        int size = columns.length;
        for (int i = 0; i < size; i++) {
            if (size > 1 && i > 0) {
                builder.append(',');
            }
            builder.append(columns[i]);
        }

        builder.append(FROM).append(table);
        return builder;
    }

    public static Select all(String table) {
        return new Select(SELECT + '*' + FROM + table);
    }

    public static String counts(String... tables) {
        Select builder = new Select("");
        int size = tables.length;
        for (int i = 0; i < size; i++) {
            if (size > 1 && i > 0) {
                builder.append(UNION);
            }
            builder.append(SELECT + "'" + tables[i] + "' as " + COLUMN_TABLE_NAME + ", COUNT(*) as " + COLUMN_COUNT + FROM + tables[i]);
        }
        return builder.toString();
    }

    public static String minIds(String... tables) {
        Select builder = new Select("");
        int size = tables.length;
        for (int i = 0; i < size; i++) {
            if (size > 1 && i > 0) {
                builder.append(UNION);
            }
            builder.append(SELECT + "'" + tables[i] + "' as " + COLUMN_TABLE_NAME + ", MIN(" + tables[i] + ".id) as " + COLUMN_MIN_ID + FROM + tables[i]);
        }
        return builder.toString();
    }
}
