package com.djrapitops.plan.data.time;

import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link GMTimes}.
 *
 * @author Rsl1122
 */
public class GMTimesTest {
    @Test
    public void allGMTimesAreSet() {
        GMTimes times = new GMTimes();
        times.setAllGMTimes(1L, 2L, 3L, 4L);

        assertEquals(1L, times.getTime("SURVIVAL"));
        assertEquals(2L, times.getTime("CREATIVE"));
        assertEquals(3L, times.getTime("ADVENTURE"));
        assertEquals(4L, times.getTime("SPECTATOR"));
    }

    @Test
    public void allGMTimesAreSetWithTooFewArguments() {
        GMTimes times = new GMTimes();
        times.setAllGMTimes(1L, 2L);

        assertEquals(1L, times.getTime("SURVIVAL"));
        assertEquals(2L, times.getTime("CREATIVE"));
        assertEquals(0L, times.getTime("ADVENTURE"));
        assertEquals(0L, times.getTime("SPECTATOR"));
    }

    @Test
    public void allGMTimesAreSetWithTooManyArguments() {
        GMTimes times = new GMTimes();
        times.setAllGMTimes(1L, 2L, 3L, 4L, 5L, 6L);

        assertEquals(1L, times.getTime("SURVIVAL"));
        assertEquals(2L, times.getTime("CREATIVE"));
        assertEquals(3L, times.getTime("ADVENTURE"));
        assertEquals(4L, times.getTime("SPECTATOR"));
    }

    @Test
    public void timesAreReset() {
        GMTimes gmTimes = new GMTimes();
        gmTimes.setAllGMTimes(4, 3, 2, 1);
        gmTimes.resetTimes(10L);

        assertEquals(10L, gmTimes.getTotal());
        assertEquals(10L, gmTimes.getTime("SURVIVAL"));
        assertEquals(0L, gmTimes.getTime("ADVENTURE"));
    }

    @Test
    public void timeIsSet() {
        GMTimes gmTimes = new GMTimes();
        gmTimes.setTime("SURVIVAL", 5L);

        assertEquals(5L, gmTimes.getTime("SURVIVAL"));
    }

    @Test
    public void stateIsRenamed() {
        GMTimes gmTimes = new GMTimes();
        gmTimes.setAllGMTimes(5L);
        gmTimes.renameState("SURVIVAL", "Survival");

        assertEquals(0L, gmTimes.getTime("SURVIVAL"));
        assertEquals(5L, gmTimes.getTime("Survival"));
    }

    @Test
    public void stateIsChangedAppropriately() {
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
    public void stateIsChangedWhenStartTimeIsDefault() {
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
    public void stateIsChangedWhenBeginStateIsDefault() {
        GMTimes test = new GMTimes();
        test.changeState("CREATIVE", 5L);

        assertEquals(5L, test.getTime("CREATIVE"));

        test.changeState("ADVENTURE", 20L);

        assertEquals(20L, test.getTime("CREATIVE"));
        assertEquals(0L, test.getTime("ADVENTURE"));
    }
}