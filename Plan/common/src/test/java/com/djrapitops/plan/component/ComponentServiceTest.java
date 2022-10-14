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
package com.djrapitops.plan.component;

import com.djrapitops.plan.PlanSystem;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import utilities.mocks.PluginMockComponent;

import java.nio.file.Path;

public class ComponentServiceTest {

    static PlanSystem system;
    static ComponentService service;

    @BeforeAll
    public static void prepareSystem(@TempDir Path tempDir) throws Exception {
        PluginMockComponent mockComponent = new PluginMockComponent(tempDir);
        system = mockComponent.getPlanSystem();
        system.enable();
        service = ComponentService.getInstance();
    }

    @AfterAll
    static void tearDownSystem() {
        if (system != null) system.disable();
    }

    @Test
    public void translateTest() {
        Assertions.assertEquals("§cred", service.translateLegacy("&cred"));
    }

    @Test
    public void invalidTranslateTest() {
        Assertions.assertEquals("&zinvalid color code", service.translateLegacy("&zinvalid color code"));
    }

    @Test
    public void testAutoDetermine() {
        Assertions.assertEquals("§cred", service.fromAutoDetermine("<red>red").intoLegacy());
        Assertions.assertEquals("§cred", service.fromAutoDetermine("&cred").intoLegacy('§'));
        Assertions.assertEquals("&cred", service.fromAutoDetermine("§cred").intoLegacy('&'));
        Assertions.assertEquals("§cred", service.fromAutoDetermine("&#ff5555red").intoLegacy());
        Assertions.assertEquals("§cred", service.fromAutoDetermine("§x§f§f§5§5§5§5red").intoLegacy());
    }

    @Test
    public void testMiniMessage() {
        String input = "<red>red";
        Assertions.assertEquals(input, service.fromMiniMessage(input).intoMiniMessage());
    }

    @Test
    public void testLegacyAm() {
        String input = "&cred";
        Assertions.assertEquals(input, service.fromLegacy(input, '&').intoLegacy('&'));
    }

    @Test
    public void testLegacySection() {
        String input = "§cred";
        Assertions.assertEquals(input, service.fromLegacy(input).intoLegacy());
    }

    @Test
    public void testLegacyAdventure() {
        String input = "&#ff555fred";
        Assertions.assertEquals(input, service.fromAdventureLegacy(input).intoAdventureLegacy());
    }

    @Test
    public void testLegacyBungee() {
        String input = "§x§f§f§5§5§5§fred";
        Assertions.assertEquals(input, service.fromBungeeLegacy(input).intoBungeeLegacy());
    }

}
