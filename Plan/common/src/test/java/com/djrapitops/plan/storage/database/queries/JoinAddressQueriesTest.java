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

import com.djrapitops.plan.delivery.domain.DateObj;
import com.djrapitops.plan.gathering.domain.FinishedSession;
import com.djrapitops.plan.gathering.domain.event.JoinAddress;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.settings.config.paths.DataGatheringSettings;
import com.djrapitops.plan.storage.database.DatabaseTestPreparer;
import com.djrapitops.plan.storage.database.queries.objects.BaseUserQueries;
import com.djrapitops.plan.storage.database.queries.objects.JoinAddressQueries;
import com.djrapitops.plan.storage.database.queries.objects.SessionQueries;
import com.djrapitops.plan.storage.database.sql.tables.JoinAddressTable;
import com.djrapitops.plan.storage.database.transactions.commands.RemoveEverythingTransaction;
import com.djrapitops.plan.storage.database.transactions.events.*;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import utilities.RandomData;
import utilities.TestConstants;
import utilities.TestData;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

public interface JoinAddressQueriesTest extends DatabaseTestPreparer {

    @Test
    default void removeEverythingRemovesJoinAddresses() {
        String joinAddress = TestConstants.GET_PLAYER_HOSTNAME.get();
        executeTransactions(new StoreJoinAddressTransaction(joinAddress));

        List<String> expected = List.of(joinAddress, JoinAddressTable.DEFAULT_VALUE_FOR_LOOKUP);
        List<String> result = db().query(JoinAddressQueries.uniqueJoinAddresses());
        assertEquals(expected, result);

        executeTransactions(new RemoveEverythingTransaction());

        expected = List.of(JoinAddressTable.DEFAULT_VALUE_FOR_LOOKUP);
        result = db().query(JoinAddressQueries.uniqueJoinAddresses());
        assertEquals(expected, result);
    }

    @Test
    default void unknownJoinAddressIsStoredInDatabaseDuringCreation() {
        List<String> expected = List.of(JoinAddressTable.DEFAULT_VALUE_FOR_LOOKUP);
        List<String> result = db().query(JoinAddressQueries.allJoinAddresses());
        assertEquals(expected, result);
    }

    @Test
    default void joinAddressCanBeUnknown() {
        db().executeTransaction(new StoreWorldNameTransaction(serverUUID(), worlds[0]));
        db().executeTransaction(new StoreWorldNameTransaction(serverUUID(), worlds[1]));
        db().executeTransaction(new PlayerRegisterTransaction(playerUUID, System::currentTimeMillis, TestConstants.PLAYER_ONE_NAME));

        FinishedSession session = RandomData.randomSession(serverUUID(), worlds, playerUUID, player2UUID);
        session.getExtraData().remove(JoinAddress.class);
        db().executeTransaction(new StoreSessionTransaction(session));

        Set<Integer> expected = Set.of(db().query(BaseUserQueries.fetchUserId(playerUUID)).orElseThrow(AssertionError::new));
        Set<Integer> result = db().query(JoinAddressQueries.userIdsOfPlayersWithJoinAddresses(List.of(JoinAddressTable.DEFAULT_VALUE_FOR_LOOKUP)));

        assertEquals(expected, result);

        Map<String, Integer> expectedAddressCounts = Map.of(JoinAddressTable.DEFAULT_VALUE_FOR_LOOKUP, 1);
        Map<String, Integer> resultAddressCounts = db().query(JoinAddressQueries.latestJoinAddresses());

        assertEquals(expectedAddressCounts, resultAddressCounts);
    }

    @Test
    default void latestJoinAddressIsUpdatedUponSecondSession() {
        joinAddressCanBeUnknown();

        Long after = db().query(SessionQueries.lastSeen(playerUUID, serverUUID()));

        FinishedSession session = RandomData.randomSession(serverUUID(), worlds, after, playerUUID, player2UUID);

        String expectedAddress = TestConstants.GET_PLAYER_HOSTNAME.get();
        session.getExtraData().put(JoinAddress.class, new JoinAddress(expectedAddress));
        db().executeTransaction(new StoreSessionTransaction(session));

        Map<String, Integer> expected = Map.of(expectedAddress, 1);
        Map<String, Integer> result = db().query(JoinAddressQueries.latestJoinAddresses());

        assertEquals(expected, result);
    }

