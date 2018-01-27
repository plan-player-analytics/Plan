package com.djrapitops.plan.data.time;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GMTimesTest {
    @Test
    public void testSetAllGMTimes() {
        GMTimes gmTimes = new GMTimes();
        gmTimes.setAllGMTimes(1L, 2L, 3L, 4L);

        Map<String, Long> times = gmTimes.getTimes();

        assertEquals(times.size(), 4);
        assertTrue(times.get("SURVIVAL") == 1L);
        assertTrue(times.get("CREATIVE") == 2L);
        assertTrue(times.get("ADVENTURE") == 3L);
        assertTrue(times.get("SPECTATOR") == 4L);
    }

    @Test
    public void testSetAllGMTimesTooFew() {
        GMTimes gmTimes = new GMTimes();
        gmTimes.setAllGMTimes(1L, 2L);

        Map<String, Long> times = gmTimes.getTimes();

        assertEquals(times.size(), 4);
        assertTrue(times.get("SURVIVAL") == 1L);
        assertTrue(times.get("CREATIVE") == 2L);
        assertTrue(times.get("ADVENTURE") == 0L);
        assertTrue(times.get("SPECTATOR") == 0L);
    }

    @Test
    public void testSetAllGMTimesTooMany() {
        GMTimes gmTimes = new GMTimes();
        gmTimes.setAllGMTimes(1L, 2L, 3L, 4L, 5L, 6L);

        Map<String, Long> times = gmTimes.getTimes();

        assertEquals(times.size(), 4);
        assertTrue(times.get("SURVIVAL") == 1L);
        assertTrue(times.get("CREATIVE") == 2L);
        assertTrue(times.get("ADVENTURE") == 3L);
        assertTrue(times.get("SPECTATOR") == 4L);
    }

    @Test
    public void testResetTimes() {
        GMTimes gmTimes = new GMTimes();
        gmTimes.setAllGMTimes(4, 3, 2, 1);
        gmTimes.resetTimes(10);

        assertTrue(gmTimes.getTotal() == 10L);
        assertTrue(gmTimes.getTime("SURVIVAL") == 10L);
        assertTrue(gmTimes.getTime("ADVENTURE") == 0L);
    }

    @Test
    public void testSetTime() {
        GMTimes gmTimes = new GMTimes();
        gmTimes.setTime("SURVIVAL", 5L);

        assertTrue(gmTimes.getTime("SURVIVAL") == 5L);
    }

    @Test
    public void testRenameState() {
        GMTimes gmTimes = new GMTimes();
        gmTimes.setAllGMTimes(5L);
        gmTimes.renameState("SURVIVAL", "Survival");

        assertTrue(gmTimes.getTime("SURVIVAL") == 0L);
        assertTrue(gmTimes.getTime("Survival") == 5L);
    }

    @Test
    public void testChangeStateNormal() {
        GMTimes gmTimes = new GMTimes(new HashMap<>(), "SURVIVAL", 0);
        gmTimes.changeState("CREATIVE", 5L);

        assertTrue(gmTimes.getTime("SURVIVAL") == 5L);
        assertTrue(gmTimes.getTime("CREATIVE") == 0L);

        gmTimes.changeState("ADVENTURE", 20L);

        assertTrue(gmTimes.getTime("SURVIVAL") == 5L);
        assertTrue(gmTimes.getTime("CREATIVE") == 15L);
        assertTrue(gmTimes.getTime("ADVENTURE") == 0L);
    }

    @Test
    public void testChangeStateMissingStartTime() {
        GMTimes gmTimes = new GMTimes("SURVIVAL");
        gmTimes.changeState("CREATIVE", 5L);

        assertTrue(5L == gmTimes.getTime("SURVIVAL"));
        assertTrue(0L == gmTimes.getTime("CREATIVE"));

        gmTimes.changeState("ADVENTURE", 20L);

        assertTrue(5L == gmTimes.getTime("SURVIVAL"));
        assertTrue(15L == gmTimes.getTime("CREATIVE"));
        assertTrue(0L == gmTimes.getTime("ADVENTURE"));
    }

    @Test
    public void testChangeStateMissingStartState() {
        GMTimes test = new GMTimes();
        test.changeState("CREATIVE", 5L);

        assertTrue(5L == test.getTime("CREATIVE"));

        test.changeState("ADVENTURE", 20L);

        assertTrue(20L == test.getTime("CREATIVE"));
        assertTrue(0L == test.getTime("ADVENTURE"));
    }
}