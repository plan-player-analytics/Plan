/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.main.java.com.djrapitops.plan.data.handling.info;

import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.KillData;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.handling.info.KillInfo;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.database.databases.SQLiteDB;
import main.java.com.djrapitops.plan.database.tables.UsersTable;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import test.java.utils.MockUtils;
import test.java.utils.TestInit;

import java.sql.SQLException;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * @author Rsl1122
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(JavaPlugin.class)
public class KillInfoTest {

    private Database db;

    /**
     *
     */
    public KillInfoTest() {
    }

    /**
     *
     */
    @Before
    public void setUp() throws Exception {
        TestInit t = TestInit.init();
        Plan plan = t.getPlanMock();
        db = new SQLiteDB(plan, "debug" + MiscUtils.getTime()) {
            @Override
            public void startConnectionPingTask() {

            }

            @Override
            public void convertBukkitDataToDB() {

            }

            @Override
            public UsersTable getUsersTable() {
                return new UsersTable(null, false) {
                    @Override
                    public int getUserId(UUID uuid) {
                         if (uuid.equals(MockUtils.getPlayerUUID())) {
                             return 2;
                         }
                         return 1;
                    }
                };
            }
        };
        when(plan.getDB()).thenReturn(db);
    }

    /**
     * @throws SQLException
     */
    @Test
    public void testProcess() throws SQLException {
        UserData data = MockUtils.mockUser();
        Player dead = MockUtils.mockPlayer2();
        KillInfo i = new KillInfo(data.getUuid(), 10L, dead, "TestWeapon");
        assertTrue(i.process(data));
        KillData expected = new KillData(dead.getUniqueId(), 1, "TestWeapon", 10L);
        assertTrue("Didn't add the kill", data.getPlayerKills().size() == 1);
        KillData result = data.getPlayerKills().get(0);
        assertEquals(expected.getDate(), result.getDate());
        assertEquals(expected.getVictim(), result.getVictim());
        assertEquals(expected.getVictimUserID(), result.getVictimUserID());
        assertEquals(expected.getWeapon(), result.getWeapon());
    }

    /**
     * @throws SQLException
     */
    @Test
    public void testProcessMobKill() throws SQLException {
        UserData data = MockUtils.mockUser();
        KillInfo i = new KillInfo(data.getUuid(), 10L, null, "TestWeapon");
        assertTrue(i.process(data));
        assertTrue("Added a kill", data.getPlayerKills().isEmpty());
        assertEquals(1, data.getMobKills());
    }
}
