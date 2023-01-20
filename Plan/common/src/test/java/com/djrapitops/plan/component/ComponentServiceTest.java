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
class ComponentServiceTest {

    static ComponentSvc service;

    @BeforeAll
    static void enableSystem(PlanSystem system) {
        system.enable();
        service = (ComponentSvc) ComponentService.getInstance();
    }

    @AfterAll
    static void tearDown(PlanSystem system) {
        service = null;
        system.disable();
    }

    @Test
    void translateTest() {
        assertEquals("§cred", service.translateLegacy("&cred"));
    }

    @Test
    void invalidTranslateTest() {
        assertEquals("&zinvalid color code", service.translateLegacy("&zinvalid color code"));
    }

    @Test
    void testAutoDetermine() {
        assertEquals("§cred", service.convert(service.fromAutoDetermine("<red>red"), ComponentOperation.LEGACY, Component.SECTION));
        assertEquals("§cred", service.convert(service.fromAutoDetermine("&cred"), ComponentOperation.LEGACY, Component.SECTION));
        assertEquals("&cred", service.convert(service.fromAutoDetermine("§cred"), ComponentOperation.LEGACY, Component.AMPERSAND));
        assertEquals("§cred", service.convert(service.fromAutoDetermine("&#ff5555red"), ComponentOperation.LEGACY, Component.SECTION));
        assertEquals("§cred", service.convert(service.fromAutoDetermine("§x§f§f§5§5§5§5red"), ComponentOperation.LEGACY, Component.SECTION));
    }

}
