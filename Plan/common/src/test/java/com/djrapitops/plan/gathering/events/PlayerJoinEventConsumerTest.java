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
package com.djrapitops.plan.gathering.events;

import com.djrapitops.plan.PlanSystem;
import com.djrapitops.plan.delivery.domain.Nickname;
import com.djrapitops.plan.gathering.cache.SessionCache;
import com.djrapitops.plan.gathering.domain.ActiveSession;
import com.djrapitops.plan.gathering.domain.BaseUser;
import com.djrapitops.plan.gathering.domain.PlatformPlayerData;
import com.djrapitops.plan.gathering.domain.UserInfo;
import com.djrapitops.plan.gathering.domain.event.PlayerJoin;
import com.djrapitops.plan.gathering.geolocation.GeolocationCache;
import com.djrapitops.plan.identification.Server;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.DataGatheringSettings;
import com.djrapitops.plan.settings.config.paths.ExportSettings;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.objects.*;
import com.djrapitops.plan.storage.database.sql.tables.JoinAddressTable;
import com.djrapitops.plan.storage.database.sql.tables.WorldTable;
import com.djrapitops.plan.storage.database.transactions.StoreServerInformationTransaction;
import com.djrapitops.plan.storage.database.transactions.commands.RemoveEverythingTransaction;
import extension.FullSystemExtension;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import utilities.TestConstants;
import utilities.dagger.PlanPluginComponent;
import utilities.mocks.objects.TestPlayerData;

import java.io.File;
import java.net.InetAddress;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.djrapitops.plan.storage.database.sql.building.Sql.FROM;
import static com.djrapitops.plan.storage.database.sql.building.Sql.SELECT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(FullSystemExtension.class)
class PlayerJoinEventConsumerTest {

    private static Server server;
    private static PlayerJoinEventConsumer underTest;

    @BeforeAll
    static void beforeAll(PlanConfig config, PlanSystem system, PlanPluginComponent component) {
        config.set(DataGatheringSettings.GEOLOCATIONS, true);
        config.set(DataGatheringSettings.ACCEPT_GEOLITE2_EULA, true);
        system.enable();
        server = system.getServerInfo().getServer();
        underTest = component.joinConsumer();
    }

    @AfterAll
    static void afterAll(PlanSystem system, Database database) throws ExecutionException, InterruptedException {
        database.executeTransaction(new RemoveEverythingTransaction()).get();
        system.disable();
        SessionCache.clear();
    }

    @BeforeEach
    void resetSystem(PlanConfig config, Database database, ServerUUID serverUUID) {
        SessionCache.clear();

        config.set(ExportSettings.PLAYER_PAGES, false);
        config.set(ExportSettings.EXPORT_ON_ONLINE_STATUS_CHANGE, false);

        database.executeTransaction(new RemoveEverythingTransaction());
        database.executeTransaction(new StoreServerInformationTransaction(new Server(serverUUID, TestConstants.SERVER_NAME, "", TestConstants.VERSION)));
    }

    PlayerJoin createPlayerJoin(PlatformPlayerData player) {
        return PlayerJoin.builder()
                .time(System.currentTimeMillis())
                .server(server)
                .player(player)
                .build();
    }

    private TestPlayerData createTestPlayer() {
        return new TestPlayerData(TestConstants.PLAYER_ONE_UUID, TestConstants.PLAYER_ONE_NAME);
    }

    @Test
    void joiningGameServerStartsSession() {
        PlayerJoin join = createPlayerJoin(createTestPlayer()
                .setCurrentWorld("World")
                .setCurrentGameMode("SURVIVAL"));

        underTest.onJoinGameServer(join);
        Awaitility.await()
                .atMost(1, TimeUnit.SECONDS)
                .until(() -> SessionCache.getCachedSession(TestConstants.PLAYER_ONE_UUID).isPresent());

        Optional<ActiveSession> cachedSession = SessionCache.getCachedSession(TestConstants.PLAYER_ONE_UUID);
        assertTrue(cachedSession.isPresent());
    }

