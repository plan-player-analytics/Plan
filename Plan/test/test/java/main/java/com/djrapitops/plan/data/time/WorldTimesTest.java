package test.java.main.java.com.djrapitops.plan.data.time;

import main.java.com.djrapitops.plan.data.time.GMTimes;
import main.java.com.djrapitops.plan.data.time.WorldTimes;
import org.junit.Before;
import org.junit.Test;
import test.java.utils.RandomData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author Rsl1122
 */
public class WorldTimesTest {

    private long time;
    private WorldTimes test;
    private final String worldOne = "ONE";
    private final String worldTwo = "TWO";

    private final String[] gms = GMTimes.getGMKeyArray();

    @Before
    public void setUp() throws Exception {
        test = new WorldTimes(worldOne, gms[0]);
        time = test.getGMTimes(worldOne).getLastStateChange();
    }

    @Test
    public void testWorldChange() {
        long changeTime = time + 1000L;
        test.updateState(worldTwo, gms[0], changeTime);
        assertEquals(1000L, test.getWorldPlaytime(worldOne));
        assertEquals(1000L, test.getGMTimes(worldOne).getTime(gms[0]));
    }

    @Test
    public void testGMChange() {
        long changeTime = time + 1000L;
        test.updateState(worldOne, gms[0], changeTime);
        assertEquals(1000L, test.getWorldPlaytime(worldOne));
        assertEquals(1000L, test.getGMTimes(worldOne).getTime(gms[0]));
    }

    @Test
    public void testBothTwiceChange() {
        long changeTime = time + 1000L;
        long changeTime2 = changeTime + 1000L;
        test.updateState(worldTwo, gms[2], changeTime);
        assertEquals(1000L, test.getWorldPlaytime(worldOne));
        assertEquals(1000L, test.getGMTimes(worldOne).getTime(gms[0]));
        test.updateState(worldOne, gms[1], changeTime2);
        assertEquals(1000L, test.getWorldPlaytime(worldOne));
        assertEquals(1000L, test.getGMTimes(worldOne).getTime(gms[0]));
        assertEquals(1000L, test.getGMTimes(worldTwo).getTime(gms[2]));
    }

    @Test
    public void testLotOfChangesWorldTime() {
        long amount = 1000L;
        String[] worlds = new String[]{worldOne, worldTwo};

        Map<String, List<String>> testedW = new HashMap<>();
        testedW.put(worldOne, new ArrayList<>());
        testedW.put(worldTwo, new ArrayList<>());

        String lastWorld = worldOne;
        String lastGM = gms[0];
        for (int i = 1; i <= 50; i++) {
            int wRndm = RandomData.randomInt(0, worlds.length);
            int gmRndm = RandomData.randomInt(0, gms.length);

            String world = worlds[wRndm];
            String gm = gms[gmRndm];
            testedW.get(lastWorld).add(lastGM);
            lastGM = gm;
            lastWorld = world;

            long time = i * amount + this.time;

            test.updateState(world, gm, time);
        }

        long worldOneCount = testedW.get(worldOne).size();
        long worldTwoCount = testedW.get(worldTwo).size();
        long worldTimeOne = worldOneCount * amount;
        long worldTimeTwo = worldTwoCount * amount;

        long time1 = test.getWorldPlaytime(worldOne);
        long time2 = test.getWorldPlaytime(worldTwo);

        // Tests World time calculation.
        assertEquals(amount * 50, time1 + time2);
        assertEquals(worldTimeOne, time1);
        assertEquals(worldTimeTwo, time2);
    }

    @Test
    public void testGMTrackingSingleWorld() {
        long changeTime = time + 1000L;
        long changeTime2 = changeTime + 1000L;
        GMTimes gmTimes = test.getGMTimes(worldOne);
        test.updateState(worldOne, "CREATIVE", changeTime);
        assertEquals(1000L, gmTimes.getTime("SURVIVAL"));
        assertEquals(0L, gmTimes.getTime("CREATIVE"));
        test.updateState(worldOne, "ADVENTURE", changeTime2);
        assertEquals(1000L, gmTimes.getTime("SURVIVAL"));
        assertEquals(1000L, gmTimes.getTime("CREATIVE"));
        assertEquals(0L, gmTimes.getTime("ADVENTURE"));
    }

    @Test
    public void testGMTrackingTwoWorlds() {
        long changeTime = time + 1000L;
        long changeTime2 = time + 2000L;
        GMTimes worldOneGMTimes = test.getGMTimes(worldOne);
        test.updateState(worldOne, "CREATIVE", changeTime);
        test.updateState(worldOne, "ADVENTURE", changeTime2);
        assertEquals(1000L, worldOneGMTimes.getTime("SURVIVAL"));
        assertEquals(1000L, worldOneGMTimes.getTime("CREATIVE"));
        assertEquals(0L, worldOneGMTimes.getTime("ADVENTURE"));

        test.updateState(worldTwo, "SURVIVAL", time + 3000L);
        GMTimes worldTwoGMTimes = test.getGMTimes(worldTwo);
        assertEquals(1000L, worldOneGMTimes.getTime("SURVIVAL"));
        assertEquals(1000L, worldOneGMTimes.getTime("CREATIVE"));
        assertEquals(1000L, worldOneGMTimes.getTime("ADVENTURE"));

        assertEquals(0L, worldTwoGMTimes.getTime("SURVIVAL"));
        assertEquals(0L, worldTwoGMTimes.getTime("CREATIVE"));
        assertEquals(0L, worldTwoGMTimes.getTime("ADVENTURE"));

        test.updateState(worldTwo, "CREATIVE", time + 4000L);

        assertEquals(1000L, worldOneGMTimes.getTime("SURVIVAL"));
        assertEquals(1000L, worldOneGMTimes.getTime("CREATIVE"));
        assertEquals(1000L, worldOneGMTimes.getTime("ADVENTURE"));

        assertEquals(1000L, worldTwoGMTimes.getTime("SURVIVAL"));
        assertEquals(0L, worldTwoGMTimes.getTime("CREATIVE"));

        test.updateState(worldTwo, "CREATIVE", time + 5000L);
        assertEquals(1000L, worldTwoGMTimes.getTime("SURVIVAL"));
        assertEquals(1000L, worldTwoGMTimes.getTime("CREATIVE"));

        // No change should occur.
        test.updateState(worldOne, "ADVENTURE", time + 5000L);
        assertEquals(1000L, worldOneGMTimes.getTime("ADVENTURE"));
        assertEquals(1000L, worldTwoGMTimes.getTime("CREATIVE"));
        test.updateState(worldTwo, "CREATIVE", time + 5000L);
        test.updateState(worldOne, "ADVENTURE", time + 6000L);
        assertEquals(1000L, worldOneGMTimes.getTime("ADVENTURE"));
        assertEquals(2000L, worldTwoGMTimes.getTime("CREATIVE"));

        test.updateState(worldTwo, "ADVENTURE", time + 7000L);
        assertEquals(2000L, worldTwoGMTimes.getTime("CREATIVE"));
        assertEquals(2000L, worldOneGMTimes.getTime("ADVENTURE"));
    }

    // TODO Test where Session is ended, check if worldTimes & session length add up.
}