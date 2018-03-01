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