    @Test
    void joiningProxyServerStartsSession() {
        PlayerJoin join = createPlayerJoin(createTestPlayer());

        underTest.onJoinProxyServer(join);
        Awaitility.await()
                .atMost(1, TimeUnit.SECONDS)
                .until(() -> SessionCache.getCachedSession(TestConstants.PLAYER_ONE_UUID).isPresent());

        Optional<ActiveSession> cachedSession = SessionCache.getCachedSession(TestConstants.PLAYER_ONE_UUID);
        assertTrue(cachedSession.isPresent());
    }

    @Test
    void joiningGameServerStoresWorldName(Database database) {
        PlayerJoin join = createPlayerJoin(createTestPlayer()
                .setCurrentWorld("World")
                .setCurrentGameMode("SURVIVAL"));

        underTest.onJoinGameServer(join);
        waitUntilDatabaseIsDone(database);

        List<String> expected = List.of("World");
        List<String> result = database.queryList(SELECT + WorldTable.NAME + FROM + WorldTable.TABLE_NAME,
                set -> set.getString(WorldTable.NAME));
        assertEquals(expected, result);
    }

    @Test
    void joiningGameServerStoresJoinAddress(Database database) {
        PlayerJoin join = createPlayerJoin(createTestPlayer()
                .setJoinAddress("play.testjoinaddress.com"));

        underTest.onJoinGameServer(join);
        waitUntilDatabaseIsDone(database);

        List<String> expected = List.of("play.testjoinaddress.com", JoinAddressTable.DEFAULT_VALUE_FOR_LOOKUP);
        List<String> result = database.query(JoinAddressQueries.allJoinAddresses());
        assertEquals(expected, result);
    }

    @Test
    void joiningGameServerStoresUser(Database database) {
        PlayerJoin join = createPlayerJoin(createTestPlayer()
                .setRegisterDate(1234L));

        underTest.onJoinGameServer(join);
        waitUntilDatabaseIsDone(database);

        Collection<BaseUser> expected = List.of(new BaseUser(TestConstants.PLAYER_ONE_UUID, TestConstants.PLAYER_ONE_NAME, 1234L, 0));
        Collection<BaseUser> result = database.query(BaseUserQueries.fetchAllBaseUsers());
        assertEquals(expected, result);
    }

    @Test
    void joiningProxyServerStoresUser(Database database) {
        PlayerJoin join = createPlayerJoin(createTestPlayer());

        underTest.onJoinProxyServer(join);
        waitUntilDatabaseIsDone(database);

        Collection<BaseUser> expected = List.of(new BaseUser(TestConstants.PLAYER_ONE_UUID, TestConstants.PLAYER_ONE_NAME, join.getTime(), 0));
        Collection<BaseUser> result = database.query(BaseUserQueries.fetchAllBaseUsers());
        assertEquals(expected, result);
    }

    private void waitUntilDatabaseIsDone(Database database) {
        Awaitility.await()
                .atMost(2, TimeUnit.SECONDS)
                .until(() -> database.getTransactionQueueSize() < 1);
    }

    @Test
    void joiningGameServerStoresUserInfo(Database database, ServerUUID serverUUID) {
        PlayerJoin join = createPlayerJoin(createTestPlayer()
                .setRegisterDate(1234L)
                .setJoinAddress("play.testjoinaddress.com"));

        underTest.onJoinGameServer(join);
        waitUntilDatabaseIsDone(database);

        Set<UserInfo> expected = Set.of(new UserInfo(TestConstants.PLAYER_ONE_UUID, serverUUID, 1234000L, false, "play.testjoinaddress.com", false));
        Set<UserInfo> result = database.query(UserInfoQueries.fetchUserInformationOfUser(TestConstants.PLAYER_ONE_UUID));
        assertEquals(expected, result);
    }

