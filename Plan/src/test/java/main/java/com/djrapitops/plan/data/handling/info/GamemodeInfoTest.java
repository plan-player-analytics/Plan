/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.main.java.com.djrapitops.plan.data.handling.info;

import com.djrapitops.plugin.utilities.player.Gamemode;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.handling.info.GamemodeInfo;
import org.junit.Before;
import org.junit.Test;
import test.java.utils.MockUtils;

import static org.junit.Assert.assertTrue;

/**
 * @author Rsl1122
 */
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
    public void setUp() throws Exception {
    }

    /**
     *
     */
    @Test
    public void testProcess() {
        UserData data = MockUtils.mockUser();
        data.setPlayTime(100L);
        data.setLastGamemode("CREATIVE");
        data.setLastGmSwapTime(50L);
        data.setLastPlayed(1000L);
        long time = 2000L;
        GamemodeInfo i = new GamemodeInfo(data.getUuid(), time, Gamemode.SURVIVAL);
        assertTrue(i.process(data));
        Long result = data.getGmTimes().get(Gamemode.CREATIVE.name());
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
    public void testProcessWrongUUID() {
        UserData data = MockUtils.mockUser();
        data.setPlayTime(100L);
        data.setLastGamemode("CREATIVE");
        data.setLastGmSwapTime(50L);
        data.setLastPlayed(1000L);
        long time = 2000L;
        GamemodeInfo i = new GamemodeInfo(null, time, Gamemode.SURVIVAL);
        assertTrue(!i.process(data));
        Long result = data.getGmTimes().get(Gamemode.CREATIVE.name());
        assertTrue("Gamemode time was " + result, result == 0L);
        result = data.getPlayTime();
        assertTrue("Playtime was" + result, result == 100L);
        result = data.getLastPlayed();
        assertTrue("Last Played was" + result, result == 1000L);
        String lastGM = data.getLastGamemode();
        assertTrue("Last gm not Creative", "CREATIVE".equals(lastGM));
        result = data.getLastGmSwapTime();
        assertTrue("Last swaptime was " + result, result == 50L);
    }

    /**
     *
     */
    @Test
    public void testProcessNullGM() {
        UserData data = MockUtils.mockUser();
        data.setPlayTime(100L);
        data.setLastGamemode("CREATIVE");
        data.setLastGmSwapTime(50L);
        data.setLastPlayed(1000L);
        long time = 2000L;
        GamemodeInfo i = new GamemodeInfo(data.getUuid(), time, null);
        assertTrue(!i.process(data));
        Long result = data.getGmTimes().get(Gamemode.CREATIVE.name());
        assertTrue("Gamemode time was " + result, result == 0L);
        result = data.getPlayTime();
        assertTrue("Playtime was" + result, result == 100L);
        result = data.getLastPlayed();
        assertTrue("Last Played was" + result, result == 1000L);
        String lastGM = data.getLastGamemode();
        assertTrue("Last gm not Creative", "CREATIVE".equals(lastGM));
        result = data.getLastGmSwapTime();
        assertTrue("Last swaptime was " + result, result == 50L);
    }

}
