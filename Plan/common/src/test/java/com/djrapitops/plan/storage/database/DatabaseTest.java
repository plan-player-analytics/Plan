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
import com.djrapitops.plan.delivery.domain.mutators.ActivityIndex;
import com.djrapitops.plan.delivery.domain.mutators.SessionsMutator;
import com.djrapitops.plan.gathering.domain.*;
import com.djrapitops.plan.identification.Server;
import com.djrapitops.plan.query.QuerySvc;
import com.djrapitops.plan.settings.config.Config;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.storage.database.queries.*;
import com.djrapitops.plan.storage.database.queries.analysis.ActivityIndexQueries;
import com.djrapitops.plan.storage.database.queries.containers.ContainerFetchQueries;
import com.djrapitops.plan.storage.database.queries.containers.ServerPlayerContainersQuery;
import com.djrapitops.plan.storage.database.queries.objects.*;
import com.djrapitops.plan.storage.database.sql.building.Sql;
import com.djrapitops.plan.storage.database.sql.tables.UserInfoTable;
import com.djrapitops.plan.storage.database.transactions.BackupCopyTransaction;
import com.djrapitops.plan.storage.database.transactions.StoreConfigTransaction;
import com.djrapitops.plan.storage.database.transactions.StoreServerInformationTransaction;
import com.djrapitops.plan.storage.database.transactions.Transaction;
import com.djrapitops.plan.storage.database.transactions.commands.*;
import com.djrapitops.plan.storage.database.transactions.events.*;
import com.djrapitops.plan.storage.database.transactions.init.CreateIndexTransaction;
import com.djrapitops.plan.storage.database.transactions.init.RemoveDuplicateUserInfoTransaction;
import com.djrapitops.plan.storage.database.transactions.patches.RegisterDateMinimizationPatch;
import com.djrapitops.plan.storage.upkeep.DBCleanTask;
import com.djrapitops.plan.utilities.comparators.DateHolderRecentComparator;
import com.djrapitops.plan.utilities.java.Lists;
import com.djrapitops.plugin.logging.console.TestPluginLogger;
import com.djrapitops.plugin.logging.error.ConsoleErrorLogger;
import com.google.common.util.concurrent.MoreExecutors;
import org.junit.jupiter.api.Test;
import utilities.*;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.nio.file.Files;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.djrapitops.plan.storage.database.sql.building.Sql.SELECT;
import static com.djrapitops.plan.storage.database.sql.building.Sql.WHERE;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Contains common Database Tests.
 *
 * @author Rsl1122
 */
public interface DatabaseTest extends DatabaseTestPreparer {

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

        List<GeoInfo> result = db().query(GeoInfoQueries.fetchAllGeoInformation()).get(playerUUID);
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

