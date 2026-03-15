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
package com.djrapitops.plan.delivery.rendering;

import com.djrapitops.plan.delivery.webserver.Addresses;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.WebserverSettings;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link BundleAddressCorrection} reverse proxy subpath support.
 */
@ExtendWith(MockitoExtension.class)
class BundleAddressCorrectionTest {

    @Mock
    PlanConfig config;
    @Mock
    Addresses addresses;
    @InjectMocks
    BundleAddressCorrection bundleAddressCorrection;

    @Test
    @DisplayName("HTML paths are corrected when External_Webserver_address has subpath")
    void htmlPathsCorrectedWithExternalSubpath() {
        when(addresses.getExternalAddress()).thenReturn(Optional.of("https://example.com/plan"));
        when(addresses.getBasePath("https://example.com/plan")).thenReturn("/plan");

        String html = "<script src=\"/static/index-abc123.js\"></script>"
                + "<link href=\"/static/index-abc123.css\">";

        String result = bundleAddressCorrection.correctAddressForWebserver(html, "index.html");

        assertTrue(result.contains("src=\"/plan/static/index-abc123.js\""),
                "JS src should be corrected to /plan/static/..., got: " + result);
        assertTrue(result.contains("href=\"/plan/static/index-abc123.css\""),
                "CSS href should be corrected to /plan/static/..., got: " + result);
    }

    @Test
    @DisplayName("HTML paths unchanged when External_Webserver_address has no subpath")
    void htmlPathsUnchangedWithoutSubpath() {
        when(addresses.getExternalAddress()).thenReturn(Optional.of("https://example.com"));
        when(addresses.getBasePath("https://example.com")).thenReturn("");
        when(addresses.getMainAddress()).thenReturn(Optional.of("http://localhost:8804"));
        when(addresses.getBasePath("http://localhost:8804")).thenReturn("");

        String html = "<script src=\"/static/index-abc123.js\"></script>";

        String result = bundleAddressCorrection.correctAddressForWebserver(html, "index.html");

        assertTrue(result.contains("src=\"/static/index-abc123.js\""),
                "JS src should stay at root when no subpath, got: " + result);
    }

    @Test
    @DisplayName("HTML paths corrected using main address when no external address")
    void htmlPathsCorrectedFromMainAddress() {
        when(addresses.getExternalAddress()).thenReturn(Optional.empty());
        when(addresses.getMainAddress()).thenReturn(Optional.of("http://example.com/plan"));
        when(addresses.getBasePath("http://example.com/plan")).thenReturn("/plan");

        String html = "<script src=\"/static/index-abc123.js\"></script>";

        String result = bundleAddressCorrection.correctAddressForWebserver(html, "index.html");

        assertTrue(result.contains("src=\"/plan/static/index-abc123.js\""),
                "Should fall back to main address base path, got: " + result);
    }

    @Test
    @DisplayName("HTML paths unchanged when no external address and main address has no subpath")
    void htmlPathsUnchangedNoSubpathAnywhere() {
        when(addresses.getExternalAddress()).thenReturn(Optional.empty());
        when(addresses.getMainAddress()).thenReturn(Optional.of("http://localhost:8804"));
        when(addresses.getBasePath("http://localhost:8804")).thenReturn("");

        String html = "<script src=\"/static/index-abc123.js\"></script>";

        String result = bundleAddressCorrection.correctAddressForWebserver(html, "index.html");

        assertTrue(result.contains("src=\"/static/index-abc123.js\""),
                "Should be unchanged when no subpath anywhere, got: " + result);
    }

    @Test
    @DisplayName("Vite preload base URL is corrected with subpath")
    void vitePreloadCorrectedWithSubpath() {
        when(addresses.getExternalAddress()).thenReturn(Optional.of("https://example.com/plan"));
        when(addresses.getBasePath("https://example.com/plan")).thenReturn("/plan");

        String js = "GN=function(l){return\"/\"+l}";

        String result = bundleAddressCorrection.correctAddressForWebserver(js, "index.js");

        assertTrue(result.contains("return\"/plan/\"+l"),
                "Vite preload should include base path, got: " + result);
    }

    @Test
    @DisplayName("Vite preload base URL unchanged at root")
    void vitePreloadUnchangedAtRoot() {
        when(addresses.getExternalAddress()).thenReturn(Optional.empty());
        when(addresses.getMainAddress()).thenReturn(Optional.of("http://localhost:8804"));
        when(addresses.getBasePath("http://localhost:8804")).thenReturn("");

        String js = "GN=function(l){return\"/\"+l}";

        String result = bundleAddressCorrection.correctAddressForWebserver(js, "index.js");

        assertTrue(result.contains("return\"/\"+l"),
                "Vite preload should be unchanged at root, got: " + result);
    }
}
