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

import com.djrapitops.plan.delivery.domain.datatransfer.extension.ExtensionDataDto;
import com.djrapitops.plan.delivery.domain.datatransfer.extension.ExtensionFlagsDataDto;
import com.djrapitops.plan.extension.CallEvents;
import com.djrapitops.plan.extension.ExtensionSvc;
import com.djrapitops.plan.extension.implementation.results.ExtensionData;
import com.djrapitops.plan.extension.implementation.storage.queries.ExtensionPlayerDataQuery;
import com.djrapitops.plan.extension.implementation.storage.queries.ExtensionServerDataQuery;
import com.djrapitops.plan.extension.implementation.storage.transactions.results.RemoveInvalidResultsTransaction;
import com.djrapitops.plan.extension.statistic.Flags;
import com.djrapitops.plan.settings.config.ExtensionSettings;
import com.djrapitops.plan.storage.database.DatabaseTestPreparer;
import com.djrapitops.plan.storage.database.queries.objects.ServerQueries;
import com.djrapitops.plan.storage.database.queries.objects.UserIdentifierQueries;
import com.djrapitops.plan.storage.database.sql.tables.extension.*;
import com.djrapitops.plan.storage.database.transactions.commands.RemovePlayerTransaction;
import com.djrapitops.plan.storage.database.transactions.commands.RemoveServerTransaction;
import com.djrapitops.plan.storage.database.transactions.events.PlayerRegisterTransaction;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import utilities.TestConstants;
import utilities.TestExtensions.FlagsExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Extension flags storage.
 *
 * @author AuroraLS3
 */
public interface ExtensionFlagsDatabaseTest extends DatabaseTestPreparer {

    @BeforeEach
    default void unregisterFlagExtension() {
        componentService().register();
        ExtensionSvc extensionService = extensionService();
        extensionService.register();
        extensionService.unregister(new FlagsExtension());
        ExtensionSettings extensionSettings = config().getExtensionSettings();
        extensionSettings.setEnabled("FlagsExtension", true);
    }

    @Test
    @DisplayName("Flag providers preserve definitions and reconcile server and player values")
    default void flagsAreStoredPerProviderAndScope() {
        db().executeTransaction(new PlayerRegisterTransaction(TestConstants.PLAYER_ONE_UUID, System::currentTimeMillis, TestConstants.PLAYER_ONE_NAME));

        FlagsExtension extension = new FlagsExtension();
        ExtensionSvc extensionService = extensionService();
        extensionService.register(extension);
        extensionService.updateServerValues(CallEvents.MANUAL);
        extensionService.updatePlayerValues(TestConstants.PLAYER_ONE_UUID, TestConstants.PLAYER_ONE_NAME, CallEvents.MANUAL);

        assertEquals(Map.of("shared_flag", true, "server_false", false, "server_omitted", true), flagValues(ExtensionFlagServerValueTable.TABLE_NAME, null));
        assertEquals(Map.of("shared_flag", false, "player_false", false, "player_omitted", true), flagValues(
                ExtensionFlagPlayerValueTable.TABLE_NAME, TestConstants.PLAYER_ONE_UUID));
        assertEquals(Set.of("shared_flag", "server_false", "server_missing", "server_omitted", "player_false", "player_missing", "player_omitted"), flagNames());
        assertEquals(2, providerCountForFlag("shared_flag"));
        assertEquals(Set.of(TableType.SERVER.getType(), TableType.PLAYER.getType()), flagTableTypes("shared_flag"));
        assertQueriedFlagValues(serverFlagsFor(), Map.of(
                "shared_flag", true, "server_false", false, "server_omitted", true), "Server Flags", "GREEN", "server_missing");
        assertQueriedFlagValues(playerFlagsFor(TestConstants.PLAYER_ONE_UUID), Map.of(
                "shared_flag", false, "player_false", false, "player_omitted", true), "Player Flags", "BLUE", "player_missing");

        extension.serverFlags = Flags.create()
                .put("shared_flag", false)
                .put("server_false", null);
        extension.playerFlags = Flags.create()
                .put("shared_flag", true)
                .put("player_false", null);
        extensionService.updateServerValues(CallEvents.MANUAL);
        extensionService.updatePlayerValues(TestConstants.PLAYER_ONE_UUID, TestConstants.PLAYER_ONE_NAME, CallEvents.MANUAL);

        assertEquals(Map.of("shared_flag", false), flagValues(ExtensionFlagServerValueTable.TABLE_NAME, null));
        assertEquals(Map.of("shared_flag", true), flagValues(
                ExtensionFlagPlayerValueTable.TABLE_NAME, TestConstants.PLAYER_ONE_UUID));
        assertEquals(Set.of("shared_flag", "server_false", "server_missing", "server_omitted", "player_false", "player_missing", "player_omitted"), flagNames());
        assertQueriedFlagValues(serverFlagsFor(), Map.of("shared_flag", false), "Server Flags", "GREEN", "server_false", "server_missing", "server_omitted");
        assertQueriedFlagValues(playerFlagsFor(TestConstants.PLAYER_ONE_UUID), Map.of("shared_flag", true), "Player Flags", "BLUE", "player_false", "player_missing", "player_omitted");
    }

