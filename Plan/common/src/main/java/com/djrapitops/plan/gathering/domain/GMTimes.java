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

import org.apache.commons.text.TextStringBuilder;

import java.util.Map;
import java.util.Optional;

/**
 * TimeKeeper class that tracks the time spent in each GameMode based on Playtime.
 *
 * @author AuroraLS3
 */
public class GMTimes extends TimeKeeper {

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

    public static String magicNumberToGMName(int magicNumber) {
        switch (magicNumber) {
            case 0:
                return SURVIVAL;
            case 1:
                return CREATIVE;
            case 2:
                return ADVENTURE;
            case 3:
                return SPECTATOR;
            default:
                return "UNKOWN";
        }
    }

    public Optional<String> getMostUsedGameMode() {
        long max = 0;
        String maxGM = null;
        for (Map.Entry<String, Long> entry : times.entrySet()) {
            if (entry.getValue() > max) {
                max = entry.getValue();
                maxGM = entry.getKey();
            }
        }
        return Optional.ofNullable(maxGM);
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
        if (times == null) throw new IllegalArgumentException("'times' should not be null!");
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

    public void resetTimes(long time) {
        resetState(SURVIVAL, time);
        resetState(CREATIVE);
        resetState(ADVENTURE);
        resetState(SPECTATOR);
    }

    @Override
    public String getState() {
        String state = super.getState();
        return state != null ? state : SURVIVAL;
    }

    @Override
    public String toString() {
        return "GMTimes{" +
                "times=" + times +
                ", state='" + state + '\'' +
                ", lastStateChange=" + lastStateChange +
                '}';
    }

    public String toJson() {
        return "{\"times\": {" +
                new TextStringBuilder().appendWithSeparators(times.entrySet().stream()
                        .map(entry -> "\"" + entry.getKey() + "\": " + entry.getValue())
                        .iterator(), ",").get() +
                "  }," +
                (state != null ? "\"state\": \"" + state + "\"," : "\"state\": null,") +
                "\"lastStateChange\": " + lastStateChange +
                "}";
    }
}