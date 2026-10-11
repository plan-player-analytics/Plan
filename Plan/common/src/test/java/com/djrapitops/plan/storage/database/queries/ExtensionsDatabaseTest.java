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
package com.djrapitops.plan.storage.database.queries;

import com.djrapitops.plan.extension.CallEvents;
import com.djrapitops.plan.extension.ExtensionSvc;
import com.djrapitops.plan.extension.icon.Color;
import com.djrapitops.plan.extension.icon.Icon;
import com.djrapitops.plan.extension.implementation.results.*;
import com.djrapitops.plan.extension.implementation.storage.queries.ExtensionPlayerDataQuery;
import com.djrapitops.plan.extension.implementation.storage.queries.ExtensionServerDataQuery;
import com.djrapitops.plan.extension.implementation.storage.queries.playertable.ExtensionServerTableDataQuery;
import com.djrapitops.plan.extension.implementation.storage.queries.statistics.ExtensionPlayerStatisticsQuery;
import com.djrapitops.plan.extension.implementation.storage.queries.statistics.ExtensionServerStatisticsQuery;
import com.djrapitops.plan.extension.table.Table;
import com.djrapitops.plan.gathering.domain.ActiveSession;
import com.djrapitops.plan.gathering.domain.WorldTimes;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.settings.config.ExtensionSettings;
import com.djrapitops.plan.storage.database.DatabaseTestPreparer;
import com.djrapitops.plan.storage.database.transactions.commands.RemoveEverythingTransaction;
import com.djrapitops.plan.storage.database.transactions.events.PlayerRegisterTransaction;
import com.djrapitops.plan.storage.database.transactions.events.StoreSessionTransaction;
import com.djrapitops.plan.storage.database.transactions.events.StoreWorldNameTransaction;
import com.djrapitops.plan.storage.database.transactions.init.RemoveOldExtensionsTransaction;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import utilities.OptionalAssert;
import utilities.RandomData;
import utilities.TestConstants;
import utilities.TestErrorLogger;
import utilities.TestExtensions.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Contains database tests for DataExtension API.
 *
 * @author AuroraLS3
 */
public interface ExtensionsDatabaseTest extends DatabaseTestPreparer {

    @BeforeEach
    default void unregisterExtensions() {
        componentService().register();
        ExtensionSvc extensionService = extensionService();
        extensionService.register();
        extensionService.unregister(new PlayerExtension());
        extensionService.unregister(new ServerExtension());
        extensionService.unregister(new ConditionalExtension());
        extensionService.unregister(new TableExtension());
        extensionService.unregister(new ThrowingExtension());
        extensionService.unregister(new ClickEventTestExtension());
        extensionService.unregister(new StatisticsExtension());
        extensionService.unregister(new PlayerStatisticsExtension());
        ExtensionSettings extensionSettings = config().getExtensionSettings();
        extensionSettings.setEnabled("PlayerExtension", true);
        extensionSettings.setEnabled("TableExtension", true);
        extensionSettings.setEnabled("ServerExtension", true);
        extensionSettings.setEnabled("StatisticsExtension", true);
        extensionSettings.setEnabled("PlayerStatisticsExtension", true);
    }

    @Test
    default void removeEverythingRemovesPlayerExtensionData() {
        extensionPlayerValuesAreStored();
        db().executeTransaction(new RemoveEverythingTransaction());
        assertTrue(db().query(new ExtensionPlayerDataQuery(playerUUID)).isEmpty());
    }

    @Test
    default void removeEverythingRemovesServerExtensionData() {
        extensionServerValuesAreStored();
        db().executeTransaction(new RemoveEverythingTransaction());
        assertTrue(db().query(new ExtensionServerDataQuery(serverUUID())).isEmpty());
    }

