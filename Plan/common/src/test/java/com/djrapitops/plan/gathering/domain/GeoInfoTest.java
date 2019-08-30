/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.gathering.domain;

import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test for functionality of GeoInfo object.
 *
 * @author Rsl1122
 */
@RunWith(JUnitPlatform.class)
class GeoInfoTest {

    @Test
    void automaticallyHidesLast16Bits() throws UnknownHostException {
        InetAddress test = InetAddress.getByName("1.2.3.4");
        String expected = "1.2.xx.xx";
        String result = new GeoInfo(test, "Irrelevant", 3).getIp();

        assertEquals(expected, result);
    }

    @Test
    void testFormatIP() throws UnknownHostException {
        InetAddress ip = InetAddress.getByName("1.2.3.4");
        InetAddress ip2 = InetAddress.getByName("1.2.3.26");
        InetAddress ip3 = InetAddress.getByName("1.2.3.235");
        String expected = "1.2.xx.xx";

        assertEquals(expected, GeoInfo.formatIP(ip));
        assertEquals(expected, GeoInfo.formatIP(ip2));
        assertEquals(expected, GeoInfo.formatIP(ip3));
    }

    @Test
    void testFormatIPv6() throws UnknownHostException {
        InetAddress ip = InetAddress.getByName("1234:1234:1234:1234:1234:1234:1234:1234%0");
        String expected = "1234:1234:1234:xx..";

        assertEquals(expected, GeoInfo.formatIP(ip));
    }

}