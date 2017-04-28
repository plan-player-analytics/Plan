/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.main.java.com.djrapitops.plan.data.handling;

import java.net.InetAddress;
import java.net.UnknownHostException;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.DemographicsData;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.handling.LoginHandling;
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
public class LoginHandlingTest {

    /**
     *
     */
    public LoginHandlingTest() {
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
     * @throws UnknownHostException
     */
    @Test
    public void testProcessLoginInfo() throws UnknownHostException {
        UserData data = new UserData(MockUtils.mockPlayer(), new DemographicsData());
        data.updateBanned(false);
        InetAddress ip = InetAddress.getByName("137.19.188.146");
        long time = 10L;
        int loginTimes = data.getLoginTimes();
        String nick = "TestProcessLoginInfo";
        LoginHandling.processLoginInfo(data, time, ip, true, nick, 1);
        assertTrue("Not Banned", data.isBanned());
        assertTrue("LastPlayed wrong", data.getLastPlayed() == time);
        assertTrue("Ip not added", data.getIps().contains(ip));
        assertTrue("Logintimes not +1", data.getLoginTimes() == loginTimes + 1);
        assertTrue("Nick not added", data.getNicknames().contains(nick));
        assertTrue("Nick not last nick", data.getLastNick().equals(nick));
        String geo = data.getDemData().getGeoLocation();
        assertTrue("Wrong location " + geo, geo.equals("United States"));
    }

    /**
     *
     * @throws UnknownHostException
     */
    @Test
    public void testUpdateGeolocation() throws UnknownHostException {
        UserData data = new UserData(MockUtils.mockPlayer(), new DemographicsData());
        InetAddress ip = InetAddress.getByName("137.19.188.146");
        LoginHandling.updateGeolocation(ip, data);
        String result = data.getDemData().getGeoLocation();
        assertTrue("Wrong location " + result, result.equals("United States"));
    }

}
