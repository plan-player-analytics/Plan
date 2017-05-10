package main.java.com.djrapitops.plan.data.additional;

/**
 *
 * @author Rsl1122
 */
public enum AnalysisType {
    INT_AVG("Average "), LONG_AVG("Average "), DOUBLE_AVG("Average "),
    INT_TOTAL, LONG_TOTAL, DOUBLE_TOTAL,
    LONG_TIME_MS_AVG, LONG_TIME_MS_TOTAL, LONG_EPOCH_MS_MINUS_NOW_TOTAL,
    BOOLEAN_PERCENTAGE, BOOLEAN_TOTAL,
    HTML, TOTAL_VALUE;

    private final String modifier;

    private AnalysisType(String modifier) {
        this.modifier = modifier;
    }

    private AnalysisType() {
        this.modifier = "";
    }

    public String getModifier() {
        return modifier;
    }
}
