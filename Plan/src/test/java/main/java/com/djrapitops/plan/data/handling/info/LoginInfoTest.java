/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.main.java.com.djrapitops.plan.data.handling.info;

import com.djrapitops.plugin.utilities.player.Gamemode;
import java.net.InetAddress;
import java.net.UnknownHostException;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.handling.info.LoginInfo;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import test.java.utils.MockUtils;

/**
 *
 * @author Rsl1122
 */
public class LoginInfoTest {

    /**
     *
     */
    public LoginInfoTest() {
    }

    /**
     *
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     *
     * @throws UnknownHostException
     */
    @Test
    public void testProcess() throws UnknownHostException {
        UserData data = MockUtils.mockUser();
        InetAddress ip = InetAddress.getByName("137.19.188.146");
        long time = 10L;
        int loginTimes = data.getLoginTimes();
        String nick = "TestProcessLoginInfo";
        LoginInfo i = new LoginInfo(data.getUuid(), time, ip, true, nick, Gamemode.CREATIVE, 1);
        assertTrue(i.process(data));
        assertTrue("LastPlayed wrong: " + data.getLastPlayed(), data.getLastPlayed() == time);
        assertTrue("Ip not added", data.getIps().contains(ip));
        assertTrue("Logintimes not +1", data.getLoginTimes() == loginTimes + 1);
        assertTrue("Nick not added", data.getNicknames().contains(nick));
        assertTrue("Nick not last nick", data.getLastNick().equals(nick));
        String geo = data.getGeolocation();
        assertTrue("Wrong location " + geo, geo.equals("United States"));
        assertTrue("Didn't process gamemode", data.getLastGamemode().equals("CREATIVE"));
    }

    /**
     *
     * @throws UnknownHostException
     */
    @Test
    public void testProcessWrongUUID() throws UnknownHostException {
        UserData data = MockUtils.mockUser();
        data.setLastPlayed(0L);
        InetAddress ip = InetAddress.getByName("137.19.188.146");
        long time = 10L;
        String nick = "TestProcessLoginInfo";
        LoginInfo i = new LoginInfo(null, time, ip, true, nick, Gamemode.CREATIVE, 1);
        assertTrue(!i.process(data));
        assertEquals(0L, data.getLastPlayed());
        assertTrue("Ip not added", !data.getIps().contains(ip));
        assertTrue("Logintimes not +1", data.getLoginTimes() == 0);
        assertTrue("Nick not added", !data.getNicknames().contains(nick));
        String geo = data.getGeolocation();
        assertTrue("Wrong location " + geo, geo.equals("Not Known"));
        assertTrue("Didn't process gamemode", data.getLastGamemode().equals("SURVIVAL"));
    }

}
