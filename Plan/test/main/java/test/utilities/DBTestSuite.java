/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package test.utilities;

import com.djrapitops.plan.system.database.MySQLTest;
import com.djrapitops.plan.system.database.SQLiteTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.io.File;

/**
 * @author Fuzzlemann
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({SQLiteTest.class, MySQLTest.class})
public class DBTestSuite {
    @BeforeClass
    public static void setUp() {
        clean();
    }

    @AfterClass
    public static void tearDown() {
        clean();
    }

    private static void clean() {
        File testFolder = TestInit.getTestFolder();

        if (!testFolder.exists() || !testFolder.isDirectory()) {
            return;
        }

        File[] files = testFolder.listFiles();

        if (files == null) {
            return;
        }

        for (File f : files) {
            if (!f.delete()) {
                f.deleteOnExit();
            }
        }
    }
}
