package test.java.main.java.com.djrapitops.plan.ui.graphs;

import main.java.com.djrapitops.plan.data.SessionData;
import org.junit.Before;

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
}
