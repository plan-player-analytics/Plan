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
import extension.FullSystemExtension;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(FullSystemExtension.class)
public class ComponentServiceTest {

    static PlanSystem system;
    static ComponentService service;

    @BeforeAll
    public static void enableSystem(PlanSystem system) {
        system.enable();
        service = ComponentService.getInstance();
    }

    @AfterAll
    public static void tearDown() {
        service = null;
        if (system != null) {
            system.disable();
        }
    }

    @Test
    public void translateTest() {
        assertEquals("§cred", service.translateLegacy("&cred"));
    }

    @Test
    public void invalidTranslateTest() {
        assertEquals("&zinvalid color code", service.translateLegacy("&zinvalid color code"));
    }

    @Test
    public void testAutoDetermine() {
        assertEquals("§cred", service.fromAutoDetermine("<red>red").intoLegacy());
        assertEquals("§cred", service.fromAutoDetermine("&cred").intoLegacy('§'));
        assertEquals("&cred", service.fromAutoDetermine("§cred").intoLegacy('&'));
        assertEquals("§cred", service.fromAutoDetermine("&#ff5555red").intoLegacy());
        assertEquals("§cred", service.fromAutoDetermine("§x§f§f§5§5§5§5red").intoLegacy());
    }

    @Test
    public void testMiniMessage() {
        String input = "<red>red";
        assertEquals(input, service.fromMiniMessage(input).intoMiniMessage());
    }

    @Test
    public void testLegacyAm() {
        String input = "&cred";
        assertEquals(input, service.fromLegacy(input, '&').intoLegacy('&'));
    }

    @Test
    public void testLegacySection() {
        String input = "§cred";
        assertEquals(input, service.fromLegacy(input).intoLegacy());
    }

    @Test
    public void testLegacyAdventure() {
        String input = "&#ff555fred";
        assertEquals(input, service.fromAdventureLegacy(input).intoAdventureLegacy());
    }

    @Test
    public void testLegacyBungee() {
        String input = "§x§f§f§5§5§5§fred";
        assertEquals(input, service.fromBungeeLegacy(input).intoBungeeLegacy());
    }

}
