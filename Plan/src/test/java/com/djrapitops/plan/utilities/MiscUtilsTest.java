/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.djrapitops.plan.utilities;

import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plugin.command.Sender;
import com.djrapitops.plugin.command.SenderType;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

/**
 * Tests for various {@link MiscUtils} methods.
 *
 * @author Rsl1122
 */
public class MiscUtilsTest {

    private Sender mockAPlayerSender(String name, boolean hasPermission) {
        Sender sender = Mockito.mock(Sender.class);
        when(sender.hasPermission(Permissions.INSPECT_OTHER.getPermission())).thenReturn(hasPermission);
        when(sender.getName()).thenReturn(name);
        when(sender.getSenderType()).thenReturn(SenderType.PLAYER);
        return sender;
    }

    @Test
    public void getNameShouldReturnNameWithPermission() {
        String[] args = new String[]{"Rsl1122", "Test"};
        Sender sender = mockAPlayerSender("TestName", true);

        String expResult = "Rsl1122";
        String result = MiscUtils.getPlayerName(args, sender);

        assertEquals(expResult, result);
    }

    @Test
    public void getNameShouldReturnNullWithoutPermission() {
        String[] args = new String[]{"Rsl1122", "Test"};
        Sender sender = mockAPlayerSender("TestName", false);

        String result = MiscUtils.getPlayerName(args, sender);

        assertNull(result);
    }

    @Test
    public void getNameShouldReturnPlayerNameWithoutArgs() {
        String[] args = new String[]{};
        String expected = "TestName";
        Sender sender = mockAPlayerSender(expected, true);

        String result = MiscUtils.getPlayerName(args, sender);

        assertEquals(expected, result);
    }

    @Test
    public void getNameShouldReturnPlayerNameWithoutArgsOrPermission() {
        String[] args = new String[]{};
        String expected = "TestName2";
        Sender sender = mockAPlayerSender(expected, false);

        String result = MiscUtils.getPlayerName(args, sender);

        assertEquals(expected, result);
    }

    @Test
    public void getNameShouldReturnPlayerNameWithoutPermissionForOwnName() {
        String[] args = new String[]{"testname2"};
        String expected = "TestName2";
        Sender sender = mockAPlayerSender(expected, false);

        String result = MiscUtils.getPlayerName(args, sender);

        assertEquals(expected, result);
    }

    @Test
    public void getNameShouldReturnArgumentForConsole() {
        String[] args = new String[]{"TestConsoleSender"};
        String expected = "TestConsoleSender";

        Sender sender = Mockito.mock(Sender.class);
        when(sender.getSenderType()).thenReturn(SenderType.CONSOLE);

        String result = MiscUtils.getPlayerName(args, sender);

        assertEquals(expected, result);
    }
}
