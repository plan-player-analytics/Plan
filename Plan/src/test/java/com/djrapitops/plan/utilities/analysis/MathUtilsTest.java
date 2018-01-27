/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.djrapitops.plan.utilities.analysis;

import org.junit.Test;
import utilities.RandomData;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * @author Rsl1122
 */
public class MathUtilsTest {

    @Test
    public void testAverageInt() {
        List<Integer> integers = Arrays.asList(0, 20, 5, 15);

        double exp = 10;
        double result = MathUtils.averageInt(integers.stream());

        assertTrue(Double.compare(exp, result) == 0);
    }

    @Test
    public void testAverageIntEmpty() {
        List<Integer> integers = Collections.emptyList();

        double exp = 0;
        double result = MathUtils.averageInt(integers.stream());

        assertTrue(result + "/" + exp, Double.compare(exp, result) == 0);
    }

    @Test
    public void testAverageLongCollection() {
        List<Long> longs = Arrays.asList(0L, 20L, 5L, 15L);

        double exp = 10;
        double result = MathUtils.averageLong(longs);

        assertTrue(result + "/" + exp, Double.compare(exp, result) == 0);
    }

    @Test
    public void testAverageDouble() {
        List<Double> doubles = Arrays.asList(0.0, 20.5, 4.5, 15.0);

        double exp = 10;
        double result = MathUtils.averageDouble(doubles.stream());

        assertTrue(result + "/" + exp, Double.compare(exp, result) == 0);

    }

    @Test
    public void testAverage() {
        double exp = 10;
        double result = MathUtils.average(40, 4);

        assertTrue(result + "/" + exp, Double.compare(exp, result) == 0);
    }

    @Test
    public void testCountTrueBoolean() {
        List<Boolean> booleans = new ArrayList<>();

        int exp = RandomData.randomInt(0, 1000);
        for (int i = 0; i < exp; i++) {
            booleans.add(true);
        }

        for (int i = exp; i < RandomData.randomInt(100, 1000); i++) {
            booleans.add(false);
        }

        long result = MathUtils.countTrueBoolean(booleans.stream());

        assertTrue(result + "/" + exp, Double.compare(exp, result) == 0);
    }

    @Test
    public void testSumInt() {
        List<Serializable> serializable = Arrays.asList(0, 20, 5, 15);

        double exp = 40;
        double result = MathUtils.sumInt(serializable.stream());

        assertTrue(result + "/" + exp, Double.compare(exp, result) == 0);
    }

    @Test
    public void testSumLong() {
        List<Serializable> serializable = Arrays.asList(0L, 20L, 5L, 15L);

        long exp = 40;
        long result = MathUtils.sumLong(serializable.stream());

        assertTrue(result + "/" + exp, Double.compare(exp, result) == 0);
    }

    @Test
    public void testSumDouble() {
        List<Serializable> serializable = Arrays.asList(0.0, 50.4, 45.0, 5.0531541);

        double exp = 100.4531541;
        double result = MathUtils.sumDouble(serializable.stream());

        assertTrue(result + "/" + exp, Double.compare(exp, result) == 0);
    }

    @Test
    public void testRoundDouble() {
        double exp = 412.5123125123;
        double result = MathUtils.round(exp);

        assertTrue(result + "/" + exp, Double.compare(412.51, result) == 0);
    }
}
