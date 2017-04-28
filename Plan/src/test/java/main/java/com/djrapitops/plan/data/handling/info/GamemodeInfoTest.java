/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.main.java.com.djrapitops.plan.data.handling.info;

import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.DemographicsData;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.handling.info.GamemodeInfo;
import org.bukkit.GameMode;
import org.bukkit.plugin.java.JavaPlugin;
import org.easymock.EasyMock;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import test.java.utils.MockUtils;
import test.java.utils.TestInit;

/**
 *
 * @author Rsl1122
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(JavaPlugin.class)
public class GamemodeInfoTest {

    /**
     *
     */
    public GamemodeInfoTest() {
    }

    /**
     *
     */
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

    /**
     *
     */
    @Test
    public void testProcess() {
        UserData data = new UserData(MockUtils.mockPlayer(), new DemographicsData());
        data.setPlayTime(100L);
        data.setLastGamemode(GameMode.CREATIVE);
        data.setLastGmSwapTime(50L);
        data.setLastPlayed(1000L);
        long time = 2000L;
        GamemodeInfo i = new GamemodeInfo(data.getUuid(), time, GameMode.SURVIVAL);
        assertTrue(i.process(data));
        Long result = data.getGmTimes().get(GameMode.CREATIVE);
        assertTrue("Gamemode time was "+result, result == 1050L);
        result = data.getPlayTime();
        assertTrue("Playtime was"+result, result == 1100L);
        result = data.getLastPlayed();
        assertTrue("Last Played was"+result, result == 2000L);
        GameMode lastGM = data.getLastGamemode();
        assertTrue("Last gm not Survival", lastGM == GameMode.SURVIVAL);
        result = data.getLastGmSwapTime();
        assertTrue("Last swaptime was "+result, result == 1100L);
    }
    
    /**
     *
     */
    @Test
    public void testProcessWrongUUID() {
        UserData data = new UserData(MockUtils.mockPlayer(), new DemographicsData());
        data.setPlayTime(100L);
        data.setLastGamemode(GameMode.CREATIVE);
        data.setLastGmSwapTime(50L);
        data.setLastPlayed(1000L);
        long time = 2000L;
        GamemodeInfo i = new GamemodeInfo(null, time, GameMode.SURVIVAL);
        assertTrue(!i.process(data));
        Long result = data.getGmTimes().get(GameMode.CREATIVE);
        assertTrue("Gamemode time was "+result, result == 0L);
        result = data.getPlayTime();
        assertTrue("Playtime was"+result, result == 100L);
        result = data.getLastPlayed();
        assertTrue("Last Played was"+result, result == 1000L);
        GameMode lastGM = data.getLastGamemode();
        assertTrue("Last gm not Survival", lastGM == GameMode.CREATIVE);
        result = data.getLastGmSwapTime();
        assertTrue("Last swaptime was "+result, result == 50L);
    }
    
    /**
     *
     */
    @Test
    public void testProcessNullGM() {
        UserData data = new UserData(MockUtils.mockPlayer(), new DemographicsData());
        data.setPlayTime(100L);
        data.setLastGamemode(GameMode.CREATIVE);
        data.setLastGmSwapTime(50L);
        data.setLastPlayed(1000L);
        long time = 2000L;
        GamemodeInfo i = new GamemodeInfo(data.getUuid(), time, null);
        assertTrue(!i.process(data));
        Long result = data.getGmTimes().get(GameMode.CREATIVE);
        assertTrue("Gamemode time was "+result, result == 0L);
        result = data.getPlayTime();
        assertTrue("Playtime was"+result, result == 100L);
        result = data.getLastPlayed();
        assertTrue("Last Played was"+result, result == 1000L);
        GameMode lastGM = data.getLastGamemode();
        assertTrue("Last gm not Survival", lastGM == GameMode.CREATIVE);
        result = data.getLastGmSwapTime();
        assertTrue("Last swaptime was "+result, result == 50L);
    }

}
