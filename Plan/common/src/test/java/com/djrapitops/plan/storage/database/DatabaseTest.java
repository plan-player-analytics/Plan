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
package com.djrapitops.plan.storage.database;

import com.djrapitops.plan.PlanSystem;
import com.djrapitops.plan.data.element.TableContainer;
import com.djrapitops.plan.delivery.domain.DateObj;
import com.djrapitops.plan.delivery.domain.Nickname;
import com.djrapitops.plan.delivery.domain.TablePlayer;
import com.djrapitops.plan.delivery.domain.WebUser;
import com.djrapitops.plan.delivery.domain.container.PlayerContainer;
import com.djrapitops.plan.delivery.domain.container.ServerContainer;
import com.djrapitops.plan.delivery.domain.keys.Key;
import com.djrapitops.plan.delivery.domain.keys.PlayerKeys;
import com.djrapitops.plan.delivery.domain.keys.ServerKeys;
import com.djrapitops.plan.delivery.domain.keys.SessionKeys;
import com.djrapitops.plan.delivery.domain.mutators.SessionsMutator;
import com.djrapitops.plan.extension.CallEvents;
import com.djrapitops.plan.extension.DataExtension;
import com.djrapitops.plan.extension.ExtensionService;
import com.djrapitops.plan.extension.ExtensionServiceImplementation;
import com.djrapitops.plan.extension.annotation.*;
import com.djrapitops.plan.extension.icon.Color;
import com.djrapitops.plan.extension.icon.Icon;
import com.djrapitops.plan.extension.implementation.results.*;
import com.djrapitops.plan.extension.implementation.storage.queries.ExtensionPlayerDataQuery;
import com.djrapitops.plan.extension.implementation.storage.queries.ExtensionServerDataQuery;
import com.djrapitops.plan.extension.implementation.storage.queries.ExtensionServerPlayerDataTableQuery;
import com.djrapitops.plan.extension.implementation.storage.transactions.results.RemoveUnsatisfiedConditionalPlayerResultsTransaction;
import com.djrapitops.plan.extension.implementation.storage.transactions.results.RemoveUnsatisfiedConditionalServerResultsTransaction;
import com.djrapitops.plan.extension.table.Table;
import com.djrapitops.plan.gathering.domain.*;
import com.djrapitops.plan.identification.Server;
import com.djrapitops.plan.query.QueryServiceImplementation;
import com.djrapitops.plan.settings.config.Config;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.storage.database.queries.*;
import com.djrapitops.plan.storage.database.queries.analysis.ActivityIndexQueries;
import com.djrapitops.plan.storage.database.queries.containers.ContainerFetchQueries;
import com.djrapitops.plan.storage.database.queries.containers.ServerPlayerContainersQuery;
import com.djrapitops.plan.storage.database.queries.objects.*;
import com.djrapitops.plan.storage.database.sql.parsing.Sql;
import com.djrapitops.plan.storage.database.transactions.*;
import com.djrapitops.plan.storage.database.transactions.commands.*;
import com.djrapitops.plan.storage.database.transactions.events.*;
import com.djrapitops.plan.storage.database.transactions.init.CreateIndexTransaction;
import com.djrapitops.plan.storage.database.transactions.init.CreateTablesTransaction;
import com.djrapitops.plan.storage.database.transactions.init.RemoveDuplicateUserInfoTransaction;
import com.djrapitops.plan.storage.database.transactions.patches.Patch;
import com.djrapitops.plan.storage.upkeep.DBCleanTask;
import com.djrapitops.plan.utilities.comparators.DateHolderRecentComparator;
import com.djrapitops.plugin.logging.console.TestPluginLogger;
import com.djrapitops.plugin.logging.error.ConsoleErrorLogger;
import com.google.common.util.concurrent.MoreExecutors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utilities.*;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.djrapitops.plan.storage.database.sql.parsing.Sql.SELECT;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Contains all common Database Tests for all Database Types
 *
 * @author Rsl1122 (Refactored into this class by Fuzzlemann)
 */
public interface DatabaseTest {

    String[] worlds = new String[]{"TestWorld", "TestWorld2"};
    UUID playerUUID = TestConstants.PLAYER_ONE_UUID;
    UUID player2UUID = TestConstants.PLAYER_TWO_UUID;

    Database db();

    UUID serverUUID();

    PlanSystem system();

    @BeforeEach
    default void setUp() {
        db().executeTransaction(new Patch() {
            @Override
            public boolean hasBeenApplied() {
                return false;
            }

            @Override
            public void applyPatch() {
                dropTable("plan_world_times");
                dropTable("plan_kills");
                dropTable("plan_sessions");
                dropTable("plan_worlds");
                dropTable("plan_users");
            }
        });
        db().executeTransaction(new CreateTablesTransaction());
        db().executeTransaction(new RemoveEverythingTransaction());

        db().executeTransaction(new StoreServerInformationTransaction(new Server(-1, serverUUID(), "ServerName", "", 20)));
        assertEquals(serverUUID(), ((SQLDB) db()).getServerUUIDSupplier().get());

        ExtensionService extensionService = system().getExtensionService();
        extensionService.unregister(new PlayerExtension());
        extensionService.unregister(new ServerExtension());
        extensionService.unregister(new ConditionalExtension());
        extensionService.unregister(new TableExtension());
    }

    default void execute(Executable executable) {
        db().executeTransaction(new Transaction() {
            @Override
            protected void performOperations() {
                execute(executable);
            }
        });
    }

    default void commitTest() {
        db().close();
        db().init();
    }

    @Test
    default void testTPSSaving() {
        Random r = new Random();

        List<TPS> expected = new ArrayList<>();

        for (int i = 0; i < RandomData.randomInt(1, 5); i++) {
            expected.add(new TPS(r.nextLong(), r.nextDouble(), r.nextInt(100000000), r.nextDouble(), r.nextLong(), r.nextInt(), r.nextInt(), r.nextLong()));
        }

        for (TPS tps : expected) {
            execute(DataStoreQueries.storeTPS(serverUUID(), tps));
        }

        commitTest();

        assertEquals(expected, db().query(TPSQueries.fetchTPSDataOfServer(serverUUID())));
    }

    default void saveUserOne() {
        playerIsRegisteredToBothTables();
        db().executeTransaction(new KickStoreTransaction(playerUUID));
    }

    default void saveUserTwo() {
        db().executeTransaction(new PlayerRegisterTransaction(player2UUID, () -> 123456789L, "Test"));
    }

