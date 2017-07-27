/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.main.java.com.djrapitops.plan.data.handling;

import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.handling.LogoutHandling;
import org.junit.Before;
import org.junit.Test;
import test.java.utils.MockUtils;

import static org.junit.Assert.assertTrue;

/**
 * @author Rsl1122
 */
public class LogoutHandlingTest {

    /**
     *
     */
    public LogoutHandlingTest() {
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
    public void testProcessLogoutInfo() {
        UserData data = MockUtils.mockUser();
        data.setLastPlayed(10L);
        data.updateBanned(false);
        long time = 20L;
        LogoutHandling.processLogoutInfo(data, time, true);
        assertTrue("Last Played wrong", data.getLastPlayed() == 20L);
        assertTrue("Playtime wrong", data.getPlayTime() == 10L);
        assertTrue("Banned wrong", data.isBanned());
    }

}