    @Test
    default void joinAddressPreservesCase() {
        joinAddressCanBeUnknown();
        config().set(DataGatheringSettings.PRESERVE_JOIN_ADDRESS_CASE, true);

        try {
            FinishedSession session = RandomData.randomSession(serverUUID(), worlds, playerUUID, player2UUID);
            String expectedAddress = "PLAY.UPPERCASE.COM";
            session.getExtraData().put(JoinAddress.class, new JoinAddress(expectedAddress));
            db().executeTransaction(new StoreSessionTransaction(session));

            Map<String, Integer> expected = Map.of(expectedAddress, 1);
            Map<String, Integer> result = db().query(JoinAddressQueries.latestJoinAddresses());

            assertEquals(expected, result);
        } finally {
            config().set(DataGatheringSettings.PRESERVE_JOIN_ADDRESS_CASE, false);
        }
    }

    @Test
    @DisplayName("Join address by day is filtered by addresses")
    default void joinAddressListIsFilteredByAddress() {
        db().executeTransaction(TestData.storeServers());

        db().executeTransaction(new StoreWorldNameTransaction(TestConstants.SERVER_TWO_UUID, worlds[0]));
        db().executeTransaction(new StoreWorldNameTransaction(TestConstants.SERVER_TWO_UUID, worlds[1]));
        FinishedSession session = RandomData.randomSession(TestConstants.SERVER_TWO_UUID, worlds, playerUUID, player2UUID);
        String expectedAddress = TestConstants.GET_PLAYER_HOSTNAME.get();
        session.getExtraData().put(JoinAddress.class, new JoinAddress(expectedAddress));
        db().executeTransaction(new StoreSessionTransaction(session));

        List<DateObj<Map<String, Integer>>> result = db().query(JoinAddressQueries.joinAddressesPerDay(0, 0, System.currentTimeMillis(), List.of("nonexistent.com")));
        assertEquals(List.of(), result);

        long startOfDay = session.getDate() - session.getDate() % TimeUnit.DAYS.toMillis(1);

        List<DateObj<Map<String, Integer>>> result2 = db().query(JoinAddressQueries.joinAddressesPerDay(0, 0, System.currentTimeMillis(), List.of(expectedAddress)));
        assertEquals(List.of(new DateObj<>(startOfDay, Map.of(expectedAddress, 1))), result2);

        List<DateObj<Map<String, Integer>>> result3 = db().query(JoinAddressQueries.joinAddressesPerDay(0, 0, System.currentTimeMillis(), List.of()));
        assertEquals(List.of(new DateObj<>(startOfDay, Map.of(expectedAddress, 1))), result3);
    }

    @Test
    @DisplayName("Server join address by day is filtered by addresses")
    default void serverJoinAddressListIsFilteredByAddress() {
        db().executeTransaction(TestData.storeServers());

        ServerUUID serverTwoUuid = TestConstants.SERVER_TWO_UUID;
        db().executeTransaction(new StoreWorldNameTransaction(serverTwoUuid, worlds[0]));
        db().executeTransaction(new StoreWorldNameTransaction(serverTwoUuid, worlds[1]));
        FinishedSession session = RandomData.randomSession(serverTwoUuid, worlds, playerUUID, player2UUID);
        String expectedAddress = TestConstants.GET_PLAYER_HOSTNAME.get();
        session.getExtraData().put(JoinAddress.class, new JoinAddress(expectedAddress));
        db().executeTransaction(new StoreSessionTransaction(session));

        List<DateObj<Map<String, Integer>>> result = db().query(JoinAddressQueries.joinAddressesPerDay(serverTwoUuid, 0, 0, System.currentTimeMillis(), List.of("nonexistent.com")));
        assertEquals(List.of(), result);

        long startOfDay = session.getDate() - session.getDate() % TimeUnit.DAYS.toMillis(1);

        List<DateObj<Map<String, Integer>>> result2 = db().query(JoinAddressQueries.joinAddressesPerDay(serverTwoUuid, 0, 0, System.currentTimeMillis(), List.of(expectedAddress)));
        assertEquals(List.of(new DateObj<>(startOfDay, Map.of(expectedAddress, 1))), result2);

        List<DateObj<Map<String, Integer>>> result3 = db().query(JoinAddressQueries.joinAddressesPerDay(serverTwoUuid, 0, 0, System.currentTimeMillis(), List.of()));
        assertEquals(List.of(new DateObj<>(startOfDay, Map.of(expectedAddress, 1))), result3);
    }

