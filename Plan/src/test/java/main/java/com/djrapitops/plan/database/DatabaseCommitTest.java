package test.java.main.java.com.djrapitops.plan.database;

import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.TPS;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.database.databases.SQLiteDB;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import test.java.utils.MockUtils;
import test.java.utils.RandomData;
import test.java.utils.TestInit;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;

@RunWith(PowerMockRunner.class)
@PrepareForTest(JavaPlugin.class)
public class DatabaseCommitTest {


    private Plan plan;
    private Database db;
    private int rows;


    @Before
    public void setUp() throws Exception {
        TestInit t = TestInit.init();
        plan = t.getPlanMock();
        db = new SQLiteDB(plan, "debug" + MiscUtils.getTime()) {
            @Override
            public void startConnectionPingTask() {

            }

            @Override
            public void convertBukkitDataToDB() {

            }
        };
        File f = new File(plan.getDataFolder(), "Errors.txt");
        rows = 0;
        if (f.exists()) {
            rows = Files.lines(f.toPath(), Charset.defaultCharset()).collect(Collectors.toList()).size();
        }
    }

    /**
     * @throws IOException
     * @throws SQLException
     */
    @After
    public void tearDown() throws IOException, SQLException {
        db.close();
        File f = new File(plan.getDataFolder(), "Errors.txt");
        int rowsAgain = 0;
        if (f.exists()) {
            List<String> lines = Files.lines(f.toPath(), Charset.defaultCharset()).collect(Collectors.toList());
            rowsAgain = lines.size();
            for (String line : lines) {
                System.out.println(line);
            }
        }
        assertTrue("Errors were caught.", rows == rowsAgain);
    }

    @Test
    public void testCommitToDBFile() throws SQLException {
        db.init();
        HashMap<String, Integer> c = new HashMap<>();
        c.put("/plan", 1);
        c.put("/tp", 4);
        c.put("/pla", 7);
        c.put("/help", 21);
        db.saveCommandUse(c);
        db.close();
        db.init();
        assertTrue(!db.getCommandUse().isEmpty());
    }

    @Test
    public void testCommitToDBFile2() throws SQLException {
        db.init();
        List<TPS> tps = RandomData.randomTPS();
        db.getTpsTable().saveTPSData(tps);
        db.close();
        db.init();
        assertTrue(!db.getTpsTable().getTPSData().isEmpty());
    }

    @Test
    public void testCommitToDBFile3() throws SQLException {
        db.init();
        UserData userData = MockUtils.mockUser();
        db.saveUserData(userData);
        db.close();
        db.init();
        assertTrue(!db.getUserDataForUUIDS(Collections.singletonList(MockUtils.getPlayerUUID())).isEmpty());
    }

    @Test
    public void testCommitToDBFile4() throws SQLException {
        db.init();
        List<UserData> data = RandomData.randomUserData();
        List<UUID> uuids = data.stream().map(UserData::getUuid).collect(Collectors.toList());
        db.saveMultipleUserData(data);
        db.close();
        db.init();
        assertTrue(!db.getUserDataForUUIDS(uuids).isEmpty());
    }
}
