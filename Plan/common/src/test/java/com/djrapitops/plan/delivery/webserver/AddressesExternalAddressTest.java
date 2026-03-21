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
package com.djrapitops.plan.delivery.webserver;

import com.djrapitops.plan.delivery.webserver.http.WebServer;
import com.djrapitops.plan.identification.properties.ServerProperties;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.WebserverSettings;
import com.djrapitops.plan.storage.database.DBSystem;
import dagger.Lazy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link Addresses#getExternalAddress()} and {@link Addresses#getBasePath(String)}.
 */
@ExtendWith(MockitoExtension.class)
class AddressesExternalAddressTest {

    @Mock
    PlanConfig config;
    @Mock
    DBSystem dbSystem;
    @Mock
    Lazy<ServerProperties> serverProperties;
    @Mock
    Lazy<WebServer> webserver;
    @InjectMocks
    Addresses addresses;

    @Test
    @DisplayName("getExternalAddress returns configured address with subpath")
    void externalAddressWithSubpath() {
        when(config.get(WebserverSettings.EXTERNAL_LINK)).thenReturn("https://example.com/plan");
        assertEquals(Optional.of("https://example.com/plan"), addresses.getExternalAddress());
    }

    @Test
    @DisplayName("getExternalAddress returns configured address without subpath")
    void externalAddressWithoutSubpath() {
        when(config.get(WebserverSettings.EXTERNAL_LINK)).thenReturn("https://example.com");
        assertEquals(Optional.of("https://example.com"), addresses.getExternalAddress());
    }

    @Test
    @DisplayName("getExternalAddress returns empty for default placeholder")
    void externalAddressDefaultPlaceholder() {
        when(config.get(WebserverSettings.EXTERNAL_LINK)).thenReturn("https://www.example.address");
        assertEquals(Optional.empty(), addresses.getExternalAddress());
    }

    @Test
    @DisplayName("getExternalAddress returns empty for http placeholder")
    void externalAddressHttpPlaceholder() {
        when(config.get(WebserverSettings.EXTERNAL_LINK)).thenReturn("http://www.example.address");
        assertEquals(Optional.empty(), addresses.getExternalAddress());
    }

    @Test
    @DisplayName("getExternalAddress returns empty for empty string")
    void externalAddressEmpty() {
        when(config.get(WebserverSettings.EXTERNAL_LINK)).thenReturn("");
        assertEquals(Optional.empty(), addresses.getExternalAddress());
    }

    @Test
    @DisplayName("getBasePath extracts subpath from address")
    void basePathExtraction() {
        assertEquals("/minecraft/stats", addresses.getBasePath("https://disqt.com/minecraft/stats"));
    }

    @Test
    @DisplayName("getBasePath returns empty for root address")
    void basePathRoot() {
        assertEquals("", addresses.getBasePath("https://example.com"));
    }

    @Test
    @DisplayName("getBasePath extracts subpath from http address")
    void basePathHttp() {
        assertEquals("/plan", addresses.getBasePath("http://example.com/plan"));
    }
}
