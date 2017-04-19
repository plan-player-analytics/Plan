/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.main.java.com.djrapitops.plan.data.handling.info;

import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.DemographicsData;
import main.java.com.djrapitops.plan.data.SessionData;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.handling.info.LogoutInfo;
import org.bukkit.GameMode;
import org.bukkit.plugin.java.JavaPlugin;
import org.easymock.EasyMock;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import test.java.utils.MockUtils;
import test.java.utils.TestInit;

/**
 *
 * @author Risto
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(JavaPlugin.class)
public class LogoutInfoTest {

    public LogoutInfoTest() {
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
        EasyMock.expect(JavaPlugin.getPlugin(Plan.class)).andReturn(plan);
        PowerMock.replay(JavaPlugin.class);
//        PowerMock.verify(JavaPlugin.class);
    }

    @Test
    public void testProcess() {
        UserData data = new UserData(MockUtils.mockPlayer(), new DemographicsData());
        data.setLastPlayed(10L);
        data.updateBanned(false);
        long time = 20L;
        Exception ex = null;
        data.setLastGamemode(GameMode.SURVIVAL);
        LogoutInfo i = new LogoutInfo(data.getUuid(), time, true, GameMode.CREATIVE, new SessionData(0, 1));
        assertTrue(i.process(data));
        assertTrue("Last Played wrong", data.getLastPlayed() == 20L);
        assertTrue("Playtime wrong", data.getPlayTime() == 10L);
        assertTrue("Banned wrong", data.isBanned());
        assertTrue("Didn't process gamemode", data.getLastGamemode() == GameMode.CREATIVE);
        assertEquals(1, data.getSessions().size());
    }

    @Test
    public void testProcessWrongUUID() {
        UserData data = new UserData(MockUtils.mockPlayer(), new DemographicsData());
        data.setLastPlayed(10L);
        data.updateBanned(false);
        long time = 20L;
        Exception ex = null;
        LogoutInfo i = new LogoutInfo(null, time, true, GameMode.CREATIVE, new SessionData(0, 1));
        try {
            assertTrue(!i.process(data));
        } catch (NullPointerException e) {
            ex = e;
        }
        assertTrue("Caught endSessionException", ex == null);
        assertTrue("Last Played wrong", data.getLastPlayed() == 10L);
        assertTrue("Playtime wrong", data.getPlayTime() == 0L);
        assertTrue("Banned wrong", !data.isBanned());
        assertTrue("Didn't process gamemode", data.getLastGamemode() == GameMode.SURVIVAL);
        assertEquals(0, data.getSessions().size());
    }

}
