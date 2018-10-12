package com.djrapitops.plan.system.cache;

import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.system.database.databases.Database;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import utilities.TestConstants;
import utilities.mocks.SystemMockUtil;

import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SessionCacheTest {

    @ClassRule
    public static TemporaryFolder temporaryFolder = new TemporaryFolder();
    private SessionCache sessionCache;
    private Session session;
    private final UUID uuid = TestConstants.PLAYER_ONE_UUID;
    private final UUID serverUUID = TestConstants.SERVER_UUID;

    private Database database; // TODO

    @BeforeClass
    public static void setUpClass() throws Exception {
        SystemMockUtil.setUp(temporaryFolder.getRoot())
                .enableProcessing();
    }

    @Before
    public void setUp() {
        sessionCache = new SessionCache(database);
        session = new Session(uuid, serverUUID, 12345L, "World1", "SURVIVAL");
        sessionCache.cacheSession(uuid, session);
    }

    @After
    public void tearDown() {
        SessionCache.clear();
    }

    @Test
    public void testAtomity() {
        SessionCache reloaded = new SessionCache(database);
        Optional<Session> cachedSession = SessionCache.getCachedSession(uuid);
        assertTrue(cachedSession.isPresent());
        assertEquals(session, cachedSession.get());
    }

    @Test
    public void testBungeeReCaching() {
        SessionCache cache = new ProxyDataCache(null, null);
        cache.cacheSession(uuid, session);
        Session expected = new Session(uuid, TestConstants.SERVER_UUID, 0, "", "");
        cache.cacheSession(uuid, expected);

        Optional<Session> result = SessionCache.getCachedSession(uuid);
        assertTrue(result.isPresent());
        assertEquals(expected, result.get());
    }
}