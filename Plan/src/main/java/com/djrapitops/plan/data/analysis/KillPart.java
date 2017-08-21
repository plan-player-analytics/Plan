package main.java.com.djrapitops.plan.data.analysis;

import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.data.KillData;
import main.java.com.djrapitops.plan.utilities.MiscUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    private final Map<UUID, List<KillData>> playerKills;
    private long mobKills;
    private long deaths;

    public KillPart() {
        playerKills = new HashMap<>();
        mobKills = 0;
        deaths = 0;
    }

    @Override
    public void analyse() {
        addValue("deathCount", deaths);
        addValue("mobKillCount", mobKills);
        int playerKillAmount = getAllPlayerKills().size();
        addValue("killCount", playerKillAmount);
    }

    /**
     * Adds kills to the dataset.
     *
     * @param uuid  Player whose kills are being added
     * @param kills all kills of a player
     * @throws IllegalArgumentException if kills is null
     */
    public void addKills(UUID uuid, List<KillData> kills) {
        Verify.nullCheck(kills);
        playerKills.put(uuid, kills);
    }

    public void addMobKills(long amount) {
        mobKills += amount;
    }

    public void addDeaths(long amount) {
        deaths += amount;
    }

    public Map<UUID, List<KillData>> getPlayerKills() {
        return playerKills;
    }

    public List<KillData> getAllPlayerKills() {
        return MiscUtils.flatMap(playerKills.values());
    }

    public long getMobKills() {
        return mobKills;
    }

    public long getDeaths() {
        return deaths;
    }
}
