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

import com.djrapitops.plan.component.Component;
import com.djrapitops.plan.component.ComponentService;
import com.djrapitops.plan.extension.CallEvents;
import com.djrapitops.plan.extension.DataExtension;
import com.djrapitops.plan.extension.ExtensionSvc;
import com.djrapitops.plan.extension.NotReadyException;
import com.djrapitops.plan.extension.annotation.*;
import com.djrapitops.plan.extension.builder.ExtensionDataBuilder;
import com.djrapitops.plan.extension.icon.Color;
import com.djrapitops.plan.extension.icon.Icon;
import com.djrapitops.plan.extension.implementation.results.*;
import com.djrapitops.plan.extension.implementation.storage.queries.ExtensionPlayerDataQuery;
import com.djrapitops.plan.extension.implementation.storage.queries.ExtensionServerDataQuery;
import com.djrapitops.plan.extension.implementation.storage.queries.table.ExtensionServerTableDataQuery;
import com.djrapitops.plan.extension.table.Table;
import com.djrapitops.plan.gathering.domain.ActiveSession;
import com.djrapitops.plan.gathering.domain.WorldTimes;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.DatabaseTestPreparer;
import com.djrapitops.plan.storage.database.transactions.commands.RemoveEverythingTransaction;
import com.djrapitops.plan.storage.database.transactions.events.PlayerRegisterTransaction;
import com.djrapitops.plan.storage.database.transactions.events.StoreSessionTransaction;
import com.djrapitops.plan.storage.database.transactions.events.StoreWorldNameTransaction;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import utilities.OptionalAssert;
import utilities.RandomData;
import utilities.TestConstants;
import utilities.TestErrorLogger;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

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
                .columnOne("a group", Icon.called("circle").build())
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


    @PluginInfo(name = "ConditionalExtension")
    class ConditionalExtension implements DataExtension {

        static boolean condition = true;

        @BooleanProvider(text = "a boolean", conditionName = "condition")
        public boolean isCondition(UUID playerUUID) {
            return condition;
        }

        @StringProvider(text = "Conditional Value")
        @Conditional("condition")
        public String conditionalValue(UUID playerUUID) {
            return "Conditional";
        }

        @StringProvider(text = "Reversed Conditional Value")
        @Conditional(value = "condition", negated = true)
        public String reversedConditionalValue(UUID playerUUID) {
            return "Reversed";
        }

        @StringProvider(text = "Unconditional")
        public String unconditional(UUID playerUUID) {
            return "unconditional";
        }

        @BooleanProvider(text = "a boolean", conditionName = "condition")
        public boolean isCondition() {
            return condition;
        }

        @StringProvider(text = "Conditional Value")
        @Conditional("condition")
        public String conditionalValue() {
            return "Conditional";
        }

        @StringProvider(text = "Reversed Conditional Value")
        @Conditional(value = "condition", negated = true)
        public String reversedConditionalValue() {
            return "Reversed";
        }

        @GroupProvider(text = "Conditional Group")
        @Conditional("condition")
        public String[] conditionalGroups(UUID playerUUID) {
            return new String[]{"Group"};
        }

        @StringProvider(text = "Unconditional")
        public String unconditional() {
            return "unconditional";
        }
    }

    @PluginInfo(name = "ConditionalExtension")
    class RemovingConditionalExtension implements DataExtension {

        static boolean condition = true;

        @BooleanProvider(text = "a boolean", conditionName = "condition")
        public boolean isCondition(UUID playerUUID) {
            return condition;
        }

        @StringProvider(text = "Conditional Value")
        @Conditional("condition")
        public String conditionalValue(UUID playerUUID) {
            return "Conditional";
        }

        @BooleanProvider(text = "a boolean", conditionName = "condition")
        public boolean isCondition() {
            return condition;
        }

        @StringProvider(text = "Conditional Value")
        @Conditional("condition")
        public String conditionalValue() {
            return "Conditional";
        }
    }

    @PluginInfo(name = "ServerExtension")
    class ServerExtension implements DataExtension {
        @NumberProvider(text = "a number")
        public long value() {
            return 5L;
        }

        @BooleanProvider(text = "a boolean")
        public boolean boolVal() {
            return false;
        }

        @DoubleProvider(text = "a double")
        public double doubleVal() {
            return 0.5;
        }

        @PercentageProvider(text = "a percentage")
        public double percentageVal() {
            return 0.5;
        }

        @StringProvider(text = "a string")
        public String stringVal() {
            return "Something";
        }
    }

    @PluginInfo(name = "PlayerExtension")
    class PlayerExtension implements DataExtension {
        @NumberProvider(text = "a number", showInPlayerTable = true)
        public long value(UUID playerUUD) {
            return 5L;
        }

        @BooleanProvider(text = "a boolean", showInPlayerTable = true)
        public boolean boolVal(UUID playerUUID) {
            return false;
        }

        @DoubleProvider(text = "a double", showInPlayerTable = true)
        public double doubleVal(UUID playerUUID) {
            return 0.5;
        }

        @PercentageProvider(text = "a percentage", showInPlayerTable = true)
        public double percentageVal(UUID playerUUID) {
            return 0.5;
        }

        @StringProvider(text = "a string", showInPlayerTable = true)
        public String stringVal(UUID playerUUID) {
            return "Something";
        }

        @GroupProvider(text = "a group")
        public String[] groupVal(UUID playerUUID) {
            return new String[]{"Group"};
        }

        @ComponentProvider(text = "colored text")
        public Component componentVal(UUID playerUUID) {
            return ComponentService.getInstance().fromLegacy("&aTest", '&');
        }
    }

    @PluginInfo(name = "TableExtension")
    class TableExtension implements DataExtension {
        @TableProvider(tableColor = Color.AMBER)
        public Table table() {
            return createTestTable();
        }

        @TableProvider(tableColor = Color.AMBER)
        public Table playerTable(UUID playerUUID) {
            return createTestTable();
        }

        private Table createTestTable() {
            return Table.builder()
                    .columnOne("first", Icon.called("gavel").of(Color.AMBER).build())
                    .columnTwo("second", Icon.called("what").of(Color.BROWN).build()) // Colors are ignored
                    .columnThree("third", null)                  // Can handle improper icons
                    .columnFive("five", Icon.called("").build()) // Can handle null column in between and ignore the next column
                    .addRow("value", 3, 0.5, 400L)               // Can handle too many row values
                    .build();
        }
    }

    @PluginInfo(name = "ThrowingExtension")
    class ThrowingExtension implements DataExtension {
        @BooleanProvider(text = "a boolean")
        public boolean booleanMethod() {
            throw new IllegalArgumentException("Failed to catch");
        }

        @BooleanProvider(text = "a boolean")
        public boolean booleanPlayerMethod(UUID playerUUID) {
            throw new NotReadyException();
        }

        @StringProvider(text = "a string")
        public String stringMethod() {
            throw new NoSuchMethodError();
        }

        @NumberProvider(text = "a string")
        public long numberMethod() {
            throw new UnsupportedOperationException();
        }

        @GroupProvider(text = "group")
        public String[] groupMethod(UUID playerUUID) {
            throw new NoClassDefFoundError();
        }

        @DataBuilderProvider
        public ExtensionDataBuilder builder() {
            return newExtensionDataBuilder()
                    .addValue(String.class, () -> {
                        throw new NotReadyException();
                    });
        }

        @DataBuilderProvider
        public ExtensionDataBuilder builder2() {
            return newExtensionDataBuilder()
                    .addValue(String.class, () -> {
                        throw new NoClassDefFoundError();
                    });
        }

        @DataBuilderProvider
        public ExtensionDataBuilder builder3() {
            throw new NoSuchMethodError();
        }
    }
}