    @Test
    default void removeOldExtensionDataCleansData() {
        db().executeTransaction(new PlayerRegisterTransaction(TestConstants.PLAYER_ONE_UUID, System::currentTimeMillis, TestConstants.PLAYER_ONE_NAME));

        ExtensionSvc extensionService = extensionService();
        extensionService.register(new TableExtension());
        extensionService.register(new PlayerExtension());
        extensionService.register(new ServerExtension());
        extensionService.updatePlayerValues(playerUUID, TestConstants.PLAYER_ONE_NAME, CallEvents.MANUAL);
        extensionService.updateServerValues(CallEvents.MANUAL);

        ExtensionSettings extensionSettings = config().getExtensionSettings();
        extensionSettings.setEnabled("PlayerExtension", false);
        extensionSettings.setEnabled("TableExtension", false);
        extensionSettings.setEnabled("ServerExtension", false);

        db().executeTransaction(new RemoveOldExtensionsTransaction(extensionService.getExtensionMetadataStorage(), extensionSettings, TimeUnit.DAYS.toMillis(1), serverUUID()));

        assertTrue(db().query(new ExtensionPlayerDataQuery(playerUUID)).isEmpty());
        assertTrue(db().query(new ExtensionServerDataQuery(serverUUID())).isEmpty());
    }

    @Test
    default void extensionPlayerValuesAreStored() {
        db().executeTransaction(new PlayerRegisterTransaction(TestConstants.PLAYER_ONE_UUID, System::currentTimeMillis, TestConstants.PLAYER_ONE_NAME));

        ExtensionSvc extensionService = extensionService();

        extensionService.register(new PlayerExtension());
        extensionService.updatePlayerValues(playerUUID, TestConstants.PLAYER_ONE_NAME, CallEvents.MANUAL);

        Map<ServerUUID, List<ExtensionData>> playerDataByServerUUID = db().query(new ExtensionPlayerDataQuery(playerUUID));
        List<ExtensionData> ofServer = playerDataByServerUUID.get(serverUUID());
        assertNotNull(ofServer);
        assertFalse(ofServer.isEmpty());

        ExtensionData extensionPlayerData = ofServer.get(0);
        List<ExtensionTabData> tabs = extensionPlayerData.getTabs();
        assertEquals(1, tabs.size()); // No tab defined, should contain 1 tab
        ExtensionTabData tabData = tabs.get(0);

        OptionalAssert.equals("5", tabData.getNumber("value").map(data -> data.getFormattedValue(Object::toString)));
        OptionalAssert.equals("No", tabData.getBoolean("boolVal").map(ExtensionBooleanData::getFormattedValue));
        OptionalAssert.equals("0.5", tabData.getDouble("doubleVal").map(data -> data.getFormattedValue(Object::toString)));
        OptionalAssert.equals("0.5", tabData.getPercentage("percentageVal").map(data -> data.getFormattedValue(Object::toString)));
        OptionalAssert.equals("Something", tabData.getString("stringVal").map(ExtensionStringData::getFormattedValue));
        OptionalAssert.equals("Group", tabData.getString("groupVal").map(ExtensionStringData::getFormattedValue));

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("color", "green");
        objectNode.put("text", "Test");
        OptionalAssert.equals(objectNode, tabData.getComponent("componentVal").map(ExtensionComponentData::getFormattedValue).map(str -> {
            try {
                return objectMapper.readTree(str);
            } catch (Throwable t) {
                return fail(t);
            }
        }));
    }

    @Test
    default void extensionPlayerValuesCanBeQueriedAsTableData() {
        extensionPlayerValuesAreStored();
        db().executeTransaction(new StoreWorldNameTransaction(serverUUID(), worlds[0]));
        db().executeTransaction(new StoreWorldNameTransaction(serverUUID(), worlds[1]));

        // Store a session to check against issue https://github.com/plan-player-analytics/Plan/issues/1039
        ActiveSession session = new ActiveSession(playerUUID, serverUUID(), 32345L, worlds[0], "SURVIVAL");
        session.getExtraData().put(WorldTimes.class, RandomData.randomWorldTimes(worlds));
        db().executeTransaction(new StoreSessionTransaction(session.toFinishedSession(42345L)));

        Map<UUID, ExtensionTabData> result = db().query(new ExtensionServerTableDataQuery(serverUUID(), 50));
        assertEquals(1, result.size());
        ExtensionTabData playerData = result.get(playerUUID);
        assertNotNull(playerData);

        OptionalAssert.equals("5", playerData.getNumber("value").map(data -> data.getFormattedValue(Object::toString)));
        OptionalAssert.equals("No", playerData.getBoolean("boolVal").map(ExtensionBooleanData::getFormattedValue));
        OptionalAssert.equals("0.5", playerData.getDouble("doubleVal").map(data -> data.getFormattedValue(Object::toString)));
        OptionalAssert.equals("0.5", playerData.getPercentage("percentageVal").map(data -> data.getFormattedValue(Object::toString)));
        OptionalAssert.equals("Something", playerData.getString("stringVal").map(ExtensionStringData::getFormattedValue));
    }

