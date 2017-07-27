package test.java.main.java.com.djrapitops.plan.ui.graphs;

import main.java.com.djrapitops.plan.data.SessionData;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * @author Rsl1122
 */
public class PlayerActivityGraphCreatorTest {

    /**
     *
     */
    public PlayerActivityGraphCreatorTest() {
    }

    /**
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

    /**
     *
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     *
     */
    @Test
    @Ignore("Outdated")
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
//        List<List<Long>> result = PlayerActivityGraphCreator.filterAndTransformSessions(test, nowMinusScale);
//        List<Long> starts = result.get(0);
//        List<Long> ends = result.get(1);
//        assertTrue("Contained invalid session" + starts, !starts.contains(invalid.getSessionStart()));
//        assertTrue("Contained invalid session" + starts, !starts.contains(invalid2.getSessionStart()));
//        assertTrue("Contained invalid session" + ends, !ends.contains(invalid2.getSessionEnd()));
//        assertTrue("Contained invalid session" + ends, !ends.contains(invalid2.getSessionEnd()));
//        assertTrue("Did not contain valid session" + starts, starts.contains(valid1.getSessionStart()));
//        assertTrue("Did not contain valid session" + ends, ends.contains(valid1.getSessionEnd()));
//        assertTrue("Did not contain valid session" + starts, starts.contains(valid2.getSessionStart()));
//        assertTrue("Did not contain valid session" + ends, ends.contains(valid2.getSessionEnd()));
//        assertTrue("Did not contain valid session" + starts, starts.contains(valid3.getSessionStart()));
//        assertTrue("Did not contain valid session" + ends, ends.contains(valid3.getSessionEnd()));
    }
}
