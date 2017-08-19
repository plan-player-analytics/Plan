/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.main.java.com.djrapitops.plan.utilities.analysis;

import main.java.com.djrapitops.plan.utilities.analysis.MathUtils;
import org.junit.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertTrue;

/**
 * @author Rsl1122
 */
public class MathUtilsTest {

    /**
     *
     */
    public MathUtilsTest() {
    }

    /**
     *
     */
    @Test
    public void testAverageInt() {
        List<Integer> l = new ArrayList<>();
        double exp = 10;
        l.add(0);
        l.add(20);
        l.add(5);
        l.add(15);
        double result = MathUtils.averageInt(l.stream());
        assertTrue(Double.compare(exp, result) == 0);
    }

    /**
     *
     */
    @Test
    public void testAverageIntEmpty() {
        List<Integer> l = Collections.emptyList();
        double exp = 0;
        double result = MathUtils.averageInt(l.stream());
        assertTrue(result + "/" + exp, Double.compare(exp, result) == 0);
    }

    /**
     *
     */
    @Test
    public void testAverageLongCollection() {
        List<Long> l = new ArrayList<>();
        double exp = 10;
        l.add(0L);
        l.add(20L);
        l.add(5L);
        l.add(15L);
        double result = MathUtils.averageLong(l);
        assertTrue(result + "/" + exp, Double.compare(exp, result) == 0);
    }

    /**
     *
     */
    @Test
    public void testAverageDouble() {
        List<Double> l = new ArrayList<>();
        double exp = 10;
        l.add(0.0);
        l.add(20.5);
        l.add(4.5);
        l.add(15.0);
        double result = MathUtils.averageDouble(l.stream());
        assertTrue(result + "/" + exp, Double.compare(exp, result) == 0);

    }

    /**
     *
     */
    @Test
    public void testAverage() {
        double exp = 10;
        double result = MathUtils.average(40, 4);
        assertTrue(result + "/" + exp, Double.compare(exp, result) == 0);
    }

    /**
     *
     */
    @Test
    public void testCountTrueBoolean() {
        List<Boolean> l = new ArrayList<>();
        int exp = new Random().nextInt(1000);
        for (int i = 0; i < exp; i++) {
            l.add(true);
        }
        for (int i = exp; i < 1000; i++) {
            l.add(false);
        }
        long result = MathUtils.countTrueBoolean(l.stream());
        assertTrue(result + "/" + exp, Double.compare(exp, result) == 0);
    }

    /**
     *
     */
    @Test
    public void testSumInt() {
        List<Serializable> l = new ArrayList<>();
        double exp = 40;
        l.add(0);
        l.add(20);
        l.add(5);
        l.add(15);
        double result = MathUtils.sumInt(l.stream());
        assertTrue(result + "/" + exp, Double.compare(exp, result) == 0);
    }

    /**
     *
     */
    @Test
    public void testSumLong() {
        List<Serializable> l = new ArrayList<>();
        long exp = 40;
        l.add(0L);
        l.add(20L);
        l.add(5L);
        l.add(15L);
        long result = MathUtils.sumLong(l.stream());
        assertTrue(result + "/" + exp, Double.compare(exp, result) == 0);
    }

    /**
     *
     */
    @Test
    public void testSumDouble() {
        List<Serializable> l = new ArrayList<>();
        double exp = 100.4531541;
        l.add(0.0);
        l.add(50.4);
        l.add(45.0);
        l.add(5.0531541);
        double result = MathUtils.sumDouble(l.stream());
        assertTrue(result + "/" + exp, Double.compare(exp, result) == 0);
    }

    @Test
    public void testRoundDouble() {
        double exp = 412.5123125123;
        double roundedExp = MathUtils.round(exp);

        assertTrue("", Double.compare(412.51, roundedExp) == 0);
    }
}
