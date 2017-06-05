/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.main.java.com.djrapitops.plan.data.handling;

import java.io.IOException;
import java.sql.SQLException;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.DemographicsData;
import main.java.com.djrapitops.plan.data.KillData;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.handling.KillHandling;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.database.databases.SQLiteDB;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import test.java.utils.MockUtils;
import test.java.utils.TestInit;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 *
 * @author Rsl1122
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(JavaPlugin.class)
public class KillHandlingTest {

    private Database db;
    private Plan plan;

    /**
     *
     */
    public KillHandlingTest() {
    }

    /**
     *
     */
    @Before
    public void setUp() {
        TestInit t = new TestInit();
        assertTrue("Not set up", t.setUp());
        plan = t.getPlanMock();
        db = new SQLiteDB(plan, "debug" + MiscUtils.getTime()) {
            @Override
            public void startConnectionPingTask(Plan plugin) {

            }

            @Override
            public void convertBukkitDataToDB() {

            }
        };
        when(plan.getDB()).thenReturn(db);
    }

    /**
     *
     * @throws SQLException
     */
    @After
    public void tearDown() throws SQLException {
        db.close();
    }

    /**
     *
     * @throws SQLException
     */
    @Test
    public void testProcessKillInfoPlayer() throws SQLException {
        UserData data = new UserData(MockUtils.mockPlayer(), new DemographicsData());
        Player dead = MockUtils.mockPlayer2();
        db.init();
        db.saveUserData(new UserData(dead, new DemographicsData()));
        KillHandling.processKillInfo(data, 10L, dead, "TestWeapon");
        KillData expected = new KillData(dead.getUniqueId(), 1, "TestWeapon", 10L);
        assertTrue("Didn't add the kill", data.getPlayerKills().size() == 1);
        KillData result = data.getPlayerKills().get(0);
        assertEquals(expected.getDate(), result.getDate());
        assertEquals(expected.getVictim(), result.getVictim());
        assertEquals(expected.getVictimUserID(), result.getVictimUserID());
        assertEquals(expected.getWeapon(), result.getWeapon());
    }

    /**
     *
     * @throws SQLException
     * @throws IOException
     */
    @Ignore
    @Test
    public void testProcessKillInfoException() throws SQLException, IOException {
        UserData data = new UserData(MockUtils.mockPlayer(), new DemographicsData());
        Player dead = MockUtils.mockPlayer2();
        KillHandling.processKillInfo(data, 10L, dead, "TestWeapon");
        assertTrue("Added the kill", data.getPlayerKills().isEmpty());
    }

    /**
     *
     * @throws SQLException
     */
    @Test
    public void testProcessKillInfoMob() throws SQLException {
        UserData data = new UserData(MockUtils.mockPlayer(), new DemographicsData());
        int mobKills = data.getMobKills();
        int exp = mobKills + 1;
        KillHandling.processKillInfo(data, 10L, null, "TestWeapon");
        int result = data.getMobKills();
        assertEquals(exp, result);
    }

}
