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
@Deprecated
public class PlaytimePart extends RawData {

    private long totalPlaytime;
    private long playtime30d;
    private long playtime7d;
    private long playtime24h;

    public PlaytimePart() {
        totalPlaytime = 0;
    }

    @Override
    public void analyse() {
        addValue("playtimeTotal", FormatUtils.formatTimeAmount(totalPlaytime));
        addValue("playtimeMonth", FormatUtils.formatTimeAmount(playtime30d));
        addValue("playtimeWeek", FormatUtils.formatTimeAmount(playtime7d));
        addValue("playtimeDay", FormatUtils.formatTimeAmount(playtime24h));
    }

    public void addToPlaytime(long amount) {
        totalPlaytime += amount;
    }

    public void setTotalPlaytime(long totalPlaytime) {
        this.totalPlaytime = totalPlaytime;
    }

    public void setPlaytime30d(long playtime30d) {
        this.playtime30d = playtime30d;
    }

    public void setPlaytime7d(long playtime7d) {
        this.playtime7d = playtime7d;
    }

    public void setPlaytime24h(long playtime24h) {
        this.playtime24h = playtime24h;
    }
}
