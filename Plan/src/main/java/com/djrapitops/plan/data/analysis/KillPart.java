package main.java.com.djrapitops.plan.data.analysis;

/**
 * Part responsible for all Death related analysis.
 * <p>
 * Placeholder values can be retrieved using the get method.
 * <p>
 * Contains following placeholders after analyzed:
 * ${killCount} - (Number)
 * ${mobKillCount} - (Number)
 * ${deathCount} - (Number)
 *
 * @author Rsl1122
 * @since 3.5.2
 */
public class KillPart extends RawData {

    private long playerKills;
    private long mobKills;
    private long deaths;

    public KillPart() {
        playerKills = 0;
        mobKills = 0;
        deaths = 0;
    }

    // TODO JoinInfo Part, sessions for kills.

    @Override
    public void analyse() {
        addValue("deathCount", deaths);
        addValue("mobKillCount", mobKills);
        addValue("killCount", playerKills);
    }

    /**
     * Adds kills to the dataset.
     *
     * @param amount amount of kills
     * @throws IllegalArgumentException if kills is null
     */
    public void addKills(long amount) {
        playerKills += amount;
    }

    public void addMobKills(long amount) {
        mobKills += amount;
    }

    public void addDeaths(long amount) {
        deaths += amount;
    }

    public long getPlayerKills() {
        return playerKills;
    }

    public long getMobKills() {
        return mobKills;
    }

    public long getDeaths() {
        return deaths;
    }
}
