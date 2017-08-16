/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.main.java.com.djrapitops.plan.data.cache.queue;

import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.cache.DBCallableProcessor;
import main.java.com.djrapitops.plan.data.cache.queue.DataCacheGetQueue;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.database.databases.SQLiteDB;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import main.java.com.djrapitops.plan.utilities.file.FileUtil;
import org.bukkit.Bukkit;
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

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
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
     * @throws IOException
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        TestInit t = TestInit.init();
        plan = t.getPlanMock();
        db = new SQLiteDB(plan, "debug" + MiscUtils.getTime()) {
            @Override
            public void startConnectionPingTask() {

            }

            @Override
            public void giveUserDataToProcessors(UUID uuid, DBCallableProcessor... processors) {
                if (uuid.equals(MockUtils.getPlayerUUID())) {
                    UserData d = MockUtils.mockUser();
                    for (DBCallableProcessor processor : processors) {
                        processor.process(d);
                    }
                } else if (uuid.equals(MockUtils.getPlayer2UUID())) {
                    UserData d = MockUtils.mockUser2();
                    for (DBCallableProcessor processor : processors) {
                        processor.process(d);
                    }
                }
            }
        };
        File f = new File(plan.getDataFolder(), "Errors.txt");
        rows = FileUtil.lines(f).size();
    }

    /**
     * @throws IOException
     * @throws SQLException
     */
    @After
    public void tearDown() throws IOException, SQLException {
        db.close();
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

    /**
     *
     */
    @Ignore("Scheduler")
    @Test
    public void testScheduleForGet() {
        UserData exp = MockUtils.mockUser2();

        DataCacheGetQueue instance = new DataCacheGetQueue(plan);
        instance.scheduleForGet(exp.getUuid(), data -> assertTrue(data.equals(exp)));
    }

    /**
     *
     */
    @Ignore("Scheduler")
    @Test
    public void testStop() {
        DataCacheGetQueue instance = new DataCacheGetQueue(plan);
        instance.stop();
        instance.scheduleForGet(MockUtils.getPlayerUUID(), data -> fail("Called get process after stop."));
    }
}
