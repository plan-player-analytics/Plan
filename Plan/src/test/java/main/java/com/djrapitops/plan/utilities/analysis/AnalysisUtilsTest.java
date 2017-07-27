/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.main.java.com.djrapitops.plan.utilities.analysis;

import main.java.com.djrapitops.plan.data.SessionData;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import main.java.com.djrapitops.plan.utilities.analysis.AnalysisUtils;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import test.java.utils.TestInit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Rsl1122
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(JavaPlugin.class)
public class AnalysisUtilsTest {

    /**
     *
     */
    public AnalysisUtilsTest() {
    }

    /**
     *
     */
    @Before
    public void setUp() throws Exception {
        TestInit t = TestInit.init();
    }

    /**
     *
     */
    @Test
    public void testIsActive() {
        long lastPlayed = MiscUtils.getTime();
        long playTime = 12638934876L;
        int loginTimes = 4;
        boolean result = AnalysisUtils.isActive(System.currentTimeMillis(), lastPlayed, playTime, loginTimes);
        assertEquals(true, result);
    }

    /**
     *
     */
    @Test
    public void testIsNotActive2() {
        long lastPlayed = MiscUtils.getTime();
        long playTime = 0L;
        int loginTimes = 4;
        boolean result = AnalysisUtils.isActive(System.currentTimeMillis(), lastPlayed, playTime, loginTimes);
        assertEquals(false, result);
    }

    /**
     *
     */
    @Test
    public void testIsNotActive3() {
        long lastPlayed = MiscUtils.getTime();
        long playTime = 12638934876L;
        int loginTimes = 0;
        boolean result = AnalysisUtils.isActive(System.currentTimeMillis(), lastPlayed, playTime, loginTimes);
        assertEquals(false, result);
    }

    /**
     *
     */
    @Test
    public void testIsNotActive() {
        long lastPlayed = 0L;
        long playTime = 12638934876L;
        int loginTimes = 4;
        boolean result = AnalysisUtils.isActive(System.currentTimeMillis(), lastPlayed, playTime, loginTimes);
        assertEquals(false, result);
    }

    /**
     *
     */
    @Test
    public void testGetNewPlayers() {
        List<Long> registered = new ArrayList<>();
        registered.add(5L);
        registered.add(1L);
        long scale = 8L;
        long now = 10L;
        int expResult = 1;
        int result = AnalysisUtils.getNewPlayers(registered, scale, now);
        assertEquals(expResult, result);
    }

    /**
     *
     */
    @Test
    public void testGetNewPlayersEmpty() {
        List<Long> registered = new ArrayList<>();
        long scale = 1L;
        long now = 2L;
        int expResult = 0;
        int result = AnalysisUtils.getNewPlayers(registered, scale, now);
        assertEquals(expResult, result);
    }

    /**
     *
     */
    @Test
    public void testTransformSessionDataToLengths() {
        Collection<SessionData> data = new ArrayList<>();
        data.add(new SessionData(0, 5L));
        data.add(new SessionData(0, 20L));
        data.add(new SessionData(0));
        List<Long> expResult = new ArrayList<>();
        expResult.add(5L);
        expResult.add(20L);
        List<Long> result = AnalysisUtils.transformSessionDataToLengths(data);
        assertEquals(expResult, result);
    }
}
