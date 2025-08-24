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
package com.djrapitops.plan.delivery.domain.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link WebPermission}.
 *
 * @author AuroraLS3
 */
class WebPermissionTest {

    @Test
    void webPermissionIsFound() {
        String permission = "access.player.self";
        WebPermission found = WebPermission.findByPermission(permission).orElseThrow(AssertionError::new);
        WebPermission expected = WebPermission.ACCESS_PLAYER_SELF;
        assertEquals(expected, found);
    }

    @Test
    void webPermissionIsDetectedAsDeprecated() {
        String permission = "page.server.join.addresses.graphs.pie";
        assertTrue(WebPermission.isDeprecated(permission));
    }

    @Test
    void webPermissionIsDetectedAsNonDeprecated() {
        String permission = "access.player.self";
        assertFalse(WebPermission.isDeprecated(permission));
    }

    @Test
    void customWebPermissionIsDetectedAsNonDeprecated() {
        String permission = "custom.permission";
        assertFalse(WebPermission.isDeprecated(permission));
    }

}