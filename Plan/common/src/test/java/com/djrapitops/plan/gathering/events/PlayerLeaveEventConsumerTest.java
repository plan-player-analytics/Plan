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
import com.djrapitops.plan.extension.DataExtension;
import com.djrapitops.plan.extension.ExtensionService;
import com.djrapitops.plan.extension.annotation.NumberProvider;
import com.djrapitops.plan.extension.annotation.PluginInfo;
import com.djrapitops.plan.gathering.cache.SessionCache;
import com.djrapitops.plan.gathering.domain.*;
import com.djrapitops.plan.gathering.domain.event.JoinAddress;
import com.djrapitops.plan.gathering.domain.event.PlayerLeave;
import com.djrapitops.plan.identification.Server;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.DataGatheringSettings;
import com.djrapitops.plan.settings.config.paths.ExportSettings;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.objects.JoinAddressQueries;
import com.djrapitops.plan.storage.database.queries.objects.SessionQueries;
import com.djrapitops.plan.storage.database.queries.objects.UserInfoQueries;
import com.djrapitops.plan.storage.database.sql.tables.JoinAddressTable;
import com.djrapitops.plan.storage.database.transactions.StoreServerInformationTransaction;
import com.djrapitops.plan.storage.database.transactions.commands.RemoveEverythingTransaction;
import com.djrapitops.plan.storage.database.transactions.events.StoreJoinAddressTransaction;
import com.djrapitops.plan.storage.database.transactions.events.StoreServerPlayerTransaction;
import com.djrapitops.plan.storage.database.transactions.events.StoreWorldNameTransaction;
import extension.FullSystemExtension;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import utilities.DBPreparer;
import utilities.TestConstants;
import utilities.dagger.PlanPluginComponent;
import utilities.mocks.objects.TestPlayerData;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(FullSystemExtension.class)
class PlayerLeaveEventConsumerTest {

    private static Server server;
    private static PlayerLeaveEventConsumer underTest;

    @BeforeAll
    static void beforeAll(PlanConfig config, PlanSystem system, PlanPluginComponent component) {
        config.set(DataGatheringSettings.GEOLOCATIONS, true);
        config.set(DataGatheringSettings.ACCEPT_GEOLITE2_EULA, true);
        system.enable();
        server = system.getServerInfo().getServer();
        underTest = component.leaveConsumer();
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
        config.set(DataGatheringSettings.PRESERVE_JOIN_ADDRESS_CASE, false);

        database.executeTransaction(new RemoveEverythingTransaction());
        database.executeTransaction(new StoreServerInformationTransaction(new Server(serverUUID, TestConstants.SERVER_NAME, "", TestConstants.VERSION)));
    }

    PlayerLeave createPlayerLeave(PlatformPlayerData player) {
        return PlayerLeave.builder()
                .time(System.currentTimeMillis())
                .server(server)
                .player(player)
                .build();
    }

    private TestPlayerData createTestPlayer() {
        return new TestPlayerData(TestConstants.PLAYER_ONE_UUID, TestConstants.PLAYER_ONE_NAME);
    }

    @Test
    void leavingGameServerSavesSession(PlanSystem system, Database database, ServerUUID serverUUID) {
        SessionCache sessionCache = system.getCacheSystem().getSessionCache();
        long sessionStart = System.currentTimeMillis();
        sessionCache.cacheSession(TestConstants.PLAYER_ONE_UUID, new ActiveSession(TestConstants.PLAYER_ONE_UUID, serverUUID, sessionStart, "World", GMTimes.SURVIVAL));
        database.executeTransaction(new StoreWorldNameTransaction(serverUUID, "World"));

        PlayerLeave leave = createPlayerLeave(createTestPlayer());

        underTest.onLeaveGameServer(leave);
        DBPreparer.awaitUntilTransactionsComplete(database);

        DataMap extraData = new DataMap();
        GMTimes gmTimes = new GMTimes(Map.of(
                GMTimes.SURVIVAL, leave.getTime() - sessionStart,
                GMTimes.CREATIVE, 0L,
                GMTimes.SPECTATOR, 0L,
                GMTimes.ADVENTURE, 0L
        ));
        extraData.put(WorldTimes.class, new WorldTimes(Map.of("World", gmTimes)));
        extraData.put(PlayerKills.class, new PlayerKills());
        extraData.put(MobKillCounter.class, new MobKillCounter());
        extraData.put(DeathCounter.class, new DeathCounter());
        extraData.put(JoinAddress.class, new JoinAddress(JoinAddressTable.DEFAULT_VALUE_FOR_LOOKUP));

        List<FinishedSession> expected = List.of(new FinishedSession(
                TestConstants.PLAYER_ONE_UUID,
                serverUUID,
                sessionStart,
                leave.getTime(),
                0L,
                extraData
        ));
        List<FinishedSession> result = database.query(SessionQueries.fetchAllSessions());
        assertEquals(expected, result);
    }

