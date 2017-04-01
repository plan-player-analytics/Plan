/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.main.java.com.djrapitops.plan.data;

import main.java.com.djrapitops.plan.Phrase;
import main.java.com.djrapitops.plan.api.Gender;
import main.java.com.djrapitops.plan.data.DemographicsData;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Risto
 */
public class DemographicsDataTest {
    
    public DemographicsDataTest() {
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
