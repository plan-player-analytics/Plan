/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.gathering.domain;

import com.djrapitops.plugin.utilities.Verify;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Keeps track of time spent in each state.
 *
 * @author Rsl1122
 */
public class TimeKeeper {

    protected Map<String, Long> times;
    protected String state;
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
     * @param ms Epoch ms the change occurred.
     * @throws IllegalArgumentException If newState is null.
     */
    public void changeState(String newState, long ms) {
        Verify.nullCheck(newState);
        if (state == null) {
            state = newState;
        }
        Long currentTime = times.getOrDefault(state, 0L);
        long diff = ms - lastStateChange;
        times.put(state, currentTime + Math.abs(diff));
        state = newState;
        lastStateChange = ms;
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