    @Test
    default void extensionServerValuesAreStored() {
        ExtensionSvc extensionService = extensionService();

        extensionService.register(new ServerExtension());
        extensionService.updateServerValues(CallEvents.SERVER_EXTENSION_REGISTER);

        List<ExtensionData> ofServer = db().query(new ExtensionServerDataQuery(serverUUID()));
        assertFalse(ofServer.isEmpty());

        ExtensionData extensionData = ofServer.get(0);
        List<ExtensionTabData> tabs = extensionData.getTabs();
        assertEquals(1, tabs.size()); // No tab defined, should contain 1 tab
        ExtensionTabData tabData = tabs.get(0);

        OptionalAssert.equals("5", tabData.getNumber("value").map(data -> data.getFormattedValue(Object::toString)));
        OptionalAssert.equals("No", tabData.getBoolean("boolVal").map(ExtensionBooleanData::getFormattedValue));
        OptionalAssert.equals("0.5", tabData.getDouble("doubleVal").map(data -> data.getFormattedValue(Object::toString)));
        OptionalAssert.equals("0.5", tabData.getPercentage("percentageVal").map(data -> data.getFormattedValue(Object::toString)));
        OptionalAssert.equals("Something", tabData.getString("stringVal").map(ExtensionStringData::getFormattedValue));
    }

    @Test
    default void extensionServerAggregateQueriesWork() {
        ExtensionSvc extensionService = extensionService();

        extensionService.register(new PlayerExtension());
        extensionService.updatePlayerValues(playerUUID, TestConstants.PLAYER_ONE_NAME, CallEvents.MANUAL);

        List<ExtensionData> ofServer = db().query(new ExtensionServerDataQuery(serverUUID()));
        assertFalse(ofServer.isEmpty());

        ExtensionData extensionData = ofServer.get(0);
        List<ExtensionTabData> tabs = extensionData.getTabs();
        assertEquals(1, tabs.size()); // No tab defined, should contain 1 tab
        ExtensionTabData tabData = tabs.get(0);

        OptionalAssert.equals("0.0", tabData.getPercentage("boolVal_aggregate").map(data -> data.getFormattedValue(Objects::toString)));
        OptionalAssert.equals("0.5", tabData.getPercentage("percentageVal_avg").map(data -> data.getFormattedValue(Objects::toString)));
        OptionalAssert.equals("0.5", tabData.getDouble("doubleVal_avg").map(data -> data.getFormattedValue(Objects::toString)));
        OptionalAssert.equals("0.5", tabData.getDouble("doubleVal_total").map(data -> data.getFormattedValue(Objects::toString)));
        OptionalAssert.equals("5", tabData.getNumber("value_avg").map(data -> data.getFormattedValue(Objects::toString)));
        OptionalAssert.equals("5", tabData.getNumber("value_total").map(data -> data.getFormattedValue(Objects::toString)));

        List<ExtensionTableData> tableData = tabData.getTableData();
        assertEquals(1, tableData.size());
        Table expected = Table.builder()
                .columnOne("a group", Icon.called("user-group").build())
                .columnTwo("Players", Icon.called("user").build())
                .addRow("Group", 1).build();
        Table result = tableData.get(0).getTable();
        assertEquals(expected, result);
    }

