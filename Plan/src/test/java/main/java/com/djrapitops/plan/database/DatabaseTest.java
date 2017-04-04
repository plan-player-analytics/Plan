/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.main.java.com.djrapitops.plan.database;

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.database.databases.SQLiteDB;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.After;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.powermock.api.easymock.PowerMock;
import test.java.utils.TestInit;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.*;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.bukkit.scheduler.BukkitScheduler;
import org.easymock.EasyMock;
import static org.easymock.EasyMock.anyLong;
import org.junit.Ignore;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

/**
 *
 * @author Risto
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({JavaPlugin.class, Bukkit.class, BukkitScheduler.class, BukkitRunnable.class})
public class DatabaseTest {

    private Plan plan;
    private int rows;

    public DatabaseTest() {
    }

    @Before
    public void setUp() throws IOException, Exception {
        TestInit t = new TestInit();
        assertTrue("Not set up", t.setUp());
        plan = t.getPlanMock();
        PowerMock.mockStatic(JavaPlugin.class);
        PowerMock.replay(JavaPlugin.class);
//        PowerMock.verify(JavaPlugin.class);      
        File f = new File(plan.getDataFolder(), "Errors.txt");
        rows = 0;
        if (f.exists()) {
            rows = Files.readLines(f, Charset.defaultCharset()).size();
        }
        BukkitRunnable mockRunnable = PowerMockito.mock(BukkitRunnable.class);
        when(mockRunnable.runTaskTimerAsynchronously(plan, anyLong(), anyLong())).thenReturn(null);
        whenNew(BukkitRunnable.class).withNoArguments().thenReturn(mockRunnable);

        PowerMock.mockStatic(Bukkit.class);
//        PowerMock.replay(Bukkit.class);
        BukkitScheduler mockScheduler = Mockito.mock(BukkitScheduler.class);
        EasyMock.expect(Bukkit.getScheduler()).andReturn(mockScheduler);
    }

    @After
    public void tearDown() throws IOException {
        File f = new File(plan.getDataFolder(), "Errors.txt");
        int rowsAgain = 0;
        if (f.exists()) {
            rowsAgain = Files.readLines(f, Charset.defaultCharset()).size();
        }
        assertTrue("Errors were caught.", rows == rowsAgain);
    }

    @Ignore @Test
    public void testInit() {
        Database db = new SQLiteDB(plan, "debug.db");
        assertTrue("Database failed to init.", db.init());
    }
}
