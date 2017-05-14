package main.java.com.djrapitops.plan.data.additional;

import main.java.com.djrapitops.plan.data.additional.advancedachievements.AdvancedAchievementsTable;
import main.java.com.djrapitops.plan.data.additional.factions.FactionsTable;
import main.java.com.djrapitops.plan.data.additional.towny.TownyTable;

/**
 * This class contains Enum values for different types of Analysis that can be
 * performed on values of PluginData.
 *
 * The enum determines what should be done to the return value of
 * PluginData.getValue() method when the analysis is run.
 *
 * Refer to the documentation on github for additional information.
 *
 * @author Rsl1122
 * @since 3.1.0
 */
public enum AnalysisType {

    /**
     * Used when the getValue() method returns an integer and average should be
     * calculated.
     *
     * -1 values will be disregarded from the calculation (size will not grow).
     */
    INT_AVG("avgInt_", "Average "),
    /**
     * Used when the getValue() method returns a long and average should be
     * calculated.
     *
     * -1 values will be disregarded from the calculation (size will not grow).
     */
    LONG_AVG("avgLong_", "Average "),
    /**
     * Used when the getValue() method returns double and average should be
     * calculated.
     *
     * -1 values will be disregarded from the calculation (size will not grow).
     */
    DOUBLE_AVG("avgDouble_", "Average "),
    /**
     * Used when the getValue() method returns an integer and total should be
     * calculated.
     *
     * -1 values will be disregarded from the calculation (size will not grow).
     */
    INT_TOTAL("totalInt_"),
    /**
     * Used when the getValue() method returns a long and total should be
     * calculated.
     *
     * -1 values will be disregarded from the calculation (size will not grow).
     */
    LONG_TOTAL("totalLong_"),
    /**
     * Used when the getValue() method returns a double and total should be
     * calculated.
     *
     * -1 values will be disregarded from the calculation (size will not grow).
     */
    DOUBLE_TOTAL("totalDouble_"),
    /**
     * Used when the getValue() method returns an amount of milliseconds as long
     * and average should be calculated.
     *
     * -1 values will be disregarded from the calculation (size will not grow).
     */
    LONG_TIME_MS_AVG("avgTimeMs_", "Average "),
    /**
     * Used when the getValue() method returns an amount of milliseconds as long
     * and total should be calculated.
     *
     * -1 values will be disregarded from the calculation (size will not grow).
     */
    LONG_TIME_MS_TOTAL("totalTimeMs_"),
    /**
     * Used when the getValue() method returns an Epoch Millisecond as long and
     * average of differences between the millisecond and current millisecond
     * should be calculated.
     *
     * For example if a player has dropped a Foo on epoch ms 1494486504000 and
     * that was 5s (5000ms) ago. Now you want to calculate the average
     * time-since for all players. Then you use this one.
     *
     * -1 values will be disregarded from the calculation (size will not grow).
     */
    LONG_EPOCH_MS_MINUS_NOW_AVG("avgEpochMsMinusNow_", "Average "),
    /**
     * Used to calculate %-true for the returned boolean values of getValue().
     */
    BOOLEAN_PERCENTAGE("perchBool_", "Percentage "),
    /**
     * Used to calculate number of true values for the returned boolean values
     * of getValue().
     *
     * Will be presented as "n / total".
     */
    BOOLEAN_TOTAL("totalBool_"),
    /**
     * Used to add html tags to the plugins tab.
     *
     * Can be used to add Tables, Images (for example maps) and other html
     * elements.
     *
     * @see AdvancedAchievementsTable
     * @see FactionsTable
     * @see TownyTable
     */
    HTML;

    private final String modifier;
    private final String placeholderModifier;

    private AnalysisType(String placeholderModifier, String modifier) {
        this.placeholderModifier = placeholderModifier;
        this.modifier = modifier;
    }

    private AnalysisType(String placeholderModifier) {
        this.placeholderModifier = placeholderModifier;
        this.modifier = "";
    }

    private AnalysisType() {
        this.placeholderModifier = "";
        this.modifier = "";
    }

    /**
     * Used to get the modifier for the Prefix of the value.
     *
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
