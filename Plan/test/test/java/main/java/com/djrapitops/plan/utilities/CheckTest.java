package test.java.main.java.com.djrapitops.plan.utilities;

import com.djrapitops.plugin.command.ISender;
import main.java.com.djrapitops.plan.utilities.Check;
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
public class CheckTest {

    @Before
    public void setUp() throws Exception {
        TestInit.init();
    }

    @Test
    public void testTrueCheck() {
        String message = RandomData.randomString(10);

        assertTrue(Check.isTrue(true, message));
    }

    @Test
    public void testTrueAtISenderCheck() {
        String message = RandomData.randomString(10);
        ISender sender = MockUtils.mockIPlayer();

        assertTrue(Check.isTrue(true, message, sender));
        assertFalse(Check.isTrue(false, message, sender));
    }

    @Test
    public void testErrorCheck() {
        String message = RandomData.randomString(10);

        assertTrue(Check.errorIfFalse(true, message));
        assertFalse(Check.errorIfFalse(false, message));
    }
}
