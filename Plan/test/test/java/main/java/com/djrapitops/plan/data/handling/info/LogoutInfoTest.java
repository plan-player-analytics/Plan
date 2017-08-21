/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.main.java.com.djrapitops.plan.data.handling.info;

import main.java.com.djrapitops.plan.data.SessionData;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.handling.info.LogoutInfo;
import org.junit.Before;
import org.junit.Test;
import test.java.utils.MockUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
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
        data.getGmTimes().setState("SURVIVAL");
        LogoutInfo i = new LogoutInfo(data.getUuid(), time, true, "CREATIVE", new SessionData(0, 1), "World");
        i.process(data);
        assertTrue("Last Played wrong", data.getLastPlayed() == 20L);
        assertTrue("Playtime wrong", data.getPlayTime() == 10L);
        assertTrue("Banned wrong", data.isBanned());
        assertEquals("CREATIVE", data.getGmTimes().getState());
//   TODO     assertEquals("World", data.getWorldTimes().getState());
        assertEquals(1, data.getSessions().size());
    }
}
