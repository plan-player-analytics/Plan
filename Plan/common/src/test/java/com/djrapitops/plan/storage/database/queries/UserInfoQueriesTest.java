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

import com.djrapitops.plan.gathering.domain.BaseUser;
import com.djrapitops.plan.gathering.domain.UserInfo;
import com.djrapitops.plan.identification.Server;
import com.djrapitops.plan.storage.database.DatabaseTestPreparer;
import com.djrapitops.plan.storage.database.queries.objects.BaseUserQueries;
import com.djrapitops.plan.storage.database.queries.objects.UserIdentifierQueries;
import com.djrapitops.plan.storage.database.queries.objects.UserInfoQueries;
import com.djrapitops.plan.storage.database.transactions.StoreServerInformationTransaction;
import com.djrapitops.plan.storage.database.transactions.Transaction;
import com.djrapitops.plan.storage.database.transactions.commands.RemoveEverythingTransaction;
import com.djrapitops.plan.storage.database.transactions.events.*;
import com.djrapitops.plan.storage.database.transactions.init.RemoveDuplicateUserInfoTransaction;
import org.junit.jupiter.api.Test;
import utilities.OptionalAssert;
import utilities.RandomData;
import utilities.TestConstants;

import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

public interface UserInfoQueriesTest extends DatabaseTestPreparer {

    @Test
    default void userInfoTableStoresCorrectUserInformation() {
        assertFalse(db().query(BaseUserQueries.fetchBaseUserOfPlayer(playerUUID)).isPresent());
        db().executeTransaction(new PlayerServerRegisterTransaction(playerUUID, () -> TestConstants.REGISTER_TIME, TestConstants.PLAYER_ONE_NAME, serverUUID(), TestConstants.GET_PLAYER_HOSTNAME));

        Set<UserInfo> userInfo = db().query(UserInfoQueries.fetchUserInformationOfUser(playerUUID));
        Set<UserInfo> expected = Collections.singleton(new UserInfo(playerUUID, serverUUID(), TestConstants.REGISTER_TIME, false, TestConstants.GET_PLAYER_HOSTNAME.get(), false));

        assertEquals(expected, userInfo);
    }

    @Test
    default void joinAddressCanBeNull() {
        assertFalse(db().query(BaseUserQueries.fetchBaseUserOfPlayer(playerUUID)).isPresent());
        db().executeTransaction(new PlayerServerRegisterTransaction(playerUUID, () -> TestConstants.REGISTER_TIME, TestConstants.PLAYER_ONE_NAME, serverUUID(), () -> null));

        Set<UserInfo> userInfo = db().query(UserInfoQueries.fetchUserInformationOfUser(playerUUID));
        Set<UserInfo> expected = Collections.singleton(new UserInfo(playerUUID, serverUUID(), TestConstants.REGISTER_TIME, false, null, false));

        assertEquals(expected, userInfo);
    }

    @Test
    default void joinAddressIsUpdatedUponSecondLogin() {
        assertFalse(db().query(BaseUserQueries.fetchBaseUserOfPlayer(playerUUID)).isPresent());
        db().executeTransaction(new PlayerServerRegisterTransaction(playerUUID, () -> TestConstants.REGISTER_TIME, TestConstants.PLAYER_ONE_NAME, serverUUID(), () -> null));
        db().executeTransaction(new PlayerServerRegisterTransaction(playerUUID, () -> TestConstants.REGISTER_TIME, TestConstants.PLAYER_ONE_NAME, serverUUID(), TestConstants.GET_PLAYER_HOSTNAME));

        Set<UserInfo> userInfo = db().query(UserInfoQueries.fetchUserInformationOfUser(playerUUID));
        Set<UserInfo> expected = Collections.singleton(new UserInfo(playerUUID, serverUUID(), TestConstants.REGISTER_TIME, false, TestConstants.GET_PLAYER_HOSTNAME.get(), false));

        assertEquals(expected, userInfo);
    }

