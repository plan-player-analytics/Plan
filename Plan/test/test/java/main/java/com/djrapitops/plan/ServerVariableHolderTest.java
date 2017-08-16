package test.java.main.java.com.djrapitops.plan;

import main.java.com.djrapitops.plan.Plan;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import test.java.utils.TestInit;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;

/**
 * @author Fuzzlemann
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(JavaPlugin.class)
public class ServerVariableHolderTest {

    @Test
    public void testServerVariable() throws Exception {
        TestInit.init();

        boolean usingPaper = Plan.getInstance().getVariable().isUsingPaper();
        assertFalse(usingPaper);

        String ip = Plan.getInstance().getVariable().getIp();
        assertEquals(ip, "0.0.0.0");
    }
}
