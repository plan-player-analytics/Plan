/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.database.sql;

/**
 * @author Fuzzlemann
 */
public abstract class WhereParser extends SqlParser {

    public WhereParser() {
        super();
    }

    public WhereParser(String start) {
        super(start);
    }

    private int conditions = 0;

    public WhereParser where(String... conditions) {
        return and(conditions);
    }

    public WhereParser and(String... conditions) {
        return whereOperator("AND", conditions);
    }
    
    public WhereParser or(String... conditions) {
        return whereOperator("OR", conditions);
    }
    
    private WhereParser whereOperator(String operator, String... conditions) {
        append(" WHERE ");
        for (String condition : conditions) {
            if (this.conditions > 0) {
                addSpace().append(operator).addSpace();
            }

            append("(").append(condition).append(")");
            this.conditions++;
        }

        return this;
    }
}
