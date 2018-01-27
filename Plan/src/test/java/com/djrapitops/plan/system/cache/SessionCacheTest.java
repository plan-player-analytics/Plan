package com.djrapitops.plan.system.cache;

import com.djrapitops.plan.data.container.Session;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
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

    @BeforeClass
    public static void setUpClass() throws Exception {
        SystemMockUtil.setUp(temporaryFolder.getRoot())
                .enableProcessingQueue();
    }

    @Before
    public void setUp() {
        sessionCache = new SessionCache(null);
        session = new Session(12345L, "World1", "SURVIVAL");
        sessionCache.cacheSession(uuid, session);
    }

    @Test
    public void testAtomity() {
        SessionCache reloaded = new SessionCache(null);
        Optional<Session> cachedSession = SessionCache.getCachedSession(uuid);
        assertTrue(cachedSession.isPresent());
        assertEquals(session, cachedSession.get());
    }
}