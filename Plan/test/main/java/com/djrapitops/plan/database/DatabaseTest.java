/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.djrapitops.plan.database;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.api.exceptions.DatabaseInitException;
import com.djrapitops.plan.data.WebUser;
import com.djrapitops.plan.data.container.*;
import com.djrapitops.plan.data.time.GMTimes;
import com.djrapitops.plan.data.time.WorldTimes;
import com.djrapitops.plan.database.databases.MySQLDB;
import com.djrapitops.plan.database.databases.SQLDB;
import com.djrapitops.plan.database.databases.SQLiteDB;
import com.djrapitops.plan.database.tables.*;
import com.djrapitops.plan.systems.cache.DataCache;
import com.djrapitops.plan.systems.info.server.ServerInfo;
import com.djrapitops.plan.systems.processing.player.RegisterProcessor;
import com.djrapitops.plan.utilities.ManageUtils;
import com.djrapitops.plan.utilities.MiscUtils;
import com.djrapitops.plan.utilities.analysis.MathUtils;
import com.djrapitops.plan.utilities.file.FileUtil;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import test.utilities.MockUtils;
import test.utilities.RandomData;
import test.utilities.TestInit;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.sql.SQLException;
import java.util.*;

import static org.junit.Assert.*;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * @author Rsl1122
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({JavaPlugin.class})
public class DatabaseTest {

    private final UUID uuid = MockUtils.getPlayerUUID();
    private final List<String> worlds = Arrays.asList("TestWorld", "TestWorld2");
    private final UUID uuid2 = MockUtils.getPlayer2UUID();
    private Plan plan;
    private Database db;
    private Database backup;
    private int rows;

    @Rule
    public Timeout globalTimeout = Timeout.seconds(7); // 5 seconds max per method tested


    @Before
    public void setUp() throws Exception {
        TestInit t = TestInit.init();
        plan = t.getPlanMock();

        db = new SQLiteDB("debug" + MiscUtils.getTime());
        db.init();

        when(plan.getDB()).thenReturn(db);
        DataCache dataCache = new DataCache(plan) {
            @Override
            public void markFirstSession(UUID uuid) {
            }
        };
        when(plan.getDataCache()).thenReturn(dataCache);

        db.getServerTable().saveCurrentServerInfo(new ServerInfo(-1, TestInit.getServerUUID(), "ServerName", "", 20));

        File f = new File(plan.getDataFolder(), "Errors.txt");
        rows = FileUtil.lines(f).size();
    }

    @After
    public void tearDown() throws IOException, SQLException {
        db.close();
        if (backup != null) {
            backup.close();
        }

        File f = new File(plan.getDataFolder(), "Errors.txt");

        List<String> lines = FileUtil.lines(f);
        int rowsAgain = lines.size();
        if (rowsAgain > 0) {
            for (String line : lines) {
                System.out.println(line);
            }
        }

        assertTrue("Errors were caught.", rows == rowsAgain);
    }

    @Test
    public void testInit() throws DatabaseInitException {
        db.init();
    }

    @Test
    public void testNoExceptionWhenCommitEmpty() throws Exception {
        db.init();

        db.commit(((SQLDB) db).getConnection());
        db.commit(((SQLDB) db).getConnection());
        db.commit(((SQLDB) db).getConnection());
    }

    @Test
    public void testSQLiteGetConfigName() {
        assertEquals("sqlite", db.getConfigName());
    }

    @Test
    public void testSQLiteGetName() {
        assertEquals("SQLite", db.getName());
    }

    @Test
    public void testMySQLGetConfigName() {
        assertEquals("mysql", new MySQLDB().getConfigName());
    }

    @Test
    public void testMySQLGetName() {
        assertEquals("MySQL", new MySQLDB().getName());
    }

    @Test(timeout = 3000)
    public void testSaveCommandUse() throws SQLException, DatabaseInitException {
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

        Map<String, Integer> commandUse = db.getCommandUse();
        assertEquals(expected, commandUse);

        for (int i = 0; i < 3; i++) {
            commandUseTable.commandUsed("test");
        }

        for (int i = 0; i < 2; i++) {
            commandUseTable.commandUsed("tp");
        }

        expected.put("test", 3);
        expected.put("tp", 6);

        commandUse = db.getCommandUse();

        assertEquals(expected, commandUse);
    }

