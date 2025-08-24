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
package com.djrapitops.plan.extension.implementation.storage.queries;

import com.djrapitops.plan.extension.CallEvents;
import com.djrapitops.plan.extension.ExtensionSvc;
import com.djrapitops.plan.extension.implementation.results.ExtensionTabData;
import com.djrapitops.plan.gathering.domain.DataMap;
import com.djrapitops.plan.gathering.domain.FinishedSession;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.DatabaseTestPreparer;
import com.djrapitops.plan.storage.database.queries.ExtensionsDatabaseTest;
import com.djrapitops.plan.storage.database.queries.objects.UserIdentifierQueries;
import com.djrapitops.plan.storage.database.transactions.events.PlayerRegisterTransaction;
import com.djrapitops.plan.storage.database.transactions.events.StoreSessionTransaction;
import com.djrapitops.plan.storage.database.transactions.events.StoreWorldNameTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import utilities.RandomData;
import utilities.TestConstants;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public interface ExtensionQueryResultTableDataQueryTest extends DatabaseTestPreparer {

    @BeforeEach
    default void unregisterExtension() {
        ExtensionSvc extensionService = extensionService();
        extensionService.unregister(new ExtensionsDatabaseTest.PlayerExtension());
    }

    @Test
    @DisplayName("Query result table query for extension data gets results")
    default void extensionQueryResultTableQueryGetsResults() {
        storeDataForTest();

        Integer userIdOne = db().query(UserIdentifierQueries.fetchUserId(TestConstants.PLAYER_ONE_UUID))
                .orElseThrow(AssertionError::new);
        Integer userIdTwo = db().query(UserIdentifierQueries.fetchUserId(TestConstants.PLAYER_TWO_UUID))
                .orElseThrow(AssertionError::new);
        Map<UUID, ExtensionTabData> result = db().query(new ExtensionQueryResultTableDataQuery(serverUUID(), List.of(userIdOne, userIdTwo)));
        assertEquals(Set.of(TestConstants.PLAYER_ONE_UUID, TestConstants.PLAYER_TWO_UUID), result.keySet());
    }

    private void storeDataForTest() {
        ExtensionSvc extensionService = extensionService();
        extensionService.register(new ExtensionsDatabaseTest.PlayerExtension());

        Database database = db();
        ServerUUID serverUUID = serverUUID();
        UUID uuid = TestConstants.PLAYER_ONE_UUID;
        database.executeTransaction(new PlayerRegisterTransaction(uuid, RandomData::randomTime, TestConstants.PLAYER_ONE_NAME));
        database.executeTransaction(new PlayerRegisterTransaction(TestConstants.PLAYER_TWO_UUID, RandomData::randomTime, TestConstants.PLAYER_TWO_NAME));
        FinishedSession session = new FinishedSession(uuid, serverUUID, 1000L, 11000L, 500L, new DataMap());
        database.executeTransaction(new StoreWorldNameTransaction(serverUUID, "world"));
        database.executeTransaction(new StoreSessionTransaction(session));

        extensionService.updatePlayerValues(uuid, TestConstants.PLAYER_ONE_NAME, CallEvents.MANUAL);
        extensionService.updatePlayerValues(TestConstants.PLAYER_TWO_UUID, TestConstants.PLAYER_TWO_NAME, CallEvents.MANUAL);
    }

}