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
package com.djrapitops.plan.placeholder;

import com.djrapitops.plan.PlanSystem;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.transactions.events.StoreServerPlayerTransaction;
import com.djrapitops.plan.storage.database.transactions.events.StoreSessionTransaction;
import com.djrapitops.plan.storage.database.transactions.events.StoreWorldNameTransaction;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import utilities.RandomData;
import utilities.TestConstants;
import utilities.dagger.PlanPluginComponent;
import utilities.mocks.PluginMockComponent;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class PlanPlaceholdersTest {

    private static PlanPluginComponent component;
    private static PlanPlaceholders underTest;
    private static ServerUUID serverUUID;
    private static UUID playerUUID;

    @BeforeAll
    static void prepareSystem(@TempDir Path tempDir) throws Exception {
        PluginMockComponent mockComponent = new PluginMockComponent(tempDir);
        component = mockComponent.getComponent();
        mockComponent.getPlanSystem().enable();
        serverUUID = component.system().getServerInfo().getServerUUID();
        underTest = component.placeholders();

        playerUUID = UUID.randomUUID();

        storeSomeData();
    }

    // Print all placeholders to console
    public static void main(String[] args) throws Exception {
        prepareSystem(Files.createTempDirectory("temp-plan-"));


        System.out.println("Player placeholders:\n\n```");
        underTest.getRegisteredPlayerPlaceholders()
                .stream()
                .map(placeholder -> placeholder.replaceAll("\\d+", "{n}"))
                .distinct()
                .forEach(System.out::println);
        System.out.println("```");

        System.out.println();

        System.out.println("Server placeholders:\n\n```");
        underTest.getRegisteredServerPlaceholders()
                .stream()
                .map(placeholder -> placeholder.replaceAll("\\d+", "{n}"))
                .distinct()
                .forEach(System.out::println);
        System.out.println("```");

        component.system().disable();
        System.exit(0);
    }

    private static void storeSomeData() {
        Database database = component.system().getDatabaseSystem().getDatabase();
        database.executeTransaction(new StoreServerPlayerTransaction(
                playerUUID,
                System::currentTimeMillis,
                RandomData.randomString(5),
                serverUUID,
                () -> RandomData.randomString(5)
        ));
        database.executeTransaction(new StoreServerPlayerTransaction(
                TestConstants.PLAYER_TWO_UUID,
                System::currentTimeMillis,
                TestConstants.PLAYER_TWO_NAME,
                serverUUID,
                () -> RandomData.randomString(5)
        ));
        String worldName = RandomData.randomString(10);
        database.executeTransaction(new StoreWorldNameTransaction(serverUUID, worldName));
        database.executeTransaction(new StoreSessionTransaction(RandomData.randomSession(serverUUID, new String[]{worldName}, playerUUID, TestConstants.PLAYER_TWO_UUID)));
    }

    @AfterAll
    static void clearSystem() {
        if (component != null) {
            PlanSystem system = component.system();
            if (system != null) {
                system.disable();
            }
        }
    }

    @TestFactory
    @DisplayName("Server placeholders return something")
    Collection<DynamicTest> testServerPlaceholders() {
        return underTest.getRegisteredServerPlaceholders().stream()
                .map(placeholder -> DynamicTest.dynamicTest("'" + placeholder + "' returns something", () -> {
                    String result = underTest.onPlaceholderRequest(UUID.randomUUID(), placeholder, Collections.emptyList());
                    System.out.println("Placeholder '" + placeholder + "' was replaced with: '" + result + "'");
                    assertNotNull(result);
                    assertNotEquals(placeholder, result);
                }))
                .collect(Collectors.toList());
    }

    @TestFactory
    @DisplayName("Server placeholders return something on console")
    Collection<DynamicTest> testServerPlaceholdersOnConsole() {
        return underTest.getRegisteredServerPlaceholders().stream()
                .map(placeholder -> DynamicTest.dynamicTest("'" + placeholder + "' returns something", () -> {
                    String result = underTest.onPlaceholderRequest(null, placeholder, Collections.emptyList());
                    System.out.println("Placeholder '" + placeholder + "' was replaced with: '" + result + "'");
                    assertNotNull(result);
                    assertNotEquals(placeholder, result);
                }))
                .collect(Collectors.toList());
    }

    @TestFactory
    @DisplayName("Server placeholders return something for another server")
    Collection<DynamicTest> testServerPlaceholdersWithParameter() {
        return underTest.getRegisteredServerPlaceholders().stream()
                .map(placeholder -> DynamicTest.dynamicTest("'" + placeholder + ":<server>' returns something", () -> {
                    String result = underTest.onPlaceholderRequest(UUID.randomUUID(), placeholder, List.of(serverUUID.toString()));
                    System.out.println("Placeholder '" + placeholder + ":" + serverUUID.toString() + "' was replaced with: '" + result + "'");
                    assertNotNull(result);
                    assertNotEquals(placeholder, result);
                }))
                .collect(Collectors.toList());
    }

    @TestFactory
    @DisplayName("Player placeholders return something")
    Collection<DynamicTest> testPlayerPlaceholders() {
        return underTest.getRegisteredPlayerPlaceholders().stream()
                .map(placeholder -> DynamicTest.dynamicTest("'" + placeholder + "' returns something", () -> {
                    String result = underTest.onPlaceholderRequest(playerUUID, placeholder, Collections.emptyList());
                    System.out.println("Placeholder '" + placeholder + "' was replaced with: '" + result + "'");
                    assertNotNull(result);
                    assertNotEquals(placeholder, result);
                }))
                .collect(Collectors.toList());
    }

    @TestFactory
    @DisplayName("Player placeholders return nothing on Console")
    Collection<DynamicTest> testPlayerPlaceholdersOnConsole() {
        return underTest.getRegisteredPlayerPlaceholders().stream()
                .map(placeholder -> DynamicTest.dynamicTest("'" + placeholder + "' returns something", () -> {
                    String result = underTest.onPlaceholderRequest(null, placeholder, Collections.emptyList());
                    System.out.println("Placeholder '" + placeholder + "' was replaced with: '" + result + "'");
                    assertNull(result);
                }))
                .collect(Collectors.toList());
    }

    @TestFactory
    @DisplayName("Player placeholders return something on Console for other player")
    Collection<DynamicTest> testPlayerPlaceholdersOnConsoleForOtherPlayer() {
        return underTest.getRegisteredPlayerPlaceholders().stream()
                .map(placeholder -> DynamicTest.dynamicTest("'" + placeholder + "' returns something", () -> {
                    String result = underTest.onPlaceholderRequest(null, placeholder, List.of(playerUUID.toString()));
                    System.out.println("Placeholder '" + placeholder + "' was replaced with: '" + result + "'");
                    assertNotNull(result);
                    assertNotEquals(placeholder, result);
                }))
                .collect(Collectors.toList());
    }

}