package main.java.com.djrapitops.plan.data.analysis;

import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.data.SessionData;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import main.java.com.djrapitops.plan.utilities.analysis.AnalysisUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Part responsible for all Player login related analysis.
 *
 * Unique per Day, Unique, New Players, Logins
 *
 * Placeholder values can be retrieved using the get method.
 *
 * Contains following place-holders: totallogins, uniquejoinsday,
 * uniquejoinsweek, uniquejoinsmonth, avguniquejoins, avguniquejoinsday,
 * avguniquejoinsweek, avguniquejoinsmonth, npday, npweek, npmonth
 *
 * @author Rsl1122
 * @since 3.5.2
 */
public class JoinInfoPart extends RawData<JoinInfoPart> {

    private final Map<UUID, List<SessionData>> sessions;
    private final List<Long> registered;
    private long loginTimes;

    public JoinInfoPart() {
        sessions = new HashMap<>();
        registered = new ArrayList<>();
        loginTimes = 0;
    }

    @Override
    public void analyse() {
        addValue("totallogins", loginTimes);

        newPlayers();
        uniquePlayers();
        uniquePlayersPerDay();
    }

    private void uniquePlayers() {
        int uniqueDay = AnalysisUtils.getUniqueJoins(sessions, TimeAmount.DAY.ms());
        int uniqueWeek = AnalysisUtils.getUniqueJoins(sessions, TimeAmount.WEEK.ms());
        int uniqueMonth = AnalysisUtils.getUniqueJoins(sessions, TimeAmount.MONTH.ms());

        addValue("uniquejoinsday", uniqueDay);
        addValue("uniquejoinsweek", uniqueWeek);
        addValue("uniquejoinsmonth", uniqueMonth);
    }

    private void uniquePlayersPerDay() {
        int perDay = AnalysisUtils.getUniqueJoinsPerDay(sessions, -1);
        int perDayDay = AnalysisUtils.getUniqueJoinsPerDay(sessions, TimeAmount.DAY.ms());
        int perDayWeek = AnalysisUtils.getUniqueJoinsPerDay(sessions, TimeAmount.WEEK.ms());
        int perDayMonth = AnalysisUtils.getUniqueJoinsPerDay(sessions, TimeAmount.MONTH.ms());

        addValue("avguniquejoins", perDay);
        addValue("avguniquejoinsday", perDayDay);
        addValue("avguniquejoinsweek", perDayWeek);
        addValue("avguniquejoinsmonth", perDayMonth);
    }

    private void newPlayers() {
        int newDay = AnalysisUtils.getNewPlayers(registered, TimeAmount.DAY.ms(), MiscUtils.getTime());
        int newWeek = AnalysisUtils.getNewPlayers(registered, TimeAmount.WEEK.ms(), MiscUtils.getTime());
        int newMonth = AnalysisUtils.getNewPlayers(registered, TimeAmount.MONTH.ms(), MiscUtils.getTime());

        addValue("npday", newDay);
        addValue("npweek", newWeek);
        addValue("npmonth", newMonth);
    }

    public void addToLoginTimes() {
        addToLoginTimes(1);
    }

    public void addToLoginTimes(int amount) {
        loginTimes += amount;
    }

    public long getLoginTimes() {
        return loginTimes;
    }

    public Map<UUID, List<SessionData>> getSessions() {
        return sessions;
    }

    public List<SessionData> getAllSessions() {
        return MiscUtils.flatMap(sessions.values());
    }

    public void addRegistered(long registerDate) {
        registered.add(registerDate);
    }

    public List<Long> getRegistered() {
        return registered;
    }

    public void addSessions(UUID uuid, List<SessionData> sessions) {
        Verify.nullCheck(uuid);
        Verify.nullCheck(sessions);
        this.sessions.put(uuid, sessions.stream().distinct().collect(Collectors.toList()));
    }
}
