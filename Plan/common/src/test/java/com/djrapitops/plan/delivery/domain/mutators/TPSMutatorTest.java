/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.delivery.domain.mutators;

import com.djrapitops.plan.gathering.domain.TPS;
import com.djrapitops.plan.gathering.domain.builders.TPSBuilder;
import com.djrapitops.plugin.api.TimeAmount;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link TPSMutator}
 *
 * @author AuroraLS3
 */
class TPSMutatorTest {

    private static List<TPS> testData;
    private static long time;

    @BeforeAll
    static void setUpTestData() {
        testData = new ArrayList<>();

        time = System.currentTimeMillis();
        long twoMonthsAgo = time - TimeAmount.MONTH.toMillis(2L);

        for (long date = twoMonthsAgo; date < time; date += TimeUnit.MINUTES.toMillis(1L)) {
            testData.add(
                    TPSBuilder.get().date(date)
                            .tps(0.0)
                            .playersOnline(0)
                            .usedCPU(0.0)
                            .usedMemory(0)
                            .entities(0)
                            .chunksLoaded(0)
                            .freeDiskSpace(0)
                            .toTPS()
            );
        }
    }

    @Test
    void noDowntimeIsCorrect() {
        long expected = 0;
        long result = new TPSMutator(testData).serverDownTime();
        assertEquals(expected, result);
    }

    @Test
    void noDowntimeOnSingleEntry() {
        long expected = 0;
        long result = new TPSMutator(Collections.singletonList(
                TPSBuilder.get().date(time - TimeUnit.DAYS.toMillis(1L))
                        .tps(0.0)
                        .playersOnline(0)
                        .usedCPU(0.0)
                        .usedMemory(0)
                        .entities(0)
                        .chunksLoaded(0)
                        .freeDiskSpace(0)
                        .toTPS()
        )).serverDownTime();
        assertEquals(expected, result);
    }

    @Test
    void fullDowntime() {
        long periodLength = TimeUnit.MINUTES.toMillis(5L);
        long expected = TimeAmount.MONTH.toMillis(2L) - periodLength;

        TPSMutator tpsMutator = new TPSMutator(testData.stream()
                .filter(tps -> (tps.getDate() - time) % periodLength == 0)
                .collect(Collectors.toList()));
        assertFalse(tpsMutator.all().isEmpty());
        assertNotEquals(testData, tpsMutator.all());

        long result = tpsMutator.serverDownTime();
        assertEquals(expected, result);
    }

    @Test
    void filteredFullMonthDowntime() {
        long periodLength = TimeUnit.MINUTES.toMillis(5L);
        long expected = TimeAmount.MONTH.toMillis(1L) - periodLength;

        long monthAgo = time - TimeAmount.MONTH.toMillis(1L);
        TPSMutator tpsMutator = new TPSMutator(testData.stream()
                .filter(tps -> (tps.getDate() - time) % periodLength == 0)
                .collect(Collectors.toList()))
                .filterDataBetween(monthAgo, time);

        assertFalse(tpsMutator.all().isEmpty());
        assertNotEquals(testData, tpsMutator.filterDataBetween(monthAgo, time).all());

        long result = tpsMutator.serverDownTime();
        assertEquals(expected, result);
    }

    @Test
    void filteredFullMonthDowntimeWhenRandomOrder() {
        long periodLength = TimeUnit.MINUTES.toMillis(5L);
        long expected = TimeAmount.MONTH.toMillis(1L) - periodLength;

        List<TPS> randomOrder = new ArrayList<>(testData);
        Collections.shuffle(randomOrder);
        long monthAgo = time - TimeAmount.MONTH.toMillis(1L);
        TPSMutator tpsMutator = new TPSMutator(randomOrder.stream()
                .filter(tps -> (tps.getDate() - time) % periodLength == 0)
                .collect(Collectors.toList()))
                .filterDataBetween(monthAgo, time);

        assertFalse(tpsMutator.all().isEmpty());
        assertNotEquals(randomOrder, tpsMutator.filterDataBetween(monthAgo, time).all());

        long result = tpsMutator.serverDownTime();
        assertEquals(expected, result);
    }

    @Test
    void filterWorksCorrectly() {
        long monthAgo = time - TimeAmount.MONTH.toMillis(1L);
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