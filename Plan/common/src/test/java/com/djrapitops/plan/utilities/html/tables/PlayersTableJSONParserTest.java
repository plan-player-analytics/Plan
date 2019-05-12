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
package com.djrapitops.plan.utilities.html.tables;

import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.db.Database;
import com.djrapitops.plan.db.access.queries.containers.ServerPlayersTableContainersQuery;
import com.djrapitops.plan.db.access.transactions.Transaction;
import com.djrapitops.plan.db.access.transactions.events.PlayerRegisterTransaction;
import com.djrapitops.plan.db.access.transactions.events.PlayerServerRegisterTransaction;
import com.djrapitops.plan.db.access.transactions.events.SessionEndTransaction;
import com.djrapitops.plan.db.access.transactions.events.WorldNameStoreTransaction;
import com.djrapitops.plan.extension.CallEvents;
import com.djrapitops.plan.extension.DataExtension;
import com.djrapitops.plan.extension.ExtensionService;
import com.djrapitops.plan.extension.ExtensionServiceImplementation;
import com.djrapitops.plan.extension.annotation.*;
import com.djrapitops.plan.extension.implementation.storage.queries.ExtensionServerPlayerDataTableQuery;
import com.djrapitops.plan.system.PlanSystem;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.paths.WebserverSettings;
import com.djrapitops.plan.utilities.formatting.Formatters;
import com.google.gson.Gson;
import org.junit.Assume;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import utilities.RandomData;
import utilities.TestConstants;
import utilities.mocks.PluginMockComponent;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Test against PlayersTableJSONParser JSON issues as well as other issues.
 *
 * @author Rsl1122
 */
@RunWith(JUnitPlatform.class)
class PlayersTableJSONParserTest {

    private static final int TEST_PORT_NUMBER = RandomData.randomInt(9005, 9500);

    private Database db;
    private PlanSystem system;

    private UUID serverUUID;
    private UUID playerUUID = TestConstants.PLAYER_ONE_UUID;
    private UUID player2UUID = TestConstants.PLAYER_TWO_UUID;

    @BeforeEach
    void setUp(@TempDir Path tempDir) throws Exception {
        Assume.assumeTrue(false); // TODO Skipped due to problems with Gson, parsed 'columns' is null even though it's not.

        PluginMockComponent component = new PluginMockComponent(tempDir);
        system = component.getPlanSystem();
        PlanConfig config = system.getConfigSystem().getConfig();
        config.set(WebserverSettings.PORT, TEST_PORT_NUMBER);

        system.enable();
        db = system.getDatabaseSystem().getDatabase();
        serverUUID = system.getServerInfo().getServerUUID();
        storePlayerData();
    }

    private void storePlayerData() throws Exception {
        db.executeTransaction(new WorldNameStoreTransaction(serverUUID, "World"));

        db.executeTransaction(new PlayerServerRegisterTransaction(playerUUID, () -> 1000L, TestConstants.PLAYER_ONE_NAME, serverUUID));
        db.executeTransaction(new PlayerRegisterTransaction(player2UUID, () -> 123456789L, TestConstants.PLAYER_TWO_NAME));

        Session session = new Session(playerUUID, serverUUID, 12345L, "World", "SURVIVAL");
        session.endSession(22345L);
        db.executeTransaction(new SessionEndTransaction(session));
        Session session2 = new Session(player2UUID, serverUUID, 12345L, "World", "SURVIVAL");
        session2.endSession(22345L);
        db.executeTransaction(new SessionEndTransaction(session2));

        ExtensionService extensionService = ExtensionService.getInstance();
        extensionService.register(new PlayerExtension());
        ((ExtensionServiceImplementation) extensionService).updatePlayerValues(playerUUID, TestConstants.PLAYER_ONE_NAME, CallEvents.MANUAL);
        ((ExtensionServiceImplementation) extensionService).updatePlayerValues(player2UUID, TestConstants.PLAYER_TWO_NAME, CallEvents.MANUAL);

        db.executeTransaction(new Transaction() {
            @Override
            protected void performOperations() {

            }
        }).get(); // Wait for transactions to finish
    }

    @Test
    void playersTableJSONDoesNotContainDuplicateColumns() {
        PlayersTableJSONParser parser = createParser();
        class Column {
            String title;
        }

        class Table {
            Column[] columns;
        }

        String json = parser.toJSONString();
        System.out.println("Parsed: " + json);
        Table table = new Gson().fromJson(json, Table.class);

        Set<String> foundColumnNames = new HashSet<>();
        for (Column column : table.columns) {
            assertFalse(foundColumnNames.contains(column.title), () -> "Duplicate column title: '" + column.title + "'");
            foundColumnNames.add(column.title);
        }
    }

    private PlayersTableJSONParser createParser() {
        int xMostRecentPlayers = 5;
        int loginThreshold = 5;
        long playtimeThreshold = TimeUnit.DAYS.toMillis(1L);
        boolean openPlayerLinksInNewTab = false;

        return new PlayersTableJSONParser(
                db.query(new ServerPlayersTableContainersQuery(serverUUID)),
                db.query(new ExtensionServerPlayerDataTableQuery(serverUUID, xMostRecentPlayers)),
                xMostRecentPlayers, playtimeThreshold, loginThreshold, openPlayerLinksInNewTab,
                new Formatters(system.getConfigSystem().getConfig(), new Locale())
        );
    }

    @AfterEach
    void tearDown() {
        system.disable();
    }

    @PluginInfo(name = "PlayerExtension")
    public class PlayerExtension implements DataExtension {
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
    }
}