/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.djrapitops.plan.system.database.databases;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.api.exceptions.database.DBException;
import com.djrapitops.plan.api.exceptions.database.DBInitException;
import com.djrapitops.plan.data.Actions;
import com.djrapitops.plan.data.WebUser;
import com.djrapitops.plan.data.container.*;
import com.djrapitops.plan.data.store.Key;
import com.djrapitops.plan.data.store.containers.AnalysisContainer;
import com.djrapitops.plan.data.store.containers.PlayerContainer;
import com.djrapitops.plan.data.store.containers.ServerContainer;
import com.djrapitops.plan.data.store.keys.AnalysisKeys;
import com.djrapitops.plan.data.store.keys.PlayerKeys;
import com.djrapitops.plan.data.store.keys.ServerKeys;
import com.djrapitops.plan.data.store.objects.Nickname;
import com.djrapitops.plan.data.time.GMTimes;
import com.djrapitops.plan.data.time.WorldTimes;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;
import com.djrapitops.plan.system.database.databases.sql.SQLiteDB;
import com.djrapitops.plan.system.database.databases.sql.tables.*;
import com.djrapitops.plan.system.info.server.Server;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.processing.processors.player.RegisterProcessor;
import com.djrapitops.plan.utilities.Base64Util;
import com.djrapitops.plan.utilities.SHA256Hash;
import com.djrapitops.plugin.StaticHolder;
import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.api.utility.log.Log;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import utilities.*;
import utilities.mocks.SystemMockUtil;

import java.io.UnsupportedEncodingException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Field;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static org.junit.Assert.*;