    @Test
    @DisplayName("Flag cleanup removes only the invalidated provider and deleted scopes")
    default void flagsAreRemovedWithProvidersAndScopes() {
        db().executeTransaction(new PlayerRegisterTransaction(TestConstants.PLAYER_ONE_UUID, System::currentTimeMillis, TestConstants.PLAYER_ONE_NAME));

        ExtensionSvc extensionService = extensionService();
        extensionService.register(new FlagsExtension());
        extensionService.updateServerValues(CallEvents.MANUAL);
        extensionService.updatePlayerValues(TestConstants.PLAYER_ONE_UUID, TestConstants.PLAYER_ONE_NAME, CallEvents.MANUAL);

        db().executeTransaction(new RemoveInvalidResultsTransaction("FlagsExtension", serverUUID(), List.of("serverFlags"))).join();
        assertEquals(0, flagValueCount(ExtensionFlagServerValueTable.TABLE_NAME));
        assertEquals(1, providerCountForFlag("shared_flag"));
        assertTrue(flagNames().contains("player_false"));
        assertFalse(flagNames().contains("server_false"));

        db().executeTransaction(new RemovePlayerTransaction(TestConstants.PLAYER_ONE_UUID)).join();
        assertEquals(0, flagValueCount(ExtensionFlagPlayerValueTable.TABLE_NAME));
        assertTrue(flagNames().contains("player_false"));

        db().executeTransaction(new RemoveServerTransaction(serverUUID())).join();
        assertEquals(0, flagValueCount(ExtensionFlagPlayerValueTable.TABLE_NAME));
        assertEquals(0, flagValueCount(ExtensionFlagServerValueTable.TABLE_NAME));
        assertEquals(0, flagDefinitionCount());
    }

    private ExtensionData serverFlagsFor() {
        return db().query(new ExtensionServerDataQuery(serverUUID())).stream()
                .filter(data -> "FlagsExtension".equals(data.getExtensionInformation().getPluginName()))
                .findFirst()
                .orElseThrow();
    }

    private ExtensionData playerFlagsFor(UUID playerUUID) {
        return db().query(new ExtensionPlayerDataQuery(playerUUID)).get(serverUUID()).stream()
                .filter(data -> "FlagsExtension".equals(data.getExtensionInformation().getPluginName()))
                .findFirst()
                .orElseThrow();
    }

    private void assertQueriedFlagValues(ExtensionData extensionData, Map<String, Boolean> expectedValues, String expectedProviderText, String expectedTableColor, String... missingFlagNames) {
        Map<String, Boolean> actualValues = new HashMap<>();
        List<ExtensionFlagsDataDto> providers = new ExtensionDataDto(extensionData).tabs().stream()
                .flatMap(tab -> tab.flagsData().stream())
                .toList();
        assertEquals(1, providers.size(), "Only providers in this scope should be returned");
        assertEquals(expectedProviderText, providers.get(0).providerText());
        assertEquals(expectedTableColor, providers.get(0).tableColor());
        actualValues.putAll(providers.get(0).flags().valueByKey());

        Map<String, Boolean> allExpectedValues = new HashMap<>(expectedValues);
        for (String missingFlagName : missingFlagNames) {
            allExpectedValues.put(missingFlagName, null);
        }
        assertEquals(allExpectedValues, actualValues);

        var serializedFlags = JsonParser.parseString(new Gson().toJson(providers.get(0)))
                .getAsJsonObject()
                .getAsJsonObject("flags")
                .getAsJsonObject("valueByKey");
        for (String missingFlagName : missingFlagNames) {
            assertTrue(serializedFlags.has(missingFlagName));
            assertTrue(serializedFlags.get(missingFlagName).isJsonNull());
        }
    }

