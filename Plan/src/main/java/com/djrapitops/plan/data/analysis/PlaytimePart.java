package main.java.com.djrapitops.plan.data.analysis;

import main.java.com.djrapitops.plan.utilities.FormatUtils;
import main.java.com.djrapitops.plan.utilities.analysis.MathUtils;

/**
 * Part responsible for all Playtime related analysis.
 *
 * Placeholder values can be retrieved using the get method.
 *
 * Contains following place-holders: totalplaytime, avgplaytime
 *
 * @author Rsl1122
 * @since 3.5.2
 */
public class PlaytimePart extends RawData<PlaytimePart> {

    private long totalPlaytime;
    private final PlayerCountPart playerCount;

    public PlaytimePart(PlayerCountPart part) {
        playerCount = part;
        totalPlaytime = 0;
    }

    @Override
    public void analyse() {
        addValue("totalplaytime", FormatUtils.formatTimeAmount(totalPlaytime));
        final long averagePlaytime = MathUtils.averageLong(totalPlaytime, playerCount.getPlayerCount());
        addValue("avgplaytime", FormatUtils.formatTimeAmount(averagePlaytime));
    }

    public void addToPlaytime(long amount) {
        totalPlaytime += amount;
    }
}
