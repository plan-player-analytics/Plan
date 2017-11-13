package main.java.com.djrapitops.plan.utilities;

import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.command.bukkit.BukkitCMDSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import test.java.utils.MockUtils;
import test.java.utils.RandomData;
import test.java.utils.TestInit;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

/**
 * @author Fuzzlemann
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(JavaPlugin.class)
public class ConditionTest {

    @Before
    public void setUp() throws Exception {
        TestInit.init();
    }

    @Test
    public void testTrueCheck() {
        String message = RandomData.randomString(10);

        assertTrue(Condition.isTrue(true, message));
    }

    @Test
    public void testTrueAtISenderCheck() {
        String message = RandomData.randomString(10);
        ISender sender = new BukkitCMDSender(MockUtils.mockPlayer());

        assertTrue(Condition.isTrue(true, message, sender));
        assertFalse(Condition.isTrue(false, message, sender));
    }

    @Test
    public void testErrorCheck() {
        String message = RandomData.randomString(10);

        assertTrue(Condition.errorIfFalse(true, message));
        assertFalse(Condition.errorIfFalse(false, message));
    }
}
