/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.main.java.com.djrapitops.plan.data.handling;

import com.djrapitops.plugin.utilities.player.Gamemode;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.handling.GamemodeHandling;
import org.junit.Before;
import org.junit.Test;
import test.java.utils.MockUtils;

import static org.junit.Assert.assertTrue;

/**
 * @author Rsl1122
 */
public class GamemodeHandlingTest {

    /**
     *
     */
    public GamemodeHandlingTest() {
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
    @Test
    public void testProcessGamemodeInfo() {
        UserData data = MockUtils.mockUser();
        data.setPlayTime(100L);
        data.setLastGamemode("CREATIVE");
        data.setLastGmSwapTime(50L);
        data.setLastPlayed(1000L);
        long time = 2000L;
        GamemodeHandling.processGamemodeInfo(data, time, Gamemode.SURVIVAL);
        Long result = data.getGmTimes().get(Gamemode.CREATIVE.name());
        assertTrue("Gamemode time was " + result, result == 1050L);
        result = data.getPlayTime();
        assertTrue("Playtime was" + result, result == 1100L);
        result = data.getLastPlayed();
        assertTrue("Last Played was" + result, result == 2000L);
        String lastGM = data.getLastGamemode();
        assertTrue("Last gm not Survival", lastGM.equals("SURVIVAL"));
        result = data.getLastGmSwapTime();
        assertTrue("Last swaptime was " + result, result == 1100L);
    }

    /**
     *
     */
    @Test
    public void testProcessGamemodeInfoSameGM() {
        UserData data = MockUtils.mockUser();
        data.setPlayTime(100L);
        data.setLastGamemode("SURVIVAL");
        data.setLastGmSwapTime(50L);
        data.setLastPlayed(1000L);
        long time = 2000L;
        GamemodeHandling.processGamemodeInfo(data, time, Gamemode.SURVIVAL);
        Long result = data.getGmTimes().get(Gamemode.SURVIVAL.name());
        assertTrue("Gamemode time was " + result, result == 1050L);
        result = data.getPlayTime();
        assertTrue("Playtime was" + result, result == 1100L);
        result = data.getLastPlayed();
        assertTrue("Last Played was" + result, result == 2000L);
        String lastGM = data.getLastGamemode();
        assertTrue("Last gm not Survival", "SURVIVAL".equals(lastGM));
        result = data.getLastGmSwapTime();
        assertTrue("Last swaptime was " + result, result == 1100L);
    }

    /**
     *
     */
    @Test
    public void testProcessGamemodeInfoNullNewGM() {
        UserData data = MockUtils.mockUser();
        data.setPlayTime(100L);
        data.setLastGamemode("SURVIVAL");
        data.setLastGmSwapTime(50L);
        data.setLastPlayed(1000L);
        long time = 2000L;
        GamemodeHandling.processGamemodeInfo(data, time, null);
        Long result = data.getGmTimes().get(Gamemode.SURVIVAL.name());
        assertTrue("Gamemode time was " + result, result == 0L);
        result = data.getPlayTime();
        assertTrue("Playtime was" + result, result == 100L);
        result = data.getLastPlayed();
        assertTrue("Last Played was" + result, result == 1000L);
        String lastGM = data.getLastGamemode();
        assertTrue("Last gm not Survival", "SURVIVAL".equals(lastGM));
        result = data.getLastGmSwapTime();
        assertTrue("Last swaptime was " + result, result == 50L);
    }

    /**
     *
     */
    @Test
    public void testProcessGamemodeInfoNullOldGM() {
        UserData data = MockUtils.mockUser();
        data.setPlayTime(100L);
        data.setLastGamemode(null);
        data.setLastGmSwapTime(50L);
        data.setLastPlayed(1000L);
        long time = 2000L;
        GamemodeHandling.processGamemodeInfo(data, time, Gamemode.SURVIVAL);
        Long result = data.getGmTimes().get(Gamemode.SURVIVAL.name());
        assertTrue("Gamemode time was " + result, result == 1050L);
        result = data.getPlayTime();
        assertTrue("Playtime was" + result, result == 1100L);
        result = data.getLastPlayed();
        assertTrue("Last Played was" + result, result == 2000L);
        String lastGM = data.getLastGamemode();
        assertTrue("Last gm not Survival", "SURVIVAL".equals(lastGM));
        result = data.getLastGmSwapTime();
        assertTrue("Last swaptime was " + result, result == 1100L);
    }

    /**
     *
     */
    @Test
    public void testProcessGamemodeInfoNullGMTimes() {
        UserData data = MockUtils.mockUser();
        data.setGmTimes(null);
        data.setPlayTime(100L);
        data.setLastGamemode(null);
        data.setLastGmSwapTime(50L);
        data.setLastPlayed(1000L);
        long time = 2000L;
        GamemodeHandling.processGamemodeInfo(data, time, Gamemode.SURVIVAL);
        Long result = data.getGmTimes().get(Gamemode.SURVIVAL.name());
        assertTrue("Gamemode time was " + result, result == 1050L);
        result = data.getPlayTime();
        assertTrue("Playtime was" + result, result == 1100L);
        result = data.getLastPlayed();
        assertTrue("Last Played was" + result, result == 2000L);
        String lastGM = data.getLastGamemode();
        assertTrue("Last gm not Survival", "SURVIVAL".equals(lastGM));
        result = data.getLastGmSwapTime();
        assertTrue("Last swaptime was " + result, result == 1100L);
    }

}
