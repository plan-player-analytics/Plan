package com.djrapitops.plan.system.processing.processors.player;

import com.djrapitops.plan.data.store.objects.DateObj;
import com.djrapitops.plan.utilities.analysis.Median;
import com.djrapitops.plugin.api.TimeAmount;
import org.junit.Before;
import org.junit.Test;
import utilities.RandomData;
import utilities.TestConstants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/**
 * Test for {@link PingInsertProcessor}.
 *
 * @author Rsl1122
 */
public class PingInsertProcessorTest {

    private List<DateObj<Integer>> testPing;

    @Before
    public void setUp() {
        testPing = new ArrayList<>();

        for (int i = 0; i < TimeAmount.MINUTE.ms(); i += TimeAmount.SECOND.ms() * 2L) {
            testPing.add(new DateObj<>(i, RandomData.randomInt(1, 4000)));
        }
    }

    @Test
    public void testMedian() {
        List<Integer> collect = testPing.stream().map(DateObj::getValue).sorted().collect(Collectors.toList());
        System.out.println(collect);
        int expected = (int) Median.forInt(collect).calculate();
        int result = new PingInsertProcessor(TestConstants.PLAYER_ONE_UUID, new ArrayList<>()).getMeanValue(testPing);
        System.out.println(result);

        assertEquals(expected, result);
    }

    @Test
    public void testMedianSingleEntry() {
        int expected = 50;
        int result = new PingInsertProcessor(TestConstants.PLAYER_ONE_UUID, new ArrayList<>()).getMeanValue(
                Collections.singletonList(new DateObj<>(0, expected))
        );

        assertEquals(expected, result);
    }

    @Test
    public void testMedianEmpty() {
        int expected = -1;
        int result = new PingInsertProcessor(TestConstants.PLAYER_ONE_UUID, new ArrayList<>()).getMeanValue(
                Collections.emptyList()
        );

        assertEquals(expected, result);
    }
}