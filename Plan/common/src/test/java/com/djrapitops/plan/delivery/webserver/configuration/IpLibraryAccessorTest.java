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
package com.djrapitops.plan.delivery.webserver.configuration;

import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IpLibraryAccessorTest {

    private IPLibraryAccessor libraryAccessor;

    @BeforeEach
    void setUp() {
        libraryAccessor = new IPLibraryAccessor(getClass().getClassLoader());
    }

    @ParameterizedTest
    @CsvSource({
            // Test examples from ipaddress library github
            "10.10.20.0/30,10.10.20.3",
            "10.10.20.0/30,10.10.20.0/31",
            "1::/64,1::1",
            "1::/64,1::/112",
            "1::3-4:5-6,1::4:5",
            "1-2::/64,2::",
            // Own tests
            "::1,::1",
            "::1,0:0:0:0:0:0:0:1",
            "0:0:0:0:0:0:0:1,0:0:0:0:0:0:0:1",
            "127.0.0.1,127.0.0.1",
            "192.168.0.0/16,192.168.102.1",
            "172.*.*.*,172.3.52.3"
    })
    void addressIsWithinRange(String range, String address) {
        assertTrue(libraryAccessor.isWithin(new IPAddressString(range).getAddress(), new IPAddressString(address).getAddress(), IPAddress.class));
    }

    @ParameterizedTest
    @CsvSource({
            // Test examples from ipaddress library github
            "10.10.20.0/30,10.10.20.5",
            "1::/64,2::1",
            "1::/64,1::/32",
            // Own tests
            "172.*.*.*,192.3.52.3"
    })
    void addressIsNotWithinRange(String range, String address) {
        assertFalse(libraryAccessor.isWithin(new IPAddressString(range).getAddress(), new IPAddressString(address).getAddress(), IPAddress.class));
    }
}