    @Test
    public void testCommandUseTableIDSystem() throws SQLException {
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

    private void saveUserOne() throws SQLException {
        saveUserOne(db);
    }

    private void saveUserOne(Database database) throws SQLException {
        database.getUsersTable().registerUser(uuid, 123456789L, "Test");
    }

    private void saveUserTwo() throws SQLException {
        saveUserTwo(db);
    }

    private void saveUserTwo(Database database) throws SQLException {
        database.getUsersTable().registerUser(uuid2, 123456789L, "Test");
    }

    @Test
    public void testActionsTable() throws SQLException {
        saveUserOne();
        ActionsTable actionsTable = db.getActionsTable();

        Action save = new Action(234567890L, Actions.FIRST_SESSION, "Additional Info");
        Action expected = new Action(234567890L, Actions.FIRST_SESSION, "Additional Info", 1);

        actionsTable.insertAction(uuid, save);

        List<Action> actions = actionsTable.getActions(uuid);
        assertEquals(expected, actions.get(0));
    }

    @Test
    public void testIPTable() throws SQLException, DatabaseInitException {
        saveUserOne();
        IPsTable ipsTable = db.getIpsTable();

        String expectedIP = "1.2.3.4";
        String expectedGeoLoc = "TestLocation";
        long time = MiscUtils.getTime();

        GeoInfo expected = new GeoInfo(expectedIP, expectedGeoLoc, time);
        ipsTable.saveGeoInfo(uuid, expected);
        ipsTable.saveGeoInfo(uuid, expected);
        commitTest();

        List<GeoInfo> getInfo = ipsTable.getGeoInfo(uuid);
        assertEquals(1, getInfo.size());
        GeoInfo actual = getInfo.get(0);
        assertEquals(expected, actual);
        assertEquals(time, actual.getLastUsed());


        Optional<String> result = ipsTable.getGeolocation(expectedIP);
        assertTrue(result.isPresent());
        assertEquals(expectedGeoLoc, result.get());
    }

    @Test
    public void testNicknamesTable() throws SQLException, DatabaseInitException {
        saveUserOne();
        NicknamesTable nickTable = db.getNicknamesTable();

        String expected = "TestNickname";
        nickTable.saveUserName(uuid, expected);
        nickTable.saveUserName(uuid, expected);
        commitTest();

        List<String> nicknames = nickTable.getNicknames(uuid);
        assertEquals(1, nicknames.size());
        assertEquals(expected, nicknames.get(0));

        Map<UUID, List<String>> allNicknames = nickTable.getAllNicknames(uuid);
        assertEquals(nicknames, allNicknames.get(Plan.getServerUUID()));
    }

    @Test
    public void testSecurityTable() throws SQLException, DatabaseInitException {
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
    public void testWorldTable() throws SQLException, DatabaseInitException {
        WorldTable worldTable = db.getWorldTable();
        List<String> worlds = Arrays.asList("Test", "Test2", "Test3");
        worldTable.saveWorlds(worlds);

        commitTest();

        List<String> saved = worldTable.getWorlds();
        assertEquals(new HashSet<>(worlds), new HashSet<>(saved));
    }

    private void saveTwoWorlds() throws SQLException {
        saveTwoWorlds(db);
    }

    private void saveTwoWorlds(Database database) throws SQLException {
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
        kills.add(new PlayerKill(uuid2, "Iron Sword", 4321L));
        kills.add(new PlayerKill(uuid2, "Gold Sword", 5321L));
        return kills;
    }

    @Test
    public void testSessionPlaytimeSaving() throws SQLException, DatabaseInitException {
        saveTwoWorlds();
        saveUserOne();
        saveUserTwo();
        Session session = new Session(12345L, "", "");
        session.endSession(22345L);
        session.setWorldTimes(createWorldTimes());
        session.setPlayerKills(createKills());

        long expectedLength = 10000L;
        assertEquals(expectedLength, session.getLength());
        assertEquals(expectedLength, session.getWorldTimes().getTotal());

        SessionsTable sessionsTable = db.getSessionsTable();
        sessionsTable.saveSession(uuid, session);

        commitTest();

        assertEquals(expectedLength, sessionsTable.getPlaytime(uuid));
        assertEquals(0L, sessionsTable.getPlaytime(uuid, 30000L));

        long playtimeOfServer = sessionsTable.getPlaytimeOfServer(TestInit.getServerUUID());
        assertEquals(expectedLength, playtimeOfServer);
        assertEquals(0L, sessionsTable.getPlaytimeOfServer(TestInit.getServerUUID(), 30000L));

        assertEquals(1, sessionsTable.getSessionCount(uuid));
        assertEquals(0, sessionsTable.getSessionCount(uuid, 30000L));
    }

    @Test
    public void testSessionSaving() throws SQLException, DatabaseInitException {
        saveUserOne();
        saveUserTwo();

        Session session = new Session(12345L, "", "");
        session.endSession(22345L);
        session.setWorldTimes(createWorldTimes());
        session.setPlayerKills(createKills());

        SessionsTable sessionsTable = db.getSessionsTable();
        sessionsTable.saveSession(uuid, session);

        commitTest();

        Map<UUID, List<Session>> sessions = sessionsTable.getSessions(uuid);

        for (Map.Entry<UUID, List<Session>> entry : sessions.entrySet()) {
            UUID key = entry.getKey();
            if (key == null) {
                System.out.print("null");
            } else {
                System.out.print(key);
            }
            System.out.println(" " + entry.getValue());
        }

        List<Session> savedSessions = sessions.get(Plan.getServerUUID());

        assertNotNull(savedSessions);
        assertEquals(1, savedSessions.size());
        assertNull(sessions.get(UUID.randomUUID()));

        assertEquals(session, savedSessions.get(0));

        Map<UUID, Long> lastSeen = sessionsTable.getLastSeenForAllPlayers();
        assertTrue(lastSeen.containsKey(uuid));
        assertFalse(lastSeen.containsKey(uuid2));
        assertEquals(22345L, (long) lastSeen.get(uuid));
    }

    @Test
    public void testUserInfoTableRegisterUnRegistered() throws SQLException, DatabaseInitException {
        UserInfoTable userInfoTable = db.getUserInfoTable();
        assertFalse(userInfoTable.isRegistered(uuid));
        UsersTable usersTable = db.getUsersTable();
        assertFalse(usersTable.isRegistered(uuid));

        userInfoTable.registerUserInfo(uuid, 123456789L);

        commitTest();

        assertTrue(usersTable.isRegistered(uuid));
        assertTrue(userInfoTable.isRegistered(uuid));

        UserInfo userInfo = userInfoTable.getUserInfo(uuid);
        assertEquals(uuid, userInfo.getUuid());
        assertEquals(123456789L, (long) usersTable.getRegisterDates().get(0));
        assertEquals(123456789L, userInfo.getRegistered());
        assertEquals(1, userInfoTable.getServerUserCount(Plan.getServerUUID()));
        assertEquals("Waiting for Update..", userInfo.getName());
        assertFalse(userInfo.isBanned());
        assertFalse(userInfo.isOpped());
    }

    @Test
    public void testUserInfoTableRegisterRegistered() throws SQLException, DatabaseInitException {
        saveUserOne();
        UsersTable usersTable = db.getUsersTable();
        assertTrue(usersTable.isRegistered(uuid));

        UserInfoTable userInfoTable = db.getUserInfoTable();
        assertFalse(userInfoTable.isRegistered(uuid));

        userInfoTable.registerUserInfo(uuid, 223456789L);
        commitTest();

        assertTrue(usersTable.isRegistered(uuid));
        assertTrue(userInfoTable.isRegistered(uuid));

        UserInfo userInfo = userInfoTable.getUserInfo(uuid);
        assertEquals(uuid, userInfo.getUuid());
        assertEquals(123456789L, (long) usersTable.getRegisterDates().get(0));
        assertEquals(223456789L, userInfo.getRegistered());
        assertEquals("Test", userInfo.getName());
        assertFalse(userInfo.isBanned());
        assertFalse(userInfo.isOpped());

        assertEquals(userInfo, userInfoTable.getServerUserInfo().get(0));
    }

    @Test
    public void testUserInfoTableUpdateBannedOpped() throws SQLException, DatabaseInitException {
        UserInfoTable userInfoTable = db.getUserInfoTable();
        userInfoTable.registerUserInfo(uuid, 223456789L);
        assertTrue(userInfoTable.isRegistered(uuid));

        userInfoTable.updateOpAndBanStatus(uuid, true, true);
        commitTest();

        UserInfo userInfo = userInfoTable.getUserInfo(uuid);
        assertTrue(userInfo.isBanned());
        assertTrue(userInfo.isOpped());

        userInfoTable.updateOpAndBanStatus(uuid, false, true);
        commitTest();

        userInfo = userInfoTable.getUserInfo(uuid);

        assertTrue(userInfo.isBanned());
        assertFalse(userInfo.isOpped());

        userInfoTable.updateOpAndBanStatus(uuid, false, false);
        commitTest();

        userInfo = userInfoTable.getUserInfo(uuid);

        assertFalse(userInfo.isBanned());
        assertFalse(userInfo.isOpped());
    }

    @Test
    public void testUsersTableUpdateName() throws SQLException, DatabaseInitException {
        saveUserOne();

        UsersTable usersTable = db.getUsersTable();

        assertEquals(uuid, usersTable.getUuidOf("Test"));
        usersTable.updateName(uuid, "NewName");

        commitTest();

        assertNull(usersTable.getUuidOf("Test"));

        assertEquals("NewName", usersTable.getPlayerName(uuid));
        assertEquals(uuid, usersTable.getUuidOf("NewName"));
    }

    @Test
    public void testUsersTableKickSaving() throws SQLException, DatabaseInitException {
        saveUserOne();
        UsersTable usersTable = db.getUsersTable();
        assertEquals(0, usersTable.getTimesKicked(uuid));

        int random = new Random().nextInt(20);

        for (int i = 0; i < random + 1; i++) {
            usersTable.kicked(uuid);
        }
        commitTest();
        assertEquals(random + 1, usersTable.getTimesKicked(uuid));
    }

    @Test
    public void testRemovalSingleUser() throws SQLException {
        saveUserTwo();

        UserInfoTable userInfoTable = db.getUserInfoTable();
        UsersTable usersTable = db.getUsersTable();
        SessionsTable sessionsTable = db.getSessionsTable();
        NicknamesTable nicknamesTable = db.getNicknamesTable();
        IPsTable ipsTable = db.getIpsTable();
        ActionsTable actionsTable = db.getActionsTable();

        userInfoTable.registerUserInfo(uuid, 223456789L);
        saveTwoWorlds();

        Session session = new Session(12345L, "", "");
        session.endSession(22345L);
        session.setWorldTimes(createWorldTimes());
        session.setPlayerKills(createKills());

        sessionsTable.saveSession(uuid, session);
        nicknamesTable.saveUserName(uuid, "TestNick");
        ipsTable.saveGeoInfo(uuid, new GeoInfo("1.2.3.4", "TestLoc", 223456789L));
        actionsTable.insertAction(uuid, new Action(1324L, Actions.FIRST_SESSION, "Add"));

        assertTrue(usersTable.isRegistered(uuid));

        db.removeAccount(uuid);

        assertFalse(usersTable.isRegistered(uuid));
        assertFalse(userInfoTable.isRegistered(uuid));
        assertTrue(nicknamesTable.getNicknames(uuid).isEmpty());
        assertTrue(ipsTable.getGeoInfo(uuid).isEmpty());
        assertTrue(sessionsTable.getSessions(uuid).isEmpty());
        assertTrue(actionsTable.getActions(uuid).isEmpty());
    }

    @Test
    public void testRemovalEverything() throws SQLException {
        UserInfoTable userInfoTable = db.getUserInfoTable();
        UsersTable usersTable = db.getUsersTable();
        SessionsTable sessionsTable = db.getSessionsTable();
        NicknamesTable nicknamesTable = db.getNicknamesTable();
        IPsTable ipsTable = db.getIpsTable();
        ActionsTable actionsTable = db.getActionsTable();
        TPSTable tpsTable = db.getTpsTable();
        SecurityTable securityTable = db.getSecurityTable();

        saveAllData(db);

        db.removeAllData();

        assertFalse(usersTable.isRegistered(uuid));
        assertFalse(usersTable.isRegistered(uuid2));
        assertFalse(userInfoTable.isRegistered(uuid));

        assertTrue(nicknamesTable.getNicknames(uuid).isEmpty());
        assertTrue(ipsTable.getGeoInfo(uuid).isEmpty());
        assertTrue(sessionsTable.getSessions(uuid).isEmpty());
        assertTrue(actionsTable.getActions(uuid).isEmpty());
        assertTrue(db.getCommandUse().isEmpty());
        assertTrue(db.getWorldTable().getWorlds().isEmpty());
        assertTrue(tpsTable.getTPSData().isEmpty());
        assertTrue(db.getServerTable().getBukkitServers().isEmpty());
        assertTrue(securityTable.getUsers().isEmpty());
    }

    private void saveAllData(Database database) throws SQLException {
        System.out.println("Saving all possible data to the Database..");
        UserInfoTable userInfoTable = database.getUserInfoTable();
        UsersTable usersTable = database.getUsersTable();
        SessionsTable sessionsTable = database.getSessionsTable();
        NicknamesTable nicknamesTable = database.getNicknamesTable();
        IPsTable ipsTable = database.getIpsTable();
        ActionsTable actionsTable = database.getActionsTable();
        TPSTable tpsTable = database.getTpsTable();
        SecurityTable securityTable = database.getSecurityTable();

        saveUserOne(database);
        saveUserTwo(database);

        userInfoTable.registerUserInfo(uuid, 223456789L);
        saveTwoWorlds(database);

        Session session = new Session(12345L, "", "");
        session.endSession(22345L);
        session.setWorldTimes(createWorldTimes());
        session.setPlayerKills(createKills());

        sessionsTable.saveSession(uuid, session);
        nicknamesTable.saveUserName(uuid, "TestNick");
        ipsTable.saveGeoInfo(uuid, new GeoInfo("1.2.3.4", "TestLoc", 223456789L));
        actionsTable.insertAction(uuid, new Action(1324L, Actions.FIRST_SESSION, "Add"));

        assertTrue(usersTable.isRegistered(uuid));

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
        final double averageCPUUsage = MathUtils.round(operatingSystemMXBean.getSystemLoadAverage() / availableProcessors * 100.0);
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
    public void testServerTableBungeeSave() throws SQLException, DatabaseInitException {
        ServerTable serverTable = db.getServerTable();

        Optional<ServerInfo> bungeeInfo = serverTable.getBungeeInfo();
        assertFalse(bungeeInfo.isPresent());

        UUID bungeeUUID = UUID.randomUUID();
        ServerInfo bungeeCord = new ServerInfo(-1, bungeeUUID, "BungeeCord", "Random:1234", 20);
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
    public void testServerTableBungee() throws SQLException, DatabaseInitException {
        testServerTableBungeeSave();
        ServerTable serverTable = db.getServerTable();

        List<ServerInfo> bukkitServers = serverTable.getBukkitServers();
        assertEquals(1, bukkitServers.size());
    }

    @Test
    public void testSessionTableNPEWhenNoPlayers() throws SQLException {
        Map<UUID, Long> lastSeen = db.getSessionsTable().getLastSeenForAllPlayers();
        assertTrue(lastSeen.isEmpty());
    }

    private void commitTest() throws DatabaseInitException, SQLException {
        db.close();
        db.init();
    }

    @Test
    public void testSessionTableGetInfoOfServer() throws SQLException, DatabaseInitException {
        saveUserOne();
        saveUserTwo();

        Session session = new Session(12345L, "", "");
        session.endSession(22345L);
        session.setWorldTimes(createWorldTimes());
        session.setPlayerKills(createKills());

        SessionsTable sessionsTable = db.getSessionsTable();
        sessionsTable.saveSession(uuid, session);

        commitTest();

        Map<UUID, List<Session>> sessions = sessionsTable.getSessionInfoOfServer();

        session.setPlayerKills(new ArrayList<>());
        session.setWorldTimes(new WorldTimes(new HashMap<>()));

        List<Session> sSessions = sessions.get(uuid);
        assertFalse(sessions.isEmpty());
        assertNotNull(sSessions);
        assertFalse(sSessions.isEmpty());
        assertEquals(session, sSessions.get(0));
    }

    @Test
    public void testKillTableGetKillsOfServer() throws SQLException, DatabaseInitException {
        saveUserOne();
        saveUserTwo();

        KillsTable killsTable = db.getKillsTable();
        List<PlayerKill> expected = createKills();
        killsTable.savePlayerKills(uuid, 1, expected);

        commitTest();

        Map<UUID, List<PlayerKill>> playerKills = killsTable.getPlayerKills();
        List<PlayerKill> kills = playerKills.get(uuid);
        assertFalse(playerKills.isEmpty());
        assertNotNull(kills);
        assertFalse(kills.isEmpty());
        assertEquals(expected, kills);
    }

    @Test
    public void testBackupAndRestore() throws SQLException, DatabaseInitException {
        SQLiteDB backup = new SQLiteDB("debug-backup" + MiscUtils.getTime());
        backup.init();

        saveAllData(db);

        ManageUtils.clearAndCopy(backup, db);

        UserInfoTable userInfoTable = backup.getUserInfoTable();
        UsersTable usersTable = backup.getUsersTable();
        SessionsTable sessionsTable = backup.getSessionsTable();
        NicknamesTable nicknamesTable = backup.getNicknamesTable();
        IPsTable ipsTable = backup.getIpsTable();
        ActionsTable actionsTable = backup.getActionsTable();
        TPSTable tpsTable = backup.getTpsTable();
        SecurityTable securityTable = backup.getSecurityTable();

        assertTrue(usersTable.isRegistered(uuid));
        assertTrue(usersTable.isRegistered(uuid2));
        assertTrue(userInfoTable.isRegistered(uuid));

        assertFalse(nicknamesTable.getNicknames(uuid).isEmpty());
        assertFalse(ipsTable.getGeoInfo(uuid).isEmpty());
        assertFalse(sessionsTable.getSessions(uuid).isEmpty());
        assertFalse(actionsTable.getActions(uuid).isEmpty());
        assertFalse(backup.getCommandUse().isEmpty());
        assertFalse(backup.getWorldTable().getWorlds().isEmpty());
        assertFalse(tpsTable.getTPSData().isEmpty());
        assertFalse(backup.getServerTable().getBukkitServers().isEmpty());
        assertFalse(securityTable.getUsers().isEmpty());
    }

    @Test
    public void testSaveWorldTimes() throws SQLException {
        saveUserOne();
        WorldTimes worldTimes = createWorldTimes();
        WorldTimesTable worldTimesTable = db.getWorldTimesTable();
        worldTimesTable.saveWorldTimes(uuid, 1, worldTimes);

        Session session = new Session(1, 12345L, 23456L, 0, 0);
        Map<Integer, Session> sessions = new HashMap<>();
        sessions.put(1, session);
        worldTimesTable.addWorldTimesToSessions(uuid, sessions);

        assertEquals(worldTimes, session.getWorldTimes());
    }

    @Test
    public void testSaveAllWorldTimes() throws SQLException {
        saveUserOne();
        WorldTimes worldTimes = createWorldTimes();
        System.out.println(worldTimes);
        WorldTimesTable worldTimesTable = db.getWorldTimesTable();
        Session session = new Session(1, 12345L, 23456L, 0, 0);
        session.setWorldTimes(worldTimes);

        Map<UUID, Map<UUID, List<Session>>> map = new HashMap<>();
        Map<UUID, List<Session>> sessionMap = new HashMap<>();
        List<Session> sessions = new ArrayList<>();
        sessions.add(session);
        sessionMap.put(uuid, sessions);
        map.put(Plan.getServerUUID(), sessionMap);

        worldTimesTable.saveWorldTimes(map);

        Map<Integer, WorldTimes> worldTimesBySessionID = worldTimesTable.getAllWorldTimesBySessionID();
        assertEquals(worldTimes, worldTimesBySessionID.get(1));
    }

    @Test
    public void testSaveSessionsWorldTimes() throws SQLException {
        SessionsTable sessionsTable = db.getSessionsTable();

        saveUserOne();
        WorldTimes worldTimes = createWorldTimes();
        System.out.println(worldTimes);
        Session session = new Session(1, 12345L, 23456L, 0, 0);
        session.setWorldTimes(worldTimes);

        Map<UUID, Map<UUID, List<Session>>> map = new HashMap<>();
        Map<UUID, List<Session>> sessionMap = new HashMap<>();
        List<Session> sessions = new ArrayList<>();
        sessions.add(session);
        sessionMap.put(uuid, sessions);
        UUID serverUUID = Plan.getServerUUID();
        map.put(serverUUID, sessionMap);

        sessionsTable.insertSessions(map, true);

        Map<UUID, Map<UUID, List<Session>>> allSessions = sessionsTable.getAllSessions(true);

        assertEquals(worldTimes, allSessions.get(serverUUID).get(uuid).get(0).getWorldTimes());
    }

    @Test
    public void testRegisterProcessorRegisterException() throws SQLException {
        assertFalse(db.getUsersTable().isRegistered(uuid));
        assertFalse(db.getUserInfoTable().isRegistered(uuid));
        for (int i = 0; i < 200; i++) {
            new RegisterProcessor(uuid, 500L, 1000L, "name", 4).process();
        }
        assertTrue(db.getUsersTable().isRegistered(uuid));
        assertTrue(db.getUserInfoTable().isRegistered(uuid));
    }

    @Test
    public void testWorldTableGetWorldNamesNoException() throws SQLException {
        Set<String> worldNames = db.getWorldTable().getWorldNames();
    }
}
