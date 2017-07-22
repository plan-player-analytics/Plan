/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.main.java.com.djrapitops.plan.data.handling.info;

import com.djrapitops.plugin.utilities.player.Gamemode;
import main.java.com.djrapitops.plan.data.SessionData;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.handling.info.LogoutInfo;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import test.java.utils.MockUtils;

/**
 *
 * @author Rsl1122
 */
public class LogoutInfoTest {

    /**
     *
     */
    public LogoutInfoTest() {
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
        data.setLastPlayed(10L);
        data.updateBanned(false);
        long time = 20L;
        Exception ex = null;
        data.setLastGamemode("SURVIVAL");
        LogoutInfo i = new LogoutInfo(data.getUuid(), time, true, Gamemode.CREATIVE, new SessionData(0, 1));
        assertTrue(i.process(data));
        assertTrue("Last Played wrong", data.getLastPlayed() == 20L);
        assertTrue("Playtime wrong", data.getPlayTime() == 10L);
        assertTrue("Banned wrong", data.isBanned());
        assertTrue("Didn't process gamemode", data.getLastGamemode().equals("CREATIVE"));
        assertEquals(1, data.getSessions().size());
    }

    /**
     *
     */
    @Test
    public void testProcessWrongUUID() {
        UserData data = MockUtils.mockUser();
        data.setLastPlayed(10L);
        data.updateBanned(false);
        long time = 20L;
        Exception ex = null;
        LogoutInfo i = new LogoutInfo(null, time, true, Gamemode.CREATIVE, new SessionData(0, 1));
        try {
            assertTrue(!i.process(data));
        } catch (NullPointerException e) {
            ex = e;
        }
        assertTrue("Caught endSessionException", ex == null);
        assertTrue("Last Played wrong", data.getLastPlayed() == 10L);
        assertTrue("Playtime wrong", data.getPlayTime() == 0L);
        assertTrue("Banned wrong", !data.isBanned());
        assertTrue("Didn't process gamemode", data.getLastGamemode().equals("SURVIVAL"));
        assertEquals(0, data.getSessions().size());
    }

}
