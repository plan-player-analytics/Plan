/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.main.java.com.djrapitops.plan.data;

import main.java.com.djrapitops.plan.data.SessionData;
import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;

/**
 *
 * @author Risto
 */
public class SessionDataTest {

    private SessionData test;
    
    public SessionDataTest() {
    }

    @Before
    public void setUp() {
        test = new SessionData(0);
    }

    @Test
    public void testEndSession() {
        test.endSession(1L);
        assertTrue("End not 1", test.getSessionEnd() == 1L);
    }

    @Test
    public void testGetSessionStart() {
        assertTrue("Start not 0", test.getSessionStart() == 0L);
    }

    @Test
    public void testIsValid() {
        test.endSession(1L);
        assertTrue("Supposed to be valid.", test.isValid());
    }
    
    @Test
    public void testInvalid() {
        assertTrue("Supposed to be invalid.", !test.isValid());
    }
    
    @Test
    public void testInvalid2() {
        test = new SessionData(3);
        test.endSession(2);
        assertTrue("Supposed to be invalid.", !test.isValid());
    }
    
    @Test
    public void testValid2() {
        test = new SessionData(3);
        test.endSession(3);
        assertTrue("Supposed to be valid.", test.isValid());
    }
    
    @Test
    public void testToString() {
        String exp = "s:0 e:-1";
        String result = test.toString();
        assertEquals(exp, result);
    }
    
    @Test
    public void testGetLength() {
        long exp = 5L;
        test.endSession(5L);
        long result = test.getLength();
        assertEquals(exp, result);
    }
    
    @Test
    public void testGetLength2() {
        long exp = 5L;
        test = new SessionData(5L);
        test.endSession(10L);
        long result = test.getLength();
        assertEquals(exp, result);
    }
}
