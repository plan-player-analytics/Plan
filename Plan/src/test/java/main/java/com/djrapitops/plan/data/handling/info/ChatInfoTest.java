/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.main.java.com.djrapitops.plan.data.handling.info;

import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.api.Gender;
import main.java.com.djrapitops.plan.data.DemographicsData;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.handling.info.ChatInfo;
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
public class ChatInfoTest {
    
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
        EasyMock.expect(JavaPlugin.getPlugin(Plan.class)).andReturn(plan);
        EasyMock.expect(JavaPlugin.getPlugin(Plan.class)).andReturn(plan);
        EasyMock.expect(JavaPlugin.getPlugin(Plan.class)).andReturn(plan);
        EasyMock.expect(JavaPlugin.getPlugin(Plan.class)).andReturn(plan);
        PowerMock.replay(JavaPlugin.class);
//        PowerMock.verify(JavaPlugin.class);
    }
    
    /**
     *
     */
    public ChatInfoTest() {
    }

    /**
     *
     */
    @Test
    public void testGetNickname() {
        ChatInfo i = new ChatInfo(null, "Test", "Message");
        assertTrue("Nick get wrong", i.getNickname().equals("Test"));
    }

    /**
     *
     */
    @Test
    public void testGetMessage() {
        ChatInfo i = new ChatInfo(null, "Test", "Message");
        assertTrue("Message get wrong", i.getMessage().equals("Message"));
    }

    /**
     *
     */
    @Test
    public void testProcessNick() {
        UserData data = new UserData(MockUtils.mockPlayer(), new DemographicsData());
        String expected = "TestNicknameChatInfo";
        ChatInfo i = new ChatInfo(data.getUuid(), expected, "im 18 male");
        assertTrue("Didn't succeed", i.process(data));
        assertTrue("Didn't add nickname", data.getNicknames().contains(expected));
        assertTrue("Didn't update gender", data.getDemData().getGender() == Gender.MALE);
    }
    
    /**
     *
     */
    @Test
    public void testProcessAge() {
        UserData data = new UserData(MockUtils.mockPlayer(), new DemographicsData());
        String expected = "TestNicknameChatInfo";
        ChatInfo i = new ChatInfo(data.getUuid(), expected, "im 18 male");
        assertTrue("Didn't succeed", i.process(data));
        assertTrue("Didn't update age", data.getDemData().getAge() == 18);
    }
    
    /**
     *
     */
    @Test
    public void testProcessGender() {
        UserData data = new UserData(MockUtils.mockPlayer(), new DemographicsData());
        String expected = "TestNicknameChatInfo";
        ChatInfo i = new ChatInfo(data.getUuid(), expected, "im 18 male");
        assertTrue("Didn't succeed", i.process(data));
        assertTrue("Didn't update gender", data.getDemData().getGender() == Gender.MALE);
    }
    
    /**
     *
     */
    @Test
    public void testProcessWrongUUID() {
        UserData data = new UserData(MockUtils.mockPlayer(), new DemographicsData());
        String expected = "TestNicknameChatInfo";
        ChatInfo i = new ChatInfo(null, expected, "im 18 male");
        assertTrue("Succeeded.", !i.process(data));
    }
}
