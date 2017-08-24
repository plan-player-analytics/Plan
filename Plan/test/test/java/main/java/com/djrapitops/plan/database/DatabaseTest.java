/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.main.java.com.djrapitops.plan.database;

import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.*;
import main.java.com.djrapitops.plan.data.time.GMTimes;
import main.java.com.djrapitops.plan.data.time.WorldTimes;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.database.databases.MySQLDB;
import main.java.com.djrapitops.plan.database.databases.SQLiteDB;
import main.java.com.djrapitops.plan.database.tables.*;
import main.java.com.djrapitops.plan.systems.info.server.ServerInfo;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import main.java.com.djrapitops.plan.utilities.analysis.MathUtils;
import main.java.com.djrapitops.plan.utilities.file.FileUtil;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import test.java.utils.MockUtils;
import test.java.utils.TestInit;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.sql.SQLException;
import java.util.*;

import static org.junit.Assert.*;

/**
 * @author Rsl1122
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(JavaPlugin.class)
public class DatabaseTest {

    private Plan plan;
    private Database db;
    private Database backup;
    private int rows;
    private UUID uuid = MockUtils.getPlayerUUID();
    private List<String> worlds = Arrays.asList("TestWorld", "TestWorld2");
    private UUID uuid2 = MockUtils.getPlayer2UUID();

    public DatabaseTest() {
    }

    @Before
    public void setUp() throws Exception {
        TestInit t = TestInit.init();
        plan = t.getPlanMock();
        db = new SQLiteDB(plan, "debug" + MiscUtils.getTime()) {
            @Override
            public void startConnectionPingTask() {

            }
        };
        db.init();
        db.getServerTable().saveCurrentServerInfo(new ServerInfo(-1, t.getServerUUID(), "ServerName", ""));
        File f = new File(plan.getDataFolder(), "Errors.txt");
        rows = FileUtil.lines(f).size();

        db.init();
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
    public void testInit() {
        assertTrue("Database failed to init.", db.init());
    }

    @Test
    public void testSqLiteGetConfigName() {
        assertEquals("sqlite", db.getConfigName());
    }

    @Test
    public void testSqLiteGetgName() {
        assertEquals("SQLite", db.getName());
    }

    @Test
    public void testMysqlGetConfigName() {
        assertEquals("mysql", new MySQLDB(plan).getConfigName());
    }

    @Test
    public void testMysqlGetName() {
        assertEquals("MySQL", new MySQLDB(plan).getName());
    }

    @Test
    public void testSaveCommandUse() throws SQLException {
        Map<String, Integer> expected = new HashMap<>();

        expected.put("plan", 1);
        expected.put("tp", 4);
        expected.put("pla", 7);
        expected.put("help", 21);
        expected.put("roiergbnougbierubieugbeigubeigubgierbgeugeg", 3);

        db.saveCommandUse(expected);

        expected.remove("roiergbnougbierubieugbeigubeigubgierbgeugeg");

        Map<String, Integer> commandUse = db.getCommandUse();
        assertEquals(expected, commandUse);

        expected.put("test", 3);
        expected.put("tp", 6);
        expected.put("pla", 4);

        db.saveCommandUse(expected);

        expected.put("pla", 7);

        commandUse = db.getCommandUse();

        assertEquals(expected, commandUse);
    }

    @Test
    public void testCommandUseTableIDSystem() throws SQLException {
        Map<String, Integer> save = new HashMap<>();
        save.put("plan", 1);
        save.put("tp", 4);
        save.put("pla", 7);
        save.put("help", 21);
        save.put("roiergbnougbierubieugbeigubeigubgierbgeugeg", 3);
        db.saveCommandUse(save);

        CommandUseTable commandUseTable = db.getCommandUseTable();
        Optional<Integer> id = commandUseTable.getCommandID("plan");
        assertTrue(id.isPresent());
        Optional<String> commandByID = commandUseTable.getCommandByID(id.get());
        assertTrue(commandByID.isPresent());
        assertEquals("plan", commandByID.get());
        assertFalse(commandUseTable.getCommandID("roiergbnougbierubieugbeigubeigubgierbgeugeg").isPresent());
    }

    @Test
    public void testTPSSaving() throws SQLException {
        db.init();
        TPSTable tpsTable = db.getTpsTable();
        Random r = new Random();

        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        int availableProcessors = ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors();
        final double averageCPUUsage = MathUtils.round(operatingSystemMXBean.getSystemLoadAverage() / availableProcessors * 100.0);

        final long usedMemory = 51231251254L;
        final int entityCount = 6123;
        final int chunksLoaded = 2134;

        List<TPS> expected = new ArrayList<>();

        expected.add(new TPS(r.nextLong(), r.nextDouble(), r.nextInt(100000000), averageCPUUsage, usedMemory, entityCount, chunksLoaded));
        expected.add(new TPS(r.nextLong(), r.nextDouble(), r.nextInt(100000000), averageCPUUsage, usedMemory, entityCount, chunksLoaded));
        expected.add(new TPS(r.nextLong(), r.nextDouble(), r.nextInt(100000000), averageCPUUsage, usedMemory, entityCount, chunksLoaded));
        expected.add(new TPS(r.nextLong(), r.nextDouble(), r.nextInt(100000000), averageCPUUsage, usedMemory, entityCount, chunksLoaded));

        for (TPS tps : expected) {
            tpsTable.insertTPS(tps);
        }

        assertEquals(expected, tpsTable.getTPSData());
    }

    private void saveUserOne() throws SQLException {
        db.getUsersTable().registerUser(uuid, 123456789L, "Test");
    }

    private void saveUserTwo() throws SQLException {
        db.getUsersTable().registerUser(uuid2, 123456789L, "Test");
    }

    @Test
    public void testActionsTable() throws SQLException {
        saveUserOne();
        ActionsTable actionsTable = db.getActionsTable();

        Action save = new Action(234567890L, Actions.REGISTERED, "Additional Info");
        Action expected = new Action(234567890L, Actions.REGISTERED, "Additional Info", 1);

        actionsTable.insertAction(uuid, save);

        List<Action> actions = actionsTable.getActions(uuid);
        assertEquals(expected, actions.get(0));
    }

    @Test
    public void testIPTable() throws SQLException {
        saveUserOne();
        IPsTable ipsTable = db.getIpsTable();

        String expectedIP = "1.2.3.4";
        String expectedGeoLoc = "TestLocation";

        ipsTable.saveIP(uuid, expectedIP, expectedGeoLoc);
        ipsTable.saveIP(uuid, expectedIP, expectedGeoLoc);

        List<String> ips = ipsTable.getIps(uuid);
        assertEquals(1, ips.size());
        assertEquals(expectedIP, ips.get(0));

        List<String> geolocations = ipsTable.getGeolocations(uuid);
        assertEquals(1, geolocations.size());
        assertEquals(expectedGeoLoc, geolocations.get(0));


        Optional<String> result = ipsTable.getGeolocation(expectedIP);
        assertTrue(result.isPresent());
        assertEquals(expectedGeoLoc, result.get());
    }

    @Test // Does not test getting sessions from another server.
    public void testNicknamesTable() throws SQLException {
        saveUserOne();
        NicknamesTable nickTable = db.getNicknamesTable();

        String expected = "TestNickname";
        nickTable.saveUserName(uuid, expected);
        nickTable.saveUserName(uuid, expected);

        List<String> nicknames = nickTable.getNicknames(uuid);
        assertEquals(1, nicknames.size());
        assertEquals(expected, nicknames.get(0));

        List<String> allNicknames = nickTable.getAllNicknames(uuid);
        assertEquals(nicknames, allNicknames);
    }

    @Test
    public void testSecurityTable() throws SQLException {
        SecurityTable securityTable = db.getSecurityTable();
        WebUser expected = new WebUser("Test", "RandomGarbageBlah", 0);
        securityTable.addNewUser(expected);
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
    public void testWorldTable() throws SQLException {
        WorldTable worldTable = db.getWorldTable();
        List<String> worlds = Arrays.asList("Test", "Test2", "Test3");
        worldTable.saveWorlds(worlds);

        List<String> saved = worldTable.getWorlds();
        assertEquals(worlds, saved);
    }

    private void saveTwoWorlds() throws SQLException {
        db.getWorldTable().saveWorlds(worlds);
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
    public void testSessionPlaytimeSaving() throws SQLException {
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

        assertEquals(expectedLength, sessionsTable.getPlaytime(uuid));
        assertEquals(0L, sessionsTable.getPlaytime(uuid, 30000L));

        long playtimeOfServer = sessionsTable.getPlaytimeOfServer(TestInit.getServerUUID());
        assertEquals(expectedLength, playtimeOfServer);
        assertEquals(0L, sessionsTable.getPlaytimeOfServer(TestInit.getServerUUID(), 30000L));

        assertEquals(1, sessionsTable.getSessionCount(uuid));
        assertEquals(0, sessionsTable.getSessionCount(uuid, 30000L));
    }

    @Test
    public void testSessionSaving() throws SQLException {
        saveTwoWorlds();
        saveUserOne();
        saveUserTwo();
        Session session = new Session(12345L, "", "");
        session.endSession(22345L);
        session.setWorldTimes(createWorldTimes());
        session.setPlayerKills(createKills());

        SessionsTable sessionsTable = db.getSessionsTable();
        sessionsTable.saveSession(uuid, session);

        Map<String, List<Session>> sessions = sessionsTable.getSessions(uuid);

        for (Map.Entry<String, List<Session>> entry : sessions.entrySet()) {
            String key = entry.getKey();
            if (key == null) {
                System.out.print("null");
            } else if (key.isEmpty()) {
                System.out.print("empty");
            } else {
                System.out.print(key);
            }
            System.out.println(" " + entry.getValue());
        }

        List<Session> savedSessions = sessions.get("ServerName");

        assertNotNull(savedSessions);
        assertEquals(1, savedSessions.size());
        assertNull(sessions.get(worlds.get(1)));

        assertEquals(session, savedSessions.get(0));
    }

    @Test
    public void testUserInfoTableRegisterUnRegistered() throws SQLException {
        UserInfoTable userInfoTable = db.getUserInfoTable();
        assertFalse(userInfoTable.isRegistered(uuid));
        UsersTable usersTable = db.getUsersTable();
        assertFalse(usersTable.isRegistered(uuid));

        userInfoTable.registerUserInfo(uuid, 123456789L);

        assertTrue(usersTable.isRegistered(uuid));
        assertTrue(userInfoTable.isRegistered(uuid));

        UserInfo userInfo = userInfoTable.getUserInfo(uuid);
        assertEquals(uuid, userInfo.getUuid());
        assertEquals(123456789L, (long) usersTable.getRegisterDates().get(0));
        assertEquals(123456789L, userInfo.getRegistered());
        assertEquals("Waiting for Update..", userInfo.getName());
        assertFalse(userInfo.isBanned());
        assertFalse(userInfo.isOpped());
    }

    @Test
    public void testUserInfoTableRegisterRegistered() throws SQLException {
        saveUserOne();
        UsersTable usersTable = db.getUsersTable();
        assertTrue(usersTable.isRegistered(uuid));

        UserInfoTable userInfoTable = db.getUserInfoTable();
        assertFalse(userInfoTable.isRegistered(uuid));

        userInfoTable.registerUserInfo(uuid, 223456789L);

        assertTrue(usersTable.isRegistered(uuid));
        assertTrue(userInfoTable.isRegistered(uuid));

        UserInfo userInfo = userInfoTable.getUserInfo(uuid);
        assertEquals(uuid, userInfo.getUuid());
        assertEquals(123456789L, (long) usersTable.getRegisterDates().get(0));
        assertEquals(223456789L, userInfo.getRegistered());
        assertEquals("Test", userInfo.getName());
        assertFalse(userInfo.isBanned());
        assertFalse(userInfo.isOpped());

        assertEquals(userInfo, userInfoTable.getAllUserInfo().get(0));
    }

    @Test
    public void testUserInfoTableUpdateBannedOpped() throws SQLException {
        UserInfoTable userInfoTable = db.getUserInfoTable();
        userInfoTable.registerUserInfo(uuid, 223456789L);
        assertTrue(userInfoTable.isRegistered(uuid));

        userInfoTable.updateOpAndBanStatus(uuid, true, true);

        UserInfo userInfo = userInfoTable.getUserInfo(uuid);
        assertTrue(userInfo.isBanned());
        assertTrue(userInfo.isOpped());

        userInfoTable.updateOpAndBanStatus(uuid, false, true);
        userInfo = userInfoTable.getUserInfo(uuid);

        assertTrue(userInfo.isBanned());
        assertFalse(userInfo.isOpped());

        userInfoTable.updateOpAndBanStatus(uuid, false, false);
        userInfo = userInfoTable.getUserInfo(uuid);

        assertFalse(userInfo.isBanned());
        assertFalse(userInfo.isOpped());
    }

    @Test
    public void testUsersTableUpdateName() throws SQLException {
        saveUserOne();

        UsersTable usersTable = db.getUsersTable();

        assertEquals(uuid, usersTable.getUuidOf("Test"));
        usersTable.updateName(uuid, "NewName");
        assertNull(usersTable.getUuidOf("Test"));

        assertEquals("NewName", usersTable.getPlayerName(uuid));
        assertEquals(uuid, usersTable.getUuidOf("NewName"));
    }

    @Test
    public void testUsersTableKickSaving() throws SQLException {
        saveUserOne();
        UsersTable usersTable = db.getUsersTable();
        assertEquals(0, usersTable.getTimesKicked(uuid));

        int random = new Random().nextInt(20);

        for (int i = 0; i < random + 1; i++) {
            usersTable.kicked(uuid);
        }

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
        ipsTable.saveIP(uuid, "1.2.3.4", "TestLoc");
        actionsTable.insertAction(uuid, new Action(1324L, Actions.REGISTERED, "Add"));

        assertTrue(usersTable.isRegistered(uuid));

        db.removeAccount(uuid);

        assertFalse(usersTable.isRegistered(uuid));
        assertFalse(userInfoTable.isRegistered(uuid));
        assertTrue(nicknamesTable.getNicknames(uuid).isEmpty());
        assertTrue(ipsTable.getGeolocations(uuid).isEmpty());
        assertTrue(ipsTable.getIps(uuid).isEmpty());
        assertTrue(sessionsTable.getSessions(uuid).isEmpty());
        assertTrue(actionsTable.getActions(uuid).isEmpty());
    }

    @Test
    public void testRemovalEverything() throws SQLException {
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
        ipsTable.saveIP(uuid, "1.2.3.4", "TestLoc");
        actionsTable.insertAction(uuid, new Action(1324L, Actions.REGISTERED, "Add"));

        assertTrue(usersTable.isRegistered(uuid));

        Map<String, Integer> save = new HashMap<>();
        save.put("plan", 1);
        save.put("tp", 4);
        save.put("pla", 7);
        save.put("help", 21);
        db.saveCommandUse(save);

        TPSTable tpsTable = db.getTpsTable();
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

        SecurityTable securityTable = db.getSecurityTable();
        securityTable.addNewUser(new WebUser("Test", "RandomGarbageBlah", 0));

        db.removeAllData();

        assertFalse(usersTable.isRegistered(uuid));
        assertFalse(usersTable.isRegistered(uuid2));
        assertFalse(userInfoTable.isRegistered(uuid));
        assertTrue(nicknamesTable.getNicknames(uuid).isEmpty());
        assertTrue(ipsTable.getGeolocations(uuid).isEmpty());
        assertTrue(ipsTable.getIps(uuid).isEmpty());
        assertTrue(sessionsTable.getSessions(uuid).isEmpty());
        assertTrue(actionsTable.getActions(uuid).isEmpty());
        assertTrue(db.getCommandUse().isEmpty());
        assertTrue(db.getWorldTable().getWorlds().isEmpty());
        assertTrue(tpsTable.getTPSData().isEmpty());
        assertTrue(db.getServerTable().getBukkitServers().isEmpty());
        assertTrue(securityTable.getUsers().isEmpty());
    }

    @Test
    public void testServerTableBungeeSave() throws SQLException {
        ServerTable serverTable = db.getServerTable();

        Optional<ServerInfo> bungeeInfo = serverTable.getBungeeInfo();
        assertFalse(bungeeInfo.isPresent());

        UUID bungeeUUID = UUID.randomUUID();
        ServerInfo bungeeCord = new ServerInfo(-1, bungeeUUID, "BungeeCord", "Random:1234");
        serverTable.saveCurrentServerInfo(bungeeCord);

        bungeeCord.setId(2);

        bungeeInfo = serverTable.getBungeeInfo();
        assertTrue(bungeeInfo.isPresent());
        assertEquals(bungeeCord, bungeeInfo.get());

        Optional<Integer> serverID = serverTable.getServerID(bungeeUUID);
        assertTrue(serverID.isPresent());
        assertEquals(2, (int) serverID.get());
    }

    @Test
    public void testServerTableBungee() throws SQLException {
        testServerTableBungeeSave();
        ServerTable serverTable = db.getServerTable();

        List<ServerInfo> bukkitServers = serverTable.getBukkitServers();
        assertEquals(1, bukkitServers.size());
    }
}
