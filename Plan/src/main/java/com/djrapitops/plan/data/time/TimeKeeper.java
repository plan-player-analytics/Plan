package com.djrapitops.plan.data.time;

import com.djrapitops.plugin.utilities.Verify;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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

    /**
     * Sets a specific time for a state.
     *
     * @param state State to set
     * @param time  Time in ms the state has been active for
     * @throws IllegalArgumentException If given state is null
     */
    public void setTime(String state, long time) {
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
     * @throws IllegalArgumentException If newState is null.
     */
    public void changeState(String newState, long playTime) {
        Verify.nullCheck(newState);
        if (state == null) {
            state = newState;
        }
        Long currentTime = times.getOrDefault(state, 0L);
        long diff = playTime - lastStateChange;
        times.put(state, currentTime + Math.abs(diff));
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
        return times.getOrDefault(state, 0L);
    }

    public void addTime(String state, long time) {
        times.put(state, times.getOrDefault(state, 0L) + time);
    }

    public long getTotal() {
        return times.values().stream().mapToLong(i -> i).sum();
    }

    public Map<String, Long> getTimes() {
        return times;
    }

    public void setTimes(Map<String, Long> times) {
        this.times = times;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public long getLastStateChange() {
        return lastStateChange;
    }

    public void setLastStateChange(long lastStateChange) {
        this.lastStateChange = lastStateChange;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeKeeper that = (TimeKeeper) o;
        return lastStateChange == that.lastStateChange &&
                Objects.equals(times, that.times) &&
                Objects.equals(state, that.state);
    }

    @Override
    public int hashCode() {
        return Objects.hash(times, state, lastStateChange);
    }

    @Override
    public String toString() {
        return "TimeKeeper{" + "times=" + times +
                ", state='" + state + "', lastStateChange=" + lastStateChange + '}';
    }
}