    @Test
    default void geoInformationIsStored() {
        saveUserOne();

        long time = System.currentTimeMillis();

        GeoInfo expected = new GeoInfo("TestLocation", time);
        saveGeoInfo(playerUUID, expected);
        commitTest();

        List<GeoInfo> result = db().query(GeoInfoQueries.fetchAllGeoInformation()).getOrDefault(playerUUID, new ArrayList<>());
        assertEquals(Collections.singletonList(expected), result);
    }

    @Test
    default void testNicknamesTable() {
        saveUserOne();

        Nickname expected = new Nickname("TestNickname", System.currentTimeMillis(), serverUUID());
        db().executeTransaction(new NicknameStoreTransaction(playerUUID, expected, (uuid, name) -> false /* Not cached */));
        db().executeTransaction(new NicknameStoreTransaction(playerUUID, expected, (uuid, name) -> true /* Cached */));
        commitTest();

        List<Nickname> nicknames = db().query(NicknameQueries.fetchNicknameDataOfPlayer(playerUUID));
        assertEquals(1, nicknames.size());
        assertEquals(expected, nicknames.get(0));
    }

    @Test
    default void webUserIsRegistered() {
        WebUser expected = new WebUser(TestConstants.PLAYER_ONE_NAME, "RandomGarbageBlah", 0);
        db().executeTransaction(new RegisterWebUserTransaction(expected));
        commitTest();

        Optional<WebUser> found = db().query(WebUserQueries.fetchWebUser(TestConstants.PLAYER_ONE_NAME));
        assertTrue(found.isPresent());
        assertEquals(expected, found.get());
    }

    @Test
    default void multipleWebUsersAreFetchedAppropriately() {
        webUserIsRegistered();
        assertEquals(1, db().query(WebUserQueries.fetchAllPlanWebUsers()).size());
    }

    @Test
    default void webUserIsRemoved() {
        webUserIsRegistered();
        db().executeTransaction(new RemoveWebUserTransaction(TestConstants.PLAYER_ONE_NAME));
        assertFalse(db().query(WebUserQueries.fetchWebUser(TestConstants.PLAYER_ONE_NAME)).isPresent());
    }

    @Test
    default void worldNamesAreStored() {
        String[] expected = {"Test", "Test2", "Test3"};
        saveWorlds(expected);

        commitTest();

        Collection<String> result = db().query(LargeFetchQueries.fetchAllWorldNames()).getOrDefault(serverUUID(), new HashSet<>());
        assertEquals(new HashSet<>(Arrays.asList(expected)), result);
    }

    default void saveWorld(String worldName) {
        db().executeTransaction(new WorldNameStoreTransaction(serverUUID(), worldName));
    }

    default void saveWorlds(String... worldNames) {
        for (String worldName : worldNames) {
            saveWorld(worldName);
        }
    }

    default void saveTwoWorlds() {
        saveWorlds(worlds);
    }

    default WorldTimes createWorldTimes() {
        Map<String, GMTimes> times = new HashMap<>();
        Map<String, Long> gm = new HashMap<>();
        String[] gms = GMTimes.getGMKeyArray();
        gm.put(gms[0], 1000L);
        gm.put(gms[1], 2000L);
        gm.put(gms[2], 3000L);
        gm.put(gms[3], 4000L);

        String worldName = worlds[0];
        times.put(worldName, new GMTimes(gm));
        db().executeTransaction(new WorldNameStoreTransaction(serverUUID(), worldName));

        return new WorldTimes(times);
    }

    default List<PlayerKill> createKills() {
        List<PlayerKill> kills = new ArrayList<>();
        kills.add(new PlayerKill(TestConstants.PLAYER_TWO_UUID, "Iron Sword", 4321L));
        kills.add(new PlayerKill(TestConstants.PLAYER_TWO_UUID, "Gold Sword", 5321L));
        kills.sort(new DateHolderRecentComparator());
        return kills;
    }

    @Test
    default void testSessionPlaytimeSaving() {
        saveTwoWorlds();
        saveUserOne();
        saveUserTwo();
        Session session = new Session(playerUUID, serverUUID(), 12345L, worlds[0], "SURVIVAL");
        session.endSession(22345L);
        session.setWorldTimes(createWorldTimes());
        session.setPlayerKills(createKills());

        long expectedLength = 10000L;
        assertEquals(expectedLength, session.getLength());
        assertEquals(expectedLength, session.getUnsafe(SessionKeys.WORLD_TIMES).getTotal());

        execute(DataStoreQueries.storeSession(session));

        commitTest();

        Map<UUID, List<Session>> sessions = db().query(SessionQueries.fetchSessionsOfPlayer(playerUUID));
        assertTrue(sessions.containsKey(serverUUID()));

        SessionsMutator sessionsMutator = new SessionsMutator(sessions.get(serverUUID()));
        SessionsMutator afterTimeSessionsMutator = sessionsMutator.filterSessionsBetween(30000, System.currentTimeMillis());

        assertEquals(expectedLength, sessionsMutator.toPlaytime());
        assertEquals(0L, afterTimeSessionsMutator.toPlaytime());

        assertEquals(1, sessionsMutator.count());
        assertEquals(0, afterTimeSessionsMutator.count());
    }

    @Test
    default void sessionsAreStoredWithAllData() {
        saveUserOne();
        saveUserTwo();

        Session session = new Session(playerUUID, serverUUID(), 12345L, worlds[0], "SURVIVAL");
        session.endSession(22345L);
        session.setWorldTimes(createWorldTimes());
        session.setPlayerKills(createKills());

        execute(DataStoreQueries.storeSession(session));

        commitTest();

        Map<UUID, List<Session>> sessions = db().query(SessionQueries.fetchSessionsOfPlayer(playerUUID));
        List<Session> savedSessions = sessions.get(serverUUID());

        assertNotNull(savedSessions);
        assertEquals(1, savedSessions.size());

        assertEquals(session, savedSessions.get(0));
    }

    @Test
    default void mostRecentSessionsCanBeQueried() {
        sessionsAreStoredWithAllData();

        Session session = new Session(playerUUID, serverUUID(), 12345L, worlds[0], "SURVIVAL");
        session.endSession(22345L);
        session.setWorldTimes(createWorldTimes());
        session.setPlayerKills(createKills());

        List<Session> expected = Collections.singletonList(session);
        List<Session> result = db().query(SessionQueries.fetchLatestSessionsOfServer(serverUUID(), 1));
        assertEquals(expected, result);
    }

    @Test
    default void userInfoTableStoresCorrectUserInformation() {
        saveUserOne();

        List<UserInfo> userInfo = db().query(UserInfoQueries.fetchUserInformationOfUser(playerUUID));
        List<UserInfo> expected = Collections.singletonList(new UserInfo(playerUUID, serverUUID(), 1000L, false, false));

        assertEquals(expected, userInfo);
    }

