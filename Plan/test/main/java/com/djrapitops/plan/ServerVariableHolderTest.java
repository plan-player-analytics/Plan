package main.java.com.djrapitops.plan;

import org.bukkit.plugin.java.JavaPlugin;
import org.junit.Before;
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

    @Before
    public void setUp() throws Exception {
        TestInit.init();
    }

    @Test
    public void testServerVariable() {
        boolean usingPaper = Plan.getInstance().getVariable().isUsingPaper();
        assertFalse(usingPaper);

        String exp = Plan.getInstance().getVariable().getIp();
        assertEquals(exp, "0.0.0.0");
    }
}
