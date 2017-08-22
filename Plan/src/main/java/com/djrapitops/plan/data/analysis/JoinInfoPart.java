package main.java.com.djrapitops.plan.data.analysis;

import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.data.Session;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import main.java.com.djrapitops.plan.utilities.analysis.AnalysisUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Part responsible for all Player login related analysis.
 * <p>
 * Placeholder values can be retrieved using the get method.
 * <p>
 * Contains following placeholders after analyzed:
 * ${playersAverage} - (Number)
 * ${playersNewAverage} - (Number)
 * <p>
 * ${playersDay} - (Number)
 * ${playersWeek} - (Number)
 * ${playersMonth} - (Number)
 * ${playersAverageDay} - (Number)
 * ${playersAverageWeek} - (Number)
 * ${playersAverageMonth} - (Number)
 * ${playersNewDay} - (Number)
 * ${playersNewWeek} - (Number)
 * ${playersNewMonth} - (Number)
 * ${playersNewAverageDay} - (Number)
 * ${playersNewAverageWeek} - (Number)
 * ${playersNewAverageMonth} - (Number)
 * //TODO ${tableBodySessions}, ${sessionCount}, ${lastPeakTime}, ${playersLastPeak}, ${bestPeakTime}, ${playersBestPeak}
 * @author Rsl1122
 * @since 3.5.2
 */
public class JoinInfoPart extends RawData {

    private final Map<UUID, List<Session>> sessions;
    private final List<Long> registered;
    private long loginTimes;

    public JoinInfoPart() {
        sessions = new HashMap<>();
        registered = new ArrayList<>();
        loginTimes = 0;
    }

    @Override
    public void analyse() {
        newPlayers();
        uniquePlayers();
        uniquePlayersPerDay();
    }

    private void uniquePlayers() {
        int uniqueDay = AnalysisUtils.getUniqueJoins(sessions, TimeAmount.DAY.ms());
        int uniqueWeek = AnalysisUtils.getUniqueJoins(sessions, TimeAmount.WEEK.ms());
        int uniqueMonth = AnalysisUtils.getUniqueJoins(sessions, TimeAmount.MONTH.ms());

        addValue("playersDay", uniqueDay);
        addValue("playersWeek", uniqueWeek);
        addValue("playersMonth", uniqueMonth);
    }

    private void uniquePlayersPerDay() {
        int perDay = AnalysisUtils.getUniqueJoinsPerDay(sessions, -1);
        int perDayDay = AnalysisUtils.getUniqueJoinsPerDay(sessions, TimeAmount.DAY.ms());
        int perDayWeek = AnalysisUtils.getUniqueJoinsPerDay(sessions, TimeAmount.WEEK.ms());
        int perDayMonth = AnalysisUtils.getUniqueJoinsPerDay(sessions, TimeAmount.MONTH.ms());

        addValue("playersAverage", perDay);
        addValue("playersAverageDay", perDayDay);
        addValue("playersAverageWeek", perDayWeek);
        addValue("playersAverageMonth", perDayMonth);
    }

    private void newPlayers() {
        long now = MiscUtils.getTime();
        long newDay = AnalysisUtils.getNewPlayers(registered, TimeAmount.DAY.ms(), now);
        long newWeek = AnalysisUtils.getNewPlayers(registered, TimeAmount.WEEK.ms(), now);
        long newMonth = AnalysisUtils.getNewPlayers(registered, TimeAmount.MONTH.ms(), now);

        addValue("playersNewDay", newDay);
        addValue("playersNewWeek", newWeek);
        addValue("playersNewMonth", newMonth);

        long newPerDay = AnalysisUtils.getNewUsersPerDay(registered, -1);
        long newPerDayDay = AnalysisUtils.getNewUsersPerDay(registered, TimeAmount.DAY.ms());
        long newPerDayWeek = AnalysisUtils.getNewUsersPerDay(registered, TimeAmount.WEEK.ms());
        long newPerDayMonth = AnalysisUtils.getNewUsersPerDay(registered, TimeAmount.MONTH.ms());

        addValue("playersNewAverage", newPerDay);
        addValue("playersNewAverageDay", newPerDayDay);
        addValue("playersNewAverageWeek", newPerDayWeek);
        addValue("playersNewAverageMonth", newPerDayMonth);
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

    public Map<UUID, List<Session>> getSessions() {
        return sessions;
    }

    public List<Session> getAllSessions() {
        return MiscUtils.flatMap(sessions.values());
    }

    public void addRegistered(long registerDate) {
        registered.add(registerDate);
    }

    public List<Long> getRegistered() {
        return registered;
    }

    public void addSessions(UUID uuid, List<Session> sessions) {
        Verify.nullCheck(uuid);
        Verify.nullCheck(sessions);
        this.sessions.put(uuid, sessions.stream().distinct().collect(Collectors.toList()));
    }
}