    @Test
    default void userInfoTableUpdatesBanStatus() {
        saveUserOne();

        db().executeTransaction(new BanStatusTransaction(playerUUID, () -> true));

        List<UserInfo> userInfo = db().query(UserInfoQueries.fetchUserInformationOfUser(playerUUID));
        List<UserInfo> expected = Collections.singletonList(new UserInfo(playerUUID, serverUUID(), 1000L, false, true));

        assertEquals(expected, userInfo);
    }

    @Test
    default void userInfoTableUpdatesOperatorStatus() {
        saveUserOne();

        db().executeTransaction(new OperatorStatusTransaction(playerUUID, true));

        List<UserInfo> userInfo = db().query(UserInfoQueries.fetchUserInformationOfUser(playerUUID));
        List<UserInfo> expected = Collections.singletonList(new UserInfo(playerUUID, serverUUID(), 1000L, true, false));

        assertEquals(expected, userInfo);
    }

    @Test
    default void playerNameIsUpdatedWhenPlayerLogsIn() {
        saveUserOne();

        OptionalAssert.equals(playerUUID, db().query(UserIdentifierQueries.fetchPlayerUUIDOf(TestConstants.PLAYER_ONE_NAME)));

        // Updates the name
        db().executeTransaction(new PlayerRegisterTransaction(playerUUID, () -> 0, "NewName"));
        commitTest();

        assertFalse(db().query(UserIdentifierQueries.fetchPlayerUUIDOf(TestConstants.PLAYER_ONE_NAME)).isPresent());

        OptionalAssert.equals(playerUUID, db().query(UserIdentifierQueries.fetchPlayerUUIDOf("NewName")));
    }

    @Test
    default void testUsersTableKickSaving() {
        saveUserOne();
        OptionalAssert.equals(1, db().query(BaseUserQueries.fetchBaseUserOfPlayer(playerUUID)).map(BaseUser::getTimesKicked));

        int random = new Random().nextInt(20);

        for (int i = 0; i < random + 1; i++) {
            db().executeTransaction(new KickStoreTransaction(playerUUID));
        }
        commitTest();
        OptionalAssert.equals(random + 2, db().query(BaseUserQueries.fetchBaseUserOfPlayer(playerUUID)).map(BaseUser::getTimesKicked));
    }

    @Test
    default void testRemovalSingleUser() {
        saveUserTwo();

        db().executeTransaction(new PlayerServerRegisterTransaction(playerUUID, () -> 223456789L, "Test_name", serverUUID()));
        saveTwoWorlds();

        Session session = new Session(playerUUID, serverUUID(), 12345L, worlds[0], "SURVIVAL");
        session.endSession(22345L);
        session.setWorldTimes(createWorldTimes());
        session.setPlayerKills(createKills());

        execute(DataStoreQueries.storeSession(session));
        db().executeTransaction(new NicknameStoreTransaction(playerUUID, new Nickname("TestNick", System.currentTimeMillis(), serverUUID()), (uuid, name) -> false /* Not cached */));
        saveGeoInfo(playerUUID, new GeoInfo("TestLoc", 223456789L));

        assertTrue(db().query(PlayerFetchQueries.isPlayerRegistered(playerUUID)));

        db().executeTransaction(new RemovePlayerTransaction(playerUUID));

        assertFalse(db().query(PlayerFetchQueries.isPlayerRegistered(playerUUID)));
        assertFalse(db().query(PlayerFetchQueries.isPlayerRegisteredOnServer(playerUUID, serverUUID())));
        assertTrue(db().query(NicknameQueries.fetchNicknameDataOfPlayer(playerUUID)).isEmpty());
        assertTrue(db().query(GeoInfoQueries.fetchPlayerGeoInformation(playerUUID)).isEmpty());
        assertQueryIsEmpty(db(), SessionQueries.fetchSessionsOfPlayer(playerUUID));
    }

    @Test
    default void testRemovalEverything() throws NoSuchAlgorithmException {
        saveAllData();

        db().executeTransaction(new RemoveEverythingTransaction());

        assertTrue(db().query(BaseUserQueries.fetchAllBaseUsers()).isEmpty());
        assertQueryIsEmpty(db(), UserInfoQueries.fetchAllUserInformation());
        assertQueryIsEmpty(db(), NicknameQueries.fetchAllNicknameData());
        assertQueryIsEmpty(db(), GeoInfoQueries.fetchAllGeoInformation());
        assertTrue(db().query(SessionQueries.fetchAllSessions()).isEmpty());
        assertQueryIsEmpty(db(), LargeFetchQueries.fetchAllWorldNames());
        assertQueryIsEmpty(db(), LargeFetchQueries.fetchAllTPSData());
        assertQueryIsEmpty(db(), ServerQueries.fetchPlanServerInformation());
        assertQueryIsEmpty(db(), PingQueries.fetchAllPingData());
        assertTrue(db().query(WebUserQueries.fetchAllPlanWebUsers()).isEmpty());
    }

    default <T extends Map> void assertQueryIsEmpty(Database database, Query<T> query) {
        assertTrue(database.query(query).isEmpty());
    }

    default void saveAllData() {
        saveUserOne();
        saveUserTwo();

        saveTwoWorlds();

        Session session = new Session(playerUUID, serverUUID(), 12345L, worlds[0], "SURVIVAL");
        session.endSession(22345L);
        session.setWorldTimes(createWorldTimes());
        session.setPlayerKills(createKills());

        execute(DataStoreQueries.storeSession(session));
        db().executeTransaction(
                new NicknameStoreTransaction(playerUUID, new Nickname("TestNick", System.currentTimeMillis(), serverUUID()), (uuid, name) -> false /* Not cached */)
        );
        saveGeoInfo(playerUUID, new GeoInfo("TestLoc", 223456789L));

        assertTrue(db().query(PlayerFetchQueries.isPlayerRegistered(playerUUID)));

        List<TPS> expected = new ArrayList<>();
        Random r = new Random();
        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        int availableProcessors = ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors();
        double averageCPUUsage = operatingSystemMXBean.getSystemLoadAverage() / availableProcessors * 100.0;
        long usedMemory = 51231251254L;
        int entityCount = 6123;
        int chunksLoaded = 2134;
        long freeDiskSpace = new File("").getUsableSpace();
        expected.add(new TPS(r.nextLong(), r.nextDouble(), r.nextInt(100000000), averageCPUUsage, usedMemory, entityCount, chunksLoaded, freeDiskSpace));
        expected.add(new TPS(r.nextLong(), r.nextDouble(), r.nextInt(100000000), averageCPUUsage, usedMemory, entityCount, chunksLoaded, freeDiskSpace));
        expected.add(new TPS(r.nextLong(), r.nextDouble(), r.nextInt(100000000), averageCPUUsage, usedMemory, entityCount, chunksLoaded, freeDiskSpace));
        expected.add(new TPS(r.nextLong(), r.nextDouble(), r.nextInt(100000000), averageCPUUsage, usedMemory, entityCount, chunksLoaded, freeDiskSpace));
        for (TPS tps : expected) {
            execute(DataStoreQueries.storeTPS(serverUUID(), tps));
        }

        db().executeTransaction(new PingStoreTransaction(
                playerUUID, serverUUID(),
                Collections.singletonList(new DateObj<>(System.currentTimeMillis(), r.nextInt())))
        );

        WebUser webUser = new WebUser(TestConstants.PLAYER_ONE_NAME, "RandomGarbageBlah", 0);
        db().executeTransaction(new RegisterWebUserTransaction(webUser));
    }

