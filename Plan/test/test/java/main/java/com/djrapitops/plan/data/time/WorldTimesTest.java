package test.java.main.java.com.djrapitops.plan.data.time;

import main.java.com.djrapitops.plan.data.time.GMTimes;
import main.java.com.djrapitops.plan.data.time.WorldTimes;
import org.junit.Before;
import org.junit.Test;
import test.java.utils.RandomData;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

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
    public void testLotOfChanges() {
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

            printCurrentState(testedW, i, gm, world);
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

        // Tests GM Time calculation
        for (Map.Entry<String, List<String>> entry : testedW.entrySet()) {
            String world = entry.getKey();
            List<String> gmList = entry.getValue();
            for (int i = 0; i < gms.length; i++) {
                final String lookFor = gms[i];
                long gmCount = gmList.stream().filter(gmNum -> gmNum.equals(lookFor)).count();

                Optional<GMTimes> gmTimes = test.getGMTimes(world);

                if (gmTimes.isPresent()) {
                    long expected = gmCount * amount;
                    long actual = gmTimes.get().getTime(lookFor);
                    assertEquals(world + ": " + lookFor + ": " + expected + " Actual: " + actual, expected, actual);
                } else {
                    fail("GM Times can't not be present.");
                }
            }
        }
    }

    private void printCurrentState(Map<String, List<String>> testedW, int i, String gm, String world) {
        int sizeW1 = testedW.get(worldOne).size();
        int sizeW2 = testedW.get(worldTwo).size();

        StringBuilder b = new StringBuilder(""+i);
        while (b.length() < 3) {
            b.append(" ");
        }
        b.append(world).append(":").append(gm).append(": ");
        while (b.length() < 18) {
            b.append(" ");
        }
        b.append(sizeW1);

        while (b.length() < 21) {
            b.append(" ");
        }

        for (final String lookFor : gms) {
            long count = testedW.get(worldOne).stream().filter(gmNum -> gmNum.equals(lookFor)).count();
            b.append(" ").append(count);
        }
        while (b.length() < 29) {
            b.append(" ");
        }
        b.append(" |");
        for (final String lookFor : gms) {
            long count = testedW.get(worldTwo).stream().filter(gmNum -> gmNum.equals(lookFor)).count();
            b.append(" ").append(count);
        }
        while (b.length() < 40) {
            b.append(" ");
        }
        b.append(" ")
                .append(sizeW2)
                .append(" = ")
                .append(sizeW1 + sizeW2);
        System.out.println(b.toString());
    }

    // TODO Test where SessionData is ended, check if worldTimes & session length add up.
}