package test.java.main.java.com.djrapitops.plan.ui.graphs;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import main.java.com.djrapitops.plan.data.SessionData;
import main.java.com.djrapitops.plan.ui.graphs.PlayerActivityGraphCreator;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author Rsl1122
 */
public class PlayerActivityGraphCreatorTest {

    /**
     *
     */
    public PlayerActivityGraphCreatorTest() {
    }

    /**
     *
     */
    @Test
    public void testGenerateDataArray() {
        List<SessionData> sessionData = createRandomSessionDataList();
        long scale = 2592000L * 1000L;
        String result = PlayerActivityGraphCreator.generateDataArray(sessionData, scale, 20)[1];
        assertTrue("0", 0 < result.length());
    }

    /**
     *
     * @return
     */
    public static List<SessionData> createRandomSessionDataList() {
        List<SessionData> list = new ArrayList<>();
        Random r = new Random();
        long now = new Date().toInstant().getEpochSecond();
        while (list.size() < 500) {
            int randomStart = r.nextInt(2592000);
            long start = now - (long) (randomStart + 10);
            long end = start + (long) r.nextInt(randomStart);
            list.add(new SessionData((start * (long) 1000), (end * (long) 1000)));
        }
        return list;
    }

    @Test
    public void testGetCount() {
        List<Long> test = new ArrayList<>();
        long exp = 5;
        test.add(5000L);
        test.add(5000L);
        test.add(5000L);
        test.add(5000L);
        test.add(5000L);
        test.add(0L);
        test.add(3450L);
        test.add(37560L);
        long result = PlayerActivityGraphCreator.getCount(test, 5000L);
        assertEquals(exp, result);
    }

    @Test
    public void testFilterSessions() {
        List<SessionData> test = new ArrayList<>();
        SessionData invalid = new SessionData(0);
        test.add(invalid);
        long now = 10000L;
        long nowMinusScale = now - 3000L;
        SessionData valid1 = new SessionData(9000L, 11000L);
        test.add(valid1);
        SessionData valid2 = new SessionData(8000L, 10000L);
        test.add(valid2);
        SessionData valid3 = new SessionData(7000L, 9000L);
        test.add(valid3);
        SessionData invalid2 = new SessionData(5000L, 5500L);
        test.add(invalid2);
        List<List<Long>> result = PlayerActivityGraphCreator.filterAndTransformSessions(test, nowMinusScale);
        List<Long> starts = result.get(0);
        List<Long> ends = result.get(1);
        assertTrue("Contained invalid session" + starts, !starts.contains(invalid.getSessionStart()));
        assertTrue("Contained invalid session" + starts, !starts.contains(invalid2.getSessionStart()));
        assertTrue("Contained invalid session" + ends, !ends.contains(invalid2.getSessionEnd()));
        assertTrue("Contained invalid session" + ends, !ends.contains(invalid2.getSessionEnd()));
        assertTrue("Did not contain valid session" + starts, starts.contains(valid1.getSessionStart()));
        assertTrue("Did not contain valid session" + ends, ends.contains(valid1.getSessionEnd()));
        assertTrue("Did not contain valid session" + starts, starts.contains(valid2.getSessionStart()));
        assertTrue("Did not contain valid session" + ends, ends.contains(valid2.getSessionEnd()));
        assertTrue("Did not contain valid session" + starts, starts.contains(valid3.getSessionStart()));
        assertTrue("Did not contain valid session" + ends, ends.contains(valid3.getSessionEnd()));
    }

    @Test
    public void testGetSecond() {
        Date test = new Date();
        long exp = test.toInstant().getEpochSecond() * 1000L;
        long result = PlayerActivityGraphCreator.getSecond(test.getTime());
        assertEquals(exp, result);
    }

    @Test
    public void testGetSecond2() {
        long exp = 2000L;
        long result = PlayerActivityGraphCreator.getSecond(2456L);
        assertEquals(exp, result);
    }

    @Test
    public void testGetSecond3() {
        long exp = 2000L;
        long result = PlayerActivityGraphCreator.getSecond(2956L);
        assertEquals(exp, result);
    }
}
