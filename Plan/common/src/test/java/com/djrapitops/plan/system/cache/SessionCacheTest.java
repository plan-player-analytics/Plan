/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.system.cache;

import com.djrapitops.plan.data.container.Session;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import utilities.TestConstants;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RunWith(JUnitPlatform.class)
class SessionCacheTest {

    private Session session;
    private final UUID uuid = TestConstants.PLAYER_ONE_UUID;
    private final UUID serverUUID = TestConstants.SERVER_UUID;

    @BeforeEach
    void setUp() {
        session = new Session(uuid, serverUUID, 12345L, "World1", "SURVIVAL");

        SessionCache sessionCache = new SessionCache();
        sessionCache.cacheSession(uuid, session);
    }

    @AfterEach
    void tearDown() {
        SessionCache.clear();
    }

    @Test
    void testAtomity() {
        Optional<Session> cachedSession = SessionCache.getCachedSession(uuid);
        assertTrue(cachedSession.isPresent());
        assertEquals(session, cachedSession.get());
    }
}