    @Test
    default void unsatisfiedPlayerConditionalResultsAreCleaned() {
        db().executeTransaction(new PlayerRegisterTransaction(playerUUID, System::currentTimeMillis, TestConstants.PLAYER_ONE_NAME));

        ExtensionSvc extensionService = extensionService();

        extensionService.register(new ConditionalExtension());

        ConditionalExtension.condition = true;
        extensionService.updatePlayerValues(playerUUID, TestConstants.PLAYER_ONE_NAME, CallEvents.MANUAL);

        // Check that the wanted data exists
        checkThatPlayerDataExists(ConditionalExtension.condition);

        // Reverse condition
        ConditionalExtension.condition = false;
        extensionService.updatePlayerValues(playerUUID, TestConstants.PLAYER_ONE_NAME, CallEvents.MANUAL);

        // Check that the wanted data exists
        checkThatPlayerDataExists(ConditionalExtension.condition);

        // Reverse condition
        ConditionalExtension.condition = false;
        extensionService.updatePlayerValues(playerUUID, TestConstants.PLAYER_ONE_NAME, CallEvents.MANUAL);

        // Check that the wanted data exists
        checkThatPlayerDataExists(ConditionalExtension.condition);
    }

    @Test
    default void unsatisfiedPlayerConditionalResultsAreCleanedCompletely() {
        db().executeTransaction(new PlayerRegisterTransaction(playerUUID, System::currentTimeMillis, TestConstants.PLAYER_ONE_NAME));

        ExtensionSvc extensionService = extensionService();

        extensionService.register(new RemovingConditionalExtension());

        RemovingConditionalExtension.condition = true;
        extensionService.updatePlayerValues(playerUUID, TestConstants.PLAYER_ONE_NAME, CallEvents.MANUAL);

        List<ExtensionData> ofServer = db().query(new ExtensionPlayerDataQuery(playerUUID)).get(serverUUID());
        assertTrue(ofServer != null && !ofServer.isEmpty() && !ofServer.get(0).getTabs().isEmpty(), "There was no data left");
        ExtensionTabData tabData = ofServer.get(0).getTabs().get(0);
        assertEquals(RemovingConditionalExtension.condition, tabData.getString("conditionalValue").isPresent());

        // Reverse condition
        RemovingConditionalExtension.condition = false;
        extensionService.updatePlayerValues(playerUUID, TestConstants.PLAYER_ONE_NAME, CallEvents.MANUAL);

        ofServer = db().query(new ExtensionPlayerDataQuery(playerUUID)).get(serverUUID());
        assertTrue(ofServer != null && !ofServer.isEmpty() && !ofServer.get(0).getTabs().isEmpty(), "There was no data left");
        tabData = ofServer.get(0).getTabs().get(0);
        assertEquals(RemovingConditionalExtension.condition, tabData.getString("conditionalValue").isPresent());
    }

    @Test
    default void unsatisfiedServerConditionalResultsAreCleanedCompletely() {
        db().executeTransaction(new PlayerRegisterTransaction(playerUUID, System::currentTimeMillis, TestConstants.PLAYER_ONE_NAME));

        ExtensionSvc extensionService = extensionService();

        extensionService.register(new RemovingConditionalExtension());

        RemovingConditionalExtension.condition = true;
        extensionService.updateServerValues(CallEvents.MANUAL);

        List<ExtensionData> ofServer = db().query(new ExtensionServerDataQuery(serverUUID()));
        assertTrue(ofServer != null && !ofServer.isEmpty() && !ofServer.get(0).getTabs().isEmpty(), "There was no data left");
        ExtensionTabData tabData = ofServer.get(0).getTabs().get(0);
        assertEquals(RemovingConditionalExtension.condition, tabData.getString("conditionalValue").isPresent());

        // Reverse condition
        RemovingConditionalExtension.condition = false;
        extensionService.updateServerValues(CallEvents.MANUAL);

        ofServer = db().query(new ExtensionServerDataQuery(serverUUID()));
        assertTrue(ofServer != null && !ofServer.isEmpty() && !ofServer.get(0).getTabs().isEmpty(), "There was no data left");
        tabData = ofServer.get(0).getTabs().get(0);
        assertEquals(RemovingConditionalExtension.condition, tabData.getString("conditionalValue").isPresent());
    }

