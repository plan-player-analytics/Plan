/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.main.java.com.djrapitops.plan.data.handling;

import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.api.Gender;
import main.java.com.djrapitops.plan.data.DemographicsData;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.handling.ChatHandling;
import org.bukkit.plugin.java.JavaPlugin;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
public class ChatHandlingTest {

    /**
     *
     */
    public ChatHandlingTest() {
    }

    /**
     *
     */
    @Before
    public void setUp() {
        TestInit t = new TestInit();
        assertTrue("Not set up", t.setUp());
        Plan plan = t.getPlanMock();
    }

    /**
     *
     */
    @Test
    public void testProcessChatInfoAddedNickname() {
        UserData data = new UserData(MockUtils.mockPlayer(), new DemographicsData());
        String expected = "TestNicknameChatHandling";
        ChatHandling.processChatInfo(data, expected, "");
        assertTrue("Didn't add nickname", data.getNicknames().contains(expected));
    }

    /**
     *
     */
    @Test
    public void testUpdateDemographicInformationMale() {
        UserData data = new UserData(MockUtils.mockPlayer(), new DemographicsData());
        ChatHandling.updateDemographicInformation("I'm male", data);
        assertTrue("Didn't update gender", data.getDemData().getGender() == Gender.MALE);
    }

    /**
     *
     */
    @Test
    public void testUpdateDemographicInformationNoInfo() {
        UserData data = new UserData(MockUtils.mockPlayer(), new DemographicsData());
        ChatHandling.updateDemographicInformation("I'm serious", data);
        assertTrue("Updated gender", data.getDemData().getGender() == Gender.UNKNOWN);
        assertTrue("Updated age", data.getDemData().getAge() == -1);
    }

    /**
     *
     */
    @Test
    public void testUpdateDemographicInformation100Age() {
        UserData data = new UserData(MockUtils.mockPlayer(), new DemographicsData());
        ChatHandling.updateDemographicInformation("I'm 100", data);
        assertTrue("Updated age", data.getDemData().getAge() == -1);
    }

    /**
     *
     */
    @Test
    public void testUpdateDemographicInformationNoTrigger() {
        UserData data = new UserData(MockUtils.mockPlayer(), new DemographicsData());
        ChatHandling.updateDemographicInformation("18 male", data);
        assertTrue("Updated gender", data.getDemData().getGender() == Gender.UNKNOWN);
        assertTrue("Updated age", data.getDemData().getAge() == -1);
    }

    /**
     *
     */
    @Test
    public void testUpdateDemographicInformationIgnore() {
        UserData data = new UserData(MockUtils.mockPlayer(), new DemographicsData());
        ChatHandling.updateDemographicInformation("im sure you're male", data);
        assertTrue("Updated gender", data.getDemData().getGender() == Gender.UNKNOWN);
        assertTrue("Updated age", data.getDemData().getAge() == -1);
    }

    /**
     *
     */
    @Test
    public void testUpdateDemographicInformationAge() {
        UserData data = new UserData(MockUtils.mockPlayer(), new DemographicsData());
        ChatHandling.updateDemographicInformation("im 18", data);
        assertTrue("Didn't update age", data.getDemData().getAge() == 18);
    }

}
