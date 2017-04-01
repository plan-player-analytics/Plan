/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.main.java.com.djrapitops.plan.api;

import main.java.com.djrapitops.plan.api.Gender;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Risto
 */
public class GenderTest {

    public GenderTest() {
    }

    @Test
    public void testParse() {
        String name = "male";
        Gender expResult = Gender.MALE;
        Gender result = Gender.parse(name);
        assertEquals(expResult, result);
    }

    @Test
    public void testParse2() {
        String name = "female";
        Gender expResult = Gender.FEMALE;
        Gender result = Gender.parse(name);
        assertEquals(expResult, result);
    }

    @Test
    public void testParse3() {
        String name = "other";
        Gender expResult = Gender.OTHER;
        Gender result = Gender.parse(name);
        assertEquals(expResult, result);
    }

    @Test
    public void testParse4() {
        String name = "noeanroe";
        Gender expResult = Gender.UNKNOWN;
        Gender result = Gender.parse(name);
        assertEquals(expResult, result);
    }

}
