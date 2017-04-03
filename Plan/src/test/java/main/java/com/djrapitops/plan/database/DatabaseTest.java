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
import org.junit.Ignore;

/**
 *
 * @author Risto
 */
public class DatabaseTest {

    private Plan plan;
    private int rows;

    public DatabaseTest() {
    }

    @Before
    public void setUp() throws IOException {
        TestInit t = new TestInit();
        assertTrue("Not set up", t.setUp());
        plan = t.getPlanMock();
        PowerMock.mockStatic(JavaPlugin.class);
        PowerMock.replay(JavaPlugin.class);
//        PowerMock.verify(JavaPlugin.class);        
        PowerMock.mockStatic(Bukkit.class);
        PowerMock.replay(Bukkit.class);
//        EasyMock.expect(Bukkit.getScheduler()).andReturn();
        File f = new File(plan.getDataFolder(), "Errors.txt");
        rows = 0;
        if (f.exists()) {
            rows = Files.readLines(f, Charset.defaultCharset()).size();
        }
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

    @Ignore("Mock scheduler") @Test
    public void testInit() {
        Database db = new SQLiteDB(plan, "debug.db");
        assertTrue("Database failed to init.", db.init());        
    }
}
