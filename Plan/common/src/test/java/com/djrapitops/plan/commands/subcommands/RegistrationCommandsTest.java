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
package com.djrapitops.plan.commands.subcommands;

import com.djrapitops.plan.PlanSystem;
import com.djrapitops.plan.commands.PlanCommand;
import com.djrapitops.plan.commands.use.Arguments;
import com.djrapitops.plan.commands.use.CMDSender;
import com.djrapitops.plan.commands.use.CommandWithSubcommands;
import com.djrapitops.plan.delivery.domain.auth.User;
import com.djrapitops.plan.delivery.webserver.Addresses;
import com.djrapitops.plan.settings.Permissions;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.changes.ConfigUpdater;
import com.djrapitops.plan.settings.config.paths.WebserverSettings;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.objects.WebUserQueries;
import com.djrapitops.plan.storage.database.transactions.commands.StoreWebUserTransaction;
import com.djrapitops.plan.utilities.PassEncryptUtil;
import com.google.gson.Gson;
import extension.FullSystemExtension;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.openqa.selenium.json.TypeToken;
import utilities.DBPreparer;
import utilities.HTTPConnector;
import utilities.TestConstants;
import utilities.TestResources;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author AuroraLS3
 */
@ExtendWith(FullSystemExtension.class)
class RegistrationCommandsTest {

    @BeforeAll
    static void beforeAll(@TempDir Path tempDir, PlanSystem system) throws Exception {
        File file = tempDir.resolve("TestCert.p12").toFile();
        File testCert = TestResources.getTestResourceFile("TestCert.p12", ConfigUpdater.class);
        Files.copy(testCert.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        String absolutePath = file.getAbsolutePath();

        PlanConfig config = system.getConfigSystem().getConfig();
        config.set(WebserverSettings.CERTIFICATE_PATH, absolutePath);
        config.set(WebserverSettings.CERTIFICATE_KEYPASS, "test");
        config.set(WebserverSettings.CERTIFICATE_STOREPASS, "test");
        config.set(WebserverSettings.CERTIFICATE_ALIAS, "test");

        system.enable();
    }

    @AfterAll
    static void afterAll(PlanSystem system) {
        system.disable();
    }

    @Test
    @DisplayName("User is registered with 'admin' group with 'plan.webgroup.admin' permission")
    void normalRegistrationFlow(Addresses addresses, PlanCommand command, Database database) throws Exception {
        String username = "normalRegistrationFlow";
        String code = registerUser(username, addresses);

        CMDSender sender = mock(CMDSender.class);
        when(sender.isPlayer()).thenReturn(true);
        when(sender.hasPermission(Permissions.REGISTER_SELF.getPermission())).thenReturn(true);
        when(sender.hasPermission("plan.webgroup.admin")).thenReturn(true);
        when(sender.getUUID()).thenReturn(Optional.of(TestConstants.PLAYER_ONE_UUID));
        when(sender.getPlayerName()).thenReturn(Optional.of(TestConstants.PLAYER_ONE_NAME));

        command.build().executeCommand(sender, new Arguments(List.of("register", "--code", code)));

        DBPreparer.awaitUntilTransactionsComplete(database);
        User user = database.query(WebUserQueries.fetchUser(username)).orElseThrow(AssertionError::new);
        assertEquals("admin", user.getPermissionGroup());
    }

    @Test
    @DisplayName("User registration fails without any plan.webgroup.{name} permission.")
    void noPermissionFlow(Addresses addresses, PlanCommand command, Database database) throws Exception {
        String username = "noPermissionFlow";
        String code = registerUser(username, addresses);

        CMDSender sender = mock(CMDSender.class);
        when(sender.isPlayer()).thenReturn(true);
        when(sender.hasPermission(Permissions.REGISTER_SELF.getPermission())).thenReturn(true);
        when(sender.getUUID()).thenReturn(Optional.of(TestConstants.PLAYER_ONE_UUID));
        when(sender.getPlayerName()).thenReturn(Optional.of(TestConstants.PLAYER_ONE_NAME));

        CommandWithSubcommands cmd = command.build();
        Arguments arguments = new Arguments(List.of("register", "--code", code));
        assertThrows(IllegalArgumentException.class, () -> cmd.executeCommand(sender, arguments));

        assertTrue(database.query(WebUserQueries.fetchUser(username)).isEmpty());
    }

    @Test
    @DisplayName("User registration fails without any plan.webgroup.{name} permission attempting to bypass.")
    void noPermissionFlowBypassAttempt(Addresses addresses, PlanCommand command, Database database) throws Exception {
        String username = "noPermissionFlowBypassAttempt";
        String code = registerUser(username, addresses);

        CMDSender sender = mock(CMDSender.class);
        when(sender.isPlayer()).thenReturn(true);
        when(sender.hasPermission(Permissions.REGISTER_SELF.getPermission())).thenReturn(true);
        when(sender.getUUID()).thenReturn(Optional.of(TestConstants.PLAYER_ONE_UUID));
        when(sender.getPlayerName()).thenReturn(Optional.of(TestConstants.PLAYER_ONE_NAME));

        CommandWithSubcommands cmd = command.build();
        Arguments arguments = new Arguments(List.of("register", "--code", code, "superuser"));
        assertThrows(IllegalArgumentException.class, () -> cmd.executeCommand(sender, arguments));

        assertTrue(database.query(WebUserQueries.fetchUser(username)).isEmpty());
    }

    @Test
    @DisplayName("User group is changed")
    void setGroupCommandTest(PlanCommand command, Database database) throws Exception {
        String username = "setGroupCommandTest";
        User user = new User(username, "console", null, PassEncryptUtil.createHash("testPass"), "admin", Collections.emptyList());
        database.executeTransaction(new StoreWebUserTransaction(user)).get();

        CMDSender sender = mock(CMDSender.class);
        when(sender.isPlayer()).thenReturn(true);
        when(sender.hasPermission(Permissions.SET_GROUP.getPermission())).thenReturn(true);
        when(sender.getUUID()).thenReturn(Optional.of(TestConstants.PLAYER_ONE_UUID));
        when(sender.getPlayerName()).thenReturn(Optional.of(TestConstants.PLAYER_ONE_NAME));

        command.build().executeCommand(sender, new Arguments(List.of("setgroup", username, "no_access")));

        DBPreparer.awaitUntilTransactionsComplete(database);
        User modifiedUser = database.query(WebUserQueries.fetchUser(username)).orElseThrow(AssertionError::new);
        assertEquals("no_access", modifiedUser.getPermissionGroup());
    }

    private static String registerUser(String username, Addresses addresses) throws IOException, KeyManagementException, NoSuchAlgorithmException {
        HTTPConnector connector = new HTTPConnector();
        HttpURLConnection connection = null;
        String code;
        try {
            String address = addresses.getFallbackLocalhostAddress();
            connection = connector.getConnection("POST", address + "/auth/register");
            connection.setDoOutput(true);
            connection.getOutputStream().write(("user=" + username + "&password=testPass").getBytes());
            try (InputStream in = connection.getInputStream()) {
                String responseBody = new String(IOUtils.toByteArray(in));
                assertTrue(responseBody.contains("\"code\":"), () -> "Not successful: " + responseBody);
                Map<String, Object> read = new Gson().fromJson(responseBody, new TypeToken<Map<String, Object>>() {}.getType());
                code = (String) read.get("code");
                System.out.println("Got registration code: " + code);
            }
        } finally {
            if (connection != null) connection.disconnect();
        }
        return code;
    }
}