package main.java.com.djrapitops.plan.data.time;

import java.util.Map;

/**
 * TimeKeeper class that tracks the time spent in each World based on Playtime.
 *
 * @author Rsl1122
 * @since 3.6.0
 */
public class WorldTimes extends TimeKeeper {

    public WorldTimes(Map<String, Long> times, String lastState, long lastStateChange) {
        super(times, lastState, lastStateChange);
    }

    public WorldTimes(String lastState, long lastStateChange) {
        super(lastState, lastStateChange);
    }

    public WorldTimes(String lastState) {
        super(lastState);
    }

    public WorldTimes(Map<String, Long> times, long lastStateChange) {
        super(times, null, lastStateChange);
    }

    public WorldTimes(Map<String, Long> times) {
        super(times);
    }

    public WorldTimes() {
        super();
    }

    @Override
    public String getState() {
        String state = super.getState();
        return state != null ? state : "Unknown";
    }

}
