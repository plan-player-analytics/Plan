package test.java.main.java.com.djrapitops.plan.utilities.dump;

import com.google.common.collect.Iterables;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.utilities.file.dump.Hastebin;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import test.java.utils.RandomData;
import test.java.utils.TestInit;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;

/**
 * @author Fuzzlemann
 */
@PowerMockIgnore("javax.net.ssl.*")
@RunWith(PowerMockRunner.class)
@PrepareForTest(JavaPlugin.class)
public class HastebinTest {

    private AtomicReference<String> testLink = new AtomicReference<>(null);

    @Before
    public void checkAvailability() throws Exception {
        TestInit.init();

        Thread thread = new Thread(() -> {
            String link = null;
            try {
                link = Hastebin.upload(RandomData.randomString(10));
            } catch (IOException e) {
                if (e.getMessage().contains("503")) {
                    return;
                }
            } catch (ParseException e) {
                /* Ignored */
            }

            Log.info(link);
            testLink.set(link);
        });

        thread.start();

        try {
            thread.join();
        } catch (InterruptedException e) {
            Log.info("InterruptedException: " + e.getMessage());
        }

        Log.info("Hastebin Availability Test Link: " + testLink.get());
    }

    @Test
    public void testSplitting() {
        Iterable<String> parts = Hastebin.split(RandomData.randomString(500000));

        int expPartCount = 2;
        int partCount = Iterables.size(parts);

        assertEquals(expPartCount, partCount);
    }

    @Test
    public void testUpload() throws Exception {
        if (testLink.get() == null) {
            Log.info("Hastebin not available, skipping testUpload()");
            return;
        }

        TestInit.init();

        String link = Hastebin.safeUpload(RandomData.randomString(10));
        assertNotNull(link);

        Log.info("Hastebin Link: " + link);
    }
}