    default void checkThatPlayerDataExists(boolean condition) {
        if (condition) { // Condition is true, conditional values exist
            List<ExtensionData> ofServer = db().query(new ExtensionPlayerDataQuery(playerUUID)).get(serverUUID());
            assertTrue(ofServer != null && !ofServer.isEmpty() && !ofServer.get(0).getTabs().isEmpty(), "There was no data left");

            ExtensionTabData tabData = ofServer.get(0).getTabs().get(0);
            OptionalAssert.equals("Yes", tabData.getBoolean("isCondition").map(ExtensionBooleanData::getFormattedValue));
            OptionalAssert.equals("Conditional", tabData.getString("conditionalValue").map(ExtensionStringData::getFormattedValue));
            OptionalAssert.equals("unconditional", tabData.getString("unconditional").map(ExtensionStringData::getFormattedValue)); // Was not removed
            OptionalAssert.equals("Group", tabData.getString("conditionalGroups").map(ExtensionStringData::getFormattedValue)); // Was not removed
            assertFalse(tabData.getString("reversedConditionalValue").isPresent(), "Value was not removed: reversedConditionalValue");
        } else { // Condition is false, reversed conditional values exist
            List<ExtensionData> ofServer = db().query(new ExtensionPlayerDataQuery(playerUUID)).get(serverUUID());
            assertTrue(ofServer != null && !ofServer.isEmpty() && !ofServer.get(0).getTabs().isEmpty(), "There was no data left");
            ExtensionTabData tabData = ofServer.get(0).getTabs().get(0);
            OptionalAssert.equals("No", tabData.getBoolean("isCondition").map(ExtensionBooleanData::getFormattedValue));
            OptionalAssert.equals("Reversed", tabData.getString("reversedConditionalValue").map(ExtensionStringData::getFormattedValue));
            OptionalAssert.equals("unconditional", tabData.getString("unconditional").map(ExtensionStringData::getFormattedValue)); // Was not removed
            assertFalse(tabData.getString("conditionalValue").isPresent(), "Value was not removed: conditionalValue");
            assertFalse(tabData.getString("conditionalGroups").isPresent(), "Value was not removed: conditionalGroups");
        }
    }

    @Test
    default void unsatisfiedServerConditionalResultsAreCleaned() {
        ExtensionSvc extensionService = extensionService();

        ConditionalExtension.condition = true;
        extensionService.register(new ConditionalExtension());
        extensionService.updateServerValues(CallEvents.MANUAL);

        // Check that the wanted data exists
        checkThatServerDataExists(ConditionalExtension.condition);

        // Reverse condition
        ConditionalExtension.condition = false;
        extensionService.updateServerValues(CallEvents.MANUAL);

        // Check that the wanted data exists
        checkThatServerDataExists(ConditionalExtension.condition);

        // Reverse condition
        ConditionalExtension.condition = false;
        extensionService.updatePlayerValues(playerUUID, TestConstants.PLAYER_ONE_NAME, CallEvents.MANUAL);

        // Check that the wanted data exists
        checkThatServerDataExists(ConditionalExtension.condition);
    }

    default void checkThatServerDataExists(boolean condition) {
        if (condition) { // Condition is true, conditional values exist
            List<ExtensionData> ofServer = db().query(new ExtensionServerDataQuery(serverUUID()));
            assertTrue(ofServer != null && !ofServer.isEmpty() && !ofServer.get(0).getTabs().isEmpty(), "There was no data left");

            ExtensionTabData tabData = ofServer.get(0).getTabs().get(0);
            OptionalAssert.equals("Yes", tabData.getBoolean("isCondition").map(ExtensionBooleanData::getFormattedValue));
            OptionalAssert.equals("Conditional", tabData.getString("conditionalValue").map(ExtensionStringData::getFormattedValue));
            OptionalAssert.equals("unconditional", tabData.getString("unconditional").map(ExtensionStringData::getFormattedValue)); // Was not removed
            assertFalse(tabData.getString("reversedConditionalValue").isPresent(), "Value was not removed: reversedConditionalValue");
        } else { // Condition is false, reversed conditional values exist
            List<ExtensionData> ofServer = db().query(new ExtensionServerDataQuery(serverUUID()));
            assertTrue(ofServer != null && !ofServer.isEmpty() && !ofServer.get(0).getTabs().isEmpty(), "There was no data left");
            ExtensionTabData tabData = ofServer.get(0).getTabs().get(0);
            OptionalAssert.equals("No", tabData.getBoolean("isCondition").map(ExtensionBooleanData::getFormattedValue));
            OptionalAssert.equals("Reversed", tabData.getString("reversedConditionalValue").map(ExtensionStringData::getFormattedValue));
            OptionalAssert.equals("unconditional", tabData.getString("unconditional").map(ExtensionStringData::getFormattedValue)); // Was not removed
            assertFalse(tabData.getString("conditionalValue").isPresent(), "Value was not removed: conditionalValue");
        }
    }

