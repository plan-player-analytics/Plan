package main.java.com.djrapitops.plan.utilities.html;

import main.java.com.djrapitops.plan.data.Session;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import test.java.utils.RandomData;
import test.java.utils.TestInit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@RunWith(PowerMockRunner.class)
@PrepareForTest(JavaPlugin.class)
public class HtmlStructureTest {

    private Map<String, List<Session>> sessions = new HashMap<>();

    @Before
    public void setUp() throws Exception {
        TestInit.init();

        for (int i = 0; i < RandomData.randomInt(0, 5); i++) {
            sessions.put(RandomData.randomString(10), RandomData.randomSessions());
        }
    }
}