    private Map<String, Boolean> flagValues(String valueTable, UUID playerUUID) {
        var serverId = db().query(ServerQueries.fetchServerId(serverUUID())).orElseThrow();
        String scope = playerUUID == null
                ? "flag_value." + ExtensionFlagServerValueTable.SERVER_ID + "=?"
                : "flag_value." + ExtensionFlagPlayerValueTable.USER_ID + "=? AND flag_value." + ExtensionFlagPlayerValueTable.SERVER_ID + "=?";
        String sql = "SELECT flag." + ExtensionFlagTable.FLAG_NAME + ", flag_value." +
                (playerUUID == null ? ExtensionFlagServerValueTable.VALUE : ExtensionFlagPlayerValueTable.VALUE) +
                " FROM " + valueTable + " flag_value" +
                " INNER JOIN " + ExtensionFlagTable.TABLE_NAME + " flag ON flag." + ExtensionFlagTable.ID + "=flag_value." +
                (playerUUID == null ? ExtensionFlagServerValueTable.FLAG_ID : ExtensionFlagPlayerValueTable.FLAG_ID) +
                " INNER JOIN " + ExtensionProviderTable.TABLE_NAME + " provider ON provider." + ExtensionProviderTable.ID + "=flag." + ExtensionFlagTable.PROVIDER_ID +
                " INNER JOIN " + ExtensionPluginTable.TABLE_NAME + " plugin ON plugin." + ExtensionPluginTable.ID + "=provider." + ExtensionProviderTable.PLUGIN_ID +
                " WHERE plugin." + ExtensionPluginTable.PLUGIN_NAME + "=? AND " + scope;
        if (playerUUID == null) {
            return db().queryMap(sql, (row, map) -> map.put(row.getString(1), row.getBoolean(2)), "FlagsExtension", serverId);
        }
        var userId = db().query(UserIdentifierQueries.fetchUserId(playerUUID)).orElseThrow();
        return db().queryMap(sql, (row, map) -> map.put(row.getString(1), row.getBoolean(2)), "FlagsExtension", userId, serverId);
    }

    private Set<String> flagNames() {
        String sql = "SELECT flag." + ExtensionFlagTable.FLAG_NAME +
                " FROM " + ExtensionFlagTable.TABLE_NAME + " flag" +
                " INNER JOIN " + ExtensionProviderTable.TABLE_NAME + " provider ON provider." + ExtensionProviderTable.ID + "=flag." + ExtensionFlagTable.PROVIDER_ID +
                " INNER JOIN " + ExtensionPluginTable.TABLE_NAME + " plugin ON plugin." + ExtensionPluginTable.ID + "=provider." + ExtensionProviderTable.PLUGIN_ID +
                " WHERE plugin." + ExtensionPluginTable.PLUGIN_NAME + "=?";
        return db().querySet(sql, row -> row.getString(1), "FlagsExtension");
    }

    private int providerCountForFlag(String flagName) {
        String sql = "SELECT COUNT(DISTINCT flag." + ExtensionFlagTable.PROVIDER_ID + ")" +
                " FROM " + ExtensionFlagTable.TABLE_NAME + " flag" +
                " INNER JOIN " + ExtensionProviderTable.TABLE_NAME + " provider ON provider." + ExtensionProviderTable.ID + "=flag." + ExtensionFlagTable.PROVIDER_ID +
                " INNER JOIN " + ExtensionPluginTable.TABLE_NAME + " plugin ON plugin." + ExtensionPluginTable.ID + "=provider." + ExtensionProviderTable.PLUGIN_ID +
                " WHERE plugin." + ExtensionPluginTable.PLUGIN_NAME + "=? AND flag." + ExtensionFlagTable.FLAG_NAME + "=?";
        return db().queryOptional(sql, row -> row.getInt(1), "FlagsExtension", flagName).orElse(0);
    }

    private Set<Integer> flagTableTypes(String flagName) {
        String sql = "SELECT flag." + ExtensionFlagTable.ID + ",flag." + ExtensionFlagTable.PROVIDER_ID +
                ",flag." + ExtensionFlagTable.FLAG_NAME + ",flag." + ExtensionFlagTable.PARAMETER_TYPE +
                " FROM " + ExtensionFlagTable.TABLE_NAME + " flag" +
                " INNER JOIN " + ExtensionProviderTable.TABLE_NAME + " provider ON provider." + ExtensionProviderTable.ID + "=flag." + ExtensionFlagTable.PROVIDER_ID +
                " INNER JOIN " + ExtensionPluginTable.TABLE_NAME + " plugin ON plugin." + ExtensionPluginTable.ID + "=provider." + ExtensionProviderTable.PLUGIN_ID +
                " WHERE plugin." + ExtensionPluginTable.PLUGIN_NAME + "=? AND flag." + ExtensionFlagTable.FLAG_NAME + "=?";
        return db().querySet(sql, row -> ExtensionFlagTable.Row.extract(row).getTableType().getType(), "FlagsExtension", flagName);
    }

    private int flagDefinitionCount() {
        return db().queryOptional("SELECT COUNT(*) FROM " + ExtensionFlagTable.TABLE_NAME, row -> row.getInt(1)).orElse(0);
    }

    private int flagValueCount(String valueTable) {
        return db().queryOptional("SELECT COUNT(*) FROM " + valueTable, row -> row.getInt(1)).orElse(0);
    }
}