    @Test
    default void extensionServerTableValuesAreInserted() {
        ExtensionSvc extensionService = extensionService();

        extensionService.register(new TableExtension());
        extensionService.updateServerValues(CallEvents.MANUAL);
        extensionService.updateServerValues(CallEvents.MANUAL);

        List<ExtensionData> ofServer = db().query(new ExtensionServerDataQuery(serverUUID()));
        assertFalse(ofServer.isEmpty());

        ExtensionData extensionData = ofServer.get(0);
        List<ExtensionTabData> tabs = extensionData.getTabs();
        assertEquals(1, tabs.size()); // No tab defined, should contain 1 tab
        ExtensionTabData tabData = tabs.get(0);

        List<ExtensionTableData> tableData = tabData.getTableData();
        assertEquals(1, tableData.size());
        ExtensionTableData table = tableData.get(0);

        Table expected = Table.builder()
                .columnOne("first", Icon.called("gavel").build())
                .columnTwo("second", Icon.called("what").build())
                .columnThree("third", Icon.called("question").build())
                .columnFive("five", Icon.called("").build())
                .addRow("value", 3, 0.5)
                .build();

        assertEquals(expected, table.getTable());
    }

    @Test
    default void extensionPlayerTableValuesAreInserted() {
        ExtensionSvc extensionService = extensionService();

        extensionService.register(new TableExtension());
        extensionService.updatePlayerValues(playerUUID, TestConstants.PLAYER_ONE_NAME, CallEvents.MANUAL);
        extensionService.updatePlayerValues(playerUUID, TestConstants.PLAYER_ONE_NAME, CallEvents.MANUAL);

        Map<ServerUUID, List<ExtensionData>> ofPlayer = db().query(new ExtensionPlayerDataQuery(playerUUID));
        assertFalse(ofPlayer.isEmpty());

        List<ExtensionData> ofServer = ofPlayer.get(serverUUID());
        assertEquals(1, ofServer.size());
        ExtensionData extensionServerData = ofServer.get(0);
        List<ExtensionTabData> tabs = extensionServerData.getTabs();
        assertEquals(1, tabs.size()); // No tab defined, should contain 1 tab
        ExtensionTabData tabData = tabs.get(0);

        List<ExtensionTableData> tableData = tabData.getTableData();
        assertEquals(1, tableData.size());
        ExtensionTableData table = tableData.get(0);

        Table expected = Table.builder()
                .columnOne("first", Icon.called("gavel").build())
                .columnTwo("second", Icon.called("what").build())
                .columnThree("third", Icon.called("question").build())
                .addRow("value", 3, 0.5)
                .build();

        assertEquals(expected, table.getTable());
    }

    @Test
    @Disabled("Flaky test, possibly due to some kind of concurrent execution - one extra exception is sometimes caught")
    default void extensionExceptionsAreCaught() {
        TestErrorLogger.throwErrors(false);
        ExtensionSvc extensionService = extensionService();
        extensionService.register(new ThrowingExtension());

        extensionService.updateServerValues(CallEvents.MANUAL);
        extensionService.updatePlayerValues(playerUUID, TestConstants.PLAYER_ONE_NAME, CallEvents.MANUAL);
        // 5 of the exceptions need to be logged, there are 8 exceptions total 3 of which are ignored.
        assertEquals(5, TestErrorLogger.getCaught().size(), () -> "Not all exceptions got logged, logged exceptions: " + TestErrorLogger.getCaught().toString());
    }

    @Test
    default void clickEventsNotAllowed() {
        ExtensionSvc extensionService = extensionService();
        extensionService.register(new ClickEventTestExtension());

        extensionService.updateServerValues(CallEvents.MANUAL);

        String expected = "{\"text\":\"<Component contained disallowed words or characters>\"}";
        String result = db().query(new ExtensionServerDataQuery(serverUUID()))
                .get(0)
                .getTabs()
                .get(0)
                .getComponent("clickEventComponent")
                .orElseThrow(AssertionError::new)
                .getFormattedValue();
        assertEquals(expected, result);
    }