    @Test
    default void joinAddressUpdateIsUniquePerServer() {
        db().executeTransaction(new PlayerServerRegisterTransaction(playerUUID, () -> TestConstants.REGISTER_TIME, TestConstants.PLAYER_ONE_NAME, serverUUID(), () -> null));
        db().executeTransaction(new PlayerServerRegisterTransaction(playerUUID, () -> TestConstants.REGISTER_TIME, TestConstants.PLAYER_ONE_NAME, serverUUID(), TestConstants.GET_PLAYER_HOSTNAME));

        db().executeTransaction(new StoreServerInformationTransaction(new Server(TestConstants.SERVER_TWO_UUID, TestConstants.SERVER_TWO_NAME, "", TestConstants.VERSION)));
        db().executeTransaction(new PlayerServerRegisterTransaction(playerUUID, () -> TestConstants.REGISTER_TIME, TestConstants.PLAYER_ONE_NAME, TestConstants.SERVER_TWO_UUID, () -> "example.join.address"));

        Set<UserInfo> userInfo = db().query(UserInfoQueries.fetchUserInformationOfUser(playerUUID));
        Set<UserInfo> expected = new HashSet<>(Arrays.asList(
                new UserInfo(playerUUID, serverUUID(), TestConstants.REGISTER_TIME, false, TestConstants.GET_PLAYER_HOSTNAME.get(), false),
                new UserInfo(playerUUID, TestConstants.SERVER_TWO_UUID, TestConstants.REGISTER_TIME, false, "example.join.address", false)
        ));

        assertEquals(expected, userInfo);
    }

    @Test
    default void userInfoTableUpdatesBanStatus() {
        db().executeTransaction(new PlayerServerRegisterTransaction(playerUUID, () -> TestConstants.REGISTER_TIME,
                TestConstants.PLAYER_ONE_NAME, serverUUID(), TestConstants.GET_PLAYER_HOSTNAME));

        db().executeTransaction(new BanStatusTransaction(playerUUID, serverUUID(), () -> true));

        Set<UserInfo> userInfo = db().query(UserInfoQueries.fetchUserInformationOfUser(playerUUID));
        Set<UserInfo> expected = Collections.singleton(new UserInfo(playerUUID, serverUUID(), TestConstants.REGISTER_TIME, false, TestConstants.GET_PLAYER_HOSTNAME.get(), true));

        assertEquals(expected, userInfo);
    }

    @Test
    default void userInfoTableUpdatesOperatorStatus() {
        db().executeTransaction(new PlayerServerRegisterTransaction(playerUUID, () -> TestConstants.REGISTER_TIME,
                TestConstants.PLAYER_ONE_NAME, serverUUID(), TestConstants.GET_PLAYER_HOSTNAME));

        db().executeTransaction(new OperatorStatusTransaction(playerUUID, serverUUID(), true));

        Set<UserInfo> userInfo = db().query(UserInfoQueries.fetchUserInformationOfUser(playerUUID));
        Set<UserInfo> expected = Collections.singleton(new UserInfo(playerUUID, serverUUID(), TestConstants.REGISTER_TIME, true, TestConstants.GET_PLAYER_HOSTNAME.get(), false));

        assertEquals(expected, userInfo);
    }

    @Test
    default void playerNameIsUpdatedWhenPlayerLogsIn() {
        db().executeTransaction(new PlayerServerRegisterTransaction(playerUUID, () -> TestConstants.REGISTER_TIME,
                TestConstants.PLAYER_ONE_NAME, serverUUID(), TestConstants.GET_PLAYER_HOSTNAME));

        OptionalAssert.equals(playerUUID, db().query(UserIdentifierQueries.fetchPlayerUUIDOf(TestConstants.PLAYER_ONE_NAME)));

        // Updates the name
        db().executeTransaction(new PlayerRegisterTransaction(playerUUID, RandomData::randomTime, "NewName"));
        forcePersistenceCheck();

        assertFalse(db().query(UserIdentifierQueries.fetchPlayerUUIDOf(TestConstants.PLAYER_ONE_NAME)).isPresent());

        OptionalAssert.equals(playerUUID, db().query(UserIdentifierQueries.fetchPlayerUUIDOf("NewName")));
    }

