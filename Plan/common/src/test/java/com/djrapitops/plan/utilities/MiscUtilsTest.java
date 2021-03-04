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
package com.djrapitops.plan.utilities;

import com.djrapitops.plan.settings.Permissions;
import com.djrapitops.plugin.command.Sender;
import com.djrapitops.plugin.command.SenderType;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

/**
 * Tests for various {@link MiscUtils} methods.
 *
 * @author AuroraLS3
 */
class MiscUtilsTest {

    private Sender mockAPlayerSender(String name, boolean hasPermission) {
        Sender sender = Mockito.mock(Sender.class);
        when(sender.hasPermission(Permissions.PLAYER_OTHER.getPermission())).thenReturn(hasPermission);
        when(sender.getName()).thenReturn(name);
        when(sender.getSenderType()).thenReturn(SenderType.PLAYER);
        return sender;
    }

    @Test
    void getNameShouldReturnNameWithPermission() {
        String[] args = new String[]{"AuroraLS3", "Test"};
        Sender sender = mockAPlayerSender("TestName", true);

        String expResult = "AuroraLS3";
        String result = MiscUtils.getPlayerName(args, sender);

        assertEquals(expResult, result);
    }

    @Test
    void getNameShouldReturnNullWithoutPermission() {
        String[] args = new String[]{"AuroraLS3", "Test"};
        Sender sender = mockAPlayerSender("TestName", false);

        String result = MiscUtils.getPlayerName(args, sender);

        assertNull(result);
    }

    @Test
    void getNameShouldReturnPlayerNameWithoutArgs() {
        String[] args = new String[]{};
        String expected = "TestName";
        Sender sender = mockAPlayerSender(expected, true);

        String result = MiscUtils.getPlayerName(args, sender);

        assertEquals(expected, result);
    }

    @Test
    void getNameShouldReturnPlayerNameWithoutArgsOrPermission() {
        String[] args = new String[]{};
        String expected = "TestName2";
        Sender sender = mockAPlayerSender(expected, false);

        String result = MiscUtils.getPlayerName(args, sender);

        assertEquals(expected, result);
    }

    @Test
    void getNameShouldReturnPlayerNameWithoutPermissionForOwnName() {
        String[] args = new String[]{"testname2"};
        String expected = "TestName2";
        Sender sender = mockAPlayerSender(expected, false);

        String result = MiscUtils.getPlayerName(args, sender);

        assertEquals(expected, result);
    }

    @Test
    void getNameShouldReturnArgumentForConsole() {
        String[] args = new String[]{"TestConsoleSender"};
        String expected = "TestConsoleSender";

        Sender sender = Mockito.mock(Sender.class);
        when(sender.getSenderType()).thenReturn(SenderType.CONSOLE);

        String result = MiscUtils.getPlayerName(args, sender);

        assertEquals(expected, result);
    }
}
