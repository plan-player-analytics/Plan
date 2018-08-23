package com.djrapitops.plan.utilities.analysis;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link Median}.
 *
 * @author Rsl1122
 */
public class MedianTest {

    @Test
    public void simpleOdd() {
        List<Integer> testValues = Arrays.asList(1, 3, 3, 6, 7, 8, 9);
        Collections.shuffle(testValues);
        double expected = 6;
        double result = Median.forInt(testValues).calculate();

        assertEquals(expected, result, 0.01);
    }

    @Test
    public void simpleEven() {
        List<Integer> testValues = Arrays.asList(1, 2, 3, 4, 5, 6, 8, 9);
        Collections.shuffle(testValues);
        double expected = 4.5;
        double result = Median.forInt(testValues).calculate();

        assertEquals(expected, result, 0.01);
    }

    @Test
    public void empty() {
        double expected = -1;
        double result = Median.forInt(Collections.emptyList()).calculate();

        assertEquals(expected, result, 0.01);
    }

    @Test
    public void singleValue() {
        double expected = 50;
        double result = Median.forInt(Collections.singletonList((int) expected)).calculate();

        assertEquals(expected, result, 0.01);
    }

    @Test
    public void twoValues() {
        List<Integer> testValues = Arrays.asList(1, 2);
        double expected = 1.5;
        double result = Median.forInt(testValues).calculate();

        assertEquals(expected, result, 0.01);
    }

    @Test
    public void overflowOdd() {
        List<Integer> testValues = Arrays.asList(Integer.MIN_VALUE, 2, Integer.MAX_VALUE);
        double expected = 2;
        double result = Median.forInt(testValues).calculate();

        assertEquals(expected, result, 0.01);
    }

    @Test
    public void overflowEven() {
        List<Integer> testValues = Arrays.asList(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
        double expected = -0.5;
        double result = Median.forInt(testValues).calculate();

        assertEquals(expected, result, 0.01);
    }

    @Test
    public void overflowEven2() {
        List<Integer> testValues = Arrays.asList(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
        double expected = Integer.MAX_VALUE;
        double result = Median.forInt(testValues).calculate();

        assertEquals(expected, result, 0.01);
    }

}