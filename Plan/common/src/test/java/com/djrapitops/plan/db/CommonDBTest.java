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
package com.djrapitops.plan.db;

import com.djrapitops.plan.api.exceptions.database.DBInitException;
import com.djrapitops.plan.data.WebUser;
import com.djrapitops.plan.data.container.*;
import com.djrapitops.plan.data.store.Key;
import com.djrapitops.plan.data.store.containers.AnalysisContainer;
import com.djrapitops.plan.data.store.containers.NetworkContainer;
import com.djrapitops.plan.data.store.containers.PlayerContainer;
import com.djrapitops.plan.data.store.containers.ServerContainer;
import com.djrapitops.plan.data.store.keys.*;
import com.djrapitops.plan.data.store.objects.Nickname;
import com.djrapitops.plan.data.time.GMTimes;
import com.djrapitops.plan.data.time.WorldTimes;
import com.djrapitops.plan.db.access.Executable;
import com.djrapitops.plan.db.access.Query;
import com.djrapitops.plan.db.access.queries.*;
import com.djrapitops.plan.db.access.queries.containers.ContainerFetchQueries;
import com.djrapitops.plan.db.access.transactions.*;
import com.djrapitops.plan.db.access.transactions.events.CommandStoreTransaction;
import com.djrapitops.plan.db.access.transactions.events.GeoInfoStoreTransaction;
import com.djrapitops.plan.db.access.transactions.events.PlayerRegisterTransaction;
import com.djrapitops.plan.db.access.transactions.events.WorldNameStoreTransaction;
import com.djrapitops.plan.db.patches.Patch;
import com.djrapitops.plan.db.sql.tables.*;
import com.djrapitops.plan.system.PlanSystem;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.info.server.Server;
import com.djrapitops.plan.system.settings.config.Config;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.paths.WebserverSettings;
import com.djrapitops.plan.utilities.SHA256Hash;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.Timeout;
import rules.ComponentMocker;
import rules.PluginComponentMocker;
import utilities.FieldFetcher;
import utilities.OptionalAssert;
import utilities.RandomData;
import utilities.TestConstants;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Contains all common Database Tests for all Database Types
 *
 * @author Rsl1122 (Refactored into this class by Fuzzlemann)
 */
public abstract class CommonDBTest {

    private static final int TEST_PORT_NUMBER = RandomData.randomInt(9005, 9500);

    @ClassRule
    public static TemporaryFolder temporaryFolder = new TemporaryFolder();
    @ClassRule
    public static ComponentMocker component = new PluginComponentMocker(temporaryFolder);

    public static UUID serverUUID;

    public static DBSystem dbSystem;
    public static SQLDB db;
    public static PlanSystem system;

    public final String[] worlds = new String[]{"TestWorld", "TestWorld2"};
    public final UUID playerUUID = TestConstants.PLAYER_ONE_UUID;
    public final UUID player2UUID = TestConstants.PLAYER_TWO_UUID;

    @Rule
    public Timeout globalTimeout = Timeout.seconds(5);

    static void handleSetup(String dbName) throws Exception {
        system = component.getPlanSystem();
        system.getConfigSystem().getConfig().set(WebserverSettings.PORT, TEST_PORT_NUMBER);
        system.enable();

        dbSystem = system.getDatabaseSystem();
        db = (SQLDB) dbSystem.getActiveDatabaseByName(dbName);

        db.init();

        serverUUID = system.getServerInfo().getServerUUID();
    }

    @AfterClass
    public static void tearDownClass() {
        if (system != null) system.disable();
    }

    @Before
    public void setUp() throws DBInitException {
        new Patch(db) {
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
        }.apply();
        db.executeTransaction(new CreateTablesTransaction());
        db.executeTransaction(new RemoveEverythingTransaction());
        ServerTable serverTable = db.getServerTable();
        serverTable.saveCurrentServerInfo(new Server(-1, serverUUID, "ServerName", "", 20));
        assertEquals(serverUUID, db.getServerUUIDSupplier().get());
    }

    private void execute(Executable executable) {
        db.executeTransaction(new Transaction() {
            @Override
            protected void performOperations() {
                execute(executable);
            }
        });
    }

    public void commitTest() throws DBInitException {
        db.close();
        db.init();
    }

    @Test
    public void testNoExceptionWhenCommitEmpty() throws Exception {
        db.commit(db.getConnection());
        db.commit(db.getConnection());
        db.commit(db.getConnection());
    }