    @Test
    default void kicksAreAddedTogether() {
        db().executeTransaction(new PlayerRegisterTransaction(playerUUID, () -> TestConstants.REGISTER_TIME, TestConstants.PLAYER_ONE_NAME));
        OptionalAssert.equals(0, db().query(BaseUserQueries.fetchBaseUserOfPlayer(playerUUID)).map(BaseUser::getTimesKicked));

        int random = new Random().nextInt(20);

        for (int i = 0; i < random; i++) {
            db().executeTransaction(new KickStoreTransaction(playerUUID));
        }
        forcePersistenceCheck();
        OptionalAssert.equals(random, db().query(BaseUserQueries.fetchBaseUserOfPlayer(playerUUID)).map(BaseUser::getTimesKicked));
    }

    @Test
    default void matchingByNameFindsNamesCaseInsensitive() {
        String exp1 = "TestName";
        String exp2 = "TestName2";

        UUID uuid1 = UUID.randomUUID();
        db().executeTransaction(new PlayerRegisterTransaction(uuid1, () -> 0L, exp1));
        db().executeTransaction(new PlayerRegisterTransaction(UUID.randomUUID(), () -> 0L, exp2));

        String searchFor = "testname";

        List<String> result = db().query(UserIdentifierQueries.fetchMatchingPlayerNames(searchFor));

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(exp1, result.get(0));
        assertEquals(exp2, result.get(1));
    }

    @Test
    default void playerIsRegisteredToUsersTable() {
        assertFalse(db().query(PlayerFetchQueries.isPlayerRegistered(playerUUID)));
        db().executeTransaction(new PlayerRegisterTransaction(playerUUID, RandomData::randomTime, TestConstants.PLAYER_ONE_NAME));
        assertTrue(db().query(PlayerFetchQueries.isPlayerRegistered(playerUUID)));
        assertFalse(db().query(PlayerFetchQueries.isPlayerRegisteredOnServer(playerUUID, serverUUID())));
    }

    @Test
    default void playerIsRegisteredToBothTables() {
        assertFalse(db().query(PlayerFetchQueries.isPlayerRegistered(playerUUID)));
        assertFalse(db().query(PlayerFetchQueries.isPlayerRegisteredOnServer(playerUUID, serverUUID())));
        db().executeTransaction(new PlayerServerRegisterTransaction(playerUUID, () -> TestConstants.REGISTER_TIME,
                TestConstants.PLAYER_ONE_NAME, serverUUID(), TestConstants.GET_PLAYER_HOSTNAME));
        assertTrue(db().query(PlayerFetchQueries.isPlayerRegistered(playerUUID)));
        assertTrue(db().query(PlayerFetchQueries.isPlayerRegisteredOnServer(playerUUID, serverUUID())));
    }

    @Test
    default void removeEverythingRemovesBaseUsers() {
        playerIsRegisteredToUsersTable();
        db().executeTransaction(new RemoveEverythingTransaction());
        assertTrue(db().query(BaseUserQueries.fetchAllBaseUsers()).isEmpty());
    }

    @Test
    default void removeEverythingRemovesUserInfo() {
        playerIsRegisteredToBothTables();
        db().executeTransaction(new RemoveEverythingTransaction());
        assertTrue(db().query(UserInfoQueries.fetchAllUserInformation()).isEmpty());
    }


