package main.java.com.djrapitops.plan.data.analysis;

import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.utilities.HtmlUtils;

/**
 * Part responsible for all Gamemode usage related analysis.
 * <p>
 * Gamemode Piechart, Percentages and Totals.
 * <p>
 * Placeholder values can be retrieved using the get method.
 * <p>
 * Contains following place-holders: gmtotal, gm0col-gm3col, gmcolors, gmlabels,
 * gm0-gm3, gmdata, gm0total-gm3total
 *
 * @author Rsl1122
 * @since 3.5.2
 */
@Deprecated
public class GamemodePart extends RawData {

    private long survivalTime;
    private long creativeTime;
    private long adventureTime;
    private long spectatorTime;

    public GamemodePart() {
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
        String gmColors = HtmlUtils.separateWithQuotes(
                "#555", "#555", "#555", "#555" // TODO Write Colors (enum) variables for GameMode colors.
        );
        addValue("gmColors", gmColors);
    }

    /**
     * Adds time to a gamemode.
     *
     * @param gm     Name of Gamemode
     * @param amount milliseconds to add
     * @throws IllegalArgumentException if gm is null
     */
    public void addTo(String gm, long amount) {
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
