package test.java.main.java.com.djrapitops.plan.utilities.dump;

import com.google.common.collect.Iterables;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.utilities.file.dump.Hastebin;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import test.java.utils.RandomData;
import test.java.utils.TestInit;

import static junit.framework.TestCase.assertEquals;

/**
 * @author Fuzzlemann
 */
@PowerMockIgnore({"javax.net.ssl.*"})
@RunWith(PowerMockRunner.class)
@PrepareForTest(JavaPlugin.class)
public class HastebinTest {

    private String content = RandomData.randomString(400000);

    @Test
    public void testSplitting() {
        Iterable<String> parts = Hastebin.split(content);

        int expPartCount = 2;
        int partCount = Iterables.size(parts);

        assertEquals(expPartCount, partCount);
    }

    @Test
    public void testUpload() throws Exception {
        TestInit.init();

        String link = Hastebin.safeUpload(content);
        Log.info("Hastebin Link: " + link);
    }
}