    @Test
    public void testSaveCommandUse() throws DBInitException {
        Map<String, Integer> expected = new HashMap<>();

        expected.put("plan", 1);
        expected.put("tp", 4);
        expected.put("pla", 7);
        expected.put("help", 21);

        useCommand("plan");
        useCommand("tp", 4);
        useCommand("pla", 7);
        useCommand("help", 21);
        useCommand("roiergbnougbierubieugbeigubeigubgierbgeugeg", 3);

        commitTest();

        Map<String, Integer> commandUse = db.query(ServerAggregateQueries.commandUsageCounts(serverUUID));
        assertEquals(expected, commandUse);
    }

    @Test
    public void commandUsageSavingDoesNotCreateNewEntriesForOldCommands() throws DBInitException {
        Map<String, Integer> expected = new HashMap<>();

        expected.put("plan", 1);
        expected.put("test", 3);
        expected.put("tp", 6);
        expected.put("pla", 7);
        expected.put("help", 21);

        testSaveCommandUse();

        useCommand("test", 3);
        useCommand("tp", 2);

        Map<String, Integer> commandUse = db.query(ServerAggregateQueries.commandUsageCounts(serverUUID));
        assertEquals(expected, commandUse);
    }

    private void useCommand(String commandName) {
        db.executeTransaction(new CommandStoreTransaction(serverUUID, commandName));
    }

    private void useCommand(String commandName, int times) {
        for (int i = 0; i < times; i++) {
            useCommand(commandName);
        }
    }

    @Test
    public void testTPSSaving() throws Exception {
        TPSTable tpsTable = db.getTpsTable();
        Random r = new Random();

        List<TPS> expected = new ArrayList<>();

        for (int i = 0; i < RandomData.randomInt(1, 5); i++) {
            expected.add(new TPS(r.nextLong(), r.nextDouble(), r.nextInt(100000000), r.nextDouble(), r.nextLong(), r.nextInt(), r.nextInt(), r.nextLong()));
        }

        for (TPS tps : expected) {
            tpsTable.insertTPS(tps);
        }

        commitTest();

        assertEquals(expected, tpsTable.getTPSData(serverUUID));
    }

    private void saveUserOne() {
        db.executeTransaction(new PlayerRegisterTransaction(playerUUID, () -> 123456789L, "Test"));
        db.getUsersTable().kicked(playerUUID);
    }

    private void saveUserTwo() {
        db.executeTransaction(new PlayerRegisterTransaction(player2UUID, () -> 123456789L, "Test"));
    }

    @Test
    public void geoInformationIsStored() throws DBInitException {
        saveUserOne();

        String expectedIP = "1.2.3.4";
        String expectedGeoLoc = "TestLocation";
        long time = System.currentTimeMillis();

        GeoInfo expected = new GeoInfo(expectedIP, expectedGeoLoc, time, "3");
        saveGeoInfo(playerUUID, expected);
        commitTest();

        List<GeoInfo> geolocations = db.query(LargeFetchQueries.fetchAllGeoInformation()).getOrDefault(playerUUID, new ArrayList<>());
        assertEquals(1, geolocations.size());
        assertEquals(expected, geolocations.get(0));
    }

    @Test
    public void testNicknamesTable() throws DBInitException {
        saveUserOne();
        NicknamesTable nickTable = db.getNicknamesTable();

        Nickname expected = new Nickname("TestNickname", System.currentTimeMillis(), serverUUID);
        nickTable.saveUserName(playerUUID, expected);
        nickTable.saveUserName(playerUUID, expected);
        commitTest();

        List<Nickname> nicknames = nickTable.getNicknameInformation(playerUUID);
        assertEquals(1, nicknames.size());
        assertEquals(expected, nicknames.get(0));
    }

    @Test
    public void webUserIsRegistered() throws DBInitException {
        WebUser expected = new WebUser("Test", "RandomGarbageBlah", 0);
        db.getSecurityTable().addNewUser(expected);
        commitTest();

        Optional<WebUser> found = db.query(OptionalFetchQueries.fetchWebUser("Test"));
        assertTrue(found.isPresent());
        assertEquals(expected, found.get());
    }

    @Test
    public void multipleWebUsersAreFetchedAppropriately() throws DBInitException {
        webUserIsRegistered();
        assertEquals(1, db.query(LargeFetchQueries.fetchAllPlanWebUsers()).size());
    }

    @Test
    public void webUserIsRemoved() throws DBInitException {
        webUserIsRegistered();
        db.getSecurityTable().removeUser("Test");
        assertFalse(db.query(OptionalFetchQueries.fetchWebUser("Test")).isPresent());
    }

