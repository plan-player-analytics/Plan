package main.java.com.djrapitops.plan.systems.cache;

import main.java.com.djrapitops.plan.data.Session;
import org.junit.Test;
import test.java.utils.MockUtils;

import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public class SessionCacheTest {

    private final UUID uuid = MockUtils.getPlayerUUID();

    @Test
    public void testAtomity() {
        SessionCache sessionCache = new SessionCache(null);
        Session session = new Session(12345L, "World1", "SURVIVAL");
        sessionCache.cacheSession(uuid, session);

        SessionCache reloaded = new SessionCache(null);
        Optional<Session> cachedSession = reloaded.getCachedSession(uuid);
        assertTrue(cachedSession.isPresent());
        assertEquals(session, cachedSession.get());
    }

}