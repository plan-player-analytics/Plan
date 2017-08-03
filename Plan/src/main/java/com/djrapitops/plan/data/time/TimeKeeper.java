package main.java.com.djrapitops.plan.data.time;

import com.djrapitops.plugin.utilities.Verify;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract class for keeping track of time spent in each state.
 */
public abstract class TimeKeeper {
    /**
     * Keeps time of states.
     */
    protected Map<String, Long> times;
    /**
     * Last State seen in
     */
    protected String state;
    /**
     * Relates to Playtime Milliseconds.
     */
    protected long lastStateChange;

    public TimeKeeper(Map<String, Long> times, String lastState, long lastStateChange) {
        this.times = times;
        this.state = lastState;
        this.lastStateChange = lastStateChange;
    }

    public TimeKeeper(String lastState, long lastStateChange) {
        this(new HashMap<>(), lastState, lastStateChange);
    }

    public TimeKeeper(String lastState) {
        this(new HashMap<>(), lastState, 0L);
    }

    public TimeKeeper(Map<String, Long> times) {
        this(times, null, 0);
    }

    public TimeKeeper() {
        this(new HashMap<>());
    }

    public void setTime(String state, long time) throws IllegalArgumentException {
        times.put(Verify.nullCheck(state), time);
    }

    public void renameState(String state, String renameTo) {
        Verify.nullCheck(state, renameTo);
        Long time = times.get(state);
        if (time != null) {
            times.put(renameTo, time);
            times.remove(state);
            if (state.equals(this.state)) {
                this.state = renameTo;
            }
        }
    }

    /**
     * Adds time to the last state while updating the status of other parameters.
     *
     * @param newState New State seen in.
     * @param playTime Current Playtime.
     * @throws IllegalArgumentException If new state is null.
     * @throws IllegalStateException    If lastStateChange time is higher than playtime.
     */
    public void changeState(String newState, long playTime) throws IllegalArgumentException, IllegalStateException {
        Verify.nullCheck(newState);
        if (playTime < lastStateChange) {
            throw new IllegalStateException("Given Playtime is lower than last status change time: " + playTime + " / " + lastStateChange);
        }
        if (state == null) {
            state = newState;
        }
        Long currentTime = times.get(state);
        if (currentTime == null) {
            currentTime = 0L;
        }
        long diff = playTime - lastStateChange;
        times.put(state, currentTime + diff);
        state = newState;
        lastStateChange = playTime;
    }

    protected void resetState(String state) {
        times.remove(Verify.nullCheck(state));
    }

    protected void resetState(String state, long time) {
        if (time > 0) {
            times.put(Verify.nullCheck(state), time);
            lastStateChange = time;
            this.state = state;
        } else {
            resetState(state);
        }
    }

    public long getTime(String state) {
        Long time = times.get(state);
        return time != null ? time : 0L;
    }

    public long getTotal() {
        return times.values().stream().mapToLong(i -> i).sum();
    }

    public void setTimes(Map<String, Long> times) {
        this.times = times;
    }

    public Map<String, Long> getTimes() {
        return times;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setLastStateChange(long lastStateChange) {
        this.lastStateChange = lastStateChange;
    }

    public String getState() {
        return state;
    }

    public long getLastStateChange() {
        return lastStateChange;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TimeKeeper that = (TimeKeeper) o;

        return lastStateChange == that.lastStateChange &&
                times != null ? times.equals(that.times) : that.times == null
                && state != null ? state.equals(that.state) : that.state == null;
    }

    @Override
    public int hashCode() {
        int result = times != null ? times.hashCode() : 0;
        result = 31 * result + (state != null ? state.hashCode() : 0);
        result = 31 * result + (int) (lastStateChange ^ (lastStateChange >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "TimeKeeper{" +
                "times=" + times +
                ", state='" + state + '\'' +
                ", lastStateChange=" + lastStateChange +
                '}';
    }
}