    @Test
    public void worldNamesAreStored() throws DBInitException {
        String[] expected = {"Test", "Test2", "Test3"};
        saveWorlds(expected);

        commitTest();

        Collection<String> result = db.query(LargeFetchQueries.fetchAllWorldNames()).getOrDefault(serverUUID, new HashSet<>());
        assertEquals(new HashSet<>(Arrays.asList(expected)), result);
    }

    private void saveWorld(String worldName) {
        db.executeTransaction(new WorldNameStoreTransaction(serverUUID, worldName));
    }

    private void saveWorlds(String... worldNames) {
        for (String worldName : worldNames) {
            saveWorld(worldName);
        }
    }

    private void saveTwoWorlds() {
        saveWorlds(worlds);
    }

    private WorldTimes createWorldTimes() {
        Map<String, GMTimes> times = new HashMap<>();
        Map<String, Long> gm = new HashMap<>();
        String[] gms = GMTimes.getGMKeyArray();
        gm.put(gms[0], 1000L);
        gm.put(gms[1], 2000L);
        gm.put(gms[2], 3000L);
        gm.put(gms[3], 4000L);

        String worldName = worlds[0];
        times.put(worldName, new GMTimes(gm));
        db.executeTransaction(new WorldNameStoreTransaction(serverUUID, worldName));

        return new WorldTimes(times);
    }

    private List<PlayerKill> createKills() {
        List<PlayerKill> kills = new ArrayList<>();
        kills.add(new PlayerKill(TestConstants.PLAYER_TWO_UUID, "Iron Sword", 4321L));
        kills.add(new PlayerKill(TestConstants.PLAYER_TWO_UUID, "Gold Sword", 5321L));
        return kills;
    }

    @Test
    public void testSessionPlaytimeSaving() throws DBInitException {
        saveTwoWorlds();
        saveUserOne();
        saveUserTwo();
        Session session = new Session(playerUUID, serverUUID, 12345L, "", "");
        session.endSession(22345L);
        session.setWorldTimes(createWorldTimes());
        session.setPlayerKills(createKills());

        long expectedLength = 10000L;
        assertEquals(expectedLength, session.getLength());
        assertEquals(expectedLength, session.getUnsafe(SessionKeys.WORLD_TIMES).getTotal());

        SessionsTable sessionsTable = db.getSessionsTable();
        execute(DataStoreQueries.storeSession(session));

        commitTest();

        assertEquals(expectedLength, sessionsTable.getPlaytime(playerUUID));
        assertEquals(0L, sessionsTable.getPlaytime(playerUUID, 30000L));

        long playtimeOfServer = sessionsTable.getPlaytimeOfServer(serverUUID);
        assertEquals(expectedLength, playtimeOfServer);
        assertEquals(0L, sessionsTable.getPlaytimeOfServer(serverUUID, 30000L));

        assertEquals(1, sessionsTable.getSessionCount(playerUUID));
        assertEquals(0, sessionsTable.getSessionCount(playerUUID, 30000L));
    }

    @Test
    public void testSessionSaving() throws DBInitException {
        saveUserOne();
        saveUserTwo();

        Session session = new Session(playerUUID, serverUUID, 12345L, "", "");
        session.endSession(22345L);
        session.setWorldTimes(createWorldTimes());
        session.setPlayerKills(createKills());

        SessionsTable sessionsTable = db.getSessionsTable();
        execute(DataStoreQueries.storeSession(session));

        commitTest();

        Map<UUID, List<Session>> sessions = sessionsTable.getSessions(playerUUID);
        List<Session> savedSessions = sessions.get(serverUUID);

        assertNotNull(savedSessions);
        assertEquals(1, savedSessions.size());
        assertNull(sessions.get(UUID.randomUUID()));

        assertEquals(session, savedSessions.get(0));

        Map<UUID, Long> lastSeen = sessionsTable.getLastSeenForAllPlayers();
        assertTrue(lastSeen.containsKey(playerUUID));
        assertFalse(lastSeen.containsKey(TestConstants.PLAYER_TWO_UUID));
        assertEquals(22345L, (long) lastSeen.get(playerUUID));
    }

    @Test
    public void userInfoTableStoresCorrectUserInformation() {
        userRegisterChecksReturnCorrectValues();

        List<UserInfo> userInfo = db.query(PlayerFetchQueries.playerServerSpecificUserInformation(playerUUID));
        List<UserInfo> expected = Collections.singletonList(new UserInfo(playerUUID, serverUUID, 223456789L, false, false));

        assertEquals(expected, userInfo);
    }