    @Test
    default void joinAddressIsTruncated() {
        db().executeTransaction(new StoreWorldNameTransaction(serverUUID(), worlds[0]));
        db().executeTransaction(new StoreWorldNameTransaction(serverUUID(), worlds[1]));
        db().executeTransaction(new PlayerRegisterTransaction(playerUUID, System::currentTimeMillis, TestConstants.PLAYER_ONE_NAME));

        FinishedSession session = RandomData.randomSession(serverUUID(), worlds, playerUUID, player2UUID);
        String joinAddress = RandomData.randomString(JoinAddressTable.JOIN_ADDRESS_MAX_LENGTH + RandomData.randomInt(0, 100));
        session.getExtraData().put(JoinAddress.class, new JoinAddress(joinAddress));
        db().executeTransaction(new StoreSessionTransaction(session));

        String expectedJoinAddress = StringUtils.truncate(joinAddress, JoinAddressTable.JOIN_ADDRESS_MAX_LENGTH);

        Set<Integer> expected = Set.of(db().query(BaseUserQueries.fetchUserId(playerUUID)).orElseThrow(AssertionError::new));
        Set<Integer> result = db().query(JoinAddressQueries.userIdsOfPlayersWithJoinAddresses(List.of(expectedJoinAddress)));

        assertEquals(expected, result);

        Map<String, Integer> expectedAddressCounts = Map.of(expectedJoinAddress, 1);
        Map<String, Integer> resultAddressCounts = db().query(JoinAddressQueries.latestJoinAddresses());

        assertEquals(expectedAddressCounts, resultAddressCounts);
    }

    @Test
    default void joinAddressIsTruncatedWhenStoringSessionsAfterRestart() {
        db().executeTransaction(new StoreWorldNameTransaction(serverUUID(), worlds[0]));
        db().executeTransaction(new StoreWorldNameTransaction(serverUUID(), worlds[1]));
        db().executeTransaction(new PlayerRegisterTransaction(playerUUID, System::currentTimeMillis, TestConstants.PLAYER_ONE_NAME));

        FinishedSession session = RandomData.randomSession(serverUUID(), worlds, playerUUID, player2UUID);
        String joinAddress = RandomData.randomString(JoinAddressTable.JOIN_ADDRESS_MAX_LENGTH + RandomData.randomInt(0, 100));
        session.getExtraData().put(JoinAddress.class, new JoinAddress(joinAddress));
        db().executeTransaction(new ShutdownDataPreservationTransaction(List.of(session)));

        String expectedJoinAddress = StringUtils.truncate(joinAddress, JoinAddressTable.JOIN_ADDRESS_MAX_LENGTH);

        Set<Integer> expected = Set.of(db().query(BaseUserQueries.fetchUserId(playerUUID)).orElseThrow(AssertionError::new));
        Set<Integer> result = db().query(JoinAddressQueries.userIdsOfPlayersWithJoinAddresses(List.of(expectedJoinAddress)));

        assertEquals(expected, result);

        Map<String, Integer> expectedAddressCounts = Map.of(expectedJoinAddress, 1);
        Map<String, Integer> resultAddressCounts = db().query(JoinAddressQueries.latestJoinAddresses());

        assertEquals(expectedAddressCounts, resultAddressCounts);
    }

    @Test
    default void joinAddressUpdateIsUniquePerServer() {
        joinAddressCanBeUnknown();

        db().executeTransaction(TestData.storeServers());

        db().executeTransaction(new StoreWorldNameTransaction(TestConstants.SERVER_TWO_UUID, worlds[0]));
        db().executeTransaction(new StoreWorldNameTransaction(TestConstants.SERVER_TWO_UUID, worlds[1]));
        FinishedSession session = RandomData.randomSession(TestConstants.SERVER_TWO_UUID, worlds, playerUUID, player2UUID);
        String expectedAddress = TestConstants.GET_PLAYER_HOSTNAME.get();
        session.getExtraData().put(JoinAddress.class, new JoinAddress(expectedAddress));
        db().executeTransaction(new StoreSessionTransaction(session));

        Map<String, Integer> expected1 = Map.of(JoinAddressTable.DEFAULT_VALUE_FOR_LOOKUP, 1);
        Map<String, Integer> result1 = db().query(JoinAddressQueries.latestJoinAddresses(serverUUID()));

        assertEquals(expected1, result1);

        Map<String, Integer> expected2 = Map.of(expectedAddress, 1);
        Map<String, Integer> result2 = db().query(JoinAddressQueries.latestJoinAddresses(TestConstants.SERVER_TWO_UUID));

        assertEquals(expected2, result2);
    }