    @Test
    default void clickEventsNotAllowedForPlayer() {
        ExtensionSvc extensionService = extensionService();
        extensionService.register(new ClickEventTestExtension());

        extensionService.updatePlayerValues(TestConstants.PLAYER_ONE_UUID, TestConstants.PLAYER_ONE_NAME, CallEvents.MANUAL);

        String expected = "{\"text\":\"<Component contained disallowed words or characters>\"}";
        String result = db().query(new ExtensionPlayerDataQuery(TestConstants.PLAYER_ONE_UUID))
                .get(serverUUID())
                .get(0)
                .getTabs()
                .get(0)
                .getComponent("clickEventPlayerComponent")
                .orElseThrow(AssertionError::new)
                .getFormattedValue();
        assertEquals(expected, result);
    }


    @Test
    @DisplayName("ExtensionServerStatisticsQuery retrieves server statistics correctly")
    default void serverStatisticsAreQueriedCorrectly() {
        ExtensionSvc extensionService = extensionService();
        extensionService.register(new StatisticsExtension());
        extensionService.updateServerValues(CallEvents.SERVER_EXTENSION_REGISTER);

        Map<Integer, ExtensionData.Builder> result = db().query(new ExtensionServerStatisticsQuery(serverUUID()));
        assertFalse(result.isEmpty());

        List<ExtensionData> serverDataList = db().query(new ExtensionServerDataQuery(serverUUID()));
        assertFalse(serverDataList.isEmpty());

        ExtensionData extensionData = serverDataList.stream()
                .filter(data -> "StatisticsExtension".equals(data.getExtensionInformation().getPluginName()))
                .findFirst()
                .orElseThrow(AssertionError::new);

        List<ExtensionTabData> tabs = extensionData.getTabs();
        assertEquals(2, tabs.size());

        ExtensionTabData defaultTab = tabs.stream()
                .filter(t -> t.getTabInformation().getTabName().isEmpty())
                .findFirst()
                .orElseThrow(AssertionError::new);

        List<ExtensionStatisticsData> defaultStatsData = defaultTab.getStatisticsData();
        assertEquals(1, defaultStatsData.size());
        ExtensionStatisticsData statData1 = defaultStatsData.get(0);
        assertEquals("stats", statData1.getProviderName());
        assertEquals(Color.GREEN, statData1.getTableColor());
        assertEquals(Map.of("stat_one", 42L, "group:stat_two", 100L), statData1.getStatistics().getValueByKey());

        ExtensionTabData customTab = tabs.stream()
                .filter(t -> "CustomTab".equals(t.getTabInformation().getTabName()))
                .findFirst()
                .orElseThrow(AssertionError::new);

        List<ExtensionStatisticsData> customStatsData = customTab.getStatisticsData();
        assertEquals(1, customStatsData.size());
        ExtensionStatisticsData statData2 = customStatsData.get(0);
        assertEquals("tabStats", statData2.getProviderName());
        assertEquals(Color.BLUE, statData2.getTableColor());
        assertEquals(Map.of("custom_stat", 999L), statData2.getStatistics().getValueByKey());
    }

