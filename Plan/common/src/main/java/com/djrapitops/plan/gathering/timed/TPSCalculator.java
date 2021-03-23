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
package com.djrapitops.plan.gathering.timed;

import com.djrapitops.plan.utilities.analysis.TimerAverage;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Utility for calculating TPS using nano time.
 * <p>
 * Math:
 * - Minecraft runs 20 ticks / second, 1 tick = 50ms
 * - To return 20 ticks, 1 second is expected
 * - If the call time is less than 1 second, the TPS is still 20, excess is not counted.
 * - If the call time is more than 1 second, the TPS is below 20
 * - If 2 seconds, TPS is 10
 * - If 4 seconds, TPS is 5
 * - If 20 seconds, TPS is 1
 * - If more than 20 seconds, TPS is 0 for 20 seconds and then according to the other rules.
 *
 * @author AuroraLS3
 */
public class TPSCalculator {

    public static final long SECOND_NS = TimeUnit.SECONDS.toNanos(1L);

    private final long maxBeforeZeroTPS;
    private long lastPulse;

    private final TimerAverage average;

    public TPSCalculator() {
        maxBeforeZeroTPS = SECOND_NS * 20L; // 20 ticks
        lastPulse = -1;

        average = new TimerAverage();
    }

    /**
     * Pulse the TPS clock.
     *
     * @param time Current epoch ms
     * @return Average TPS for the minute, or empty.
     */
    public Optional<Double> pulse(long time) {
        boolean firstRun = lastPulse < 0;
        long currentPulse = System.nanoTime();
        long difference = currentPulse - lastPulse;
        lastPulse = currentPulse;
        if (firstRun) {
            return Optional.empty(); // Can not calculate on first check.
        }

        // Cap the TPS to a maximum by making nominator 1.
        // Expecting the pulse period to be 1 second.
        if (difference < SECOND_NS) difference = SECOND_NS;

        // Add missed ticks, TPS has been low for a while, see the math in the class javadoc.
        while (difference > maxBeforeZeroTPS) {
            average.add(time, 0.0);
            difference -= maxBeforeZeroTPS;
        }

        double tps = maxBeforeZeroTPS * 1.0 / difference;
        if (average.add(time, tps)) {
            return Optional.of(average.getAverageAndReset(time));
        }
        return Optional.empty();
    }
}