    @Test
    default void joinAddressQueryHasNoNullValues() {
        joinAddressCanBeUnknown();

        Map<String, Integer> expected = Collections.singletonMap(JoinAddressTable.DEFAULT_VALUE_FOR_LOOKUP, 1);
        Map<String, Integer> result = db().query(JoinAddressQueries.latestJoinAddresses());
        assertEquals(expected, result);
    }

    @Test
    default void joinAddressQueryHasDistinctPlayers() {
        joinAddressCanBeUnknown();

        db().executeTransaction(TestData.storeServers());

        FinishedSession session = RandomData.randomSession(serverUUID(), worlds, player2UUID, playerUUID);
        String expectedAddress = TestConstants.GET_PLAYER_HOSTNAME.get();
        session.getExtraData().put(JoinAddress.class, new JoinAddress(expectedAddress));
        db().executeTransaction(new StoreSessionTransaction(session));

        Map<String, Integer> expected = Map.of(JoinAddressTable.DEFAULT_VALUE_FOR_LOOKUP, 1, expectedAddress, 1);
        Map<String, Integer> result = db().query(JoinAddressQueries.latestJoinAddresses());

        assertEquals(expected, result);
    }

    @Test
    default void joinAddressFilterOptionsAreFetched() {
        executeTransactions(
                new StoreJoinAddressTransaction(TestConstants.GET_PLAYER_HOSTNAME.get())
        );

        List<String> expected = List.of(TestConstants.GET_PLAYER_HOSTNAME.get().toLowerCase(), JoinAddressTable.DEFAULT_VALUE_FOR_LOOKUP);
        List<String> result = db().query(JoinAddressQueries.uniqueJoinAddresses());
        assertEquals(expected, result);
    }

    @Test
    default void joinAddressFilterOptionsAreFetchedWhenThereAreMultiple() {
        executeTransactions(
                new StoreJoinAddressTransaction(TestConstants.GET_PLAYER_HOSTNAME.get()),
                new StoreJoinAddressTransaction(TestConstants.GET_PLAYER_HOSTNAME.get() + "_a"),
                new StoreJoinAddressTransaction(TestConstants.GET_PLAYER_HOSTNAME.get() + "_b")
        );

        List<String> expected = Arrays.asList(
                TestConstants.GET_PLAYER_HOSTNAME.get().toLowerCase(),
                TestConstants.GET_PLAYER_HOSTNAME.get().toLowerCase() + "_a",
                TestConstants.GET_PLAYER_HOSTNAME.get().toLowerCase() + "_b",
                JoinAddressTable.DEFAULT_VALUE_FOR_LOOKUP
        );
        List<String> result = db().query(JoinAddressQueries.uniqueJoinAddresses());

        assertEquals(expected, result);
    }

    @Test
    default void joinAddressFilterUUIDsAreFetched() {
        latestJoinAddressIsUpdatedUponSecondSession();

        Set<Integer> expected = Set.of(db().query(BaseUserQueries.fetchUserId(playerUUID)).orElseThrow(AssertionError::new));
        Set<Integer> result = db().query(JoinAddressQueries.userIdsOfPlayersWithJoinAddresses(
                List.of(TestConstants.GET_PLAYER_HOSTNAME.get().toLowerCase()))
        );
        assertEquals(expected, result);
    }

    @Test
    default void joinAddressFilterUUIDsAreFetchedWhenUnknown() {
        joinAddressCanBeUnknown();

        Set<Integer> expected = Set.of(db().query(BaseUserQueries.fetchUserId(playerUUID)).orElseThrow(AssertionError::new));
        Set<Integer> result = db().query(JoinAddressQueries.userIdsOfPlayersWithJoinAddresses(
                List.of(JoinAddressTable.DEFAULT_VALUE_FOR_LOOKUP))
        );
        assertEquals(expected, result);
    }

    @Test
    default void playerSpecificJoinAddressCanBeFetched() {
        latestJoinAddressIsUpdatedUponSecondSession();

        Map<UUID, String> expected = Map.of(playerUUID, TestConstants.GET_PLAYER_HOSTNAME.get());
        Map<UUID, String> result = db().query(JoinAddressQueries.latestJoinAddressesOfPlayers());
        assertEquals(expected, result);
    }

    @Test
    default void playerSpecificJoinAddressCanBeFetchedForServer() {
        joinAddressUpdateIsUniquePerServer();

        Map<UUID, String> expected = Map.of(playerUUID, TestConstants.GET_PLAYER_HOSTNAME.get());
        Map<UUID, String> result = db().query(JoinAddressQueries.latestJoinAddressesOfPlayers(TestConstants.SERVER_TWO_UUID));
        assertEquals(expected, result);
    }
}
