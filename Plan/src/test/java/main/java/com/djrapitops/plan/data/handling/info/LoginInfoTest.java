/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.main.java.com.djrapitops.plan.data.handling.info;

import java.net.InetAddress;
import java.net.UnknownHostException;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.DemographicsData;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.handling.LoginHandling;
import main.java.com.djrapitops.plan.data.handling.info.LoginInfo;
import org.bukkit.GameMode;
import org.bukkit.plugin.java.JavaPlugin;
import org.easymock.EasyMock;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
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
public class LoginInfoTest {

    public LoginInfoTest() {
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
        PowerMock.replay(JavaPlugin.class);
//        PowerMock.verify(JavaPlugin.class);
    }

    @Test
    public void testProcess() throws UnknownHostException {
        UserData data = new UserData(MockUtils.mockPlayer(), new DemographicsData());
        InetAddress ip = InetAddress.getByName("137.19.188.146");
        long time = 10L;
        int loginTimes = data.getLoginTimes();
        String nick = "TestProcessLoginInfo";
        LoginInfo i = new LoginInfo(data.getUuid(), time, ip, true, nick, GameMode.CREATIVE, 1);
        assertTrue(i.process(data));
        assertTrue("LastPlayed wrong: " + data.getLastPlayed(), data.getLastPlayed() == time);
        assertTrue("Ip not added", data.getIps().contains(ip));
        assertTrue("Logintimes not +1", data.getLoginTimes() == loginTimes + 1);
        assertTrue("Nick not added", data.getNicknames().contains(nick));
        assertTrue("Nick not last nick", data.getLastNick().equals(nick));
        String geo = data.getDemData().getGeoLocation();
        assertTrue("Wrong location " + geo, geo.equals("United States"));
        assertTrue("Didn't process gamemode", data.getLastGamemode() == GameMode.CREATIVE);
    }
    
    @Test
    public void testProcessWrongUUID() throws UnknownHostException {
        UserData data = new UserData(MockUtils.mockPlayer(), new DemographicsData());
        InetAddress ip = InetAddress.getByName("137.19.188.146");
        long time = 10L;
        String nick = "TestProcessLoginInfo";
        LoginInfo i = new LoginInfo(null, time, ip, true, nick, GameMode.CREATIVE, 1);
        assertTrue(!i.process(data));
        assertTrue("LastPlayed wrong: " + data.getLastPlayed(), data.getLastPlayed() == 0L);
        assertTrue("Ip not added", !data.getIps().contains(ip));
        assertTrue("Logintimes not +1", data.getLoginTimes() == 0);
        assertTrue("Nick not added", !data.getNicknames().contains(nick));
        String geo = data.getDemData().getGeoLocation();
        assertTrue("Wrong location " + geo, geo.equals("Not Known"));
        assertTrue("Didn't process gamemode", data.getLastGamemode() == GameMode.SURVIVAL);
    }

}
