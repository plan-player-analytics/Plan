/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.main.java.com.djrapitops.plan.data.cache.queue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.DemographicsData;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.cache.DBCallableProcessor;
import main.java.com.djrapitops.plan.data.cache.queue.DataCacheGetQueue;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.database.databases.SQLiteDB;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;
import org.easymock.EasyMock;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import test.java.utils.MockUtils;
import test.java.utils.TestInit;

/**
 *
 * @author Rsl1122
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({JavaPlugin.class, Bukkit.class})
public class DataCacheGetQueueTest {

    private Plan plan;
    private Database db;
    private int rows;

    /**
     *
     */
    public DataCacheGetQueueTest() {
    }

    /**
     *
     * @throws IOException
     * @throws Exception
     */
    @Before
    public void setUp() throws IOException, Exception {
        TestInit t = new TestInit();
        assertTrue("Not set up", t.setUp());
        plan = t.getPlanMock();
        db = new SQLiteDB(plan, "debug" + new Date().getTime()) {
            @Override
            public void startConnectionPingTask(Plan plugin) {

            }

            @Override
            public void giveUserDataToProcessors(UUID uuid, DBCallableProcessor... processors) {
                if (uuid.equals(MockUtils.getPlayerUUID())) {
                    OfflinePlayer op = MockUtils.mockPlayer();
                    UserData d = new UserData(op, new DemographicsData());
                    for (DBCallableProcessor processor : processors) {
                        processor.process(d);
                    }
                } else if (uuid.equals(MockUtils.getPlayer2UUID())) {
                    OfflinePlayer op = MockUtils.mockPlayer2();
                    UserData d = new UserData(op, new DemographicsData());
                    for (DBCallableProcessor processor : processors) {
                        processor.process(d);
                    }
                }
            }
        };
        PowerMock.mockStatic(JavaPlugin.class);
        EasyMock.expect(JavaPlugin.getPlugin(Plan.class)).andReturn(plan);
        EasyMock.expect(JavaPlugin.getPlugin(Plan.class)).andReturn(plan);
        EasyMock.expect(JavaPlugin.getPlugin(Plan.class)).andReturn(plan);
        EasyMock.expect(JavaPlugin.getPlugin(Plan.class)).andReturn(plan);
        EasyMock.expect(JavaPlugin.getPlugin(Plan.class)).andReturn(plan);
        EasyMock.expect(JavaPlugin.getPlugin(Plan.class)).andReturn(plan);
        PowerMock.replay(JavaPlugin.class);
        File f = new File(plan.getDataFolder(), "Errors.txt");
        rows = 0;
        if (f.exists()) {
            rows = Files.lines(f.toPath(), Charset.defaultCharset()).collect(Collectors.toList()).size();
        }
    }

    /**
     *
     * @throws IOException
     * @throws SQLException
     */
    @After
    public void tearDown() throws IOException, SQLException {
        db.close();
        File f = new File(plan.getDataFolder(), "Errors.txt");
        int rowsAgain = 0;
        if (f.exists()) {
            rowsAgain = Files.lines(f.toPath(), Charset.defaultCharset()).collect(Collectors.toList()).size();
        }
        assertTrue("Errors were caught.", rows == rowsAgain);
    }

    /**
     *
     */
    @Test
    public void testScheduleForGet() {
        OfflinePlayer op = MockUtils.mockPlayer2();
        UserData exp = new UserData(op, new DemographicsData());

        DataCacheGetQueue instance = new DataCacheGetQueue(plan);
        instance.scheduleForGet(exp.getUuid(), new DBCallableProcessor() {
            @Override
            public void process(UserData data) {
                assertTrue(data.equals(exp));
            }
        });
    }

    /**
     *
     */
    @Test
    public void testStop() {
        DataCacheGetQueue instance = new DataCacheGetQueue(plan);
        instance.stop();
        instance.scheduleForGet(MockUtils.getPlayerUUID(), new DBCallableProcessor() {
            @Override
            public void process(UserData data) {
                fail("Called get process after stop.");
            }
        });
    }
}
