package com.djrapitops.plan.data;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.data.container.UserInfo;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import test.utilities.TestInit;

/**
 * @author Rsl1122
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(JavaPlugin.class)
@Ignore
public class UserInfoTest {
    // TODO Rewrite

    private UserInfo session;
    private Plan plan;

    @Before
    public void setUp() throws Exception {
        TestInit.init();
    }

    @Test
    public void test() {
        // TODO Rewrite
    }
}
