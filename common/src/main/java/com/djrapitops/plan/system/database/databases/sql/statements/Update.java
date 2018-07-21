/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.database.databases.sql.statements;

import java.util.Arrays;

/**
 * @author Fuzzlemann
 */
public class Update extends WhereParser {

    public Update(String table) {
        super("UPDATE " + table + " SET");
        addSpace();
    }

    public static Update values(String table, Column... values) {
        String[] cols = Arrays.stream(values).map(Column::get).toArray(String[]::new);
        return values(table, cols);
    }

    public static Update values(String table, String... values) {
        Update parser = new Update(table);

        int size = values.length;
        for (int i = 0; i < size; i++) {
            if (size > 1 && i > 0) {
                parser.append(", ");
            }
            parser.append(values[i] + "=?");
        }

        return parser;
    }
}
