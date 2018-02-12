/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.database.databases.sql.statements;

/**
 * @author Fuzzlemann
 */
public class Update extends WhereParser {

    public Update(String table) {
        super("UPDATE " + table + " SET");
        addSpace();
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
