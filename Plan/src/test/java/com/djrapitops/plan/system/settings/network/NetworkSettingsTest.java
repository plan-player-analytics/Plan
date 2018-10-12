package com.djrapitops.plan.system.settings.network;

import com.djrapitops.plan.system.database.databases.sql.SQLDB;
import com.djrapitops.plan.system.database.databases.sql.tables.ServerTable;
import com.djrapitops.plan.system.info.server.Server;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import utilities.TestConstants;
import utilities.mocks.SystemMockUtil;

import static org.junit.Assert.assertEquals;

public class NetworkSettingsTest {

    @ClassRule
    public static TemporaryFolder temporaryFolder = new TemporaryFolder();
    private static SQLDB db;

    @BeforeClass
    public static void setUpClass() throws Exception {
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
        assertEquals(db.getServerUUIDSupplier().get(), TestConstants.SERVER_UUID);
    }

    @Test
    @Ignore
    public void testTransfer() {
        NetworkSettings networkSettings = null; // TODO new NetworkSettings();
        networkSettings.placeToDatabase();
        networkSettings.loadFromDatabase();
    }

}