    default void saveGeoInfo(UUID uuid, GeoInfo geoInfo) {
        db().executeTransaction(new GeoInfoStoreTransaction(uuid, geoInfo));
    }

    @Test
    default void testSessionTableGetInfoOfServer() {
        saveUserOne();
        saveUserTwo();

        Session session = new Session(playerUUID, serverUUID(), 12345L, worlds[0], "SURVIVAL");
        session.endSession(22345L);
        session.setWorldTimes(createWorldTimes());
        session.setPlayerKills(createKills());
        execute(DataStoreQueries.storeSession(session));

        commitTest();

        Map<UUID, List<Session>> sessions = db().query(SessionQueries.fetchSessionsOfServer(serverUUID()));

        List<Session> sSessions = sessions.get(playerUUID);
        assertFalse(sessions.isEmpty());
        assertNotNull(sSessions);
        assertFalse(sSessions.isEmpty());
        assertEquals(session, sSessions.get(0));
    }

    @Test
    default void cleanDoesNotCleanActivePlayers() {
        saveUserOne();
        saveTwoWorlds();

        long sessionStart = System.currentTimeMillis();
        Session session = new Session(playerUUID, serverUUID(), sessionStart, worlds[0], "SURVIVAL");
        session.endSession(sessionStart + 22345L);
        execute(DataStoreQueries.storeSession(session));

        TestPluginLogger logger = new TestPluginLogger();
        ConsoleErrorLogger errorHandler = new ConsoleErrorLogger(logger);
        new DBCleanTask(
                system().getConfigSystem().getConfig(),
                new Locale(),
                system().getDatabaseSystem(),
                new QueryServiceImplementation(system().getDatabaseSystem(), system().getServerInfo(), logger, errorHandler),
                system().getServerInfo(),
                logger,
                errorHandler
        ).cleanOldPlayers(db());

        Collection<BaseUser> found = db().query(BaseUserQueries.fetchServerBaseUsers(serverUUID()));
        assertFalse(found.isEmpty(), "All users were deleted!! D:");
    }

    @Test
    default void cleanRemovesOnlyDuplicatedUserInfo() {
        // Store one duplicate
        db().executeTransaction(new Transaction() {
            @Override
            protected void performOperations() {
                execute(DataStoreQueries.registerUserInfo(playerUUID, 0L, serverUUID()));
                execute(DataStoreQueries.registerUserInfo(playerUUID, 0L, serverUUID()));
                execute(DataStoreQueries.registerUserInfo(player2UUID, 0L, serverUUID()));
            }
        });

        db().executeTransaction(new RemoveDuplicateUserInfoTransaction());

        List<UserInfo> found = db().query(UserInfoQueries.fetchUserInformationOfUser(playerUUID));
        assertEquals(
                Collections.singletonList(new UserInfo(playerUUID, serverUUID(), 0, false, false)),
                found
        );

        List<UserInfo> found2 = db().query(UserInfoQueries.fetchUserInformationOfUser(player2UUID));
        assertEquals(
                Collections.singletonList(new UserInfo(player2UUID, serverUUID(), 0, false, false)),
                found2
        );
    }

    @Test
    default void testKillTableGetKillsOfServer() {
        saveUserOne();
        saveUserTwo();

        Session session = createSession();
        List<PlayerKill> expected = createKills();
        session.setPlayerKills(expected);
        execute(DataStoreQueries.storeSession(session));

        commitTest();

        Map<UUID, List<Session>> sessions = db().query(SessionQueries.fetchSessionsOfPlayer(playerUUID));
        List<Session> savedSessions = sessions.get(serverUUID());
        assertNotNull(savedSessions);
        assertFalse(savedSessions.isEmpty());

        Session savedSession = savedSessions.get(0);
        assertNotNull(savedSession);

        List<PlayerKill> kills = savedSession.getPlayerKills();
        assertNotNull(kills);
        assertFalse(kills.isEmpty());
        assertEquals(expected, kills);
    }

    default Session createSession() {
        Session session = new Session(
                playerUUID,
                serverUUID(),
                System.currentTimeMillis(),
                "world",
                GMTimes.getGMKeyArray()[0]
        );
        db().executeTransaction(new WorldNameStoreTransaction(serverUUID(), "world"));
        session.endSession(System.currentTimeMillis() + 1L);
        return session;
    }

    @Test
    default void testBackupAndRestoreSQLite() throws Exception {
        File tempFile = Files.createTempFile(system().getPlanFiles().getDataFolder().toPath(), "backup-", ".db").toFile();
        tempFile.deleteOnExit();
        SQLiteDB backup = system().getDatabaseSystem().getSqLiteFactory().usingFile(tempFile);
        backup.setTransactionExecutorServiceProvider(MoreExecutors::newDirectExecutorService);
        try {
            backup.init();

            saveAllData();

            backup.executeTransaction(new BackupCopyTransaction(db(), backup));

            assertQueryResultIsEqual(db(), backup, BaseUserQueries.fetchAllBaseUsers());
            assertQueryResultIsEqual(db(), backup, UserInfoQueries.fetchAllUserInformation());
            assertQueryResultIsEqual(db(), backup, NicknameQueries.fetchAllNicknameData());
            assertQueryResultIsEqual(db(), backup, GeoInfoQueries.fetchAllGeoInformation());
            assertQueryResultIsEqual(db(), backup, SessionQueries.fetchAllSessions());
            assertQueryResultIsEqual(db(), backup, LargeFetchQueries.fetchAllWorldNames());
            assertQueryResultIsEqual(db(), backup, LargeFetchQueries.fetchAllTPSData());
            assertQueryResultIsEqual(db(), backup, ServerQueries.fetchPlanServerInformation());
            assertQueryResultIsEqual(db(), backup, WebUserQueries.fetchAllPlanWebUsers());
        } finally {
            backup.close();
        }
    }

