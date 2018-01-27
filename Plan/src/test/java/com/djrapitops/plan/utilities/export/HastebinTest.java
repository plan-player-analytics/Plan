package com.djrapitops.plan.utilities.export;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.utilities.file.export.Hastebin;
import com.djrapitops.plugin.StaticHolder;
import com.djrapitops.plugin.api.utility.log.Log;
import com.google.common.collect.Iterables;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import utilities.RandomData;
import utilities.TestInit;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;

/**
 * @author Fuzzlemann
 */
@PowerMockIgnore("javax.net.ssl.*")
@RunWith(PowerMockRunner.class)
@PrepareForTest(JavaPlugin.class)
public class HastebinTest {

    private final AtomicBoolean testLink = new AtomicBoolean(false);

    @Before
    public void checkAvailability() throws Exception {
        TestInit.init();

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
    public void testUpload() throws Exception {
        if (!testLink.get()) {
            Log.info("Hastebin not available, skipping testUpload()");
            return;
        }

        TestInit.init();

        String link = Hastebin.safeUpload(RandomData.randomString(10));
        assertNotNull(link);

        Log.info("Hastebin Link: " + link);
    }
}