    @Test
    @DisplayName("ExtensionPlayerStatisticsQuery retrieves player statistics correctly")
    default void playerStatisticsAreQueriedCorrectly() {
        db().executeTransaction(new PlayerRegisterTransaction(TestConstants.PLAYER_ONE_UUID, System::currentTimeMillis, TestConstants.PLAYER_ONE_NAME));

        ExtensionSvc extensionService = extensionService();
        extensionService.register(new PlayerStatisticsExtension());
        extensionService.updatePlayerValues(TestConstants.PLAYER_ONE_UUID, TestConstants.PLAYER_ONE_NAME, CallEvents.MANUAL);

        Map<Integer, ExtensionData.Builder> result = db().query(new ExtensionPlayerStatisticsQuery(TestConstants.PLAYER_ONE_UUID));
        assertFalse(result.isEmpty());

        Map<ServerUUID, List<ExtensionData>> playerDataMap = db().query(new ExtensionPlayerDataQuery(TestConstants.PLAYER_ONE_UUID));
        assertFalse(playerDataMap.isEmpty());

        List<ExtensionData> playerDataList = playerDataMap.get(serverUUID());
        assertNotNull(playerDataList);

        ExtensionData extensionData = playerDataList.stream()
                .filter(data -> "PlayerStatisticsExtension".equals(data.getExtensionInformation().getPluginName()))
                .findFirst()
                .orElseThrow(AssertionError::new);

        List<ExtensionTabData> tabs = extensionData.getTabs();
        assertEquals(2, tabs.size());

        ExtensionTabData defaultTab = tabs.stream()
                .filter(t -> t.getTabInformation().getTabName().isEmpty())
                .findFirst()
                .orElseThrow(AssertionError::new);

        List<ExtensionStatisticsData> defaultStatsData = defaultTab.getStatisticsData();
        assertEquals(1, defaultStatsData.size());
        ExtensionStatisticsData statData1 = defaultStatsData.get(0);
        assertEquals("stats", statData1.getProviderName());
        assertEquals(Color.GREEN, statData1.getTableColor());
        assertEquals(Map.of("stat_one", 42L, "group:stat_two", 100L), statData1.getStatistics().getValueByKey());

        ExtensionTabData customTab = tabs.stream()
                .filter(t -> "CustomTab".equals(t.getTabInformation().getTabName()))
                .findFirst()
                .orElseThrow(AssertionError::new);

        List<ExtensionStatisticsData> customStatsData = customTab.getStatisticsData();
        assertEquals(1, customStatsData.size());
        ExtensionStatisticsData statData2 = customStatsData.get(0);
        assertEquals("tabStats", statData2.getProviderName());
        assertEquals(Color.BLUE, statData2.getTableColor());
        assertEquals(Map.of("custom_stat", 999L), statData2.getStatistics().getValueByKey());
    }

    @Test
    @DisplayName("ExtensionPlayerStatisticsQuery includes missing player statistics as null")
    default void playerMissingStatisticsAreNull() {
        db().executeTransaction(new PlayerRegisterTransaction(TestConstants.PLAYER_ONE_UUID, System::currentTimeMillis, TestConstants.PLAYER_ONE_NAME));
        db().executeTransaction(new PlayerRegisterTransaction(TestConstants.PLAYER_TWO_UUID, System::currentTimeMillis, TestConstants.PLAYER_TWO_NAME));

        ExtensionSvc extensionService = extensionService();
        extensionService.register(new PlayerStatisticsExtension());
        // First update player one so that stat_one and group:stat_two are registered in ExtensionStatisticTable
        extensionService.updatePlayerValues(TestConstants.PLAYER_ONE_UUID, TestConstants.PLAYER_ONE_NAME, CallEvents.MANUAL);
        // Player two only has stat_one
        extensionService.updatePlayerValues(TestConstants.PLAYER_TWO_UUID, TestConstants.PLAYER_TWO_NAME, CallEvents.MANUAL);

        Map<Integer, ExtensionData.Builder> result = db().query(new ExtensionPlayerStatisticsQuery(TestConstants.PLAYER_TWO_UUID));
        assertFalse(result.isEmpty());

        Map<ServerUUID, List<ExtensionData>> playerDataMap = db().query(new ExtensionPlayerDataQuery(TestConstants.PLAYER_TWO_UUID));
        List<ExtensionData> playerDataList = playerDataMap.get(serverUUID());
        assertNotNull(playerDataList);

        ExtensionData extensionData = playerDataList.stream()
                .filter(data -> "PlayerStatisticsExtension".equals(data.getExtensionInformation().getPluginName()))
                .findFirst()
                .orElseThrow(AssertionError::new);

        ExtensionTabData defaultTab = extensionData.getTabs().stream()
                .filter(t -> t.getTabInformation().getTabName().isEmpty())
                .findFirst()
                .orElseThrow(AssertionError::new);

        ExtensionStatisticsData statData1 = defaultTab.getStatisticsData().get(0);
        Map<String, Long> values = statData1.getStatistics().getValueByKey();
        assertEquals(10L, values.get("stat_one"));
        assertTrue(values.containsKey("group:stat_two"));
        assertNull(values.get("group:stat_two"));
    }
}