    @Test
    default void testBackupAndRestoreH2() throws Exception {
        File tempFile = Files.createTempFile(system().getPlanFiles().getDataFolder().toPath(), "backup-", ".db").toFile();
        tempFile.deleteOnExit();
        H2DB backup = system().getDatabaseSystem().getH2Factory().usingFile(tempFile);
        backup.setTransactionExecutorServiceProvider(MoreExecutors::newDirectExecutorService);
        try {
            backup.init();

            saveAllData();

            backup.executeTransaction(new BackupCopyTransaction(db(), backup));

            assertQueryResultIsEqual(db(), backup, BaseUserQueries.fetchAllBaseUsers());
            assertQueryResultIsEqual(db(), backup, UserInfoQueries.fetchAllUserInformation());
            assertQueryResultIsEqual(db(), backup, NicknameQueries.fetchAllNicknameData());
            assertQueryResultIsEqual(db(), backup, GeoInfoQueries.fetchAllGeoInformation());
            assertQueryResultIsEqual(db(), backup, SessionQueries.fetchAllSessions());
            assertQueryResultIsEqual(db(), backup, LargeFetchQueries.fetchAllWorldNames());
            assertQueryResultIsEqual(db(), backup, LargeFetchQueries.fetchAllTPSData());
            assertQueryResultIsEqual(db(), backup, ServerQueries.fetchPlanServerInformation());
            assertQueryResultIsEqual(db(), backup, WebUserQueries.fetchAllPlanWebUsers());
        } finally {
            backup.close();
        }
    }

    default <T> void assertQueryResultIsEqual(Database one, Database two, Query<T> query) {
        assertEquals(one.query(query), two.query(query));
    }

    @Test
    default void sessionWorldTimesAreFetchedCorrectly() {
        saveUserOne();
        WorldTimes worldTimes = createWorldTimes();
        Session session = new Session(1, playerUUID, serverUUID(), 12345L, 23456L, 0, 0, 0);
        session.setWorldTimes(worldTimes);
        execute(DataStoreQueries.storeSession(session));

        // Fetch the session
        Map<UUID, List<Session>> sessions = db().query(SessionQueries.fetchSessionsOfPlayer(playerUUID));
        List<Session> serverSessions = sessions.get(serverUUID());
        assertNotNull(serverSessions);
        assertFalse(serverSessions.isEmpty());

        Session savedSession = serverSessions.get(0);
        assertEquals(worldTimes, savedSession.getUnsafe(SessionKeys.WORLD_TIMES));
    }

    @Test
    default void worldTimesAreSavedWithAllSessionSave() {
        saveTwoWorlds();
        saveUserOne();

        WorldTimes worldTimes = createWorldTimes();

        Session session = createSession();
        session.setWorldTimes(worldTimes);
        List<Session> sessions = new ArrayList<>();
        sessions.add(session);
        db().executeTransaction(new Transaction() {
            @Override
            protected void performOperations() {
                execute(LargeStoreQueries.storeAllSessionsWithKillAndWorldData(sessions));
            }
        });

        Map<UUID, WorldTimes> saved = db().query(WorldTimesQueries.fetchPlayerWorldTimesOnServers(playerUUID));
        WorldTimes savedWorldTimes = saved.get(serverUUID());
        assertEquals(worldTimes, savedWorldTimes);
    }

    @Test
    default void worldTimesAreSavedWithSession() {
        saveTwoWorlds();
        saveUserOne();

        WorldTimes worldTimes = createWorldTimes();
        Session session = createSession();
        session.setWorldTimes(worldTimes);
        List<Session> sessions = new ArrayList<>();
        sessions.add(session);
        db().executeTransaction(new Transaction() {
            @Override
            protected void performOperations() {
                execute(LargeStoreQueries.storeAllSessionsWithKillAndWorldData(sessions));
            }
        });

        List<Session> allSessions = db().query(SessionQueries.fetchAllSessions());

        assertEquals(worldTimes, allSessions.get(0).getUnsafe(SessionKeys.WORLD_TIMES));
    }

    @Test
    default void playersWorldTimesMatchTotal() {
        worldTimesAreSavedWithSession();
        WorldTimes worldTimesOfUser = db().query(WorldTimesQueries.fetchPlayerTotalWorldTimes(playerUUID));
        assertEquals(createWorldTimes(), worldTimesOfUser);
    }

    @Test
    default void serverWorldTimesMatchTotal() {
        worldTimesAreSavedWithSession();
        WorldTimes worldTimesOfServer = db().query(WorldTimesQueries.fetchServerTotalWorldTimes(serverUUID()));
        assertEquals(createWorldTimes(), worldTimesOfServer);
    }

    @Test
    default void emptyServerWorldTimesIsEmpty() {
        WorldTimes worldTimesOfServer = db().query(WorldTimesQueries.fetchServerTotalWorldTimes(serverUUID()));
        assertEquals(new WorldTimes(), worldTimesOfServer);
    }

    @Test
    default void playerIsRegisteredToUsersTable() {
        assertFalse(db().query(PlayerFetchQueries.isPlayerRegistered(playerUUID)));
        db().executeTransaction(new PlayerRegisterTransaction(playerUUID, () -> 1000L, TestConstants.PLAYER_ONE_NAME));
        assertTrue(db().query(PlayerFetchQueries.isPlayerRegistered(playerUUID)));
        assertFalse(db().query(PlayerFetchQueries.isPlayerRegisteredOnServer(playerUUID, serverUUID())));
    }

    @Test
    default void playerIsRegisteredToBothTables() {
        assertFalse(db().query(PlayerFetchQueries.isPlayerRegistered(playerUUID)));
        assertFalse(db().query(PlayerFetchQueries.isPlayerRegisteredOnServer(playerUUID, serverUUID())));
        db().executeTransaction(new PlayerServerRegisterTransaction(playerUUID, () -> 1000L, TestConstants.PLAYER_ONE_NAME, serverUUID()));
        assertTrue(db().query(PlayerFetchQueries.isPlayerRegistered(playerUUID)));
        assertTrue(db().query(PlayerFetchQueries.isPlayerRegisteredOnServer(playerUUID, serverUUID())));
    }

