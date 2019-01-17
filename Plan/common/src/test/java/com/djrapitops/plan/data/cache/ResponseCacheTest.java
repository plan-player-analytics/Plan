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
package com.djrapitops.plan.data.cache;

import com.djrapitops.plan.system.webserver.cache.ResponseCache;
import com.djrapitops.plan.system.webserver.response.Response;
import org.junit.Test;
import utilities.RandomData;

import static junit.framework.TestCase.*;

/**
 * @author Fuzzlemann
 */
public class ResponseCacheTest {
    private final String IDENTIFIER = RandomData.randomString(10);
    private final String RESPONSE_STRING = RandomData.randomString(10);
    private final Response RESPONSE = new Response() {
        @Override
        public String getResponse() {
            return RESPONSE_STRING;
        }
    };

    @Test
    public void testCache() {
        assertFalse(ResponseCache.isCached(IDENTIFIER));

        Response response = ResponseCache.loadResponse(IDENTIFIER, () -> RESPONSE);
        assertTrue(ResponseCache.isCached(IDENTIFIER));

        assertEquals(RESPONSE, response);
    }

    @Test
    public void testClearCache() {
        ResponseCache.cacheResponse(IDENTIFIER, () -> RESPONSE);
        assertTrue(ResponseCache.isCached(IDENTIFIER));

        ResponseCache.clearCache();
        assertFalse(ResponseCache.isCached(IDENTIFIER));
    }
}
