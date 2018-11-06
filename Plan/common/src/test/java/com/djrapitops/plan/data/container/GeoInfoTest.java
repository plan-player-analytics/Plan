package com.djrapitops.plan.data.container;

import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;

import static org.junit.Assert.assertEquals;

/**
 * Test for functionality of GeoInfo object.
 *
 * @author Rsl1122
 */
public class GeoInfoTest {

    @Test
    public void automaticallyHidesLast16Bits() throws NoSuchAlgorithmException, UnknownHostException {
        InetAddress test = InetAddress.getByName("1.2.3.4");
        String expected = "1.2.xx.xx";
        String result = new GeoInfo(test, "Irrelevant", 3).getIp();

        assertEquals(expected, result);
    }

    @Test
    public void testFormatIP() throws UnknownHostException {
        InetAddress ip = InetAddress.getByName("1.2.3.4");
        InetAddress ip2 = InetAddress.getByName("1.2.3.26");
        InetAddress ip3 = InetAddress.getByName("1.2.3.235");
        String expected = "1.2.xx.xx";

        assertEquals(expected, GeoInfo.formatIP(ip));
        assertEquals(expected, GeoInfo.formatIP(ip2));
        assertEquals(expected, GeoInfo.formatIP(ip3));
    }

    @Test
    public void testFormatIPv6() throws UnknownHostException {
        InetAddress ip = InetAddress.getByName("1234:1234:1234:1234:1234:1234:1234:1234%0");
        String expected = "1234:1234:1234:xx..";

        assertEquals(expected, GeoInfo.formatIP(ip));
    }

}