/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.main.java.com.djrapitops.plan.utilities;

import java.util.Date;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.DemographicsData;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.utilities.NewPlayerCreator;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.easymock.EasyMock;
import org.junit.After;
import static org.junit.Assert.assertTrue;
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
 * @author Risto
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(JavaPlugin.class)
public class NewPlayerCreatorTest {
    
    public NewPlayerCreatorTest() {
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
        EasyMock.expect(JavaPlugin.getPlugin(Plan.class)).andReturn(plan);
        EasyMock.expect(JavaPlugin.getPlugin(Plan.class)).andReturn(plan);
        PowerMock.replay(JavaPlugin.class);
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testCreateNewPlayer_Player() {
        OfflinePlayer p = MockUtils.mockPlayer2();
        UserData result = NewPlayerCreator.createNewPlayer(p);
        UserData exp = new UserData(p, new DemographicsData());
        exp.setLastGamemode(GameMode.SURVIVAL);
        exp.setLastPlayed(new Date().getTime());
        long zero = Long.parseLong("0");
        exp.setPlayTime(zero);
        exp.setTimesKicked(0);
        exp.setLoginTimes(0);
        exp.setLastGmSwapTime(zero);
        exp.setDeaths(0);
        exp.setMobKills(0);
        assertTrue(exp.equals(result));
    }

    @Test
    public void testCreateNewPlayer_OfflinePlayer() {
        Player p = MockUtils.mockPlayer2();
        UserData result = NewPlayerCreator.createNewPlayer(p);
        UserData exp = new UserData(p, new DemographicsData());
        exp.setLastGamemode(GameMode.SPECTATOR);
        exp.setLastPlayed(new Date().getTime());
        long zero = Long.parseLong("0");
        exp.setPlayTime(zero);
        exp.setTimesKicked(0);
        exp.setLoginTimes(0);
        exp.setLastGmSwapTime(zero);
        exp.setDeaths(0);
        exp.setMobKills(0);
        assertTrue(exp.equals(result));
    }

    @Test
    public void testCreateNewPlayer_OfflinePlayer_GameMode() {
        Player p = MockUtils.mockPlayer();
        UserData result = NewPlayerCreator.createNewPlayer(p, GameMode.CREATIVE);
        UserData exp = new UserData(p, new DemographicsData());
        exp.setLastGamemode(GameMode.CREATIVE);
        exp.setLastPlayed(new Date().getTime());
        long zero = Long.parseLong("0");
        exp.setPlayTime(zero);
        exp.setTimesKicked(0);
        exp.setLoginTimes(0);
        exp.setLastGmSwapTime(zero);
        exp.setDeaths(0);
        exp.setMobKills(0);
        assertTrue(exp.equals(result));
    }
    
}