    @Test
    default void testNewContainerForPlayer() throws NoSuchAlgorithmException {
        saveAllData();

        long start = System.nanoTime();

        PlayerContainer container = db().query(ContainerFetchQueries.fetchPlayerContainer(playerUUID));

        assertTrue(container.supports(PlayerKeys.UUID));
        assertTrue(container.supports(PlayerKeys.REGISTERED));
        assertTrue(container.supports(PlayerKeys.NAME));
        assertTrue(container.supports(PlayerKeys.KICK_COUNT));

        assertTrue(container.supports(PlayerKeys.GEO_INFO));
        assertTrue(container.supports(PlayerKeys.NICKNAMES));

        assertTrue(container.supports(PlayerKeys.PER_SERVER));

        assertTrue(container.supports(PlayerKeys.OPERATOR));
        assertTrue(container.supports(PlayerKeys.BANNED));

        assertTrue(container.supports(PlayerKeys.SESSIONS));
        assertTrue(container.supports(PlayerKeys.WORLD_TIMES));
        assertTrue(container.supports(PlayerKeys.LAST_SEEN));
        assertTrue(container.supports(PlayerKeys.DEATH_COUNT));
        assertTrue(container.supports(PlayerKeys.MOB_KILL_COUNT));
        assertTrue(container.supports(PlayerKeys.PLAYER_KILLS));
        assertTrue(container.supports(PlayerKeys.PLAYER_KILL_COUNT));

        assertFalse(container.supports(PlayerKeys.ACTIVE_SESSION));
        container.putRawData(PlayerKeys.ACTIVE_SESSION, new Session(playerUUID, serverUUID(), System.currentTimeMillis(), "TestWorld", "SURVIVAL"));
        assertTrue(container.supports(PlayerKeys.ACTIVE_SESSION));

        long end = System.nanoTime();

        assertFalse(end - start > TimeUnit.SECONDS.toNanos(1L), () -> "Took too long: " + ((end - start) / 1000000.0) + "ms");

        OptionalAssert.equals(playerUUID, container.getValue(PlayerKeys.UUID));
        OptionalAssert.equals(1000L, container.getValue(PlayerKeys.REGISTERED));
        OptionalAssert.equals(TestConstants.PLAYER_ONE_NAME, container.getValue(PlayerKeys.NAME));
        OptionalAssert.equals(1, container.getValue(PlayerKeys.KICK_COUNT));

        List<GeoInfo> expectedGeoInfo =
                Collections.singletonList(new GeoInfo("TestLoc", 223456789));
        OptionalAssert.equals(expectedGeoInfo, container.getValue(PlayerKeys.GEO_INFO));

        List<Nickname> expectedNicknames = Collections.singletonList(new Nickname("TestNick", -1, serverUUID()));
        OptionalAssert.equals(expectedNicknames, container.getValue(PlayerKeys.NICKNAMES));

        OptionalAssert.equals(false, container.getValue(PlayerKeys.OPERATOR));
        OptionalAssert.equals(false, container.getValue(PlayerKeys.BANNED));

        // TODO Test rest
    }

    @Test
    default void playerContainerSupportsAllPlayerKeys() throws NoSuchAlgorithmException, IllegalAccessException {
        saveAllData();

        PlayerContainer playerContainer = db().query(ContainerFetchQueries.fetchPlayerContainer(playerUUID));
        // Active sessions are added after fetching
        playerContainer.putRawData(PlayerKeys.ACTIVE_SESSION, RandomData.randomSession());

        List<String> unsupported = new ArrayList<>();
        List<Key> keys = FieldFetcher.getPublicStaticFields(PlayerKeys.class, Key.class);
        for (Key key : keys) {
            if (!playerContainer.supports(key)) {
                unsupported.add(key.getKeyName());
            }
        }

        assertTrue(unsupported.isEmpty(), () -> "Some keys are not supported by PlayerContainer: PlayerKeys." + unsupported.toString());
    }

    @Test
    default void uninstallingServerStopsItFromBeingReturnedInServerQuery() {
        db().executeTransaction(new SetServerAsUninstalledTransaction(serverUUID()));

        Optional<Server> found = db().query(ServerQueries.fetchServerMatchingIdentifier(serverUUID()));
        assertFalse(found.isPresent());
    }

    @Test
    default void uninstallingServerStopsItFromBeingReturnedInServersQuery() {
        db().executeTransaction(new SetServerAsUninstalledTransaction(serverUUID()));

        Collection<Server> found = db().query(ServerQueries.fetchPlanServerInformationCollection());
        assertTrue(found.isEmpty());
    }

    @Test
    default void serverContainerSupportsAllServerKeys() throws NoSuchAlgorithmException, IllegalAccessException {
        saveAllData();

        ServerContainer serverContainer = db().query(ContainerFetchQueries.fetchServerContainer(serverUUID()));

        List<String> unsupported = new ArrayList<>();
        List<Key> keys = FieldFetcher.getPublicStaticFields(ServerKeys.class, Key.class);
        for (Key key : keys) {
            if (!serverContainer.supports(key)) {
                unsupported.add(key.getKeyName());
            }
        }

        assertTrue(unsupported.isEmpty(), () -> "Some keys are not supported by ServerContainer: ServerKeys." + unsupported.toString());
    }

