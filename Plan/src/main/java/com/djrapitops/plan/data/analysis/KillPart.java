package main.java.com.djrapitops.plan.data.analysis;

import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.data.KillData;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import main.java.com.djrapitops.plan.utilities.analysis.MathUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Part responsible for all Death related analysis.
 * <p>
 * Totals
 * <p>
 * Placeholder values can be retrieved using the get method.
 * <p>
 * Contains following place-holders: deaths, mobkills, playerkills, avgdeaths, avgmobkills, avgplayerkills
 *
 * @author Rsl1122
 * @since 3.5.2
 */
public class KillPart extends RawData<KillPart> {

    private final PlayerCountPart playerCount;
    private final Map<UUID, List<KillData>> playerKills;
    private long mobKills;
    private long deaths;

    public KillPart(PlayerCountPart playerCount) {
        this.playerCount = playerCount;
        playerKills = new HashMap<>();
        mobKills = 0;
        deaths = 0;
    }

    @Override
    public void analyse() {
        addValue("deaths", deaths);
        addValue("mobkills", mobKills);
        int playerKillAmount = getAllPlayerKills().size();
        addValue("playerkills", playerKillAmount);
        int playerCount = this.playerCount.getPlayerCount();
        addValue("avgdeaths", MathUtils.averageLong(deaths, playerCount));
        addValue("avgmobkills", MathUtils.averageLong(mobKills, playerCount));
        addValue("avgplayerkills", MathUtils.averageLong(playerKillAmount, playerCount));
    }

    public void addKills(UUID uuid, List<KillData> kills) throws IllegalArgumentException {
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
