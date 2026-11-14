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
import com.djrapitops.plan.gathering.domain.builders.TPSBuilder;
import com.djrapitops.plan.identification.Server;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.transactions.StoreServerInformationTransaction;
import com.djrapitops.plan.storage.database.transactions.events.TPSStoreTransaction;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import utilities.DBPreparer;
import utilities.TestConstants;
import utilities.dagger.PlanPluginComponent;
import utilities.mocks.PluginMockComponent;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author AuroraLS3
 */
class NetworkPlayersOnlinePlaceholdersTest {

    private static PlanPluginComponent component;
    private static PlanPlaceholders underTest;

    @BeforeAll
    static void prepareSystem(@TempDir Path tempDir) {
        PluginMockComponent mockComponent = new PluginMockComponent(tempDir);
        component = mockComponent.getComponent();
        mockComponent.getPlanSystem().enable();
        underTest = component.placeholders();
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

    @Test
    void networkPlayersOnlineGraphDisplaysDataSingleProxy() {
        Database database = component.system().getDatabaseSystem().getDatabase();
        database.executeTransaction(new StoreServerInformationTransaction(new Server(-1, TestConstants.SERVER_UUID, TestConstants.SERVER_NAME, "", false, TestConstants.VERSION)));
        database.executeTransaction(new StoreServerInformationTransaction(new Server(-1, TestConstants.SERVER_TWO_UUID, TestConstants.SERVER_TWO_NAME, "", true, TestConstants.VERSION)));
        database.executeTransaction(new TPSStoreTransaction(TestConstants.SERVER_UUID, TPSBuilder.get()
                .playersOnline(1)
                .date(System.currentTimeMillis())
                .toTPS()));
        database.executeTransaction(new TPSStoreTransaction(TestConstants.SERVER_TWO_UUID, TPSBuilder.get()
                .playersOnline(2)
                .date(System.currentTimeMillis())
                .toTPS()));

        DBPreparer.awaitUntilTransactionsComplete(database);
        String value = underTest.onPlaceholderRequest(TestConstants.PLAYER_ONE_UUID, "network_players_online", List.of());
        assertEquals("2", value);
    }

    @Test
    void networkPlayersOnlineGraphDisplaysDataTwoProxies() {
        Database database = component.system().getDatabaseSystem().getDatabase();
        database.executeTransaction(new StoreServerInformationTransaction(new Server(-1, TestConstants.SERVER_UUID, TestConstants.SERVER_NAME, "", true, TestConstants.VERSION)));
        database.executeTransaction(new StoreServerInformationTransaction(new Server(-1, TestConstants.SERVER_TWO_UUID, TestConstants.SERVER_TWO_NAME, "", true, TestConstants.VERSION)));
        database.executeTransaction(new TPSStoreTransaction(TestConstants.SERVER_UUID, TPSBuilder.get()
                .playersOnline(1)
                .date(System.currentTimeMillis())
                .toTPS()));
        database.executeTransaction(new TPSStoreTransaction(TestConstants.SERVER_TWO_UUID, TPSBuilder.get()
                .playersOnline(2)
                .date(System.currentTimeMillis())
                .toTPS()));

        DBPreparer.awaitUntilTransactionsComplete(database);

        String value = underTest.onPlaceholderRequest(TestConstants.PLAYER_ONE_UUID, "network_players_online", List.of());
        assertEquals("3", value);
    }

    @Test
    void networkPlayersOnlineGraphDisplaysDataNoProxies() {
        Database database = component.system().getDatabaseSystem().getDatabase();
        database.executeTransaction(new StoreServerInformationTransaction(new Server(-1, TestConstants.SERVER_UUID, TestConstants.SERVER_NAME, "", false, TestConstants.VERSION)));
        database.executeTransaction(new StoreServerInformationTransaction(new Server(-1, TestConstants.SERVER_TWO_UUID, TestConstants.SERVER_TWO_NAME, "", false, TestConstants.VERSION)));
        database.executeTransaction(new TPSStoreTransaction(TestConstants.SERVER_UUID, TPSBuilder.get()
                .playersOnline(1)
                .date(System.currentTimeMillis())
                .toTPS()));
        database.executeTransaction(new TPSStoreTransaction(TestConstants.SERVER_TWO_UUID, TPSBuilder.get()
                .playersOnline(2)
                .date(System.currentTimeMillis())
                .toTPS()));
        DBPreparer.awaitUntilTransactionsComplete(database);

        String value = underTest.onPlaceholderRequest(TestConstants.PLAYER_ONE_UUID, "network_players_online", List.of());
        assertEquals("3", value);
    }
}