    @Test
    default void testGetMatchingNames() {
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
    default void testGetMatchingNickNames() {
        UUID uuid = UUID.randomUUID();
        String userName = RandomData.randomString(10);

        db().executeTransaction(new PlayerRegisterTransaction(uuid, () -> 0L, userName));
        db().executeTransaction(new PlayerRegisterTransaction(playerUUID, () -> 1L, "Not random"));

        String nickname = "2" + RandomData.randomString(10);
        db().executeTransaction(new NicknameStoreTransaction(uuid, new Nickname(nickname, System.currentTimeMillis(), serverUUID()), (u, name) -> false /* Not cached */));
        db().executeTransaction(new NicknameStoreTransaction(playerUUID, new Nickname("No nick", System.currentTimeMillis(), serverUUID()), (u, name) -> true /* Cached */));

        String searchFor = "2";

        List<String> result = db().query(UserIdentifierQueries.fetchMatchingPlayerNames(searchFor));

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(userName, result.get(0));
    }

    @Test
    default void configIsStoredInTheDatabase() {
        PlanConfig config = system().getConfigSystem().getConfig();

        db().executeTransaction(new StoreConfigTransaction(serverUUID(), config, System.currentTimeMillis()));

        Optional<Config> foundConfig = db().query(new NewerConfigQuery(serverUUID(), 0));
        assertTrue(foundConfig.isPresent());
        assertEquals(config, foundConfig.get());
    }

    @Test
    default void unchangedConfigDoesNotUpdateInDatabase() {
        configIsStoredInTheDatabase();
        long savedMs = System.currentTimeMillis();

        PlanConfig config = system().getConfigSystem().getConfig();

        db().executeTransaction(new StoreConfigTransaction(serverUUID(), config, System.currentTimeMillis()));

        assertFalse(db().query(new NewerConfigQuery(serverUUID(), savedMs)).isPresent());
    }

    @Test
    default void indexCreationWorksWithoutErrors() throws Exception {
        Transaction transaction = new CreateIndexTransaction();
        db().executeTransaction(transaction).get(); // get to ensure transaction is finished
        assertTrue(transaction.wasSuccessful());
    }

    @Test
    default void playerMaxPeakIsCorrect() {
        List<TPS> tpsData = RandomData.randomTPS();

        for (TPS tps : tpsData) {
            db().executeTransaction(new TPSStoreTransaction(serverUUID(), Collections.singletonList(tps)));
        }

        tpsData.sort(Comparator.comparingInt(TPS::getPlayers));
        int expected = tpsData.get(tpsData.size() - 1).getPlayers();
        int actual = db().query(TPSQueries.fetchAllTimePeakPlayerCount(serverUUID())).map(DateObj::getValue).orElse(-1);
        assertEquals(expected, actual, () -> "Wrong return value. " + tpsData.stream().map(TPS::getPlayers).collect(Collectors.toList()).toString());
    }

    @Test
    default void playerCountForServersIsCorrect() {
        Map<UUID, Integer> expected = Collections.singletonMap(serverUUID(), 1);
        saveUserOne();

        Map<UUID, Integer> result = db().query(ServerAggregateQueries.serverUserCounts());
        assertEquals(expected, result);
    }

    default void executeTransactions(Transaction... transactions) {
        for (Transaction transaction : transactions) {
            db().executeTransaction(transaction);
        }
    }

    @Test
    default void baseUsersQueryDoesNotReturnDuplicatePlayers() {
        db().executeTransaction(TestData.storeServers());
        executeTransactions(TestData.storePlayerOneData());
        executeTransactions(TestData.storePlayerTwoData());

        Collection<BaseUser> expected = new HashSet<>(Arrays.asList(TestData.getPlayerBaseUser(), TestData.getPlayer2BaseUser()));
        Collection<BaseUser> result = db().query(BaseUserQueries.fetchServerBaseUsers(TestConstants.SERVER_UUID));

        assertEquals(expected, result);

        result = db().query(BaseUserQueries.fetchServerBaseUsers(TestConstants.SERVER_TWO_UUID));

        assertEquals(expected, result);
    }

    @Test
    default void serverPlayerContainersQueryDoesNotReturnDuplicatePlayers() {
        db().executeTransaction(TestData.storeServers());
        executeTransactions(TestData.storePlayerOneData());
        executeTransactions(TestData.storePlayerTwoData());

        List<UUID> expected = Arrays.asList(playerUUID, player2UUID);
        Collections.sort(expected);

        Collection<UUID> result = db().query(new ServerPlayerContainersQuery(TestConstants.SERVER_UUID))
                .stream().map(player -> player.getUnsafe(PlayerKeys.UUID))
                .sorted()
                .collect(Collectors.toList());

        assertEquals(expected, result);
    }

    @Test
    default void sqlDateConversionSanityCheck() {
        Database db = db();

        long expected = System.currentTimeMillis() / 1000;

        Sql sql = db.getType().getSql();
        String testSQL = SELECT + sql.dateToEpochSecond(sql.epochSecondToDate(Long.toString(expected))) + " as ms";

        long result = db.query(new QueryAllStatement<Long>(testSQL) {
            @Override
            public Long processResults(ResultSet set) throws SQLException {
                return set.next() ? set.getLong("ms") : -1L;
            }
        });
        assertEquals(expected, result);
    }

    @Test
    default void sqlDateParsingSanityCheck() {
        Database db = db();

        long time = System.currentTimeMillis();

        Sql sql = db.getType().getSql();
        String testSQL = SELECT + sql.dateToDayStamp(sql.epochSecondToDate(Long.toString(time / 1000))) + " as date";

        System.out.println(testSQL);
        String expected = system().getDeliveryUtilities().getFormatters().iso8601NoClockLong().apply(time);
        String result = db.query(new QueryAllStatement<String>(testSQL) {
            @Override
            public String processResults(ResultSet set) throws SQLException {
                return set.next() ? set.getString("date") : null;
            }
        });
        assertEquals(expected, result);
    }

    @Test
    default void extensionPlayerValuesAreStored() {
        ExtensionServiceImplementation extensionService = (ExtensionServiceImplementation) system().getExtensionService();

        extensionService.register(new PlayerExtension());
        extensionService.updatePlayerValues(playerUUID, TestConstants.PLAYER_ONE_NAME, CallEvents.MANUAL);

        Map<UUID, List<ExtensionData>> playerDataByServerUUID = db().query(new ExtensionPlayerDataQuery(playerUUID));
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
    }

    @Test
    default void extensionPlayerValuesCanBeQueriedAsTableData() {
        extensionPlayerValuesAreStored();
        sessionsAreStoredWithAllData(); // This query requires sessions for a last seen date

        // Store a second session to check against issue https://github.com/plan-player-analytics/Plan/issues/1039
        Session session = new Session(playerUUID, serverUUID(), 32345L, worlds[0], "SURVIVAL");
        session.endSession(42345L);
        session.setWorldTimes(createWorldTimes());
        session.setPlayerKills(createKills());

        execute(DataStoreQueries.storeSession(session));

        Map<UUID, ExtensionTabData> result = db().query(new ExtensionServerPlayerDataTableQuery(serverUUID(), 50));
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
        ExtensionServiceImplementation extensionService = (ExtensionServiceImplementation) system().getExtensionService();

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
        ExtensionServiceImplementation extensionService = (ExtensionServiceImplementation) system().getExtensionService();

        extensionService.register(new PlayerExtension());
        extensionService.updatePlayerValues(playerUUID, TestConstants.PLAYER_ONE_NAME, CallEvents.MANUAL);

        List<ExtensionData> ofServer = db().query(new ExtensionServerDataQuery(serverUUID()));
        assertFalse(ofServer.isEmpty());

        ExtensionData extensionData = ofServer.get(0);
        List<ExtensionTabData> tabs = extensionData.getTabs();
        assertEquals(1, tabs.size()); // No tab defined, should contain 1 tab
        ExtensionTabData tabData = tabs.get(0);

        System.out.println(tabData.getValueOrder());

        OptionalAssert.equals("0.0", tabData.getPercentage("boolVal_aggregate").map(data -> data.getFormattedValue(Objects::toString)));
        OptionalAssert.equals("0.5", tabData.getPercentage("percentageVal_avg").map(data -> data.getFormattedValue(Objects::toString)));
        OptionalAssert.equals("0.5", tabData.getDouble("doubleVal_avg").map(data -> data.getFormattedValue(Objects::toString)));
        OptionalAssert.equals("0.5", tabData.getDouble("doubleVal_total").map(data -> data.getFormattedValue(Objects::toString)));
        OptionalAssert.equals("5", tabData.getNumber("value_avg").map(data -> data.getFormattedValue(Objects::toString)));
        OptionalAssert.equals("5", tabData.getNumber("value_total").map(data -> data.getFormattedValue(Objects::toString)));

        List<ExtensionTableData> tableData = tabData.getTableData();
        assertEquals(1, tableData.size());
        TableContainer table = tableData.get(0).getHtmlTable();
        assertEquals("<tbody><tr><td>Group</td><td>1</td></tr></tbody>", table.parseBody());
    }

    @Test
    default void unsatisfiedPlayerConditionalResultsAreCleaned() {
        ExtensionServiceImplementation extensionService = (ExtensionServiceImplementation) system().getExtensionService();

        extensionService.register(new ConditionalExtension());

        ConditionalExtension.condition = true;
        extensionService.updatePlayerValues(playerUUID, TestConstants.PLAYER_ONE_NAME, CallEvents.MANUAL);

        // Check that the wanted data exists
        checkThatPlayerDataExists(ConditionalExtension.condition);

        // Reverse condition
        ConditionalExtension.condition = false;
        extensionService.updatePlayerValues(playerUUID, TestConstants.PLAYER_ONE_NAME, CallEvents.MANUAL);

        db().executeTransaction(new RemoveUnsatisfiedConditionalPlayerResultsTransaction());

        // Check that the wanted data exists
        checkThatPlayerDataExists(ConditionalExtension.condition);

        // Reverse condition
        ConditionalExtension.condition = false;
        extensionService.updatePlayerValues(playerUUID, TestConstants.PLAYER_ONE_NAME, CallEvents.MANUAL);

        db().executeTransaction(new RemoveUnsatisfiedConditionalPlayerResultsTransaction());

        // Check that the wanted data exists
        checkThatPlayerDataExists(ConditionalExtension.condition);
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
        ExtensionServiceImplementation extensionService = (ExtensionServiceImplementation) system().getExtensionService();

        ConditionalExtension.condition = true;
        extensionService.register(new ConditionalExtension());
        extensionService.updateServerValues(CallEvents.MANUAL);

        // Check that the wanted data exists
        checkThatServerDataExists(ConditionalExtension.condition);

        // Reverse condition
        ConditionalExtension.condition = false;
        extensionService.updateServerValues(CallEvents.MANUAL);

        db().executeTransaction(new RemoveUnsatisfiedConditionalServerResultsTransaction());

        // Check that the wanted data exists
        checkThatServerDataExists(ConditionalExtension.condition);

        // Reverse condition
        ConditionalExtension.condition = false;
        extensionService.updatePlayerValues(playerUUID, TestConstants.PLAYER_ONE_NAME, CallEvents.MANUAL);

        db().executeTransaction(new RemoveUnsatisfiedConditionalServerResultsTransaction());

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
        ExtensionServiceImplementation extensionService = (ExtensionServiceImplementation) system().getExtensionService();

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

        TableContainer expected = new TableContainer(
                "<i class=\" fa fa-gavel\"></i> first",
                "<i class=\" fa fa-what\"></i> second",
                "<i class=\" fa fa-question\"></i> third"
        );
        expected.setColor("amber");
        expected.addRow("value", 3, 0.5, 400L);

        assertEquals(expected.parseHtml(), table.getHtmlTable().parseHtml());
    }

    @Test
    default void extensionPlayerTableValuesAreInserted() {
        ExtensionServiceImplementation extensionService = (ExtensionServiceImplementation) system().getExtensionService();

        extensionService.register(new TableExtension());
        extensionService.updatePlayerValues(playerUUID, TestConstants.PLAYER_ONE_NAME, CallEvents.MANUAL);
        extensionService.updatePlayerValues(playerUUID, TestConstants.PLAYER_ONE_NAME, CallEvents.MANUAL);

        Map<UUID, List<ExtensionData>> ofPlayer = db().query(new ExtensionPlayerDataQuery(playerUUID));
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

        TableContainer expected = new TableContainer(
                "<i class=\" fa fa-gavel\"></i> first",
                "<i class=\" fa fa-what\"></i> second",
                "<i class=\" fa fa-question\"></i> third"
        );
        expected.setColor("amber");
        expected.addRow("value", 3, 0.5, 400L);

        assertEquals(expected.parseHtml(), table.getHtmlTable().parseHtml());
    }

    @Test
    default void activeTurnedInactiveQueryHasAllParametersSet() {
        Integer result = db().query(ActivityIndexQueries.countRegularPlayersTurnedInactive(
                0, System.currentTimeMillis(), serverUUID(),
                TimeUnit.HOURS.toMillis(2L)
        ));
        assertNotNull(result);
    }

    @Test
    default void serverTablePlayersQueryQueriesAtLeastOnePlayer() {
        sessionsAreStoredWithAllData();

        List<TablePlayer> result = db().query(new ServerTablePlayersQuery(serverUUID(), System.currentTimeMillis(), 10L, 1));
        assertNotEquals(Collections.emptyList(), result);
    }

    @Test
    default void networkTablePlayersQueryQueriesAtLeastOnePlayer() {
        sessionsAreStoredWithAllData();

        List<TablePlayer> result = db().query(new NetworkTablePlayersQuery(System.currentTimeMillis(), 10L, 1));
        assertNotEquals(Collections.emptyList(), result);
    }

    @Test
    default void kdrCastAsDoubleDoesNotCauseExceptions() {
        sessionsAreStoredWithAllData();
        db().executeTransaction(new PlayerServerRegisterTransaction(player2UUID, () -> 123456789L, "Test", serverUUID()));

        Long killCount = db().query(KillQueries.playerKillCount(0L, System.currentTimeMillis(), serverUUID()));
        assertEquals(2, killCount); // Ensure the kills were saved

        Double result = db().query(KillQueries.averageKDR(0L, System.currentTimeMillis(), serverUUID()));
        assertEquals(1.0, result, 0.1);
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
}
