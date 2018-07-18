package com.djrapitops.plan.system.settings.network;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.api.exceptions.connection.UnsupportedTransferDatabaseException;
import com.djrapitops.plan.api.exceptions.database.DBException;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;
import com.djrapitops.plan.system.database.databases.sql.SQLiteDB;
import com.djrapitops.plan.system.database.databases.sql.tables.ServerTable;
import com.djrapitops.plan.system.info.server.Server;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plugin.StaticHolder;
import com.djrapitops.plugin.api.utility.log.Log;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import utilities.Teardown;
import utilities.TestConstants;
import utilities.TestErrorManager;
import utilities.mocks.SystemMockUtil;

import static org.junit.Assert.assertEquals;

public class NetworkSettingsTest {

    @ClassRule
    public static TemporaryFolder temporaryFolder = new TemporaryFolder();
    private static SQLDB db;

    @BeforeClass
    public static void setUpClass() throws Exception {
        Teardown.resetSettingsTempValues();
        StaticHolder.saveInstance(NetworkSettingsTest.class, Plan.class);

        SystemMockUtil mockUtil = SystemMockUtil.setUp(temporaryFolder.getRoot())
                .enableConfigSystem();
        db = new SQLiteDB();
        mockUtil.enableDatabaseSystem(db)
                .enableServerInfoSystem();

        Log.setErrorManager(new TestErrorManager());
        Log.setDebugMode("console");
        Settings.DEV_MODE.setTemporaryValue(true);
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
        db.remove().everything();
        ServerTable serverTable = db.getServerTable();
        serverTable.saveCurrentServerInfo(new Server(-1, TestConstants.SERVER_UUID, "ServerName", "", 20));
        assertEquals(ServerInfo.getServerUUID(), TestConstants.SERVER_UUID);
    }

    @Test
    public void testTransfer() throws DBException, UnsupportedTransferDatabaseException {
        NetworkSettings networkSettings = new NetworkSettings();
        networkSettings.placeToDatabase();
        networkSettings.loadFromDatabase();
    }

}