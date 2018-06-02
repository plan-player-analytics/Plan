package com.djrapitops.plan.data.container;

import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import static org.junit.Assert.assertEquals;

/**
 * Test for functionality of GeoInfo object.
 *
 * @author Rsl1122
 */
public class GeoInfoTest {

    @Test
    public void automaticallyHidesLast16Bits() throws UnsupportedEncodingException, NoSuchAlgorithmException {
        String test = "1.2.3.4";
        String expected = "1.2.xx.xx";
        String result = new GeoInfo(test, "Irrelevant", 3).getIp();

        assertEquals(expected, result);
    }

}