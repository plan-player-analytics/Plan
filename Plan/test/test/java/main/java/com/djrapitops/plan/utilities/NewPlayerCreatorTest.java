/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.main.java.com.djrapitops.plan.utilities;

import com.djrapitops.plugin.utilities.player.Gamemode;
import com.djrapitops.plugin.utilities.player.IOfflinePlayer;
import com.djrapitops.plugin.utilities.player.IPlayer;
import com.djrapitops.plugin.utilities.player.bukkit.BukkitOfflinePlayer;
import com.djrapitops.plugin.utilities.player.bukkit.BukkitPlayer;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import main.java.com.djrapitops.plan.utilities.NewPlayerCreator;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import test.java.utils.MockUtils;

import static org.junit.Assert.assertTrue;

/**
 * @author Rsl1122
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(JavaPlugin.class)
public class NewPlayerCreatorTest {

    /**
     *
     */
    public NewPlayerCreatorTest() {
    }

    /**
     *
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     *
     */
    @After
    public void tearDown() {
    }

    /**
     *
     */
    @Test
    public void testCreateNewPlayer() {
        IOfflinePlayer p = BukkitOfflinePlayer.wrap(MockUtils.mockPlayer2());
        UserData result = NewPlayerCreator.createNewOfflinePlayer(p);
        UserData exp = new UserData(p);
        exp.getGmTimes().setState("SURVIVAL");
        exp.setLastPlayed(MiscUtils.getTime());
        exp.setPlayTime(0);
        exp.setTimesKicked(0);
        exp.setLoginTimes(0);
        exp.setDeaths(0);
        exp.setMobKills(0);
        assertTrue(exp.equals(result));
    }

    /**
     *
     */
    @Test
    public void testCreateNewOfflinePlayer() {
        IPlayer p = BukkitPlayer.wrap(MockUtils.mockPlayer2());
        UserData result = NewPlayerCreator.createNewPlayer(p);
        UserData exp = new UserData(p);
        exp.getGmTimes().setState("SPECTATOR");
        exp.setLastPlayed(MiscUtils.getTime());
        exp.setPlayTime(0);
        exp.setTimesKicked(0);
        exp.setLoginTimes(0);
        exp.setDeaths(0);
        exp.setMobKills(0);
        assertTrue(exp.equals(result));
    }

    /**
     *
     */
    @Test
    public void testCreateNewPlayerWithGameMode() {
        IOfflinePlayer p = BukkitOfflinePlayer.wrap(MockUtils.mockPlayer());
        UserData result = NewPlayerCreator.createNewPlayer(p, Gamemode.CREATIVE);
        UserData exp = new UserData(p);
        exp.getGmTimes().setState("CREATIVE");
        exp.setLastPlayed(MiscUtils.getTime());
        exp.setPlayTime(0);
        exp.setTimesKicked(0);
        exp.setLoginTimes(0);
        exp.setDeaths(0);
        exp.setMobKills(0);
        assertTrue(exp.equals(result));
    }

}