    @Test
    default void cleanRemovesOnlyDuplicatedUserInfo() throws ExecutionException, InterruptedException {
        db().executeTransaction(new PlayerRegisterTransaction(playerUUID, System::currentTimeMillis, TestConstants.PLAYER_ONE_NAME));
        db().executeTransaction(new PlayerRegisterTransaction(player2UUID, System::currentTimeMillis, TestConstants.PLAYER_TWO_NAME));

        // Store one duplicate
        db().executeTransaction(new Transaction() {
            @Override
            protected void performOperations() {
                execute(DataStoreQueries.registerUserInfo(playerUUID, 0L,
                        serverUUID(), TestConstants.GET_PLAYER_HOSTNAME.get()));
                execute(DataStoreQueries.registerUserInfo(playerUUID, 0L,
                        serverUUID(), TestConstants.GET_PLAYER_HOSTNAME.get()));
                execute(DataStoreQueries.registerUserInfo(player2UUID, 0L,
                        serverUUID(), TestConstants.GET_PLAYER_HOSTNAME.get()));
            }
        }).get();

        db().executeTransaction(new RemoveDuplicateUserInfoTransaction());

        Set<UserInfo> found = db().query(UserInfoQueries.fetchUserInformationOfUser(playerUUID));
        assertEquals(
                Collections.singleton(new UserInfo(playerUUID, serverUUID(), 0, false, TestConstants.GET_PLAYER_HOSTNAME.get(), false)),
                found
        );

        Set<UserInfo> found2 = db().query(UserInfoQueries.fetchUserInformationOfUser(player2UUID));
        assertEquals(
                Collections.singleton(new UserInfo(player2UUID, serverUUID(), 0, false, TestConstants.GET_PLAYER_HOSTNAME.get(), false)),
                found2
        );
    }

    @Test
    default void registeredUUIDsWithinDateAreFetched() throws ExecutionException, InterruptedException {
        db().executeTransaction(new Transaction() {
            @Override
            protected void performOperations() {
                execute(DataStoreQueries.registerBaseUser(playerUUID, 0L, TestConstants.PLAYER_ONE_NAME));
                execute(DataStoreQueries.registerBaseUser(player2UUID, 5000L, TestConstants.PLAYER_TWO_NAME));
                execute(DataStoreQueries.registerBaseUser(player3UUID, 10000L, TestConstants.PLAYER_THREE_NAME));
            }
        }).get();

        Set<Integer> expected = Set.of(db().query(BaseUserQueries.fetchUserId(player2UUID)));
        Set<Integer> result = db().query(BaseUserQueries.userIdsOfRegisteredBetween(2500L, 7500L));
        assertEquals(expected, result);
    }

    @Test
    default void minimumRegisterDateIsFetched() throws ExecutionException, InterruptedException {
        db().executeTransaction(new Transaction() {
            @Override
            protected void performOperations() {
                execute(DataStoreQueries.registerBaseUser(playerUUID, 0L, TestConstants.PLAYER_ONE_NAME));
                execute(DataStoreQueries.registerBaseUser(player2UUID, 5000L, TestConstants.PLAYER_TWO_NAME));
                execute(DataStoreQueries.registerBaseUser(player3UUID, 10000L, TestConstants.PLAYER_THREE_NAME));
            }
        }).get();

        long expected = 0L;
        assertEquals(expected, db().query(BaseUserQueries.minimumRegisterDate()).orElseThrow(AssertionError::new));
    }

    @Test
    default void noMinimumRegisterDateIsFetchedWithNoData() {
        assertFalse(db().query(BaseUserQueries.minimumRegisterDate()).isPresent());
    }

    @Test
    default void joinAddressQueryHasNoNullValues() {
        joinAddressCanBeNull();

        Map<String, Integer> expected = Collections.singletonMap("unknown", 1);
        Map<String, Integer> result = db().query(UserInfoQueries.joinAddresses());
        assertEquals(expected, result);
    }

    @Test
    default void serverJoinAddressQueryHasNoNullValues() {
        joinAddressCanBeNull();

        Map<String, Integer> expected = Collections.singletonMap("unknown", 1);
        Map<String, Integer> result = db().query(UserInfoQueries.joinAddresses(serverUUID()));
        assertEquals(expected, result);
    }

