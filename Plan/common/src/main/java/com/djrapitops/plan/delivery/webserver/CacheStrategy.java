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
package com.djrapitops.plan.delivery.webserver;

/**
 * @author AuroraLS3
 */
public class CacheStrategy {

    public static final String CACHE_IN_BROWSER = "max-age: 2592000";
    public static final String CHECK_ETAG = "no-cache";
    public static final String CHECK_ETAG_USER_SPECIFIC = "no-cache, private";

    private CacheStrategy() {
        // Static variable class
    }

}