    @Test
    public void userRegisterChecksReturnCorrectValues() {
        UsersTable usersTable = db.getUsersTable();
        assertFalse(db.query(PlayerFetchQueries.isPlayerRegistered(playerUUID)));
        saveUserOne();
        assertTrue(db.query(PlayerFetchQueries.isPlayerRegistered(playerUUID)));

        UserInfoTable userInfoTable = db.getUserInfoTable();
        assertFalse(userInfoTable.isRegistered(playerUUID));
        userInfoTable.registerUserInfo(playerUUID, 223456789L);
        assertTrue(userInfoTable.isRegistered(playerUUID));

        assertEquals(123456789L, (long) db.getUsersTable().getRegisterDates().get(0));
    }

    @Test
    public void userInfoTableUpdatesBanStatus() {
        userRegisterChecksReturnCorrectValues();

        db.getUserInfoTable().updateBanStatus(playerUUID, true);

        List<UserInfo> userInfo = db.query(PlayerFetchQueries.playerServerSpecificUserInformation(playerUUID));
        List<UserInfo> expected = Collections.singletonList(new UserInfo(playerUUID, serverUUID, 223456789L, false, true));

        assertEquals(expected, userInfo);
    }

    @Test
    public void userInfoTableUpdatesOperatorStatus() {
        userRegisterChecksReturnCorrectValues();

        db.getUserInfoTable().updateOpStatus(playerUUID, true);

        List<UserInfo> userInfo = db.query(PlayerFetchQueries.playerServerSpecificUserInformation(playerUUID));
        List<UserInfo> expected = Collections.singletonList(new UserInfo(playerUUID, serverUUID, 223456789L, true, false));

        assertEquals(expected, userInfo);
    }

    @Test
    public void testUsersTableUpdateName() throws DBInitException {
        saveUserOne();

        UsersTable usersTable = db.getUsersTable();

        assertEquals(playerUUID, usersTable.getUuidOf("Test"));
        usersTable.updateName(playerUUID, "NewName");

        commitTest();

        assertNull(usersTable.getUuidOf("Test"));

        assertEquals("NewName", usersTable.getPlayerName(playerUUID));
        assertEquals(playerUUID, usersTable.getUuidOf("NewName"));
    }

    @Test
    public void testUsersTableKickSaving() throws DBInitException {
        saveUserOne();
        UsersTable usersTable = db.getUsersTable();
        assertEquals(1, usersTable.getTimesKicked(playerUUID));

        int random = new Random().nextInt(20);

        for (int i = 0; i < random + 1; i++) {
            usersTable.kicked(playerUUID);
        }
        commitTest();
        assertEquals(random + 2, usersTable.getTimesKicked(playerUUID));
    }

    @Test
    public void testRemovalSingleUser() {
        saveUserTwo();

        UserInfoTable userInfoTable = db.getUserInfoTable();
        UsersTable usersTable = db.getUsersTable();
        SessionsTable sessionsTable = db.getSessionsTable();
        NicknamesTable nicknamesTable = db.getNicknamesTable();

        db.executeTransaction(new PlayerRegisterTransaction(playerUUID, () -> 223456789L, "Test_name"));
        userInfoTable.registerUserInfo(playerUUID, 223456789L);
        saveTwoWorlds();

        Session session = new Session(playerUUID, serverUUID, 12345L, "", "");
        session.endSession(22345L);
        session.setWorldTimes(createWorldTimes());
        session.setPlayerKills(createKills());

        execute(DataStoreQueries.storeSession(session));
        nicknamesTable.saveUserName(playerUUID, new Nickname("TestNick", System.currentTimeMillis(), serverUUID));
        saveGeoInfo(playerUUID, new GeoInfo("1.2.3.4", "TestLoc", 223456789L, "3"));

        assertTrue(db.query(PlayerFetchQueries.isPlayerRegistered(playerUUID)));

        db.executeTransaction(new RemovePlayerTransaction(playerUUID));

        assertFalse(db.query(PlayerFetchQueries.isPlayerRegistered(playerUUID)));
        assertFalse(userInfoTable.isRegistered(playerUUID));
        assertTrue(nicknamesTable.getNicknames(playerUUID).isEmpty());
        assertTrue(db.query(PlayerFetchQueries.playerGeoInfo(playerUUID)).isEmpty());
        assertTrue(sessionsTable.getSessions(playerUUID).isEmpty());
    }

