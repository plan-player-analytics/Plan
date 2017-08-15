package test.java.main.java.com.djrapitops.plan.data.time;

import main.java.com.djrapitops.plan.data.time.GMTimes;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class GMTimesTest {
    @Test
    public void setAllGMTimes() throws Exception {
        GMTimes test = new GMTimes();
        test.setAllGMTimes(1L, 2L, 3L, 4L);
        Map<String, Long> times = test.getTimes();
        assertTrue(times.get("SURVIVAL") == 1L);
        assertTrue(times.get("CREATIVE") == 2L);
        assertTrue(times.get("ADVENTURE") == 3L);
        assertTrue(times.get("SPECTATOR") == 4L);
    }

    @Test
    public void setAllGMTimesTooFew() throws Exception {
        GMTimes test = new GMTimes();
        test.setAllGMTimes(1L, 2L);
        Map<String, Long> times = test.getTimes();
        assertTrue(times.get("SURVIVAL") == 1L);
        assertTrue(times.get("CREATIVE") == 2L);
        assertTrue(times.get("ADVENTURE") == 0L);
        assertTrue(times.get("SPECTATOR") == 0L);
    }

    @Test
    public void setAllGMTimesTooMany() throws Exception {
        GMTimes test = new GMTimes();
        test.setAllGMTimes(1L, 2L, 3L, 4L, 5L, 6L);
        Map<String, Long> times = test.getTimes();
        assertTrue(times.get("SURVIVAL") == 1L);
        assertTrue(times.get("CREATIVE") == 2L);
        assertTrue(times.get("ADVENTURE") == 3L);
        assertTrue(times.get("SPECTATOR") == 4L);
    }

    @Test
    public void resetTimes() throws Exception {
        GMTimes test = new GMTimes();
        test.setAllGMTimes(4, 3, 2, 1);
        test.resetTimes(10);
        assertTrue(10L == test.getTime("SURVIVAL"));
        assertTrue(0L == test.getTime("ADVENTURE"));
    }

    @Test
    public void setTime() throws Exception {
        GMTimes test = new GMTimes();
        test.setTime("SURVIVAL", 5L);
        assertTrue(5L == test.getTime("SURVIVAL"));
    }

    @Test
    public void renameState() throws Exception {
        GMTimes test = new GMTimes();
        test.setAllGMTimes(5L);
        test.renameState("SURVIVAL", "Survival");
        assertTrue(0L == test.getTime("SURVIVAL"));
        assertTrue(5L == test.getTime("Survival"));
    }

    @Test
    public void changeStateNormal() throws Exception {
        GMTimes test = new GMTimes(new HashMap<>(), "SURVIVAL", 0);
        test.changeState("CREATIVE", 5L);
        assertTrue(5L == test.getTime("SURVIVAL"));
        assertTrue(0L == test.getTime("CREATIVE"));
        test.changeState("ADVENTURE", 20L);
        assertTrue(5L == test.getTime("SURVIVAL"));
        assertTrue(15L == test.getTime("CREATIVE"));
        assertTrue(0L == test.getTime("ADVENTURE"));
    }

    @Test
    public void changeStateMissingStartTime() throws Exception {
        GMTimes test = new GMTimes("SURVIVAL");
        test.changeState("CREATIVE", 5L);
        assertTrue(5L == test.getTime("SURVIVAL"));
        assertTrue(0L == test.getTime("CREATIVE"));
        test.changeState("ADVENTURE", 20L);
        assertTrue(5L == test.getTime("SURVIVAL"));
        assertTrue(15L == test.getTime("CREATIVE"));
        assertTrue(0L == test.getTime("ADVENTURE"));
    }

    @Test
    public void changeStateMissingStartState() throws Exception {
        GMTimes test = new GMTimes();
        test.changeState("CREATIVE", 5L);
        assertTrue(5L == test.getTime("CREATIVE"));
        test.changeState("ADVENTURE", 20L);
        assertTrue(20L == test.getTime("CREATIVE"));
        assertTrue(0L == test.getTime("ADVENTURE"));
    }
}