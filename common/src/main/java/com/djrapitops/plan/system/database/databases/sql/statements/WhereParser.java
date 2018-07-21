/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.database.databases.sql.statements;

/**
 * @author Fuzzlemann
 */
public abstract class WhereParser extends SqlParser {

    private int conditions = 0;

    public WhereParser() {
        super();
    }

    public WhereParser(String start) {
        super(start);
    }

    public WhereParser where(String... conditions) {
        append(" WHERE ");
        for (String condition : conditions) {
            if (this.conditions > 0) {
                append(" AND ");
            }
            append("(").append(condition).append(")");
            this.conditions++;
        }

        return this;
    }

    public WhereParser and(String condition) {
        append(" AND ");
        append("(").append(condition).append(")");
        this.conditions++;
        return this;
    }

    public WhereParser or(String condition) {
        append(" OR ");
        append("(").append(condition).append(")");
        this.conditions++;
        return this;
    }
}
