package main.java.com.djrapitops.plan.data.analysis;

import com.djrapitops.plugin.utilities.Verify;
import java.util.Arrays;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.utilities.FormatUtils;
import main.java.com.djrapitops.plan.utilities.HtmlUtils;

/**
 * Part responsible for all Gamemode usage related analysis.
 *
 * Gamemode Piechart, Percentages and Totals.
 *
 * Placeholder values can be retrieved using the get method.
 *
 * Contains following place-holders: gmtotal, gm0col-gm3col, gmcolors, gmlabels,
 * gm0-gm3, gmdata, gm0total-gm3total
 *
 * @author Rsl1122
 * @since 3.5.2
 */
public class GamemodePart extends RawData<GamemodePart> {

    private final PlayerCountPart playerCount;
    private long survivalTime;
    private long creativeTime;
    private long adventureTime;
    private long spectatorTime;

    public GamemodePart(PlayerCountPart playerCount) {
        this.playerCount = playerCount;
        survivalTime = 0;
        creativeTime = 0;
        adventureTime = 0;
        spectatorTime = 0;
    }

    @Override
    public void analyse() {
        gamemodePiechart();
    }

    private void gamemodePiechart() {
        long totalTime = survivalTime + creativeTime + adventureTime + spectatorTime;

        addValue("gmtotal", FormatUtils.formatTimeAmount(totalTime));

        double[] percentages = new double[]{
            (survivalTime * 100.0) / totalTime,
            (creativeTime * 100.0) / totalTime,
            (adventureTime * 100.0) / totalTime,
            (spectatorTime * 100.0) / totalTime
        };
        long[] times = new long[]{
            survivalTime, creativeTime, adventureTime, spectatorTime
        };
        String col0 = Settings.HCOLOR_GMP_0 + "";
        String col1 = Settings.HCOLOR_GMP_1 + "";
        String col2 = Settings.HCOLOR_GMP_2 + "";
        String col3 = Settings.HCOLOR_GMP_3 + "";

        addValue("%gm0col%", col0);
        addValue("%gm1col%", col1);
        addValue("%gm2col%", col2);
        addValue("%gm3col%", col3);
        String gmColors = HtmlUtils.separateWithQuotes(
                "#" + col0, "#" + col1, "#" + col2, "#" + col3
        );
        String gmLabels = "[" + HtmlUtils.separateWithQuotes(
                "Survival", "Creative", "Adventure", "Spectator") + "]";
        addValue("%gmcolors%", gmColors);
        addValue("%gmlabels%", gmLabels);

        // Adds Percentage indicators
        for (int i = 0; i < percentages.length; i++) {
            addValue("gm" + i, (int) (percentages[i]) + "%");
        }
        // Adds Value array for graph
        addValue("gmdata", Arrays.toString(times));

        // Adds formatted time amounts for each gamemode
        for (int i = 0; i < times.length; i++) {
            addValue("gm" + i + "total", FormatUtils.formatTimeAmount(times[i]));
        }
    }

    public void addTo(String gm, long amount) throws IllegalArgumentException {
        Verify.nullCheck(gm);
        switch (gm) {
            case "SURVIVAL":
                addToSurvival(amount);
                break;
            case "CREATIVE":
                addToCreative(amount);
                break;
            case "ADVENTURE":
                addToAdventure(amount);
                break;
            case "SPECTATOR":
                addToSpectator(amount);
                break;
            default:
                break;
        }
    }

    public void addToSurvival(long amount) {
        if (amount > 0) {
            survivalTime += amount;
        }
    }

    public void addToCreative(long amount) {
        if (amount > 0) {
            creativeTime += amount;
        }
    }

    public void addToAdventure(long amount) {
        if (amount > 0) {
            adventureTime += amount;
        }
    }

    public void addToSpectator(long amount) {
        if (amount > 0) {
            spectatorTime += amount;
        }
    }

    public long getSurvivalTime() {
        return survivalTime;
    }

    public long getCreativeTime() {
        return creativeTime;
    }

    public long getAdventureTime() {
        return adventureTime;
    }

    public long getSpectatorTime() {
        return spectatorTime;
    }
}
