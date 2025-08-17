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
import com.djrapitops.plan.identification.Server;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.settings.Permissions;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.WebserverSettings;
import com.djrapitops.plan.settings.locale.lang.CommandLang;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.transactions.StoreServerInformationTransaction;
import com.djrapitops.plan.storage.database.transactions.commands.RemoveEverythingTransaction;
import com.djrapitops.plan.storage.file.PlanFiles;
import extension.FullSystemExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import utilities.DBPreparer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(FullSystemExtension.class)
class PlanCommandTest {

    PlanCommand underTest;

    @BeforeEach
    void preparePlanCommand(PlanConfig config, PlanCommand command, PlanSystem system) {
        config.set(WebserverSettings.DISABLED, true);
        system.enable();
        underTest = command;
    }

    @AfterEach
    void tearDownSystem(PlanSystem system) {
        if (system != null) system.disable();
    }

    @Test
    void buildingHasNoBuilderErrors(PlanCommand command) {
        assertNotNull(command.build());
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
            "db removejoinaddresses 1",
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
        ConsoleMessageBuilder messageBuilder = Mockito.spy(new ConsoleMessageBuilder(System.out::println));
        ConsoleChatFormatter chatFormatter = new ConsoleChatFormatter();
        lenient().when(sender.buildMessage()).thenReturn(messageBuilder);
        lenient().when(sender.getFormatter()).thenReturn(chatFormatter);

        // Permissions
        lenient().when(sender.supportsChatEvents()).thenReturn(true);
        lenient().when(sender.hasAllPermissionsFor(any())).thenCallRealMethod();
        lenient().when(sender.isMissingPermissionsFor(any())).thenCallRealMethod();
        lenient().when(sender.hasPermission((Permissions) any())).thenCallRealMethod();
        for (String permission : permissions) {
            when(sender.hasPermission(permission)).thenReturn(true);
        }

        lenient().when(sender.getUUID()).thenReturn(Optional.of(UUID.randomUUID()));

        return sender;
    }

    @Test
    void serverCommandSendsLink() {
        CMDSender sender = runCommand("server", "plan.server");

        verify(sender.buildMessage(), times(1)).link(anyString());
    }

    @Test
    void networkCommandSendsLink(Database database) throws ExecutionException, InterruptedException {
        try {
            Server server = new Server(ServerUUID.randomUUID(), "Serve", "", "");
            server.setProxy(true);
            database.executeTransaction(new StoreServerInformationTransaction(server));
            DBPreparer.awaitUntilTransactionsComplete(database);

            CMDSender sender = runCommand("network", "plan.network");

            verify(sender.buildMessage(), times(1)).link(anyString());
        } finally {
            database.executeTransaction(new RemoveEverythingTransaction());
        }
    }

    @Test
    void playerSelfCommandSendsLink() {
        CMDSender sender = runCommand("player", "plan.player.self");

        verify(sender.buildMessage(), times(1)).link(anyString());
    }

    @Test
    void playerOtherCommandSendsLink() {
        CMDSender sender = runCommand("player Test", "plan.player.self", "plan.player.other");

        verify(sender.buildMessage(), times(1)).link(anyString());
    }

    @Test
    void jsonSelfCommandSendsLink() {
        CMDSender sender = runCommand("json", "plan.json.self");

        verify(sender.buildMessage(), times(1)).link(anyString());
    }

    @Test
    void jsonOtherCommandSendsLink() {
        CMDSender sender = runCommand("json Test", "plan.json.self", "plan.json.other");

        verify(sender.buildMessage(), times(1)).link(anyString());
    }

    @Test
    void backupCommandCreatesBackup(PlanFiles files) throws IOException {
        runCommand("db backup SQLite", "plan.data.base", "plan.data.backup");

        Path dataDirectory = files.getDataDirectory();
        try (Stream<Path> list = Files.list(dataDirectory)) {
            String foundBackupFile = list
                    .map(Path::toFile)
                    .map(File::getName)
                    .filter(name -> name.endsWith(".db") && !name.startsWith("database"))
                    .findFirst()
                    .orElseThrow(AssertionError::new);

            System.out.println("Found: " + foundBackupFile);
            assertTrue(foundBackupFile.contains("backup"));
        }
    }
}