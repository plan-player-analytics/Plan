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
package com.djrapitops.plan.system.database.databases.sql.statements;

import java.util.Arrays;

public class Select extends WhereParser {

    public Select(String start) {
        super(start);
    }

    public static Select from(String table, Column... columns) {
        String[] cols = Arrays.stream(columns).map(Column::get).toArray(String[]::new);
        return from(table, cols);
    }

    public static Select from(String table, String... columns) {
        Select parser = new Select("SELECT ");
        int size = columns.length;
        for (int i = 0; i < size; i++) {
            if (size > 1 && i > 0) {
                parser.append(", ");
            }
            parser.append(columns[i]);
        }

        parser.append(" FROM ").append(table);
        return parser;
    }

    public static Select all(String table) {
        return new Select("SELECT * FROM " + table);
    }
}
