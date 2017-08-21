package test.java.main.java.com.djrapitops.plan.data.time;

import main.java.com.djrapitops.plan.data.time.GMTimes;
import main.java.com.djrapitops.plan.data.time.WorldTimes;
import org.junit.Before;
import org.junit.Test;
import test.java.utils.RandomData;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
        Optional<GMTimes> gmTimes = test.getGMTimes(worldOne);
        gmTimes.ifPresent(gmTimes1 ->
                time = gmTimes1.getLastStateChange()
        );
        System.out.println(test);
    }

    @Test
    public void testWorldChange() {
        long changeTime = time + 1000L;
        test.updateState(worldTwo, gms[0], changeTime);
        System.out.println(test);
        assertEquals(1000L, (long) test.getWorldPlaytime(worldOne).get());
        assertEquals(1000L, test.getGMTimes(worldOne).get().getTime(gms[0]));
    }

    @Test
    public void testGMChange() {
        long changeTime = time + 1000L;
        test.updateState(worldOne, gms[0], changeTime);
        System.out.println(test);
        assertEquals(1000L, (long) test.getWorldPlaytime(worldOne).get());
        assertEquals(1000L, test.getGMTimes(worldOne).get().getTime(gms[0]));
    }

    @Test
    public void testBothTwiceChange() {
        long changeTime = time + 1000L;
        long changeTime2 = changeTime + 1000L;
        test.updateState(worldTwo, gms[2], changeTime);
        System.out.println(test);
        assertEquals(1000L, (long) test.getWorldPlaytime(worldOne).get());
        assertEquals(1000L, test.getGMTimes(worldOne).get().getTime(gms[0]));
        test.updateState(worldOne, gms[1], changeTime2);
        System.out.println(test);
        assertEquals(1000L, (long) test.getWorldPlaytime(worldOne).get());
        assertEquals(1000L, test.getGMTimes(worldOne).get().getTime(gms[0]));
        assertEquals(1000L, test.getGMTimes(worldTwo).get().getTime(gms[2]));
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

        long time1 = 0L;
        long time2 = 0L;
        Optional<Long> worldPlaytime = test.getWorldPlaytime(worldOne);
        if (worldPlaytime.isPresent()) {
            time1 += worldPlaytime.get();
        }
        Optional<Long> worldPlaytime2 = test.getWorldPlaytime(worldTwo);
        if (worldPlaytime2.isPresent()) {
            time2 += worldPlaytime2.get();
        }
        System.out.println(test);

        // Tests World time calculation.
        assertEquals(amount * 50, time1 + time2);
        assertEquals(worldTimeOne, time1);
        assertEquals(worldTimeTwo, time2);
    }

    @Test
    public void testGMTrackingSingleWorld() {
        long changeTime = time + 1000L;
        long changeTime2 = changeTime + 1000L;
        GMTimes gmTimes = test.getGMTimes(worldOne).get();
        test.updateState(worldOne, "CREATIVE", changeTime);
        assertTrue(1000L == gmTimes.getTime("SURVIVAL"));
        assertTrue(0L == gmTimes.getTime("CREATIVE"));
        test.updateState(worldOne, "ADVENTURE", changeTime2);
        assertTrue(1000L == gmTimes.getTime("SURVIVAL"));
        assertTrue(1000L == gmTimes.getTime("CREATIVE"));
        assertTrue(0L == gmTimes.getTime("ADVENTURE"));
    }

    @Test
    public void testGMTrackingTwoWorlds() {
        long changeTime = time + 1000L;
        long changeTime2 = time + 2000L;
        GMTimes worldOneGMTimes = test.getGMTimes(worldOne).get();
        test.updateState(worldOne, "CREATIVE", changeTime);
        test.updateState(worldOne, "ADVENTURE", changeTime2);
        // Same state as above.

        test.updateState(worldTwo, "SURVIVAL", time + 3000L);
        assertTrue(1000L == worldOneGMTimes.getTime("SURVIVAL"));
        assertTrue(1000L == worldOneGMTimes.getTime("CREATIVE"));
        assertTrue(1000L == worldOneGMTimes.getTime("ADVENTURE"));

        GMTimes worldTwoGMTimes = test.getGMTimes(worldTwo).get();

        assertTrue(0L == worldTwoGMTimes.getTime("SURVIVAL"));
        assertTrue(0L == worldTwoGMTimes.getTime("CREATIVE"));
        assertTrue(0L == worldTwoGMTimes.getTime("ADVENTURE"));

        test.updateState(worldTwo, "CREATIVE", time + 4000L);

        assertTrue(1000L == worldOneGMTimes.getTime("SURVIVAL"));
        assertTrue(1000L == worldOneGMTimes.getTime("CREATIVE"));
        assertTrue(1000L == worldOneGMTimes.getTime("ADVENTURE"));

        assertTrue(1000L == worldTwoGMTimes.getTime("SURVIVAL"));
        assertTrue(0L == worldTwoGMTimes.getTime("CREATIVE"));

        test.updateState(worldTwo, "CREATIVE", time + 5000L);
        assertTrue(1000L == worldTwoGMTimes.getTime("SURVIVAL"));
        assertTrue(1000L == worldTwoGMTimes.getTime("CREATIVE"));

        // No change should occur.
        test.updateState(worldOne, "ADVENTURE", time + 5000L);
        assertTrue(1000L == worldOneGMTimes.getTime("ADVENTURE"));
        assertTrue(1000L == worldTwoGMTimes.getTime("CREATIVE"));
        test.updateState(worldTwo, "CREATIVE", time + 5000L);
        System.out.println(test);
        test.updateState(worldOne, "ADVENTURE", time + 6000L);
        System.out.println(test);
        assertTrue(1000L == worldOneGMTimes.getTime("ADVENTURE"));
        assertTrue(2000L == worldTwoGMTimes.getTime("CREATIVE"));

        test.updateState(worldTwo, "ADVENTURE", time + 7000L);
        assertTrue(2000L == worldTwoGMTimes.getTime("CREATIVE"));
        assertTrue(2000L == worldOneGMTimes.getTime("ADVENTURE"));
    }

    // TODO Test where SessionData is ended, check if worldTimes & session length add up.
}