    @Test
    public void testRemovalEverything() throws NoSuchAlgorithmException {
        saveAllData();

        db.executeTransaction(new RemoveEverythingTransaction());

        assertTrue(db.query(LargeFetchQueries.fetchAllCommonUserInformation()).isEmpty());
        assertQueryIsEmpty(db, LargeFetchQueries.fetchPerServerUserInformation());
        assertQueryIsEmpty(db, LargeFetchQueries.fetchAllNicknameData());
        assertQueryIsEmpty(db, LargeFetchQueries.fetchAllGeoInformation());
        assertQueryIsEmpty(db, LargeFetchQueries.fetchAllSessionsWithoutKillOrWorldData());
        assertQueryIsEmpty(db, LargeFetchQueries.fetchAllCommandUsageData());
        assertQueryIsEmpty(db, LargeFetchQueries.fetchAllWorldNames());
        assertQueryIsEmpty(db, LargeFetchQueries.fetchAllTPSData());
        assertQueryIsEmpty(db, LargeFetchQueries.fetchPlanServerInformation());
        assertQueryIsEmpty(db, LargeFetchQueries.fetchAllPingData());
        assertTrue(db.query(LargeFetchQueries.fetchAllPlanWebUsers()).isEmpty());
    }

    private <T extends Map> void assertQueryIsEmpty(Database database, Query<T> query) {
        assertTrue(database.query(query).isEmpty());
    }

    private void saveAllData() throws NoSuchAlgorithmException {
        UserInfoTable userInfoTable = db.getUserInfoTable();
        UsersTable usersTable = db.getUsersTable();
        NicknamesTable nicknamesTable = db.getNicknamesTable();
        TPSTable tpsTable = db.getTpsTable();
        SecurityTable securityTable = db.getSecurityTable();
        PingTable pingTable = db.getPingTable();

        saveUserOne();
        saveUserTwo();

        userInfoTable.registerUserInfo(playerUUID, 223456789L);
        saveTwoWorlds();

        Session session = new Session(playerUUID, serverUUID, 12345L, "", "");
        session.endSession(22345L);
        session.setWorldTimes(createWorldTimes());
        session.setPlayerKills(createKills());

        execute(DataStoreQueries.storeSession(session));
        nicknamesTable.saveUserName(playerUUID, new Nickname("TestNick", System.currentTimeMillis(), serverUUID));
        saveGeoInfo(playerUUID, new GeoInfo("1.2.3.4", "TestLoc", 223456789L,
                new SHA256Hash("1.2.3.4").create()));

        assertTrue(db.query(PlayerFetchQueries.isPlayerRegistered(playerUUID)));

        useCommand("plan");
        useCommand("plan");
        useCommand("tp");
        useCommand("help");
        useCommand("help");
        useCommand("help");

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
            tpsTable.insertTPS(tps);
        }

        pingTable.insertPing(playerUUID, new Ping(
                System.currentTimeMillis(), serverUUID,
                r.nextInt(), r.nextInt(), r.nextDouble()
        ));

