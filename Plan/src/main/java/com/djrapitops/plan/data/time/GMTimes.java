package main.java.com.djrapitops.plan.data.time;

import com.djrapitops.plugin.utilities.Verify;

import java.util.Map;

/**
 * TimeKeeper class that tracks the time spent in each GameMode based on Playtime.
 *
 * @author Rsl1122
 * @since 3.6.0
 */
public class GMTimes extends TimeKeeper {

    // TODO Make private once GMTimesTable is removed
    public static final String SURVIVAL = "SURVIVAL";
    public static final String CREATIVE = "CREATIVE";
    public static final String ADVENTURE = "ADVENTURE";
    public static final String SPECTATOR = "SPECTATOR";

    public GMTimes(Map<String, Long> times, String lastState, long lastStateChange) {
        super(times, lastState, lastStateChange);
    }

    public GMTimes(String lastState, long lastStateChange) {
        super(lastState, lastStateChange);
    }

    public GMTimes(String lastState) {
        super(lastState);
    }

    public GMTimes(Map<String, Long> times) {
        super(times);
    }

    public GMTimes() {
        super();
    }

    public static String[] getGMKeyArray() {
        return new String[]{SURVIVAL, CREATIVE, ADVENTURE, SPECTATOR};
    }

    /**
     * Sets times for all 4 gamemodes.
     * <p>
     * Give 1 - 4 parameters.
     * times starts from Survival, ends in Spectator.
     * <p>
     * Given too few parameters (Under 4, rest are set as 0L)
     * Extra parameters are ignored (Over 4)
     *
     * @param times 1-4 time parameters.
     * @throws IllegalArgumentException If any parameter is null.
     */
    public void setAllGMTimes(long... times) {
        Verify.nullCheck(times);
        String[] gms = getGMKeyArray();
        int size = times.length;
        for (int i = 0; i < 4; i++) {
            if (i >= size) {
                setTime(gms[i], 0L);
            } else {
                setTime(gms[i], times[i]);
            }
        }
    }

    public void resetTimes(long playtime) {
        resetState(SURVIVAL, playtime);
        resetState(CREATIVE);
        resetState(ADVENTURE);
        resetState(SPECTATOR);
    }

    @Override
    public String getState() {
        String state = super.getState();
        return state != null ? state : SURVIVAL;
    }
}