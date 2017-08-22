/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package test.java.utils;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import test.java.main.java.com.djrapitops.plan.database.DatabaseCommitTest;
import test.java.main.java.com.djrapitops.plan.database.DatabaseTest;

import java.io.File;
import java.io.IOException;

/**
 * @author Fuzzlemann
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({DatabaseCommitTest.class, DatabaseTest.class})
public class DBTestSuite {
    @BeforeClass
    public static void setUp() throws IOException {
        clean(true);
    }

    @BeforeClass
    public static void tearDown() throws IOException {
        clean(false);
    }

    private static void clean(boolean dbOnly) throws IOException {
        File testFolder = TestInit.getTestFolder();

        if (!testFolder.exists() || !testFolder.isDirectory()) {
            return;
        }

        for (File f : testFolder.listFiles()) {
            if (dbOnly && !f.getName().endsWith(".db")) {
                continue;
            }

            f.delete();
        }
    }
}
