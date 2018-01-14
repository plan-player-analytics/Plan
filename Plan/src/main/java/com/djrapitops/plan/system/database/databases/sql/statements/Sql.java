package com.djrapitops.plan.system.database.databases.sql.statements;

public class Sql {
    public static final String INT = "integer";
    public static final String DOUBLE = "double";
    public static final String LONG = "bigint";
    public static final String BOOL = "boolean";

    private Sql() {
        throw new IllegalStateException("Variable Class");
    }

    public static String varchar(int length) {
        return "varchar(" + length + ")";
    }
}