    @Test
    void leavingGameServerSavesBanStatus(PlanSystem system, Database database, ServerUUID serverUUID) {
        SessionCache sessionCache = system.getCacheSystem().getSessionCache();
        long sessionStart = System.currentTimeMillis();
        sessionCache.cacheSession(TestConstants.PLAYER_ONE_UUID, new ActiveSession(TestConstants.PLAYER_ONE_UUID, serverUUID, sessionStart, "World", GMTimes.SURVIVAL));
        registerPlayer(database, serverUUID);
        PlayerLeave leave = createPlayerLeave(createTestPlayer()
                .setBanned(true));

        underTest.onLeaveGameServer(leave);
        DBPreparer.awaitUntilTransactionsComplete(database);

        Set<Integer> result = database.query(UserInfoQueries.userIdsOfBanned());
        assertEquals(1, result.size());
        result = database.query(UserInfoQueries.userIdsOfNotBanned());
        assertEquals(0, result.size());
    }

    @Test
    void leavingGameServerExportsPlayerPage(PlanConfig config, Database database, ServerUUID serverUUID) {
        registerPlayer(database, serverUUID);

        config.set(ExportSettings.PLAYER_PAGES, true);
        config.set(ExportSettings.EXPORT_ON_ONLINE_STATUS_CHANGE, true);

        PlayerLeave leave = createPlayerLeave(createTestPlayer());

        underTest.onLeaveGameServer(leave);
        DBPreparer.awaitUntilTransactionsComplete(database);

        File playerExportDir = config.getPageExportPath().resolve("player/" + TestConstants.PLAYER_ONE_UUID).toFile();
        Awaitility.await()
                .atMost(2, TimeUnit.SECONDS)
                .until(playerExportDir::exists);

        assertTrue(playerExportDir.exists());
        assertTrue(playerExportDir.isDirectory());
    }

    @Test
    void leavingProxyServerExportsPlayerPage(PlanConfig config, Database database, ServerUUID serverUUID) {
        registerPlayer(database, serverUUID);

        config.set(ExportSettings.PLAYER_PAGES, true);
        config.set(ExportSettings.EXPORT_ON_ONLINE_STATUS_CHANGE, true);

        PlayerLeave leave = createPlayerLeave(createTestPlayer());

        underTest.onLeaveProxyServer(leave);
        DBPreparer.awaitUntilTransactionsComplete(database);

        File playerExportDir = config.getPageExportPath().resolve("player/" + TestConstants.PLAYER_ONE_UUID).toFile();
        Awaitility.await()
                .atMost(2, TimeUnit.SECONDS)
                .until(playerExportDir::exists);

        assertTrue(playerExportDir.exists());
        assertTrue(playerExportDir.isDirectory());
    }

    private void registerPlayer(Database database, ServerUUID serverUUID) {
        database.executeTransaction(new StoreServerPlayerTransaction(TestConstants.PLAYER_ONE_UUID, System::currentTimeMillis, TestConstants.PLAYER_ONE_NAME, serverUUID, () -> null))
                .join(); // Wait until complete
    }

    @Test
    void extensionDataIsUpdatedBeforeLeave() {
        AtomicBoolean called = new AtomicBoolean(false);
        @PluginInfo(name = "Extension")
        class Extension implements DataExtension {
            @NumberProvider(text = "Value")
            public long value(UUID playerUUID) {
                called.set(true);
                return 0L;
            }
        }
        Extension extension = new Extension();
        try {
            ExtensionService.getInstance().register(extension);

            underTest.beforeLeave(createPlayerLeave(createTestPlayer()));

            Awaitility.await()
                    .atMost(2, TimeUnit.SECONDS)
                    .until(called::get);
            assertTrue(called.get());
        } finally {
            ExtensionService.getInstance().unregister(extension);
        }
    }

    @Test
    void joinAddressCaseIsPreserved(PlanSystem system, PlanConfig config, Database database, ServerUUID serverUUID) {
        config.set(DataGatheringSettings.PRESERVE_JOIN_ADDRESS_CASE, true);

        SessionCache sessionCache = system.getCacheSystem().getSessionCache();
        long sessionStart = System.currentTimeMillis();
        ActiveSession activeSession = new ActiveSession(TestConstants.PLAYER_ONE_UUID, serverUUID, sessionStart, "World", GMTimes.SURVIVAL);
        activeSession.getExtraData().put(JoinAddress.class, new JoinAddress("PLAY.UPPERCASE.COM"));
        sessionCache.cacheSession(TestConstants.PLAYER_ONE_UUID, activeSession);

        database.executeTransaction(new StoreJoinAddressTransaction("play.uppercase.com")); // The wrong address
        database.executeTransaction(new StoreWorldNameTransaction(serverUUID, "World"));

        PlayerLeave leave = createPlayerLeave(createTestPlayer()
                .setJoinAddress("PLAY.UPPERCASE.COM"));

        underTest.onLeaveGameServer(leave);
        DBPreparer.awaitUntilTransactionsComplete(database);

        List<String> expected = List.of("PLAY.UPPERCASE.COM", "play.uppercase.com", JoinAddressTable.DEFAULT_VALUE_FOR_LOOKUP);
        List<String> result = database.query(JoinAddressQueries.allJoinAddresses());
        assertEquals(expected, result);
    }
}
