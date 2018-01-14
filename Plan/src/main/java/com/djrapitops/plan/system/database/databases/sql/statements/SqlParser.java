package com.djrapitops.plan.system.database.databases.sql.statements;

/**
 * Class for parsing different SQL strings.
 *
 * @author Rsl1122
 * @since 3.7.0
 */
public class SqlParser {

    private final StringBuilder s;

    public SqlParser() {
        s = new StringBuilder();
    }

    public SqlParser(String start) {
        s = new StringBuilder(start);
    }

    public SqlParser addSpace() {
        s.append(" ");
        return this;
    }

    public SqlParser append(String string) {
        s.append(string);
        return this;
    }

    @Override
    public String toString() {
        return s.toString();
    }
}
