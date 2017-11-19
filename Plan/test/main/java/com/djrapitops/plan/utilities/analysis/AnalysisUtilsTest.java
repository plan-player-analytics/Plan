/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.com.djrapitops.plan.utilities.analysis;

import main.java.com.djrapitops.plan.data.Session;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import test.java.utils.TestInit;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Rsl1122
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(JavaPlugin.class)
public class AnalysisUtilsTest {

    @Before
    public void setUp() throws Exception {
        TestInit.init();
    }

    @Test
    public void testIsActive() {
        long lastPlayed = MiscUtils.getTime();
        long playTime = 12638934876L;
        int loginTimes = 4;

        boolean result = AnalysisUtils.isActive(System.currentTimeMillis(), lastPlayed, playTime, loginTimes);
        assertEquals(true, result);
    }

    @Test
    public void testIsNotActive2() {
        long lastPlayed = MiscUtils.getTime();
        long playTime = 0L;
        int loginTimes = 4;

        boolean result = AnalysisUtils.isActive(System.currentTimeMillis(), lastPlayed, playTime, loginTimes);
        assertEquals(false, result);
    }

    @Test
    public void testIsNotActive3() {
        long lastPlayed = MiscUtils.getTime();
        long playTime = 12638934876L;
        int loginTimes = 0;

        boolean result = AnalysisUtils.isActive(System.currentTimeMillis(), lastPlayed, playTime, loginTimes);
        assertEquals(false, result);
    }

    @Test
    public void testIsNotActive() {
        long lastPlayed = 0L;
        long playTime = 12638934876L;
        int loginTimes = 4;

        boolean result = AnalysisUtils.isActive(System.currentTimeMillis(), lastPlayed, playTime, loginTimes);
        assertEquals(false, result);
    }

    @Test
    public void testGetNewPlayers() {
        List<Long> registered = Arrays.asList(5L, 1L);

        long scale = 8L;
        long now = 10L;
        long result = AnalysisUtils.getNewPlayers(registered, scale, now);

        assertEquals(1L, result);
    }

    @Test
    public void testGetNewPlayersEmpty() {
        long scale = 1L;
        long now = 2L;
        long result = AnalysisUtils.getNewPlayers(Collections.emptyList(), scale, now);

        assertEquals(0L, result);
    }

    @Test
    public void testTransformSessionDataToLengths() {
        Collection<Session> data = Arrays.asList(
                new Session(1, 0L, 5L, 0, 0),
                new Session(1, 0L, 20L, 0, 0)
        );

        List<Long> expResult = Arrays.asList(5L, 20L);
        List<Long> result = AnalysisUtils.transformSessionDataToLengths(data);

        assertEquals(expResult, result);
    }
}