    @Test
    default void joinAddressQueryHasDistinctPlayers() {
        db().executeTransaction(new StoreServerInformationTransaction(new Server(TestConstants.SERVER_TWO_UUID, TestConstants.SERVER_TWO_NAME, "", TestConstants.VERSION)));
        db().executeTransaction(new PlayerServerRegisterTransaction(playerUUID, () -> TestConstants.REGISTER_TIME, TestConstants.PLAYER_ONE_NAME, serverUUID(), TestConstants.GET_PLAYER_HOSTNAME));
        db().executeTransaction(new PlayerServerRegisterTransaction(playerUUID, () -> TestConstants.REGISTER_TIME, TestConstants.PLAYER_ONE_NAME, TestConstants.SERVER_TWO_UUID, TestConstants.GET_PLAYER_HOSTNAME));

        Map<String, Integer> expected = Collections.singletonMap(TestConstants.GET_PLAYER_HOSTNAME.get(), 1);
        Map<String, Integer> result = db().query(UserInfoQueries.joinAddresses());
        assertEquals(expected, result);
    }

    @Test
    default void joinAddressFilterOptionsAreFetched() {
        joinAddressIsUpdatedUponSecondLogin();

        List<String> expected = Collections.singletonList(TestConstants.GET_PLAYER_HOSTNAME.get().toLowerCase());
        List<String> result = db().query(UserInfoQueries.uniqueJoinAddresses());
        assertEquals(expected, result);
    }

    @Test
    default void joinAddressFilterOptionsAreFetchedWhenThereAreMultiple() {
        joinAddressIsUpdatedUponSecondLogin();
        db().executeTransaction(new StoreServerInformationTransaction(new Server(TestConstants.SERVER_TWO_UUID, TestConstants.SERVER_TWO_NAME, "", TestConstants.VERSION)));

        db().executeTransaction(new PlayerServerRegisterTransaction(playerUUID, () -> TestConstants.REGISTER_TIME, TestConstants.PLAYER_ONE_NAME, serverUUID(), () -> TestConstants.GET_PLAYER_HOSTNAME.get() + "_b"));
        db().executeTransaction(new PlayerServerRegisterTransaction(player2UUID, () -> TestConstants.REGISTER_TIME, TestConstants.PLAYER_ONE_NAME, TestConstants.SERVER_TWO_UUID, () -> TestConstants.GET_PLAYER_HOSTNAME.get() + "_a"));

        List<String> expected = Arrays.asList(
                TestConstants.GET_PLAYER_HOSTNAME.get().toLowerCase() + "_a",
                TestConstants.GET_PLAYER_HOSTNAME.get().toLowerCase() + "_b"
        );
        List<String> result = db().query(UserInfoQueries.uniqueJoinAddresses());

        assertEquals(expected, result);
    }

    @Test
    default void joinAddressFilterUUIDsAreFetched() {
        joinAddressIsUpdatedUponSecondLogin();

        Set<Integer> expected = Set.of(db().query(BaseUserQueries.fetchUserId(playerUUID)));
        Set<Integer> result = db().query(UserInfoQueries.userIdsOfPlayersWithJoinAddresses(
                Collections.singletonList(TestConstants.GET_PLAYER_HOSTNAME.get().toLowerCase()))
        );
        assertEquals(expected, result);
    }

    @Test
    default void joinAddressFilterUUIDsAreFetchedWhenUnknown() {
        joinAddressCanBeNull();

        Set<Integer> expected = Set.of(db().query(BaseUserQueries.fetchUserId(playerUUID)));
        Set<Integer> result = db().query(UserInfoQueries.userIdsOfPlayersWithJoinAddresses(
                Collections.singletonList("unknown"))
        );
        assertEquals(expected, result);
    }
}
