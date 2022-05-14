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
import com.djrapitops.plan.gathering.domain.BaseUser;
import com.djrapitops.plan.gathering.domain.GeoInfo;
import com.djrapitops.plan.gathering.domain.Ping;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.DatabaseTestPreparer;
import com.djrapitops.plan.storage.database.queries.objects.BaseUserQueries;
import com.djrapitops.plan.storage.database.queries.objects.GeoInfoQueries;
import com.djrapitops.plan.storage.database.queries.objects.PingQueries;
import com.djrapitops.plan.storage.database.transactions.commands.RemoveEverythingTransaction;
import com.djrapitops.plan.storage.database.transactions.events.GeoInfoStoreTransaction;
import com.djrapitops.plan.storage.database.transactions.events.PingStoreTransaction;
import com.djrapitops.plan.storage.database.transactions.events.PlayerRegisterTransaction;
import com.djrapitops.plan.storage.database.transactions.events.PlayerServerRegisterTransaction;
import org.junit.jupiter.api.Test;
import utilities.RandomData;
import utilities.TestConstants;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public interface GeolocationQueriesTest extends DatabaseTestPreparer {

    @Test
    default void geoInfoStoreTransactionOutOfOrderDoesNotFailDueToMissingUser() {
        List<GeoInfo> expected = RandomData.randomGeoInfo();
        for (GeoInfo geoInfo : expected) {
            save(playerUUID, geoInfo);
        }

        List<GeoInfo> result = db().query(GeoInfoQueries.fetchAllGeoInformation()).get(playerUUID);
        assertEquals(expected, result);
    }

    @Test
    default void geoInfoStoreTransactionOutOfOrderUpdatesUserInformation() {
        List<GeoInfo> geoInfos = RandomData.randomGeoInfo();
        for (GeoInfo geoInfo : geoInfos) {
            save(playerUUID, geoInfo);
        }

        long registerDate = RandomData.randomTime();
        db().executeTransaction(new PlayerRegisterTransaction(playerUUID, () -> registerDate, TestConstants.PLAYER_ONE_NAME));

        Optional<BaseUser> expected = Optional.of(new BaseUser(playerUUID, TestConstants.PLAYER_ONE_NAME, registerDate, 0));
        Optional<BaseUser> result = db().query(BaseUserQueries.fetchBaseUserOfPlayer(playerUUID));
        assertEquals(expected, result);
    }

    @Test
    default void geoInformationIsStored() {
        db().executeTransaction(new PlayerServerRegisterTransaction(playerUUID, RandomData::randomTime,
                TestConstants.PLAYER_ONE_NAME, serverUUID(), TestConstants.GET_PLAYER_HOSTNAME));

        List<GeoInfo> expected = RandomData.randomGeoInfo();
        for (GeoInfo geoInfo : expected) {
            save(playerUUID, geoInfo);
        }

        forcePersistenceCheck();

        List<GeoInfo> result = db().query(GeoInfoQueries.fetchAllGeoInformation()).get(playerUUID);
        assertEquals(expected, result);
    }

    default void save(UUID uuid, GeoInfo info) {
        db().executeTransaction(new GeoInfoStoreTransaction(uuid, info));
    }

    @Test
    default void serverGeolocationsAreCountedAppropriately() {
        storeSpecificGeolocations();

        Map<String, Integer> got = db().query(GeoInfoQueries.serverGeolocationCounts(serverUUID()));

        Map<String, Integer> expected = new HashMap<>();
        // first user has a more recent connection from Finland so their country should be counted as Finland.
        expected.put("Finland", 1);
        expected.put("Sweden", 1);
        expected.put("Not Known", 1);
        expected.put("Local Machine", 1);
        expected.put("Denmark", 2);

        assertEquals(expected, got);
    }

    @Test
    default void networkGeolocationsAreCountedAppropriately() {
        storeSpecificGeolocations();

        Map<String, Integer> got = db().query(GeoInfoQueries.networkGeolocationCounts());

        Map<String, Integer> expected = new HashMap<>();
        // first user has a more recent connection from Finland so their country should be counted as Finland.
        expected.put("Finland", 1);
        expected.put("Sweden", 1);
        expected.put("Not Known", 1);
        expected.put("Local Machine", 1);
        expected.put("Denmark", 2);

        assertEquals(expected, got);
    }

    default UUID[] storeSpecificGeolocations() {
        UUID firstUuid = UUID.randomUUID();
        UUID secondUuid = UUID.randomUUID();
        UUID thirdUuid = UUID.randomUUID();
        UUID fourthUuid = UUID.randomUUID();
        UUID fifthUuid = UUID.randomUUID();
        UUID sixthUuid = UUID.randomUUID();
        UUID[] uuids = {firstUuid, secondUuid, thirdUuid, fourthUuid, fifthUuid, sixthUuid};

        Database db = db();
        for (UUID uuid : uuids) {
            db.executeTransaction(new PlayerServerRegisterTransaction(uuid, () -> 0L, "", serverUUID(),
                    TestConstants.GET_PLAYER_HOSTNAME));
        }

        save(firstUuid, new GeoInfo("Norway", 0));
        save(firstUuid, new GeoInfo("Finland", 5));
        save(secondUuid, new GeoInfo("Sweden", 0));
        save(thirdUuid, new GeoInfo("Denmark", 0));
        save(fourthUuid, new GeoInfo("Denmark", 0));
        save(fifthUuid, new GeoInfo("Not Known", 0));
        save(sixthUuid, new GeoInfo("Local Machine", 0));
        return uuids;
    }

    @Test
    default void pingIsGroupedByGeolocationAppropriately() {
        UUID[] uuids = storeSpecificGeolocations();

        Database db = db();

        long time = System.currentTimeMillis();
        List<DateObj<Integer>> ping = Collections.singletonList(new DateObj<>(time, 5));
        for (UUID uuid : uuids) {
            db.executeTransaction(new PingStoreTransaction(uuid, serverUUID(), ping));
        }

        Map<String, Ping> got = db.query(PingQueries.fetchPingDataOfServerByGeolocation(serverUUID()));

        Map<String, Ping> expected = new HashMap<>();
        // first user has a more recent connection from Finland so their country should be counted as Finland.
        Ping expectedPing = new Ping(time, serverUUID(), 5, 5, 5);
        expected.put("Finland", expectedPing);
        expected.put("Sweden", expectedPing);
        expected.put("Not Known", expectedPing);
        expected.put("Local Machine", expectedPing);
        expected.put("Denmark", expectedPing);

        assertEquals(expected, got);
    }

    @Test
    default void removeEverythingRemovesGeolocations() {
        geoInformationIsStored();
        db().executeTransaction(new RemoveEverythingTransaction());
        assertTrue(db().query(GeoInfoQueries.fetchAllGeoInformation()).isEmpty());
    }

    @Test
    default void filterOptionGeolocationsAreUnique() {
        db().executeTransaction(new PlayerServerRegisterTransaction(playerUUID, RandomData::randomTime,
                TestConstants.PLAYER_ONE_NAME, serverUUID(), TestConstants.GET_PLAYER_HOSTNAME));

        List<GeoInfo> savedData = RandomData.randomGeoInfo();
        for (GeoInfo geoInfo : savedData) {
            save(playerUUID, geoInfo);
        }

        Set<String> expected = savedData.stream().map(GeoInfo::getGeolocation)
                .collect(Collectors.toSet());
        Set<String> result = new HashSet<>(db().query(GeoInfoQueries.uniqueGeolocations()));
        assertEquals(expected, result);
    }

    @Test
    default void geolocationFilterResultsGetThePlayer() {
        db().executeTransaction(new PlayerServerRegisterTransaction(playerUUID, RandomData::randomTime,
                TestConstants.PLAYER_ONE_NAME, serverUUID(), TestConstants.GET_PLAYER_HOSTNAME));

        List<GeoInfo> savedData = RandomData.randomGeoInfo();
        for (GeoInfo geoInfo : savedData) {
            save(playerUUID, geoInfo);
        }

        Set<Integer> expected = Set.of(db().query(BaseUserQueries.fetchUserId(playerUUID)));
        Set<Integer> result = db().query(GeoInfoQueries.userIdsOfPlayersWithGeolocations(
                Collections.singletonList(savedData.get(0).getGeolocation()))
        );
        assertEquals(expected, result);
    }
}