        Collection<String> result = db().query(LargeFetchQueries.fetchAllWorldNames()).get(serverUUID());
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
    default void testRemovalEverything() {
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

    default <T extends Map<?, ?>> void assertQueryIsEmpty(Database database, Query<T> query) {
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
                new QuerySvc(system().getDatabaseSystem(), system().getServerInfo(), logger, errorHandler),
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
    default void testNewContainerForPlayer() {
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
    default void playerContainerSupportsAllPlayerKeys() throws IllegalAccessException {
        saveAllData();

        PlayerContainer playerContainer = db().query(ContainerFetchQueries.fetchPlayerContainer(playerUUID));
        // Active sessions are added after fetching
        playerContainer.putRawData(PlayerKeys.ACTIVE_SESSION, RandomData.randomSession());

        List<String> unsupported = new ArrayList<>();
        List<Key> keys = FieldFetcher.getPublicStaticFields(PlayerKeys.class, Key.class);
        for (Key<?> key : keys) {
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
    default void serverContainerSupportsAllServerKeys() throws IllegalAccessException {
        saveAllData();

        ServerContainer serverContainer = db().query(ContainerFetchQueries.fetchServerContainer(serverUUID()));

        List<String> unsupported = new ArrayList<>();
        List<Key> keys = FieldFetcher.getPublicStaticFields(ServerKeys.class, Key.class);
        for (Key<?> key : keys) {
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
            db().executeTransaction(new TPSStoreTransaction(serverUUID(), tps));
        }

        tpsData.sort(Comparator.comparingInt(TPS::getPlayers));
        int expected = tpsData.get(tpsData.size() - 1).getPlayers();
        int actual = db().query(TPSQueries.fetchAllTimePeakPlayerCount(serverUUID())).map(DateObj::getValue).orElse(-1);
        assertEquals(expected, actual, () -> "Wrong return value. " + Lists.map(tpsData, TPS::getPlayers).toString());
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
        int offset = TimeZone.getDefault().getOffset(time);

        Sql sql = db.getType().getSql();
        String testSQL = SELECT + sql.dateToDayStamp(sql.epochSecondToDate(Long.toString((time + offset) / 1000))) + " as date";

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
    default void activityIndexCoalesceSanityCheck() {
        sessionsAreStoredWithAllData();
        Map<String, Integer> groupings = db().query(
                ActivityIndexQueries.fetchActivityIndexGroupingsOn(System.currentTimeMillis(), serverUUID(), TimeUnit.HOURS.toMillis(2L))
        );
        Map<String, Integer> expected = Collections.singletonMap(ActivityIndex.getDefaultGroups()[4], 1); // Inactive
        assertEquals(expected, groupings);
    }

    @Test
    default void serverGeolocationsAreCountedAppropriately() {
        UUID firstUuid = UUID.randomUUID();
        UUID secondUuid = UUID.randomUUID();
        UUID thirdUuid = UUID.randomUUID();
        UUID fourthUuid = UUID.randomUUID();
        UUID fifthUuid = UUID.randomUUID();
        UUID sixthUuid = UUID.randomUUID();

        Database database = db();

        database.executeTransaction(new PlayerServerRegisterTransaction(firstUuid, () -> 0L, "", serverUUID()));
        database.executeTransaction(new PlayerServerRegisterTransaction(secondUuid, () -> 0L, "", serverUUID()));
        database.executeTransaction(new PlayerServerRegisterTransaction(thirdUuid, () -> 0L, "", serverUUID()));
        database.executeTransaction(new PlayerServerRegisterTransaction(fourthUuid, () -> 0L, "", serverUUID()));
        database.executeTransaction(new PlayerServerRegisterTransaction(fifthUuid, () -> 0L, "", serverUUID()));
        database.executeTransaction(new PlayerServerRegisterTransaction(sixthUuid, () -> 0L, "", serverUUID()));

        saveGeoInfo(firstUuid, new GeoInfo("Norway", 0));
        saveGeoInfo(firstUuid, new GeoInfo("Finland", 5));
        saveGeoInfo(secondUuid, new GeoInfo("Sweden", 0));
        saveGeoInfo(thirdUuid, new GeoInfo("Denmark", 0));
        saveGeoInfo(fourthUuid, new GeoInfo("Denmark", 0));
        saveGeoInfo(fifthUuid, new GeoInfo("Not Known", 0));
        saveGeoInfo(sixthUuid, new GeoInfo("Local Machine", 0));

        Map<String, Integer> got = database.query(GeoInfoQueries.serverGeolocationCounts(serverUUID()));

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
        UUID firstUuid = UUID.randomUUID();
        UUID secondUuid = UUID.randomUUID();
        UUID thirdUuid = UUID.randomUUID();
        UUID fourthUuid = UUID.randomUUID();
        UUID fifthUuid = UUID.randomUUID();
        UUID sixthUuid = UUID.randomUUID();

        Database db = db();
        db.executeTransaction(new PlayerRegisterTransaction(firstUuid, () -> 0L, ""));
        db.executeTransaction(new PlayerRegisterTransaction(secondUuid, () -> 0L, ""));
        db.executeTransaction(new PlayerRegisterTransaction(thirdUuid, () -> 0L, ""));

        saveGeoInfo(firstUuid, new GeoInfo("Norway", 0));
        saveGeoInfo(firstUuid, new GeoInfo("Finland", 5));
        saveGeoInfo(secondUuid, new GeoInfo("Sweden", 0));
        saveGeoInfo(thirdUuid, new GeoInfo("Denmark", 0));
        saveGeoInfo(fourthUuid, new GeoInfo("Denmark", 0));
        saveGeoInfo(fifthUuid, new GeoInfo("Not Known", 0));
        saveGeoInfo(sixthUuid, new GeoInfo("Local Machine", 0));

        Map<String, Integer> got = db.query(GeoInfoQueries.networkGeolocationCounts());

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
    default void bungeeInformationIsStored() {
        Optional<Server> bungeeInfo = db().query(ServerQueries.fetchProxyServerInformation());
        assertFalse(bungeeInfo.isPresent());

        UUID bungeeUUID = UUID.randomUUID();
        Server bungeeCord = new Server(-1, bungeeUUID, "BungeeCord", "Random:1234", 20);
        db().executeTransaction(new StoreServerInformationTransaction(bungeeCord));

        commitTest();

        bungeeCord.setId(2);

        bungeeInfo = db().query(ServerQueries.fetchProxyServerInformation());
        assertTrue(bungeeInfo.isPresent());
        assertEquals(bungeeCord, bungeeInfo.get());

        Optional<Server> found = db().query(ServerQueries.fetchServerMatchingIdentifier(bungeeUUID));
        OptionalAssert.equals(bungeeCord.getWebAddress(), found.map(Server::getWebAddress));
    }

    @Test
    default void proxyIsDetected() {
        bungeeInformationIsStored();

        Map<UUID, Server> serverInformation = db().query(ServerQueries.fetchPlanServerInformation());

        assertEquals(1, serverInformation.values().stream().filter(Server::isNotProxy).count());
        assertEquals(1, serverInformation.values().stream().filter(Server::isProxy).count());
    }

    @Test
    default void pingIsGroupedByGeolocationAppropriately() {
        UUID firstUuid = UUID.randomUUID();
        UUID secondUuid = UUID.randomUUID();
        UUID thirdUuid = UUID.randomUUID();
        UUID fourthUuid = UUID.randomUUID();
        UUID fifthUuid = UUID.randomUUID();
        UUID sixthUuid = UUID.randomUUID();

        Database database = db();

        database.executeTransaction(new PlayerServerRegisterTransaction(firstUuid, () -> 0L, "", serverUUID()));
        database.executeTransaction(new PlayerServerRegisterTransaction(secondUuid, () -> 0L, "", serverUUID()));
        database.executeTransaction(new PlayerServerRegisterTransaction(thirdUuid, () -> 0L, "", serverUUID()));
        database.executeTransaction(new PlayerServerRegisterTransaction(fourthUuid, () -> 0L, "", serverUUID()));
        database.executeTransaction(new PlayerServerRegisterTransaction(fifthUuid, () -> 0L, "", serverUUID()));
        database.executeTransaction(new PlayerServerRegisterTransaction(sixthUuid, () -> 0L, "", serverUUID()));

        saveGeoInfo(firstUuid, new GeoInfo("Norway", 0));
        saveGeoInfo(firstUuid, new GeoInfo("Finland", 5));
        saveGeoInfo(secondUuid, new GeoInfo("Sweden", 0));
        saveGeoInfo(thirdUuid, new GeoInfo("Denmark", 0));
        saveGeoInfo(fourthUuid, new GeoInfo("Denmark", 0));
        saveGeoInfo(fifthUuid, new GeoInfo("Not Known", 0));
        saveGeoInfo(sixthUuid, new GeoInfo("Local Machine", 0));

        long time = System.currentTimeMillis();
        List<DateObj<Integer>> ping = Collections.singletonList(new DateObj<>(time, 5));
        database.executeTransaction(new PingStoreTransaction(firstUuid, serverUUID(), ping));
        database.executeTransaction(new PingStoreTransaction(secondUuid, serverUUID(), ping));
        database.executeTransaction(new PingStoreTransaction(thirdUuid, serverUUID(), ping));
        database.executeTransaction(new PingStoreTransaction(fourthUuid, serverUUID(), ping));
        database.executeTransaction(new PingStoreTransaction(fifthUuid, serverUUID(), ping));
        database.executeTransaction(new PingStoreTransaction(sixthUuid, serverUUID(), ping));

        Map<String, Ping> got = database.query(PingQueries.fetchPingDataOfServerByGeolocation(serverUUID()));

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
    default void activityIndexCalculationsMatch() {
        sessionsAreStoredWithAllData();

        long date = System.currentTimeMillis();
        long playtimeThreshold = TimeUnit.HOURS.toMillis(5L);
        List<Session> sessions = db().query(SessionQueries.fetchSessionsOfPlayer(playerUUID))
                .values().stream().flatMap(Collection::stream).collect(Collectors.toList());

        ActivityIndex javaCalculation = new ActivityIndex(sessions, date, playtimeThreshold);

        List<TablePlayer> players = db().query(new ServerTablePlayersQuery(serverUUID(), date, playtimeThreshold, 5));
        Optional<TablePlayer> found = players.stream().filter(tp -> playerUUID.equals(tp.getPlayerUUID())).findFirst();
        assertTrue(found.isPresent());
        Optional<ActivityIndex> currentActivityIndex = found.get().getCurrentActivityIndex();
        assertTrue(currentActivityIndex.isPresent());

        assertEquals(javaCalculation.getValue(), currentActivityIndex.get().getValue(), 0.001);
    }

    @Test
    default void networkActivityIndexCalculationsMatch() {
        sessionsAreStoredWithAllData();

        long date = System.currentTimeMillis();
        long playtimeThreshold = TimeUnit.HOURS.toMillis(5L);
        List<Session> sessions = db().query(SessionQueries.fetchSessionsOfPlayer(playerUUID))
                .values().stream().flatMap(Collection::stream).collect(Collectors.toList());

        ActivityIndex javaCalculation = new ActivityIndex(sessions, date, playtimeThreshold);

        List<TablePlayer> players = db().query(new NetworkTablePlayersQuery(date, playtimeThreshold, 5));
        Optional<TablePlayer> found = players.stream().filter(tp -> playerUUID.equals(tp.getPlayerUUID())).findFirst();
        assertTrue(found.isPresent());
        Optional<ActivityIndex> currentActivityIndex = found.get().getCurrentActivityIndex();
        assertTrue(currentActivityIndex.isPresent());

        assertEquals(javaCalculation.getValue(), currentActivityIndex.get().getValue(), 0.001);
    }

    @Test
    default void registerDateIsMinimized() {
        executeTransactions(
                new PlayerServerRegisterTransaction(playerUUID, () -> 1000, TestConstants.PLAYER_ONE_NAME, serverUUID())
                , new Transaction() {
                    @Override
                    protected void performOperations() {
                        execute("UPDATE " + UserInfoTable.TABLE_NAME + " SET " + UserInfoTable.REGISTERED + "=0" + WHERE + UserInfoTable.USER_UUID + "='" + playerUUID + "'");
                    }
                }
        );

        // Check test assumptions
        Map<UUID, Long> registerDates = db().query(UserInfoQueries.fetchRegisterDates(0L, System.currentTimeMillis(), serverUUID()));
        assertEquals(0L, registerDates.get(playerUUID));
        Optional<BaseUser> baseUser = db().query(BaseUserQueries.fetchBaseUserOfPlayer(playerUUID));
        assertEquals(1000L, baseUser.isPresent() ? baseUser.get().getRegistered() : null);

        RegisterDateMinimizationPatch testedPatch = new RegisterDateMinimizationPatch();
        executeTransactions(testedPatch);

        // Test expected result
        Optional<BaseUser> updatedBaseUser = db().query(BaseUserQueries.fetchBaseUserOfPlayer(playerUUID));
        assertEquals(0L, updatedBaseUser.isPresent() ? updatedBaseUser.get().getRegistered() : null);
        assertTrue(testedPatch.hasBeenApplied());
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
}
