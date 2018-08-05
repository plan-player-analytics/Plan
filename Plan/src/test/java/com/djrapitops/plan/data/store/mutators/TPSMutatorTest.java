package com.djrapitops.plan.data.store.mutators;

import com.djrapitops.plan.data.container.TPS;
import com.djrapitops.plan.data.container.builders.TPSBuilder;
import com.djrapitops.plugin.api.TimeAmount;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Tests for {@link TPSMutator}
 *
 * @author Rsl1122
 */
public class TPSMutatorTest {

    private List<TPS> testData;
    private long time;

    @Before
    public void setUp() {
        testData = new ArrayList<>();

        time = System.currentTimeMillis();
        long twoMonthsAgo = time - TimeAmount.MONTH.ms() * 2L;

        for (long date = twoMonthsAgo; date < time; date += TimeAmount.MINUTE.ms()) {
            testData.add(
                    TPSBuilder.get().date(date)
                            .tps(0.0)
                            .playersOnline(0)
                            .usedCPU(0.0)
                            .usedMemory(0)
                            .entities(0)
                            .chunksLoaded(0)
                            .toTPS()
            );
        }
    }

    @Test
    public void noDownTimeIsCorrect() {
        long expected = 0;
        long result = new TPSMutator(testData).serverDownTime();
        assertEquals(expected, result);
    }

    @Test
    public void noDownTimeOnSingleEntry() {
        long expected = 0;
        long result = new TPSMutator(Collections.singletonList(
                TPSBuilder.get().date(time - TimeAmount.DAY.ms())
                        .tps(0.0)
                        .playersOnline(0)
                        .usedCPU(0.0)
                        .usedMemory(0)
                        .entities(0)
                        .chunksLoaded(0)
                        .toTPS()
        )).serverDownTime();
        assertEquals(expected, result);
    }

    @Test
    public void fullDownTime() {
        long periodLength = TimeAmount.MINUTE.ms() * 5L;
        long expected = TimeAmount.MONTH.ms() * 2L - periodLength;

        TPSMutator tpsMutator = new TPSMutator(testData.stream()
                .filter(tps -> (tps.getDate() - time) % (periodLength) == 0)
                .collect(Collectors.toList()));
        assertFalse(tpsMutator.all().isEmpty());
        assertNotEquals(testData, tpsMutator.all());

        long result = tpsMutator.serverDownTime();
        assertEquals(expected, result);
    }

    @Test
    public void filteredFullMonthDownTime() {
        long periodLength = TimeAmount.MINUTE.ms() * 5L;
        long expected = TimeAmount.MONTH.ms() - periodLength;

        long monthAgo = time - TimeAmount.MONTH.ms();
        TPSMutator tpsMutator = new TPSMutator(testData.stream()
                .filter(tps -> (tps.getDate() - time) % (periodLength) == 0)
                .collect(Collectors.toList()))
                .filterDataBetween(monthAgo, time);

        assertFalse(tpsMutator.all().isEmpty());
        assertNotEquals(testData, tpsMutator.filterDataBetween(monthAgo, time).all());

        long result = tpsMutator.serverDownTime();
        assertEquals(expected, result);
    }

    @Test
    public void filteredFullMonthDownTimeWhenRandomOrder() {
        long periodLength = TimeAmount.MINUTE.ms() * 5L;
        long expected = TimeAmount.MONTH.ms() - periodLength;

        List<TPS> randomOrder = testData;
        Collections.shuffle(randomOrder);
        long monthAgo = time - TimeAmount.MONTH.ms();
        TPSMutator tpsMutator = new TPSMutator(randomOrder.stream()
                .filter(tps -> (tps.getDate() - time) % (periodLength) == 0)
                .collect(Collectors.toList()))
                .filterDataBetween(monthAgo, time);

        assertFalse(tpsMutator.all().isEmpty());
        assertNotEquals(randomOrder, tpsMutator.filterDataBetween(monthAgo, time).all());

        long result = tpsMutator.serverDownTime();
        assertEquals(expected, result);
    }

    @Test
    public void filterWorksCorrectly() {
        long monthAgo = time - TimeAmount.MONTH.ms();
        List<TPS> filtered = new TPSMutator(testData).filterDataBetween(monthAgo, time).all();

        for (TPS tps : filtered) {
            long date = tps.getDate();
            if (date < monthAgo) {
                fail("Data from over month ago was present");
            }
            if (date > time) {
                fail("Data from after 'time' was present");
            }
        }
    }
}