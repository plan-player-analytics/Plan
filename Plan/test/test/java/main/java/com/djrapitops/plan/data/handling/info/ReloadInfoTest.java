/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.main.java.com.djrapitops.plan.data.handling.info;

import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.handling.info.ReloadInfo;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import test.java.utils.TestInit;
import test.java.utils.MockUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Rsl1122
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(JavaPlugin.class)
public class ReloadInfoTest {

    /**
     *
     */
    public ReloadInfoTest() {
    }

    /**
     *
     */
    @Before
    public void setUp() throws Exception {
        TestInit.init();
    }

    /**
     * @throws UnknownHostException
     */
    @Test
    public void testProcess() throws UnknownHostException {
        UserData data = MockUtils.mockUser();
        InetAddress ip = InetAddress.getByName("137.19.188.146");
        long time = MiscUtils.getTime();
        int loginTimes = data.getLoginTimes();
        String nick = "TestProcessLoginInfo";
        ReloadInfo i = new ReloadInfo(data.getUuid(), time, ip, true, nick, "CREATIVE", "World");
        assertTrue(i.process(data));
        assertTrue("LastPlayed wrong: " + data.getLastPlayed(), data.getLastPlayed() == time);
        assertTrue("Ip not added", data.getIps().contains(ip));
        assertTrue("Login times is not the same", data.getLoginTimes() == loginTimes);
        assertTrue("Nick not added", data.getNicknames().contains(nick));
        assertEquals(nick, data.getLastNick());
        assertEquals("United States", data.getGeolocation());
        assertEquals("CREATIVE", data.getGmTimes().getState());
        assertEquals("World", data.getWorldTimes().getState());
    }
}
