/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.main.java.com.djrapitops.plan.data.handling;

import com.djrapitops.plugin.utilities.player.IPlayer;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.KillData;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.handling.KillHandling;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.database.databases.SQLiteDB;
import main.java.com.djrapitops.plan.database.tables.UsersTable;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import test.java.utils.MockUtils;
import test.java.utils.TestInit;

import java.io.IOException;
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
public class KillHandlingTest {

    private Database db;

    /**
     *
     */
    public KillHandlingTest() {
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
                            return -1;
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
    @After
    public void tearDown() throws SQLException {
        db.close();
    }

    /**
     * @throws SQLException
     */
    @Test
    public void testProcessKillInfoPlayer() throws SQLException {
        UserData data = MockUtils.mockUser();
        IPlayer dead = MockUtils.mockIPlayer2();
        KillHandling.processKillInfo(data, 10L, (Player) dead.getWrappedPlayerClass(), "TestWeapon");
        KillData expected = new KillData(dead.getUuid(), 1, "TestWeapon", 10L);
        assertTrue("Didn't add the kill", data.getPlayerKills().size() == 1);
        KillData result = data.getPlayerKills().get(0);
        assertEquals(expected.getDate(), result.getDate());
        assertEquals(expected.getVictim(), result.getVictim());
        assertEquals(expected.getVictimUserID(), result.getVictimUserID());
        assertEquals(expected.getWeapon(), result.getWeapon());
    }

    /**
     * @throws SQLException
     * @throws IOException
     */
    @Test
    public void testProcessKillInfoException() throws SQLException, IOException {
        UserData data = MockUtils.mockUser2();
        Player dead = (Player) MockUtils.mockIPlayer().getWrappedPlayerClass();
        KillHandling.processKillInfo(data, 10L, dead, "TestWeapon");
        assertTrue("Added the kill", data.getPlayerKills().isEmpty());
    }

    /**
     * @throws SQLException
     */
    @Test
    public void testProcessKillInfoMob() throws SQLException {
        UserData data = MockUtils.mockUser();
        int mobKills = data.getMobKills();
        int exp = mobKills + 1;
        KillHandling.processKillInfo(data, 10L, null, "TestWeapon");
        int result = data.getMobKills();
        assertEquals(exp, result);
    }

    @Test
    public void testNormalizeMaterialName() {
        Material material = Material.GOLD_SWORD;
        String name = material.name();
        String normalizedName = KillHandling.normalizeMaterialName(material);

        assertEquals(name, "GOLD_SWORD");
        assertEquals(normalizedName, "Gold Sword");
    }
}
