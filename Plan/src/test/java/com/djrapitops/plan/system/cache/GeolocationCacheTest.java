package com.djrapitops.plan.system.cache;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.system.BukkitSystem;
import com.djrapitops.plugin.StaticHolder;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import utilities.mocks.BukkitMockUtil;

import static org.junit.Assert.assertEquals;

/**
 * Test for GeolocationCache.
 *
 * @author Rsl1122
 */
public class GeolocationCacheTest {

    @ClassRule
    public static TemporaryFolder temporaryFolder = new TemporaryFolder();
    private static Plan planMock;

    @BeforeClass
    public static void setUpClass() throws Exception {
        BukkitMockUtil mockUtil = BukkitMockUtil.setUp()
                .withDataFolder(temporaryFolder.getRoot())
                .withLogging()
                .withPluginDescription()
                .withResourceFetchingFromJar()
                .withServer();
        planMock = mockUtil.getPlanMock();
        StaticHolder.saveInstance(GeolocationCacheTest.class, planMock.getClass());
    }

    @Test
    public void testGeolocationCache() throws EnableException {
        BukkitSystem system = new BukkitSystem(planMock);
        system.enable();

        String expected = "Germany";
        String result = GeolocationCache.getCountry("141.52.255.1");
        assertEquals(expected, result);
    }

}