package com.djrapitops.plan.system.settings.network;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;
import com.djrapitops.plan.system.database.databases.sql.tables.ServerTable;
import com.djrapitops.plan.system.info.server.Server;
import com.djrapitops.plugin.StaticHolder;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import utilities.TestConstants;
import utilities.mocks.SystemMockUtil;

public class NetworkSettingsTest {

    @ClassRule
    public static TemporaryFolder temporaryFolder = new TemporaryFolder();
    private static SQLDB db;

    @BeforeClass
    public static void setUpClass() throws Exception {
        StaticHolder.saveInstance(NetworkSettingsTest.class, Plan.class);

        SystemMockUtil mockUtil = SystemMockUtil.setUp(temporaryFolder.getRoot())
                .enableConfigSystem();
        db = null; // TODO
        mockUtil.enableDatabaseSystem(db)
                .enableServerInfoSystem();

//        Log.setErrorManager(new TestErrorManager());
//        Log.setDebugMode("console");
//        Settings.DEV_MODE.setTemporaryValue(true);
    }

    @AfterClass
    public static void tearDownClass() {
        if (db != null) {
            db.close();
        }
    }

    @Before
    public void setUp() {
        db.remove().everything();
        ServerTable serverTable = db.getServerTable();
        serverTable.saveCurrentServerInfo(new Server(-1, TestConstants.SERVER_UUID, "ServerName", "", 20));
//        assertEquals(ServerInfo.getServerUUID_Old(), TestConstants.SERVER_UUID); TODO check if assert is necessary.
    }

    @Test
    @Ignore
    public void testTransfer() {
        NetworkSettings networkSettings = null; // TODO new NetworkSettings();
        networkSettings.placeToDatabase();
        networkSettings.loadFromDatabase();
    }

}