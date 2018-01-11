package com.djrapitops.plan.database.sql;

public class Insert extends SqlParser {

    public Insert(String table) {
        super("INSERT INTO " + table);
        addSpace();
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
