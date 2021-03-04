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
package com.djrapitops.plan.utilities.analysis;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link Median}.
 *
 * @author AuroraLS3
 */
class MedianTest {

    @Test
    void simpleOdd() {
        List<Integer> testValues = Arrays.asList(1, 3, 3, 6, 7, 8, 9);
        Collections.shuffle(testValues);
        double expected = 6;
        double result = Median.forList(testValues).calculate();

        assertEquals(expected, result, 0.01);
    }

    @Test
    void simpleEven() {
        List<Integer> testValues = Arrays.asList(1, 2, 3, 4, 5, 6, 8, 9);
        Collections.shuffle(testValues);
        double expected = 4.5;
        double result = Median.forList(testValues).calculate();

        assertEquals(expected, result, 0.01);
    }

    @Test
    void empty() {
        double expected = -1;
        double result = Median.forList(new ArrayList<Integer>()).calculate();

        assertEquals(expected, result, 0.01);
    }

    @Test
    void singleValue() {
        double expected = 50;
        double result = Median.forList(Collections.singletonList((int) expected)).calculate();

        assertEquals(expected, result, 0.01);
    }

    @Test
    void twoValues() {
        List<Integer> testValues = Arrays.asList(1, 2);
        double expected = 1.5;
        double result = Median.forList(testValues).calculate();

        assertEquals(expected, result, 0.01);
    }

    @Test
    void overflowOdd() {
        List<Integer> testValues = Arrays.asList(Integer.MIN_VALUE, 2, Integer.MAX_VALUE);
        double expected = 2;
        double result = Median.forList(testValues).calculate();

        assertEquals(expected, result, 0.01);
    }

    @Test
    void overflowEven() {
        List<Integer> testValues = Arrays.asList(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
        double expected = -0.5;
        double result = Median.forList(testValues).calculate();

        assertEquals(expected, result, 0.01);
    }

    @Test
    void overflowEven2() {
        List<Integer> testValues = Arrays.asList(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
        double expected = Integer.MAX_VALUE;
        double result = Median.forList(testValues).calculate();

        assertEquals(expected, result, 0.01);
    }

}