    @Test
    void joiningGameServerStoresNickname(Database database) {
        PlayerJoin join = createPlayerJoin(createTestPlayer()
                .setDisplayName("Nickname"));

        underTest.onJoinGameServer(join);
        waitUntilDatabaseIsDone(database);

        List<String> expected = List.of("Nickname");
        List<String> result = database.query(NicknameQueries.fetchNicknameDataOfPlayer(TestConstants.PLAYER_ONE_UUID))
                .stream().map(Nickname::getName)
                .collect(Collectors.toList());
        assertEquals(expected, result);
    }

    @Test
    void joiningGameServerStoresGeolocation(PlanSystem system, Database database) throws Exception {
        GeolocationCache geolocationCache = system.getCacheSystem().getGeolocationCache();
        Awaitility.await()
                .atMost(5, TimeUnit.SECONDS)
                .until(geolocationCache::canGeolocate);

        PlayerJoin join = createPlayerJoin(createTestPlayer()
                .setIp(InetAddress.getByName("156.53.159.86")));

        underTest.onJoinGameServer(join);
        waitUntilDatabaseIsDone(database);

        List<String> expected = List.of("United States");
        List<String> result = database.query(GeoInfoQueries.uniqueGeolocations());
        assertEquals(expected, result);
    }

    @Test
    void joiningProxyServerStoresGeolocation(PlanSystem system, Database database) throws Exception {
        GeolocationCache geolocationCache = system.getCacheSystem().getGeolocationCache();
        Awaitility.await()
                .atMost(5, TimeUnit.SECONDS)
                .until(geolocationCache::canGeolocate);

        PlayerJoin join = createPlayerJoin(createTestPlayer()
                .setIp(InetAddress.getByName("156.53.159.86")));

        underTest.onJoinProxyServer(join);
        waitUntilDatabaseIsDone(database);

        List<String> expected = List.of("United States");
        List<String> result = database.query(GeoInfoQueries.uniqueGeolocations());
        assertEquals(expected, result);
    }

    @Test
    void joiningGameServerStoresOperatorStatus(Database database) {
        PlayerJoin join = createPlayerJoin(createTestPlayer()
                .setOperator(true));

        underTest.onJoinGameServer(join);
        waitUntilDatabaseIsDone(database);

        Set<Integer> result = database.query(UserInfoQueries.userIdsOfNonOperators());
        assertTrue(result.isEmpty());
        result = database.query(UserInfoQueries.userIdsOfOperators());
        assertEquals(1, result.size());
    }

    @Test
    void joiningGameServerExportsPlayerPage(PlanConfig config) {
        config.set(ExportSettings.PLAYER_PAGES, true);
        config.set(ExportSettings.EXPORT_ON_ONLINE_STATUS_CHANGE, true);

        PlayerJoin join = createPlayerJoin(createTestPlayer());

        underTest.onJoinGameServer(join);

        File playerExportDir = config.getPageExportPath().resolve("player/" + TestConstants.PLAYER_ONE_UUID).toFile();
        Awaitility.await()
                .atMost(2, TimeUnit.SECONDS)
                .until(playerExportDir::exists);

        assertTrue(playerExportDir.exists());
        assertTrue(playerExportDir.isDirectory());
    }

    @Test
    void joiningProxyServerExportsPlayerPage(PlanConfig config) {
        config.set(ExportSettings.PLAYER_PAGES, true);
        config.set(ExportSettings.EXPORT_ON_ONLINE_STATUS_CHANGE, true);

        PlayerJoin join = createPlayerJoin(createTestPlayer());

        underTest.onJoinProxyServer(join);

        File playerExportDir = config.getPageExportPath().resolve("player/" + TestConstants.PLAYER_ONE_UUID).toFile();
        Awaitility.await()
                .atMost(5, TimeUnit.SECONDS)
                .until(playerExportDir::exists);

        assertTrue(playerExportDir.exists());
        assertTrue(playerExportDir.isDirectory());
    }

}
