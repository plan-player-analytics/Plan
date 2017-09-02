package main.java.com.djrapitops.plan.data.analysis;

import main.java.com.djrapitops.plan.data.PlayerKill;
import main.java.com.djrapitops.plan.data.Session;

import java.util.Collection;
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

    private final JoinInfoPart joinInfoPart;

    private long playerKills;
    private long mobKills;
    private long deaths;

    public KillPart(JoinInfoPart joinInfoPart) {
        this.joinInfoPart = joinInfoPart;

        playerKills = 0;
        mobKills = 0;
        deaths = 0;
    }

    @Override
    public void analyse() {
        List<Session> sessions = joinInfoPart.getAllSessions();
        deaths += sessions.stream().mapToLong(Session::getDeaths).sum();
        mobKills += sessions.stream().mapToLong(Session::getMobKills).sum();
        playerKills += sessions.stream().map(Session::getPlayerKills).mapToLong(Collection::size).sum();

        addValue("deathCount", this.deaths);
        addValue("mobKillCount", mobKills);
        addValue("killCount", playerKills);
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

    public void addKills(Map<UUID, List<PlayerKill>> playerKills) {
        this.playerKills += playerKills.values().stream().mapToLong(Collection::size).sum();
    }
}
