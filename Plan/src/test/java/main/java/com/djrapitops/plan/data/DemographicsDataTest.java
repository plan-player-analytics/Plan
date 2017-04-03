/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.main.java.com.djrapitops.plan.data;

import main.java.com.djrapitops.plan.Phrase;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.api.Gender;
import main.java.com.djrapitops.plan.data.DemographicsData;
import org.bukkit.plugin.java.JavaPlugin;
import org.easymock.EasyMock;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import test.java.utils.TestInit;

/**
 *
 * @author Risto
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(JavaPlugin.class)
public class DemographicsDataTest {

    public DemographicsDataTest() {
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
    public void testGetAge() {
        DemographicsData instance = new DemographicsData();
        int expResult = -1;
        int result = instance.getAge();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetAge2() {
        DemographicsData instance = new DemographicsData(10, Gender.OTHER, "None");
        int expResult = 10;
        int result = instance.getAge();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetGender() {
        DemographicsData instance = new DemographicsData();
        Gender expResult = Gender.UNKNOWN;
        Gender result = instance.getGender();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetGender2() {
        DemographicsData instance = new DemographicsData(10, Gender.OTHER, "None");
        Gender expResult = Gender.OTHER;
        Gender result = instance.getGender();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetGeoLocation() {
        DemographicsData instance = new DemographicsData();
        String expResult = Phrase.DEM_UNKNOWN.parse();
        String result = instance.getGeoLocation();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetGeoLocation2() {
        DemographicsData instance = new DemographicsData(10, Gender.OTHER, "None");
        String expResult = "None";
        String result = instance.getGeoLocation();
        assertEquals(expResult, result);
    }

    @Test
    public void testSetAge() {
        int age = 10;
        DemographicsData instance = new DemographicsData();
        instance.setAge(age);
        int result = instance.getAge();
        assertEquals(age, result);
    }

    @Test
    public void testSetGender() {
        Gender gender = Gender.MALE;
        DemographicsData instance = new DemographicsData();
        instance.setGender(gender);
        Gender result = instance.getGender();
        assertEquals(gender, result);
    }

    @Test
    public void testSetGeoLocation() {
        String geoLocation = "None";
        DemographicsData instance = new DemographicsData();
        instance.setGeoLocation(geoLocation);
        String result = instance.getGeoLocation();
        assertEquals(geoLocation, result);
    }

    @Test
    public void testToString() {
        DemographicsData instance = new DemographicsData();
        String expResult = "{age:-1|gender:UNKNOWN|geoLocation:Not Known}";
        String result = instance.toString();
        assertEquals(expResult, result);
    }

}
