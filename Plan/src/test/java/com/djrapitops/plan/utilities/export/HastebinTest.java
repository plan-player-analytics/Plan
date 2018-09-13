package com.djrapitops.plan.utilities.export;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.utilities.file.export.Hastebin;
import com.djrapitops.plugin.StaticHolder;
import com.djrapitops.plugin.api.utility.log.Log;
import com.google.common.collect.Iterables;
import org.json.simple.parser.ParseException;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import utilities.RandomData;
import utilities.mocks.SystemMockUtil;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;

/**
 * @author Fuzzlemann
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class HastebinTest {

    private final AtomicBoolean testLink = new AtomicBoolean(false);

    @ClassRule
    public static TemporaryFolder temporaryFolder = new TemporaryFolder();

    @BeforeClass
    public static void setUpClass() throws Exception {
        SystemMockUtil.setUp(temporaryFolder.getRoot())
                .enableConfigSystem()
                .enableDatabaseSystem();
    }

    @Before
    public void checkAvailability() {
        Thread thread = new Thread(() -> {
            StaticHolder.saveInstance(this.getClass(), PlanPlugin.getInstance().getClass());
            try {
                Hastebin.upload(RandomData.randomString(10));
            } catch (IOException e) {
                if (e.getMessage().contains("503")) {
                    return;
                }

                Log.toLog("checkAvailability()", e);
            } catch (ParseException e) {
                /* Ignored */
            }

            testLink.set(true);
        });

        thread.start();

        try {
            thread.join(5000);
        } catch (InterruptedException e) {
            Log.info("Hastebin timed out");
        }

        Log.info("Hastebin Available: " + testLink.get());
    }

    @Test
    public void testSplitting() {
        Iterable<String> parts = Hastebin.split(RandomData.randomString(500000));

        int expPartCount = 2;
        int partCount = Iterables.size(parts);

        assertEquals(expPartCount, partCount);
    }

    @Test
    public void testUpload() throws IOException, ParseException {
        // Hastebin not available, skipping testUpload()
        Assume.assumeTrue(testLink.get());

        String link = Hastebin.safeUpload(RandomData.randomString(10));
        assertNotNull(link);

        Log.info("Hastebin Link: " + link);
    }
}