        securityTable.addNewUser(new WebUser("Test", "RandomGarbageBlah", 0));
    }

    void saveGeoInfo(UUID uuid, GeoInfo geoInfo) {
        db.executeTransaction(new GeoInfoStoreTransaction(uuid, geoInfo));
    }

    @Test
    public void testSessionTableNPEWhenNoPlayers() {
        Map<UUID, Long> lastSeen = db.getSessionsTable().getLastSeenForAllPlayers();
        assertTrue(lastSeen.isEmpty());
    }

    @Test
    public void testSessionTableGetInfoOfServer() throws DBInitException {
        saveUserOne();
        saveUserTwo();

        Session session = new Session(playerUUID, serverUUID, 12345L, "", "");
        session.endSession(22345L);
        session.setWorldTimes(createWorldTimes());
        session.setPlayerKills(createKills());

        SessionsTable sessionsTable = db.getSessionsTable();
        execute(DataStoreQueries.storeSession(session));

        commitTest();

        Map<UUID, List<Session>> sessions = sessionsTable.getSessionInfoOfServer();

        session.setPlayerKills(new ArrayList<>());
        session.setWorldTimes(new WorldTimes(new HashMap<>()));

        List<Session> sSessions = sessions.get(playerUUID);
        assertFalse(sessions.isEmpty());
        assertNotNull(sSessions);
        assertFalse(sSessions.isEmpty());
        assertEquals(session, sSessions.get(0));
    }

    @Test
    public void testKillTableGetKillsOfServer() throws DBInitException {
        saveUserOne();
        saveUserTwo();

        Session session = createSession();
        List<PlayerKill> expected = createKills();
        session.setPlayerKills(expected);
        execute(DataStoreQueries.storeSession(session));

        commitTest();

        Map<UUID, List<Session>> sessions = db.getSessionsTable().getSessions(playerUUID);
        List<Session> savedSessions = sessions.get(serverUUID);
        assertNotNull(savedSessions);
        assertFalse(savedSessions.isEmpty());

        Session savedSession = savedSessions.get(0);
        assertNotNull(savedSession);

        List<PlayerKill> kills = savedSession.getPlayerKills();
        assertNotNull(kills);
        assertFalse(kills.isEmpty());
        assertEquals(expected, kills);
    }

    private Session createSession() {
        Session session = new Session(
                playerUUID,
                serverUUID,
                System.currentTimeMillis(),
                "world",
                GMTimes.getGMKeyArray()[0]
        );
        db.executeTransaction(new WorldNameStoreTransaction(serverUUID, "world"));
        session.endSession(System.currentTimeMillis() + 1L);
        return session;
    }

    @Test
    public void testBackupAndRestore() throws Exception {
        H2DB backup = dbSystem.getH2Factory().usingFile(temporaryFolder.newFile("backup.db"));
        backup.init();

        saveAllData();

        backup.executeTransaction(new BackupCopyTransaction(db));

        assertQueryResultIsEqual(db, backup, LargeFetchQueries.fetchAllCommonUserInformation());
        assertQueryResultIsEqual(db, backup, LargeFetchQueries.fetchPerServerUserInformation());
        assertQueryResultIsEqual(db, backup, LargeFetchQueries.fetchAllNicknameData());
        assertQueryResultIsEqual(db, backup, LargeFetchQueries.fetchAllGeoInformation());
        assertQueryResultIsEqual(db, backup, LargeFetchQueries.fetchAllSessionsWithKillAndWorldData());
        assertQueryResultIsEqual(db, backup, LargeFetchQueries.fetchAllCommandUsageData());
        assertQueryResultIsEqual(db, backup, LargeFetchQueries.fetchAllWorldNames());
        assertQueryResultIsEqual(db, backup, LargeFetchQueries.fetchAllTPSData());
        assertQueryResultIsEqual(db, backup, LargeFetchQueries.fetchPlanServerInformation());
        assertQueryResultIsEqual(db, backup, LargeFetchQueries.fetchAllPlanWebUsers());
    }

    private <T> void assertQueryResultIsEqual(Database one, Database two, Query<T> query) {
        assertEquals(one.query(query), two.query(query));
    }

    @Test
    public void testSaveWorldTimes() {
        saveUserOne();
        WorldTimes worldTimes = createWorldTimes();
        WorldTimesTable worldTimesTable = db.getWorldTimesTable();

        Session session = new Session(1, playerUUID, serverUUID, 12345L, 23456L, 0, 0, 0);
        session.setWorldTimes(worldTimes);
        execute(DataStoreQueries.storeSession(session));

        Map<Integer, Session> sessions = new HashMap<>();
        sessions.put(1, session);
        worldTimesTable.addWorldTimesToSessions(playerUUID, sessions);

        assertEquals(worldTimes, session.getUnsafe(SessionKeys.WORLD_TIMES));
    }

    @Test
    public void testSaveAllWorldTimes() {
        saveTwoWorlds();
        saveUserOne();
        WorldTimes worldTimes = createWorldTimes();

        WorldTimesTable worldTimesTable = db.getWorldTimesTable();

        Session session = createSession();
        session.setWorldTimes(worldTimes);
        List<Session> sessions = new ArrayList<>();
        sessions.add(session);
        db.executeTransaction(new Transaction() {
            @Override
            protected void performOperations() {
                execute(LargeStoreQueries.storeAllSessionsWithKillAndWorldData(sessions));
            }
        });

        Map<Integer, WorldTimes> worldTimesBySessionID = worldTimesTable.getAllWorldTimesBySessionID();
        System.out.println(worldTimesBySessionID);
        assertEquals(worldTimes, worldTimesBySessionID.get(1));
    }

    @Test
    public void testSaveSessionsWorldTimes() {
        saveTwoWorlds();
        saveUserOne();

        WorldTimes worldTimes = createWorldTimes();
        Session session = createSession();
        session.setWorldTimes(worldTimes);
        List<Session> sessions = new ArrayList<>();
        sessions.add(session);
        db.executeTransaction(new Transaction() {
            @Override
            protected void performOperations() {
                execute(LargeStoreQueries.storeAllSessionsWithKillAndWorldData(sessions));
            }
        });

        List<Session> allSessions = db.query(LargeFetchQueries.fetchAllSessionsFlatWithKillAndWorldData());

        assertEquals(worldTimes, allSessions.get(0).getUnsafe(SessionKeys.WORLD_TIMES));
    }

    @Test
    public void testGetUserWorldTimes() {
        testSaveSessionsWorldTimes();
        WorldTimes worldTimesOfUser = db.query(PlayerAggregateQueries.totalWorldTimes(playerUUID));
        assertEquals(createWorldTimes(), worldTimesOfUser);
    }

    @Test
    public void testGetServerWorldTimes() {
        testSaveSessionsWorldTimes();
        WorldTimes worldTimesOfServer = db.query(ServerAggregateQueries.totalWorldTimes(serverUUID));
        assertEquals(createWorldTimes(), worldTimesOfServer);
    }

    @Test
    public void testGetServerWorldTimesNoSessions() {
        WorldTimes worldTimesOfServer = db.query(ServerAggregateQueries.totalWorldTimes(serverUUID));
        assertEquals(new WorldTimes(new HashMap<>()), worldTimesOfServer);
    }

    @Test
    public void testRegister() {
        assertFalse(db.query(PlayerFetchQueries.isPlayerRegistered(playerUUID)));
        assertFalse(db.check().isPlayerRegisteredOnThisServer(playerUUID));
        db.executeTransaction(new PlayerRegisterTransaction(playerUUID, () -> 1000L, "name"));
        db.save().registerNewUserOnThisServer(playerUUID, 500L);
        assertTrue(db.query(PlayerFetchQueries.isPlayerRegistered(playerUUID)));
        assertTrue(db.check().isPlayerRegisteredOnThisServer(playerUUID));
    }

    @Test
    public void testNewContainerForPlayer() throws NoSuchAlgorithmException {
        saveAllData();

        long start = System.nanoTime();

        PlayerContainer container = db.query(ContainerFetchQueries.fetchPlayerContainer(playerUUID));

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
        container.putRawData(PlayerKeys.ACTIVE_SESSION, new Session(playerUUID, serverUUID, System.currentTimeMillis(), "TestWorld", "SURVIVAL"));
        assertTrue(container.supports(PlayerKeys.ACTIVE_SESSION));

        long end = System.nanoTime();

        assertFalse("Took too long: " + ((end - start) / 1000000.0) + "ms", end - start > TimeUnit.SECONDS.toNanos(1L));

        OptionalAssert.equals(playerUUID, container.getValue(PlayerKeys.UUID));
        OptionalAssert.equals(123456789L, container.getValue(PlayerKeys.REGISTERED));
        OptionalAssert.equals("Test", container.getValue(PlayerKeys.NAME));
        OptionalAssert.equals(1, container.getValue(PlayerKeys.KICK_COUNT));

        List<GeoInfo> expectedGeoInfo =
                Collections.singletonList(new GeoInfo("1.2.3.4", "TestLoc", 223456789, "ZpT4PJ9HbaMfXfa8xSADTn5X1CHSR7nTT0ntv8hKdkw="));
        OptionalAssert.equals(expectedGeoInfo, container.getValue(PlayerKeys.GEO_INFO));

        List<Nickname> expectedNicknames = Collections.singletonList(new Nickname("TestNick", -1, serverUUID));
        OptionalAssert.equals(expectedNicknames, container.getValue(PlayerKeys.NICKNAMES));

        OptionalAssert.equals(false, container.getValue(PlayerKeys.OPERATOR));
        OptionalAssert.equals(false, container.getValue(PlayerKeys.BANNED));

        // TODO Test rest
    }

    @Test
    public void playerContainerSupportsAllPlayerKeys() throws NoSuchAlgorithmException, IllegalAccessException {
        saveAllData();

        PlayerContainer playerContainer = db.query(ContainerFetchQueries.fetchPlayerContainer(playerUUID));
        // Active sessions are added after fetching
        playerContainer.putRawData(PlayerKeys.ACTIVE_SESSION, RandomData.randomSession());

        List<String> unsupported = new ArrayList<>();
        List<Key> keys = FieldFetcher.getPublicStaticFields(PlayerKeys.class, Key.class);
        for (Key key : keys) {
            if (!playerContainer.supports(key)) {
                unsupported.add(key.getKeyName());
            }
        }

        assertTrue("Some keys are not supported by PlayerContainer: PlayerKeys." + unsupported.toString(), unsupported.isEmpty());
    }

    @Test
    public void serverContainerSupportsAllServerKeys() throws NoSuchAlgorithmException, IllegalAccessException {
        saveAllData();

        ServerContainer serverContainer = db.query(ContainerFetchQueries.fetchServerContainer(serverUUID));

        List<String> unsupported = new ArrayList<>();
        List<Key> keys = FieldFetcher.getPublicStaticFields(ServerKeys.class, Key.class);
        for (Key key : keys) {
            if (!serverContainer.supports(key)) {
                unsupported.add(key.getKeyName());
            }
        }

        assertTrue("Some keys are not supported by ServerContainer: ServerKeys." + unsupported.toString(), unsupported.isEmpty());
    }

    @Test
    public void analysisContainerSupportsAllAnalysisKeys() throws IllegalAccessException, NoSuchAlgorithmException {
        serverContainerSupportsAllServerKeys();
        AnalysisContainer.Factory factory = constructAnalysisContainerFactory();
        AnalysisContainer analysisContainer = factory.forServerContainer(
                db.query(ContainerFetchQueries.fetchServerContainer(serverUUID))
        );
        Collection<String> unsupported = new ArrayList<>();
        List<Key> keys = FieldFetcher.getPublicStaticFields(AnalysisKeys.class, Key.class);
        for (Key key : keys) {
            if (!analysisContainer.supports(key)) {
                unsupported.add(key.getKeyName());
            }
        }

        assertTrue("Some keys are not supported by AnalysisContainer: AnalysisKeys." + unsupported.toString(), unsupported.isEmpty());
    }

    private AnalysisContainer.Factory constructAnalysisContainerFactory() {
        return new AnalysisContainer.Factory(
                "1.0.0",
                system.getConfigSystem().getConfig(),
                system.getLocaleSystem().getLocale(),
                system.getConfigSystem().getTheme(),
                dbSystem,
                system.getServerInfo().getServerProperties(),
                system.getHtmlUtilities().getFormatters(),
                system.getHtmlUtilities().getGraphs(),
                system.getHtmlUtilities().getHtmlTables(),
                system.getHtmlUtilities().getAccordions(),
                system.getHtmlUtilities().getAnalysisPluginsTabContentCreator()
        );
    }

    @Test
    public void networkContainerSupportsAllNetworkKeys() throws IllegalAccessException, NoSuchAlgorithmException {
        serverContainerSupportsAllServerKeys();
        NetworkContainer networkContainer = db.query(ContainerFetchQueries.fetchNetworkContainer());

        List<String> unsupported = new ArrayList<>();
        List<Key> keys = FieldFetcher.getPublicStaticFields(NetworkKeys.class, Key.class);
        for (Key key : keys) {
            if (!networkContainer.supports(key)) {
                unsupported.add(key.getKeyName());
            }
        }

        assertTrue("Some keys are not supported by NetworkContainer: NetworkKeys." + unsupported.toString(), unsupported.isEmpty());
    }

    @Test
    public void testGetMatchingNames() {
        String exp1 = "TestName";
        String exp2 = "TestName2";

        UUID uuid1 = UUID.randomUUID();
        db.executeTransaction(new PlayerRegisterTransaction(uuid1, () -> 0L, exp1));
        db.executeTransaction(new PlayerRegisterTransaction(UUID.randomUUID(), () -> 0L, exp2));

        String search = "testname";

        List<String> result = db.search().matchingPlayers(search);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(exp1, result.get(0));
        assertEquals(exp2, result.get(1));
    }

    @Test
    public void testGetMatchingNickNames() {
        UUID uuid = UUID.randomUUID();
        String userName = RandomData.randomString(10);

        db.executeTransaction(new PlayerRegisterTransaction(uuid, () -> 0L, userName));
        db.executeTransaction(new PlayerRegisterTransaction(playerUUID, () -> 1L, "Not random"));

        String nickname = "2" + RandomData.randomString(10);
        db.getNicknamesTable().saveUserName(uuid, new Nickname(nickname, System.currentTimeMillis(), serverUUID));
        db.getNicknamesTable().saveUserName(playerUUID, new Nickname("No nick", System.currentTimeMillis(), serverUUID));

        String search = "2";

        List<String> result = db.search().matchingPlayers(search);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(userName, result.get(0));
    }

    @Test
    public void configIsStoredInTheDatabase() {
        PlanConfig config = system.getConfigSystem().getConfig();

        SettingsTable settingsTable = db.getSettingsTable();
        settingsTable.storeConfig(serverUUID, config, System.currentTimeMillis());

        Optional<Config> foundConfig = settingsTable.fetchNewerConfig(0, serverUUID);
        assertTrue(foundConfig.isPresent());
        assertEquals(config, foundConfig.get());
    }

    @Test
    public void unchangedConfigDoesNotUpdateInDatabase() {
        configIsStoredInTheDatabase();
        long savedMs = System.currentTimeMillis();

        PlanConfig config = system.getConfigSystem().getConfig();

        SettingsTable settingsTable = db.getSettingsTable();
        settingsTable.storeConfig(serverUUID, config, System.currentTimeMillis());

        assertFalse(settingsTable.fetchNewerConfig(savedMs, serverUUID).isPresent());
    }

    @Test
    public void indexCreationWorksWithoutErrors() {
        db.executeTransaction(new CreateIndexTransaction(db.getType()));
    }

}
