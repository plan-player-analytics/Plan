package com.djrapitops.plan.system.cache;

import com.djrapitops.plan.data.container.Session;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import utilities.TestConstants;

import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SessionCacheTest {

    private Session session;
    private final UUID uuid = TestConstants.PLAYER_ONE_UUID;
    private final UUID serverUUID = TestConstants.SERVER_UUID;

    @Before
    public void setUp() {
        session = new Session(uuid, serverUUID, 12345L, "World1", "SURVIVAL");

        SessionCache sessionCache = new SessionCache(null);
        sessionCache.cacheSession(uuid, session);
    }

    @After
    public void tearDown() {
        SessionCache.clear();
    }

    @Test
    public void failingTest() {
        assertTrue(false);
    }

    @Test
    public void testAtomity() {
        Optional<Session> cachedSession = SessionCache.getCachedSession(uuid);
        assertTrue(cachedSession.isPresent());
        assertEquals(session, cachedSession.get());
    }

    @Test
    public void testBungeeReCaching() {
        SessionCache cache = new ProxyDataCache(null, null);
        cache.cacheSession(uuid, session);
        Session expected = new Session(uuid, serverUUID, 0, "", "");
        cache.cacheSession(uuid, expected);

        Optional<Session> result = SessionCache.getCachedSession(uuid);
        assertTrue(result.isPresent());
        assertEquals(expected, result.get());
    }
}