/**
 * @author Rsl1122
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class SQLiteTest {

    private final List<String> worlds = Arrays.asList("TestWorld", "TestWorld2");
    @ClassRule
    public static TemporaryFolder temporaryFolder = new TemporaryFolder();
    private static SQLDB db;
    private final UUID playerUUID = TestConstants.PLAYER_ONE_UUID;
    private final UUID player2UUID = TestConstants.PLAYER_TWO_UUID;
    @Rule
    public Timeout globalTimeout = Timeout.seconds(5);

    @BeforeClass
    public static void setUpClass() throws Exception {
        System.out.println("--- Test Class Setup     ---");
        db = new SQLiteDB();
        SystemMockUtil.setUp(temporaryFolder.getRoot())
                .enableConfigSystem()
                .enableDatabaseSystem(db)
                .enableServerInfoSystem();
        StaticHolder.saveInstance(SQLDB.class, Plan.class);
        StaticHolder.saveInstance(SQLiteTest.class, Plan.class);

        Log.setErrorManager(new TestErrorManager());
        Log.setDebugMode("console");
//        Settings.DEV_MODE.setTemporaryValue(true);

        db.init();
        System.out.println("--- Class Setup Complete ---\n");
    }

    @AfterClass
    public static void tearDownClass() {
        if (db != null) {
            db.close();
        }
        Teardown.resetSettingsTempValues();
    }

    @Before
    public void setUp() {
        assertEquals(db, Database.getActive());
        System.out.println("\n-- Clearing Test Database --");
        db.remove().everything();
        ServerTable serverTable = db.getServerTable();
        serverTable.saveCurrentServerInfo(new Server(-1, TestConstants.SERVER_UUID, "ServerName", "", 20));
        assertEquals(ServerInfo.getServerUUID(), TestConstants.SERVER_UUID);
        System.out.println("--     Clear Complete     --\n");
    }

    @Test
    public void testInit() throws DBInitException {
        db.init();
    }

    @Test
    public void testNoExceptionWhenCommitEmpty() throws Exception {
        db.commit(db.getConnection());
        db.commit(db.getConnection());
        db.commit(db.getConnection());
    }

    @Test
    public void testSQLiteGetConfigName() {
        assertEquals("sqlite", db.getConfigName());
    }

    @Test
    public void testSQLiteGetName() {
        assertEquals("SQLite", db.getName());
    }

    @Test(timeout = 3000)
    public void testSaveCommandUse() throws DBInitException {
        CommandUseTable commandUseTable = db.getCommandUseTable();
        Map<String, Integer> expected = new HashMap<>();

        expected.put("plan", 1);
        expected.put("tp", 4);
        expected.put("pla", 7);
        expected.put("help", 21);

        commandUseTable.commandUsed("plan");

        for (int i = 0; i < 4; i++) {
            commandUseTable.commandUsed("tp");
        }

        for (int i = 0; i < 7; i++) {
            commandUseTable.commandUsed("pla");
        }

        for (int i = 0; i < 21; i++) {
            commandUseTable.commandUsed("help");
        }

        for (int i = 0; i < 3; i++) {
            commandUseTable.commandUsed("roiergbnougbierubieugbeigubeigubgierbgeugeg");
        }

        commitTest();

        Map<String, Integer> commandUse = db.getCommandUseTable().getCommandUse();
        assertEquals(expected, commandUse);

        for (int i = 0; i < 3; i++) {
            commandUseTable.commandUsed("test");
        }

        for (int i = 0; i < 2; i++) {
            commandUseTable.commandUsed("tp");
        }

        expected.put("test", 3);
        expected.put("tp", 6);

        commandUse = db.getCommandUseTable().getCommandUse();

        assertEquals(expected, commandUse);
    }

    @Test
    public void testCommandUseTableIDSystem() {
        CommandUseTable commandUseTable = db.getCommandUseTable();
        commandUseTable.commandUsed("plan");

        for (int i = 0; i < 4; i++) {
            commandUseTable.commandUsed("tp");
        }

        for (int i = 0; i < 7; i++) {
            commandUseTable.commandUsed("pla");
        }

        for (int i = 0; i < 21; i++) {
            commandUseTable.commandUsed("help");
        }

        for (int i = 0; i < 3; i++) {
            commandUseTable.commandUsed("roiergbnougbierubieugbeigubeigubgierbgeugeg");
        }

        Optional<Integer> id = commandUseTable.getCommandID("plan");

        assertTrue(id.isPresent());

        Optional<String> commandByID = commandUseTable.getCommandByID(id.get());

        assertTrue(commandByID.isPresent());
        assertEquals("plan", commandByID.get());
        assertFalse(commandUseTable.getCommandID("roiergbnougbierubieugbeigubeigubgierbgeugeg").isPresent());
    }

    @Test
    public void testTPSSaving() throws Exception {
        TPSTable tpsTable = db.getTpsTable();
        Random r = new Random();

        List<TPS> expected = new ArrayList<>();

        for (int i = 0; i < RandomData.randomInt(1, 5); i++) {
            expected.add(new TPS(r.nextLong(), r.nextDouble(), r.nextInt(100000000), r.nextDouble(), r.nextLong(), r.nextInt(), r.nextInt()));
        }

        for (TPS tps : expected) {
            tpsTable.insertTPS(tps);
        }

        commitTest();

        assertEquals(expected, tpsTable.getTPSData());
    }

    private void saveUserOne() {
        saveUserOne(db);
    }

    private void saveUserOne(SQLDB database) {
        database.getUsersTable().registerUser(playerUUID, 123456789L, "Test");
        database.getUsersTable().kicked(playerUUID);
    }

    private void saveUserTwo() {
        saveUserTwo(db);
    }

    private void saveUserTwo(SQLDB database) {
        database.getUsersTable().registerUser(player2UUID, 123456789L, "Test");
    }

    @Test
    public void testActionsTable() {
        saveUserOne();
        ActionsTable actionsTable = db.getActionsTable();

        Action save = new Action(234567890L, Actions.FIRST_SESSION, "Additional Info");
        Action expected = new Action(234567890L, Actions.FIRST_SESSION, "Additional Info", 1);

        actionsTable.insertAction(playerUUID, save);

        List<Action> actions = actionsTable.getActions(playerUUID);
        assertEquals(expected, actions.get(0));
    }

    @Test
    public void testIPTable() throws DBInitException {
        saveUserOne();
        GeoInfoTable geoInfoTable = db.getGeoInfoTable();

        String expectedIP = "1.2.3.4";
        String expectedGeoLoc = "TestLocation";
        long time = System.currentTimeMillis();

        GeoInfo expected = new GeoInfo(expectedIP, expectedGeoLoc, time, "3");
        geoInfoTable.saveGeoInfo(playerUUID, expected);
        geoInfoTable.saveGeoInfo(playerUUID, expected);
        commitTest();

        List<GeoInfo> getInfo = geoInfoTable.getGeoInfo(playerUUID);
        assertEquals(1, getInfo.size());
        GeoInfo actual = getInfo.get(0);
        assertEquals(expected, actual);
        assertEquals(time, actual.getDate());

        Optional<String> result = geoInfoTable.getGeolocation(expectedIP);
        assertTrue(result.isPresent());
        assertEquals(expectedGeoLoc, result.get());
    }

    @Test
    public void testNicknamesTable() throws DBInitException {
        saveUserOne();
        NicknamesTable nickTable = db.getNicknamesTable();

        String expected = "TestNickname";
        nickTable.saveUserName(playerUUID, expected);
        nickTable.saveUserName(playerUUID, expected);
        commitTest();

        List<String> nicknames = nickTable.getNicknames(playerUUID);
        assertEquals(1, nicknames.size());
        assertEquals(expected, nicknames.get(0));

        Map<UUID, List<String>> allNicknames = nickTable.getAllNicknames(playerUUID);
        assertEquals(nicknames, allNicknames.get(ServerInfo.getServerUUID()));
    }

    @Test
    public void testSecurityTable() throws DBInitException {
        SecurityTable securityTable = db.getSecurityTable();
        WebUser expected = new WebUser("Test", "RandomGarbageBlah", 0);
        securityTable.addNewUser(expected);
        commitTest();

        assertTrue(securityTable.userExists("Test"));
        WebUser test = securityTable.getWebUser("Test");
        assertEquals(expected, test);

        assertFalse(securityTable.userExists("NotExist"));
        assertNull(securityTable.getWebUser("NotExist"));

        assertEquals(1, securityTable.getUsers().size());

        securityTable.removeUser("Test");
        assertFalse(securityTable.userExists("Test"));
        assertNull(securityTable.getWebUser("Test"));

        assertEquals(0, securityTable.getUsers().size());
    }

    @Test
    public void testWorldTable() throws DBInitException {
        WorldTable worldTable = db.getWorldTable();
        List<String> worlds = Arrays.asList("Test", "Test2", "Test3");
        worldTable.saveWorlds(worlds);

        commitTest();

        List<String> saved = worldTable.getAllWorlds();
        assertEquals(new HashSet<>(worlds), new HashSet<>(saved));
    }

    private void saveTwoWorlds() {
        saveTwoWorlds(db);
    }

    private void saveTwoWorlds(SQLDB database) {
        database.getWorldTable().saveWorlds(worlds);
    }

    private WorldTimes createWorldTimes() {
        Map<String, GMTimes> times = new HashMap<>();
        Map<String, Long> gm = new HashMap<>();
        String[] gms = GMTimes.getGMKeyArray();
        gm.put(gms[0], 1000L);
        gm.put(gms[1], 2000L);
        gm.put(gms[2], 3000L);
        gm.put(gms[3], 4000L);
        times.put(worlds.get(0), new GMTimes(gm));

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
        Session session = new Session(TestConstants.PLAYER_ONE_UUID, 12345L, "", "");
        session.endSession(22345L);
        session.setWorldTimes(createWorldTimes());
        session.setPlayerKills(createKills());

        long expectedLength = 10000L;
        assertEquals(expectedLength, session.getLength());
        assertEquals(expectedLength, session.getWorldTimes().getTotal());

        SessionsTable sessionsTable = db.getSessionsTable();
        sessionsTable.saveSession(playerUUID, session);

        commitTest();

        assertEquals(expectedLength, sessionsTable.getPlaytime(playerUUID));
        assertEquals(0L, sessionsTable.getPlaytime(playerUUID, 30000L));

        UUID serverUUID = TestConstants.SERVER_UUID;
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

        Session session = new Session(TestConstants.PLAYER_ONE_UUID, 12345L, "", "");
        session.endSession(22345L);
        session.setWorldTimes(createWorldTimes());
        session.setPlayerKills(createKills());

        SessionsTable sessionsTable = db.getSessionsTable();
        sessionsTable.saveSession(playerUUID, session);

        commitTest();

        Map<UUID, List<Session>> sessions = sessionsTable.getSessions(playerUUID);

        for (Map.Entry<UUID, List<Session>> entry : sessions.entrySet()) {
            UUID key = entry.getKey();
            if (key == null) {
                System.out.print("null");
            } else {
                System.out.print(key);
            }
            System.out.println(" " + entry.getValue());
        }

        List<Session> savedSessions = sessions.get(ServerInfo.getServerUUID());

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
    public void testUserInfoTableRegisterUnRegistered() throws DBInitException {
        UserInfoTable userInfoTable = db.getUserInfoTable();
        assertFalse(userInfoTable.isRegistered(playerUUID));
        UsersTable usersTable = db.getUsersTable();
        assertFalse(usersTable.isRegistered(playerUUID));

        userInfoTable.registerUserInfo(playerUUID, 123456789L);

        commitTest();

        assertTrue(usersTable.isRegistered(playerUUID));
        assertTrue(userInfoTable.isRegistered(playerUUID));

        UserInfo userInfo = userInfoTable.getUserInfo(playerUUID);
        assertEquals(playerUUID, userInfo.getUuid());
        assertEquals(123456789L, (long) usersTable.getRegisterDates().get(0));
        assertEquals(123456789L, userInfo.getRegistered());
        assertEquals(1, userInfoTable.getServerUserCount(ServerInfo.getServerUUID()));
        assertEquals("Waiting for Update..", userInfo.getName());
        assertFalse(userInfo.isBanned());
        assertFalse(userInfo.isOperator());
    }

    @Test
    public void testUserInfoTableRegisterRegistered() throws DBInitException {
        saveUserOne();
        UsersTable usersTable = db.getUsersTable();
        assertTrue(usersTable.isRegistered(playerUUID));

        UserInfoTable userInfoTable = db.getUserInfoTable();
        assertFalse(userInfoTable.isRegistered(playerUUID));

        userInfoTable.registerUserInfo(playerUUID, 223456789L);
        commitTest();

        assertTrue(usersTable.isRegistered(playerUUID));
        assertTrue(userInfoTable.isRegistered(playerUUID));

        UserInfo userInfo = userInfoTable.getUserInfo(playerUUID);
        assertEquals(playerUUID, userInfo.getUuid());
        assertEquals(123456789L, (long) usersTable.getRegisterDates().get(0));
        assertEquals(223456789L, userInfo.getRegistered());
        assertEquals("Test", userInfo.getName());
        assertFalse(userInfo.isBanned());
        assertFalse(userInfo.isOperator());

        assertEquals(userInfo, userInfoTable.getServerUserInfo().get(0));
    }

    @Test
    public void testUserInfoTableUpdateBannedOpped() throws DBInitException {
        UserInfoTable userInfoTable = db.getUserInfoTable();
        userInfoTable.registerUserInfo(playerUUID, 223456789L);
        assertTrue(userInfoTable.isRegistered(playerUUID));

        userInfoTable.updateOpStatus(playerUUID, true);
        userInfoTable.updateBanStatus(playerUUID, true);
        commitTest();

        UserInfo userInfo = userInfoTable.getUserInfo(playerUUID);
        assertTrue(userInfo.isBanned());
        assertTrue(userInfo.isOperator());

        userInfoTable.updateOpStatus(playerUUID, false);
        userInfoTable.updateBanStatus(playerUUID, true);
        commitTest();

        userInfo = userInfoTable.getUserInfo(playerUUID);

        assertTrue(userInfo.isBanned());
        assertFalse(userInfo.isOperator());

        userInfoTable.updateOpStatus(playerUUID, true);
        userInfoTable.updateBanStatus(playerUUID, false);
        commitTest();

        userInfo = userInfoTable.getUserInfo(playerUUID);

        assertFalse(userInfo.isBanned());
        assertTrue(userInfo.isOperator());
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
        GeoInfoTable geoInfoTable = db.getGeoInfoTable();
        ActionsTable actionsTable = db.getActionsTable();

        userInfoTable.registerUserInfo(playerUUID, 223456789L);
        saveTwoWorlds();

        Session session = new Session(TestConstants.PLAYER_ONE_UUID, 12345L, "", "");
        session.endSession(22345L);
        session.setWorldTimes(createWorldTimes());
        session.setPlayerKills(createKills());

        sessionsTable.saveSession(playerUUID, session);
        nicknamesTable.saveUserName(playerUUID, "TestNick");
        geoInfoTable.saveGeoInfo(playerUUID, new GeoInfo("1.2.3.4", "TestLoc", 223456789L, "3"));
        actionsTable.insertAction(playerUUID, new Action(1324L, Actions.FIRST_SESSION, "Add"));

        assertTrue(usersTable.isRegistered(playerUUID));

        db.remove().player(playerUUID);

        assertFalse(usersTable.isRegistered(playerUUID));
        assertFalse(userInfoTable.isRegistered(playerUUID));
        assertTrue(nicknamesTable.getNicknames(playerUUID).isEmpty());
        assertTrue(geoInfoTable.getGeoInfo(playerUUID).isEmpty());
        assertTrue(sessionsTable.getSessions(playerUUID).isEmpty());
        assertTrue(actionsTable.getActions(playerUUID).isEmpty());
    }

    @Test
    public void testRemovalEverything() throws UnsupportedEncodingException, NoSuchAlgorithmException {
        UserInfoTable userInfoTable = db.getUserInfoTable();
        UsersTable usersTable = db.getUsersTable();
        SessionsTable sessionsTable = db.getSessionsTable();
        NicknamesTable nicknamesTable = db.getNicknamesTable();
        GeoInfoTable geoInfoTable = db.getGeoInfoTable();
        ActionsTable actionsTable = db.getActionsTable();
        TPSTable tpsTable = db.getTpsTable();
        SecurityTable securityTable = db.getSecurityTable();

        saveAllData(db);

        db.remove().everything();

        assertFalse(usersTable.isRegistered(playerUUID));
        assertFalse(usersTable.isRegistered(TestConstants.PLAYER_TWO_UUID));
        assertFalse(userInfoTable.isRegistered(playerUUID));

        assertTrue(nicknamesTable.getNicknames(playerUUID).isEmpty());
        assertTrue(geoInfoTable.getGeoInfo(playerUUID).isEmpty());
        assertTrue(sessionsTable.getSessions(playerUUID).isEmpty());
        assertTrue(actionsTable.getActions(playerUUID).isEmpty());
        assertTrue(db.getCommandUseTable().getCommandUse().isEmpty());
        assertTrue(db.getWorldTable().getAllWorlds().isEmpty());
        assertTrue(tpsTable.getTPSData().isEmpty());
        assertTrue(db.getServerTable().getBukkitServers().isEmpty());
        assertTrue(securityTable.getUsers().isEmpty());
    }

    private void saveAllData(SQLDB database) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        System.out.println("Saving all possible data to the Database..");
        UserInfoTable userInfoTable = database.getUserInfoTable();
        UsersTable usersTable = database.getUsersTable();
        SessionsTable sessionsTable = database.getSessionsTable();
        NicknamesTable nicknamesTable = database.getNicknamesTable();
        GeoInfoTable geoInfoTable = database.getGeoInfoTable();
        ActionsTable actionsTable = database.getActionsTable();
        TPSTable tpsTable = database.getTpsTable();
        SecurityTable securityTable = database.getSecurityTable();

        saveUserOne(database);
        saveUserTwo(database);

        userInfoTable.registerUserInfo(playerUUID, 223456789L);
        saveTwoWorlds(database);

        Session session = new Session(TestConstants.PLAYER_ONE_UUID, 12345L, "", "");
        session.endSession(22345L);
        session.setWorldTimes(createWorldTimes());
        session.setPlayerKills(createKills());

        sessionsTable.saveSession(playerUUID, session);
        nicknamesTable.saveUserName(playerUUID, "TestNick");
        geoInfoTable.saveGeoInfo(playerUUID, new GeoInfo("1.2.3.4", "TestLoc", 223456789L,
                new SHA256Hash("1.2.3.4").create()));
        actionsTable.insertAction(playerUUID, new Action(1324L, Actions.FIRST_SESSION, "Add"));

        assertTrue(usersTable.isRegistered(playerUUID));

        CommandUseTable commandUseTable = database.getCommandUseTable();
        commandUseTable.commandUsed("plan");
        commandUseTable.commandUsed("plan");
        commandUseTable.commandUsed("tp");
        commandUseTable.commandUsed("help");
        commandUseTable.commandUsed("help");
        commandUseTable.commandUsed("help");

        List<TPS> expected = new ArrayList<>();
        Random r = new Random();
        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        int availableProcessors = ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors();
        final double averageCPUUsage = operatingSystemMXBean.getSystemLoadAverage() / availableProcessors * 100.0;
        final long usedMemory = 51231251254L;
        final int entityCount = 6123;
        final int chunksLoaded = 2134;
        expected.add(new TPS(r.nextLong(), r.nextDouble(), r.nextInt(100000000), averageCPUUsage, usedMemory, entityCount, chunksLoaded));
        expected.add(new TPS(r.nextLong(), r.nextDouble(), r.nextInt(100000000), averageCPUUsage, usedMemory, entityCount, chunksLoaded));
        expected.add(new TPS(r.nextLong(), r.nextDouble(), r.nextInt(100000000), averageCPUUsage, usedMemory, entityCount, chunksLoaded));
        expected.add(new TPS(r.nextLong(), r.nextDouble(), r.nextInt(100000000), averageCPUUsage, usedMemory, entityCount, chunksLoaded));
        for (TPS tps : expected) {
            tpsTable.insertTPS(tps);
        }

        securityTable.addNewUser(new WebUser("Test", "RandomGarbageBlah", 0));
        System.out.println("Done!\n");
    }

    @Test
    public void testServerTableBungeeSave() throws DBInitException {
        ServerTable serverTable = db.getServerTable();

        Optional<Server> bungeeInfo = serverTable.getBungeeInfo();
        assertFalse(bungeeInfo.isPresent());

        UUID bungeeUUID = UUID.randomUUID();
        Server bungeeCord = new Server(-1, bungeeUUID, "BungeeCord", "Random:1234", 20);
        serverTable.saveCurrentServerInfo(bungeeCord);

        commitTest();

        bungeeCord.setId(2);

        bungeeInfo = serverTable.getBungeeInfo();
        assertTrue(bungeeInfo.isPresent());
        assertEquals(bungeeCord, bungeeInfo.get());

        Optional<Integer> serverID = serverTable.getServerID(bungeeUUID);
        assertTrue(serverID.isPresent());
        assertEquals(2, (int) serverID.get());
    }

    @Test
    public void testServerTableBungee() throws DBInitException {
        testServerTableBungeeSave();
        ServerTable serverTable = db.getServerTable();

        Map<UUID, Server> bukkitServers = serverTable.getBukkitServers();
        assertEquals(1, bukkitServers.size());
    }

    @Test
    public void testSessionTableNPEWhenNoPlayers() {
        Map<UUID, Long> lastSeen = db.getSessionsTable().getLastSeenForAllPlayers();
        assertTrue(lastSeen.isEmpty());
    }

    private void commitTest() throws DBInitException {
        db.close();
        db.init();
    }

    @Test
    public void testSessionTableGetInfoOfServer() throws DBInitException {
        saveUserOne();
        saveUserTwo();

        Session session = new Session(TestConstants.PLAYER_ONE_UUID, 12345L, "", "");
        session.endSession(22345L);
        session.setWorldTimes(createWorldTimes());
        session.setPlayerKills(createKills());

        SessionsTable sessionsTable = db.getSessionsTable();
        sessionsTable.saveSession(playerUUID, session);

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

        KillsTable killsTable = db.getKillsTable();
        List<PlayerKill> expected = createKills();
        killsTable.savePlayerKills(playerUUID, 1, expected);

        commitTest();

        Map<UUID, List<PlayerKill>> playerKills = killsTable.getPlayerKills();
        List<PlayerKill> kills = playerKills.get(playerUUID);
        assertFalse(playerKills.isEmpty());
        assertNotNull(kills);
        assertFalse(kills.isEmpty());
        assertEquals(expected, kills);
    }

    @Test
    public void testBackupAndRestore() throws DBException, UnsupportedEncodingException, NoSuchAlgorithmException {
        System.out.println("- Creating Backup Database -");
        SQLiteDB backup = new SQLiteDB("debug-backup" + System.currentTimeMillis());
        backup.init();
        System.out.println("- Backup Database Created  -");

        saveAllData(db);

        db.backup().backup(backup);

        UserInfoTable userInfoTable = backup.getUserInfoTable();
        UsersTable usersTable = backup.getUsersTable();
        SessionsTable sessionsTable = backup.getSessionsTable();
        NicknamesTable nicknamesTable = backup.getNicknamesTable();
        GeoInfoTable ipsTable = backup.getGeoInfoTable();
        ActionsTable actionsTable = backup.getActionsTable();
        TPSTable tpsTable = backup.getTpsTable();
        SecurityTable securityTable = backup.getSecurityTable();

        assertTrue(usersTable.isRegistered(playerUUID));
        assertTrue(usersTable.isRegistered(TestConstants.PLAYER_TWO_UUID));
        assertTrue(userInfoTable.isRegistered(playerUUID));

        assertFalse(nicknamesTable.getNicknames(playerUUID).isEmpty());
        assertFalse(ipsTable.getGeoInfo(playerUUID).isEmpty());
        assertFalse(sessionsTable.getSessions(playerUUID).isEmpty());
        assertFalse(actionsTable.getActions(playerUUID).isEmpty());
        assertFalse(backup.getCommandUseTable().getCommandUse().isEmpty());
        assertFalse(backup.getWorldTable().getAllWorlds().isEmpty());
        assertFalse(tpsTable.getTPSData().isEmpty());
        assertFalse(backup.getServerTable().getBukkitServers().isEmpty());
        assertFalse(securityTable.getUsers().isEmpty());
    }

    @Test
    public void testSaveWorldTimes() {
        saveUserOne();
        WorldTimes worldTimes = createWorldTimes();
        WorldTimesTable worldTimesTable = db.getWorldTimesTable();
        worldTimesTable.saveWorldTimes(playerUUID, 1, worldTimes);

        Session session = new Session(1, playerUUID, TestConstants.SERVER_UUID, 12345L, 23456L, 0, 0, 0);
        Map<Integer, Session> sessions = new HashMap<>();
        sessions.put(1, session);
        worldTimesTable.addWorldTimesToSessions(playerUUID, sessions);

        assertEquals(worldTimes, session.getWorldTimes());
    }

    @Test
    public void testSaveAllWorldTimes() {
        saveUserOne();
        WorldTimes worldTimes = createWorldTimes();
        System.out.println(worldTimes);
        WorldTimesTable worldTimesTable = db.getWorldTimesTable();
        Session session = new Session(1, playerUUID, TestConstants.SERVER_UUID, 12345L, 23456L, 0, 0, 0);
        session.setWorldTimes(worldTimes);

        Map<UUID, Map<UUID, List<Session>>> map = new HashMap<>();
        Map<UUID, List<Session>> sessionMap = new HashMap<>();
        List<Session> sessions = new ArrayList<>();
        sessions.add(session);
        sessionMap.put(playerUUID, sessions);
        map.put(ServerInfo.getServerUUID(), sessionMap);

        worldTimesTable.saveWorldTimes(map);

        Map<Integer, WorldTimes> worldTimesBySessionID = worldTimesTable.getAllWorldTimesBySessionID();
        assertEquals(worldTimes, worldTimesBySessionID.get(1));
    }

    @Test
    public void testSaveSessionsWorldTimes() {
        SessionsTable sessionsTable = db.getSessionsTable();

        saveUserOne();
        WorldTimes worldTimes = createWorldTimes();
        System.out.println(worldTimes);
        Session session = new Session(1, playerUUID, TestConstants.SERVER_UUID, 12345L, 23456L, 0, 0, 0);
        session.setWorldTimes(worldTimes);

        Map<UUID, Map<UUID, List<Session>>> map = new HashMap<>();
        Map<UUID, List<Session>> sessionMap = new HashMap<>();
        List<Session> sessions = new ArrayList<>();
        sessions.add(session);
        sessionMap.put(playerUUID, sessions);
        UUID serverUUID = ServerInfo.getServerUUID();
        map.put(serverUUID, sessionMap);

        sessionsTable.insertSessions(map, true);

        Map<UUID, Map<UUID, List<Session>>> allSessions = sessionsTable.getAllSessions(true);

        assertEquals(worldTimes, allSessions.get(serverUUID).get(playerUUID).get(0).getWorldTimes());
    }

    @Test
    public void testGetUserWorldTimes() {
        testSaveSessionsWorldTimes();
        WorldTimes worldTimesOfUser = db.getWorldTimesTable().getWorldTimesOfUser(playerUUID);
        assertEquals(createWorldTimes(), worldTimesOfUser);
    }

    @Test
    public void testGetServerWorldTimes() {
        testSaveSessionsWorldTimes();
        WorldTimes worldTimesOfServer = db.getWorldTimesTable().getWorldTimesOfServer(TestConstants.SERVER_UUID);
        assertEquals(createWorldTimes(), worldTimesOfServer);
    }

    @Test
    public void testRegisterProcessorRegisterException() {
        assertFalse(db.getUsersTable().isRegistered(playerUUID));
        assertFalse(db.getUserInfoTable().isRegistered(playerUUID));
        System.out.println("\n- Running RegisterProcessors -");
        List<RegisterProcessor> processors = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            processors.add(new RegisterProcessor(playerUUID, 500L, 1000L, "name", 4));
        }
        for (RegisterProcessor processor : processors) {
            processor.run();
        }
        System.out.println("- RegisterProcessors Run -\n");
        assertTrue(db.getUsersTable().isRegistered(playerUUID));
        assertTrue(db.getUserInfoTable().isRegistered(playerUUID));
    }

    @Test
    public void testRegister() {
        assertFalse(db.check().isPlayerRegistered(playerUUID));
        assertFalse(db.check().isPlayerRegisteredOnThisServer(playerUUID));
        db.save().registerNewUser(playerUUID, 1000L, "name");
        db.save().registerNewUserOnThisServer(playerUUID, 500L);
        assertTrue(db.check().isPlayerRegistered(playerUUID));
        assertTrue(db.check().isPlayerRegisteredOnThisServer(playerUUID));
    }

    @Test
    public void testWorldTableGetWorldNamesNoException() throws UnsupportedEncodingException, NoSuchAlgorithmException {
        saveAllData(db);
        Set<String> worldNames = db.getWorldTable().getWorldNames(TestConstants.SERVER_UUID);
        assertEquals(new HashSet<>(worlds), worldNames);
    }

    @Test
    public void testSettingTransfer() {
        String testString = RandomData.randomString(100);

        TransferTable transferTable = db.getTransferTable();
        transferTable.storeConfigSettings(Base64Util.encode(testString));
        Optional<String> configSettings = transferTable.getConfigSettings();

        assertTrue(configSettings.isPresent());
        assertEquals(testString, Base64Util.decode(configSettings.get()));
    }

    @Test
    public void testGetNetworkGeolocations() {
        GeoInfoTable geoInfoTable = db.getGeoInfoTable();
        UUID firstUuid = UUID.randomUUID();
        UUID secondUuid = UUID.randomUUID();
        UUID thirdUuid = UUID.randomUUID();

        UsersTable usersTable = db.getUsersTable();
        usersTable.registerUser(firstUuid, 0, "");
        usersTable.registerUser(secondUuid, 0, "");
        usersTable.registerUser(thirdUuid, 0, "");

        geoInfoTable.saveGeoInfo(firstUuid, new GeoInfo("-", "Test1", 0, "3"));
        GeoInfo secondInfo = new GeoInfo("-", "Test2", 5, "3");
        geoInfoTable.saveGeoInfo(firstUuid, secondInfo);
        geoInfoTable.saveGeoInfo(secondUuid, new GeoInfo("-", "Test3", 0, "3"));
        geoInfoTable.saveGeoInfo(thirdUuid, new GeoInfo("-", "Test4", 0, "3"));

        List<String> geolocations = geoInfoTable.getNetworkGeolocations();
        System.out.println(geolocations);

        assertNotNull(geolocations);
        assertFalse(geolocations.isEmpty());
        assertEquals(3, geolocations.size());
        assertTrue(geolocations.contains(secondInfo.getGeolocation()));
    }

    @Test
    public void testWorldTableAlterV16() {
        saveUserOne();
        new Table("test", db) {
            @Override
            public void createTable() {
                execute(
                        "INSERT INTO " + WorldTable.TABLE_NAME + " (" +
                                WorldTable.Col.NAME + ", " +
                                WorldTable.Col.SERVER_ID +
                                ") VALUES ('Test', '0')"
                );
                execute(
                        "INSERT INTO " + SessionsTable.TABLE_NAME + " (" +
                                SessionsTable.Col.SESSION_START + ", " +
                                SessionsTable.Col.SESSION_END + ", " +
                                SessionsTable.Col.AFK_TIME + ", " +
                                SessionsTable.Col.DEATHS + ", " +
                                SessionsTable.Col.MOB_KILLS + ", " +
                                SessionsTable.Col.SERVER_ID + ", " +
                                SessionsTable.Col.USER_ID +
                                ") VALUES ('0', '0', '0', '0', '0', '1', '1')"
                );
                execute(
                        "INSERT INTO " + WorldTimesTable.TABLE_NAME + " (" +
                                WorldTimesTable.Col.SERVER_ID + ", " +
                                WorldTimesTable.Col.SESSION_ID + ", " +
                                WorldTimesTable.Col.USER_ID + ", " +
                                WorldTimesTable.Col.WORLD_ID + ", " +
                                WorldTimesTable.Col.SURVIVAL + ", " +
                                WorldTimesTable.Col.CREATIVE + ", " +
                                WorldTimesTable.Col.SPECTATOR + ", " +
                                WorldTimesTable.Col.ADVENTURE +
                                ") VALUES ('1', '1', '1', '1', '0','0','0','0')"
                );
                execute(
                        "INSERT INTO " + ServerTable.TABLE_NAME + " (" +
                                ServerTable.Col.SERVER_UUID + ", " +
                                ServerTable.Col.SERVER_ID + ", " +
                                ServerTable.Col.MAX_PLAYERS + ", " +
                                ServerTable.Col.WEBSERVER_ADDRESS + ", " +
                                ServerTable.Col.INSTALLED + ", " +
                                ServerTable.Col.NAME +
                                ") VALUES ('" + UUID.randomUUID() + "', '2', '0', '0', '1', '2')"
                );
                execute(
                        "INSERT INTO " + SessionsTable.TABLE_NAME + " (" +
                                SessionsTable.Col.SESSION_START + ", " +
                                SessionsTable.Col.SESSION_END + ", " +
                                SessionsTable.Col.AFK_TIME + ", " +
                                SessionsTable.Col.DEATHS + ", " +
                                SessionsTable.Col.MOB_KILLS + ", " +
                                SessionsTable.Col.SERVER_ID + ", " +
                                SessionsTable.Col.USER_ID +
                                ") VALUES ('0', '0', '0', '0', '0', '2', '1')"
                );
                execute(
                        "INSERT INTO " + WorldTimesTable.TABLE_NAME + " (" +
                                WorldTimesTable.Col.SERVER_ID + ", " +
                                WorldTimesTable.Col.SESSION_ID + ", " +
                                WorldTimesTable.Col.USER_ID + ", " +
                                WorldTimesTable.Col.WORLD_ID + ", " +
                                WorldTimesTable.Col.SURVIVAL + ", " +
                                WorldTimesTable.Col.CREATIVE + ", " +
                                WorldTimesTable.Col.SPECTATOR + ", " +
                                WorldTimesTable.Col.ADVENTURE +
                                ") VALUES ('2', '2', '1', '1', '0','0','0','0')"
                );
            }
        }.createTable();

        WorldTable worldTable = db.getWorldTable();
        Map<Integer, List<Integer>> before = worldTable.getWorldIDsByServerIDs();
        System.out.println("\nBefore: " + before);
        System.out.println("Before: " + worldTable.getWorldObjects() + "\n");

        worldTable.alterTableV16();

        Map<Integer, List<Integer>> after = worldTable.getWorldIDsByServerIDs();
        System.out.println("\nAfter: " + after);
        System.out.println("After: " + worldTable.getWorldObjects() + "\n");
        assertNull(after.get(0));
        assertNotNull(after.get(1));
        assertEquals(1, after.get(1).size());
        assertNotNull(after.get(2));
        assertEquals(1, after.get(2).size());
    }

    @Test
    public void testNewContainerForPlayer() throws UnsupportedEncodingException, NoSuchAlgorithmException {
        saveAllData(db);

        long start = System.nanoTime();

        PlayerContainer container = db.fetch().getPlayerContainer(playerUUID);

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
        container.putRawData(PlayerKeys.ACTIVE_SESSION, new Session(TestConstants.PLAYER_ONE_UUID, System.currentTimeMillis(), "TestWorld", "SURVIVAL"));
        assertTrue(container.supports(PlayerKeys.ACTIVE_SESSION));

        long end = System.nanoTime();

        assertFalse("Took too long: " + ((end - start) / 1000000.0) + "ms", end - start > TimeAmount.SECOND.ns());

        OptionalAssert.equals(playerUUID, container.getValue(PlayerKeys.UUID));
        OptionalAssert.equals(123456789L, container.getValue(PlayerKeys.REGISTERED));
        OptionalAssert.equals("Test", container.getValue(PlayerKeys.NAME));
        OptionalAssert.equals(1, container.getValue(PlayerKeys.KICK_COUNT));

        List<GeoInfo> expectedGeoInfo =
                Collections.singletonList(new GeoInfo("1.2.3.4", "TestLoc", 223456789, "ZpT4PJ9HbaMfXfa8xSADTn5X1CHSR7nTT0ntv8hKdkw="));
        OptionalAssert.equals(expectedGeoInfo, container.getValue(PlayerKeys.GEO_INFO));

        List<Nickname> expectedNicknames = Collections.singletonList(new Nickname("TestNick", -1, TestConstants.SERVER_UUID));
        OptionalAssert.equals(expectedNicknames, container.getValue(PlayerKeys.NICKNAMES));

        OptionalAssert.equals(false, container.getValue(PlayerKeys.OPERATOR));
        OptionalAssert.equals(false, container.getValue(PlayerKeys.BANNED));

        // TODO Test rest
    }

    @Test
    public void playerContainerSupportsAllPlayerKeys() throws UnsupportedEncodingException, NoSuchAlgorithmException, IllegalAccessException {
        saveAllData(db);

        PlayerContainer playerContainer = db.fetch().getPlayerContainer(TestConstants.PLAYER_ONE_UUID);
        // Active sessions are added after fetching
        playerContainer.putRawData(PlayerKeys.ACTIVE_SESSION, RandomData.randomSession());

        List<String> unsupported = new ArrayList<>();
        for (Field field : PlayerKeys.class.getDeclaredFields()) {
            Key key = (Key) field.get(null);
            if (!playerContainer.supports(key)) {
                unsupported.add(field.getName());
            }
        }

        assertTrue("Some keys are not supported by PlayerContainer: PlayerKeys." + unsupported.toString(), unsupported.isEmpty());
    }

    @Test
    public void serverContainerSupportsAllServerKeys() throws UnsupportedEncodingException, NoSuchAlgorithmException, IllegalAccessException {
        saveAllData(db);

        ServerContainer serverContainer = db.fetch().getServerContainer(TestConstants.SERVER_UUID);

        List<String> unsupported = new ArrayList<>();
        for (Field field : ServerKeys.class.getDeclaredFields()) {
            Key key = (Key) field.get(null);
            if (!serverContainer.supports(key)) {
                unsupported.add(field.getName());
            }
        }

        assertTrue("Some keys are not supported by ServerContainer: ServerKeys." + unsupported.toString(), unsupported.isEmpty());
    }

    @Test
    public void analysisContainerSupportsAllAnalysisKeys() throws IllegalAccessException, UnsupportedEncodingException, NoSuchAlgorithmException {
        serverContainerSupportsAllServerKeys();
        AnalysisContainer analysisContainer = new AnalysisContainer(db.fetch().getServerContainer(TestConstants.SERVER_UUID));

        List<String> unsupported = new ArrayList<>();
        for (Field field : AnalysisKeys.class.getDeclaredFields()) {
            Key key = (Key) field.get(null);
            if (!analysisContainer.supports(key)) {
                unsupported.add(field.getName());
            }
        }

        assertTrue("Some keys are not supported by AnalysisContainer: AnalysisKeys." + unsupported.toString(), unsupported.isEmpty());
    }
}
