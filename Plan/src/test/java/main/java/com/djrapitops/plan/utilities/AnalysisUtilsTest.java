/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.main.java.com.djrapitops.plan.utilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.SessionData;
import main.java.com.djrapitops.plan.utilities.AnalysisUtils;
import org.bukkit.plugin.java.JavaPlugin;
import org.easymock.EasyMock;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import test.java.utils.TestInit;

/**
 *
 * @author Risto
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(JavaPlugin.class)
public class AnalysisUtilsTest {
    
    public AnalysisUtilsTest() {
    }

    @Before
    public void setUp() {
        TestInit t = new TestInit();
        assertTrue("Not set up", t.setUp());
        Plan plan = t.getPlanMock();
        PowerMock.mockStatic(JavaPlugin.class);
        EasyMock.expect(JavaPlugin.getPlugin(Plan.class)).andReturn(plan);
        EasyMock.expect(JavaPlugin.getPlugin(Plan.class)).andReturn(plan);
        EasyMock.expect(JavaPlugin.getPlugin(Plan.class)).andReturn(plan);
        PowerMock.replay(JavaPlugin.class);
//        PowerMock.verify(JavaPlugin.class);
    }
    
    @Test
    public void testIsActive() {
        long lastPlayed = new Date().getTime();
        long playTime = 12638934876L;
        int loginTimes = 4;
        boolean expResult = true;
        boolean result = AnalysisUtils.isActive(lastPlayed, playTime, loginTimes);
        assertEquals(expResult, result);
    }
    
    @Test
    public void testIsNotActive2() {
        long lastPlayed = new Date().getTime();
        long playTime = 0L;
        int loginTimes = 4;
        boolean expResult = false;
        boolean result = AnalysisUtils.isActive(lastPlayed, playTime, loginTimes);
        assertEquals(expResult, result);
    }    
    
    @Test
    public void testIsNotActive3() {
        long lastPlayed = new Date().getTime();
        long playTime = 12638934876L;
        int loginTimes = 0;
        boolean expResult = false;
        boolean result = AnalysisUtils.isActive(lastPlayed, playTime, loginTimes);
        assertEquals(expResult, result);
    }
    
    @Test
    public void testIsNotActive() {
        long lastPlayed = 0L;
        long playTime = 12638934876L;
        int loginTimes = 4;
        boolean expResult = false;
        boolean result = AnalysisUtils.isActive(lastPlayed, playTime, loginTimes);
        assertEquals(expResult, result);
    }

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
    
    @Test
    public void testGetNewPlayersEmpty() {
        List<Long> registered = new ArrayList<>();
        long scale = 1L;
        long now = 2L;
        int expResult = 0;
        int result = AnalysisUtils.getNewPlayers(registered, scale, now);
        assertEquals(expResult, result);
    }

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

    @Test
    public void testAverage() {
        Collection<Long> o = new ArrayList<>();
        o.add(0L);
        o.add(1L);
        o.add(2L);        
        o.add(3L);
        o.add(4L);
        long expResult = 2L;
        long result = AnalysisUtils.average(o);
        assertEquals(expResult, result);
    }
    
    @Test
    public void testAverageEmpty() {
        Collection<Long> list = new ArrayList<>();
        long expResult = 0L;
        long result = AnalysisUtils.average(list);
        assertEquals(expResult, result);
    }
    
}
