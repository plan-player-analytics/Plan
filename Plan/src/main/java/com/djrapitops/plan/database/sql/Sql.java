package main.java.com.djrapitops.plan.database.sql;

public class Sql {
    public static final String INT = "integer";
    public static final String DOUBLE = "double";
    public static final String LONG = "bigint";
    public static final String BOOL = "boolean";

    public static String VARCHAR(int length) {
        return "varchar("+length+")";
    }
}
