package main.java.com.djrapitops.plan.data.analysis;

import main.java.com.djrapitops.plan.utilities.FormatUtils;

/**
 * Part responsible for all Playtime related analysis.
 * <p>
 * Placeholder values can be retrieved using the get method.
 * <p>
 * Contains following placeholders after analyzed:
 * ${playtimeTotal} - Formatted time amount
 *
 * @author Rsl1122
 * @since 3.5.2
 */
public class PlaytimePart extends RawData {

    private long totalPlaytime;

    public PlaytimePart() {
        totalPlaytime = 0;
    }

    @Override
    public void analyse() {
        addValue("playtimeTotal", FormatUtils.formatTimeAmount(totalPlaytime));
    }

    public void addToPlaytime(long amount) {
        totalPlaytime += amount;
    }
}
