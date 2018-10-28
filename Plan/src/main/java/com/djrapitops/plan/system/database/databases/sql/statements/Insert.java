/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the LGNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  LGNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.system.database.databases.sql.statements;

import java.util.Arrays;

public class Insert extends SqlParser {

    public Insert(String table) {
        super("INSERT INTO " + table);
        addSpace();
    }

    public static String values(String table, Column... columns) {
        String[] cols = Arrays.stream(columns).map(Column::get).toArray(String[]::new);
        return values(table, cols);
    }

    public static String values(String table, String... columns) {
        Insert parser = new Insert(table);
        parser.append("(");
        int size = columns.length;
        for (int i = 0; i < size; i++) {
            if (size > 1 && i > 0) {
                parser.append(", ");
            }
            parser.append(columns[i]);
        }
        parser.append(") VALUES (");
        for (int i = 0; i < size; i++) {
            if (size > 1 && i > 0) {
                parser.append(", ");
            }
            parser.append("?");
        }
        parser.append(")");
        return parser.toString();
    }
}
