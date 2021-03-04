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

import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link GMTimes}.
 *
 * @author AuroraLS3
 */
class GMTimesTest {

    @Test
    void allGMTimesAreSet() {
        GMTimes times = new GMTimes();
        times.setAllGMTimes(1L, 2L, 3L, 4L);

        assertEquals(1L, times.getTime("SURVIVAL"));
        assertEquals(2L, times.getTime("CREATIVE"));
        assertEquals(3L, times.getTime("ADVENTURE"));
        assertEquals(4L, times.getTime("SPECTATOR"));
    }

    @Test
    void allGMTimesAreSetWithTooFewArguments() {
        GMTimes times = new GMTimes();
        times.setAllGMTimes(1L, 2L);

        assertEquals(1L, times.getTime("SURVIVAL"));
        assertEquals(2L, times.getTime("CREATIVE"));
        assertEquals(0L, times.getTime("ADVENTURE"));
        assertEquals(0L, times.getTime("SPECTATOR"));
    }

    @Test
    void allGMTimesAreSetWithTooManyArguments() {
        GMTimes times = new GMTimes();
        times.setAllGMTimes(1L, 2L, 3L, 4L, 5L, 6L);

        assertEquals(1L, times.getTime("SURVIVAL"));
        assertEquals(2L, times.getTime("CREATIVE"));
        assertEquals(3L, times.getTime("ADVENTURE"));
        assertEquals(4L, times.getTime("SPECTATOR"));
    }

    @Test
    void timesAreReset() {
        GMTimes gmTimes = new GMTimes();
        gmTimes.setAllGMTimes(4, 3, 2, 1);
        gmTimes.resetTimes(10L);

        assertEquals(10L, gmTimes.getTotal());
        assertEquals(10L, gmTimes.getTime("SURVIVAL"));
        assertEquals(0L, gmTimes.getTime("ADVENTURE"));
    }

    @Test
    void timeIsSet() {
        GMTimes gmTimes = new GMTimes();
        gmTimes.setTime("SURVIVAL", 5L);

        assertEquals(5L, gmTimes.getTime("SURVIVAL"));
    }

    @Test
    void stateIsRenamed() {
        GMTimes gmTimes = new GMTimes();
        gmTimes.setAllGMTimes(5L);
        gmTimes.renameState("SURVIVAL", "Survival");

        assertEquals(0L, gmTimes.getTime("SURVIVAL"));
        assertEquals(5L, gmTimes.getTime("Survival"));
    }

    @Test
    void stateIsChangedAppropriately() {
        GMTimes gmTimes = new GMTimes(new HashMap<>(), "SURVIVAL", 0);
        gmTimes.changeState("CREATIVE", 5L);

        assertEquals(5L, gmTimes.getTime("SURVIVAL"));
        assertEquals(0L, gmTimes.getTime("CREATIVE"));

        gmTimes.changeState("ADVENTURE", 20L);

        assertEquals(5L, gmTimes.getTime("SURVIVAL"));
        assertEquals(15L, gmTimes.getTime("CREATIVE"));
        assertEquals(0L, gmTimes.getTime("ADVENTURE"));
    }

    @Test
    void stateIsChangedWhenStartTimeIsDefault() {
        GMTimes gmTimes = new GMTimes("SURVIVAL");
        gmTimes.changeState("CREATIVE", 5L);

        assertEquals(5L, gmTimes.getTime("SURVIVAL"));
        assertEquals(0L, gmTimes.getTime("CREATIVE"));

        gmTimes.changeState("ADVENTURE", 20L);

        assertEquals(5L, gmTimes.getTime("SURVIVAL"));
        assertEquals(15L, gmTimes.getTime("CREATIVE"));
        assertEquals(0L, gmTimes.getTime("ADVENTURE"));
    }

    @Test
    void stateIsChangedWhenBeginStateIsDefault() {
        GMTimes test = new GMTimes();
        test.changeState("CREATIVE", 5L);

        assertEquals(5L, test.getTime("CREATIVE"));

        test.changeState("ADVENTURE", 20L);

        assertEquals(20L, test.getTime("CREATIVE"));
        assertEquals(0L, test.getTime("ADVENTURE"));
    }
}