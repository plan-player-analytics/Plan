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
package com.djrapitops.plan.commands;

import com.djrapitops.plan.PlanSystem;
import com.djrapitops.plan.commands.use.*;
import com.djrapitops.plan.settings.Permissions;
import com.djrapitops.plan.settings.locale.lang.CommandLang;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import utilities.mocks.PluginMockComponent;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PlanCommandTest {

    PlanCommand underTest;
    PlanSystem system;

    @BeforeEach
    void preparePlanCommand(@TempDir Path tempDir) throws Exception {
        PluginMockComponent mockComponent = new PluginMockComponent(tempDir);
        system = mockComponent.getPlanSystem();
        system.enable();
        underTest = mockComponent.getComponent().planCommand();
    }

    @AfterEach
    void tearDownSystem() {
        if (system != null) system.disable();
    }

    @Test
    void buildingHasNoBuilderErrors() {
        assertNotNull(underTest.build());
    }

    @ParameterizedTest(name = "Command not executed without permission: /plan {0}")
    @CsvSource({
            "server",
            "server Server 1",
            "network",
            "player",
            "player Test",
            "search Test",
            "ingame",
            "ingame Test",
            "json",
            "json Test",
            "register",
            "unregister",
            "unregister Test",
            "logout Test",
            "users",
            "info",
            "reload",
            "disable",
            "disable kickCount",
            "export players",
            "export",
            "db",
            "db backup SQLite",
            "db clear SQLite",
            "db remove Test",
            "db uninstalled 1",
    })
    void commandWithoutPermissionsReturnsPermissionDenied(String command) {
        CMDSender sender = runCommand(command);

        Set<String> requiredPermissions = underTest.build().findSubCommand(new Arguments(command))
                .map(Subcommand::getRequiredPermissions)
                .orElse(Collections.emptySet());

        verify(sender, times(1)).send(CommandLang.FAIL_NO_PERMISSION.getDefault() + " " + requiredPermissions);
    }

    private CMDSender runCommand(String command, String... permissions) {
        CommandWithSubcommands executor = underTest.build();
        CMDSender sender = mockSender(permissions);

        executor.executeCommand(sender, new Arguments(command));

        return sender;
    }

    private CMDSender mockSender(String[] permissions) {
        CMDSender sender = Mockito.mock(CMDSender.class);

        // Sending messages
        ConsoleMessageBuilder messageBuilder = new ConsoleMessageBuilder(System.out::println);
        ConsoleChatFormatter chatFormatter = new ConsoleChatFormatter();
        lenient().when(sender.buildMessage()).thenReturn(messageBuilder);
        lenient().when(sender.getFormatter()).thenReturn(chatFormatter);

        // Permissions
        lenient().when(sender.hasAllPermissionsFor(any())).thenCallRealMethod();
        lenient().when(sender.isMissingPermissionsFor(any())).thenCallRealMethod();
        lenient().when(sender.hasPermission((Permissions) any())).thenCallRealMethod();
        for (String permission : permissions) {
            when(sender.hasPermission(permission)).thenReturn(true);
        }

        return sender;
    }

}