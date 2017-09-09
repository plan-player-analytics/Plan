package main.java.com.djrapitops.plan.data.additional;

/**
 * This class contains Enum values for different types of Analysis that can be
 * performed on values of PluginData.
 * <p>
 * The enum determines what should be done to the return value of
 * PluginData.getValue() method when the analysis is run.
 * <p>
 * Refer to the documentation on GitHub for additional information.
 *
 * @author Rsl1122
 * @since 3.1.0
 */
public enum AnalysisType {

    /**
     * Used when the getValue() method returns an integer and average should be
     * calculated.
     * <p>
     * -1 values will be disregarded from the calculation (size will not grow).
     */
    INT_AVG("_avgInt", "Average "),
    /**
     * Used when the getValue() method returns a long and average should be
     * calculated.
     * <p>
     * -1 values will be disregarded from the calculation (size will not grow).
     */
    LONG_AVG("_avgLong", "Average "),
    /**
     * Used when the getValue() method returns double and average should be
     * calculated.
     * <p>
     * -1 values will be disregarded from the calculation (size will not grow).
     */
    DOUBLE_AVG("_avgDouble", "Average "),
    /**
     * Used when the getValue() method returns an integer and total should be
     * calculated.
     * <p>
     * -1 values will be disregarded from the calculation (size will not grow).
     */
    INT_TOTAL("_totalInt", "Total "),
    /**
     * Used when the getValue() method returns a long and total should be
     * calculated.
     * <p>
     * -1 values will be disregarded from the calculation (size will not grow).
     */
    LONG_TOTAL("_totalLong", "Total "),
    /**
     * Used when the getValue() method returns a double and total should be
     * calculated.
     * <p>
     * -1 values will be disregarded from the calculation (size will not grow).
     */
    DOUBLE_TOTAL("_totalDouble", "Total "),
    /**
     * Used when the getValue() method returns an amount of milliseconds as long
     * and average should be calculated.
     * <p>
     * -1 values will be disregarded from the calculation (size will not grow).
     */
    LONG_TIME_MS_AVG("_avgTimeMs", "Average "),
    /**
     * Used when the getValue() method returns an amount of milliseconds as long
     * and total should be calculated.
     * <p>
     * -1 values will be disregarded from the calculation (size will not grow).
     */
    LONG_TIME_MS_TOTAL("_totalTimeMs"),
    /**
     * Used when the getValue() method returns an Epoch Millisecond as long and
     * average of differences between the millisecond and current millisecond
     * should be calculated.
     * <p>
     * For example if a player has dropped a Foo on epoch ms 1494486504000 and
     * that was 5s (5000ms) ago. Now you want to calculate the average
     * time-since for all players. Then you use this one.
     * <p>
     * -1 values will be disregarded from the calculation (size will not grow).
     */
    LONG_EPOCH_MS_MINUS_NOW_AVG("_avgEpochMsMinusNow", "Average "),
    /**
     * Used to calculate %-true for the returned boolean values of getValue().
     */
    BOOLEAN_PERCENTAGE("_perchBool", "Percentage "),
    /**
     * Used to calculate number of true values for the returned boolean values
     * of getValue().
     * <p>
     * Will be presented as "n / total".
     */
    BOOLEAN_TOTAL("_totalBool"),
    /**
     * Used to add html tags to the plugins tab.
     * <p>
     * Can be used to add Tables, Images (for example maps) and other html
     * elements.
     */
    HTML;

    private final String modifier;
    private final String placeholderModifier;

    AnalysisType(String placeholderModifier, String modifier) {
        this.placeholderModifier = placeholderModifier;
        this.modifier = modifier;
    }

    AnalysisType(String placeholderModifier) {
        this.placeholderModifier = placeholderModifier;
        this.modifier = "";
    }

    AnalysisType() {
        this.placeholderModifier = "";
        this.modifier = "";
    }

    /**
     * Used to get the modifier for the Prefix of the value.
     * <p>
     * For example: "Average Votes" when INT_AVG is used and Prefix is set as
     * "Votes".
     *
     * @return Modifier, can be empty.
     */
    public String getModifier() {
        return modifier;
    }

    /**
     * Used to get the Placeholder modifier.
     *
     * @return for example "_total"
     */
    public String getPlaceholderModifier() {
        return placeholderModifier;
    }
}
